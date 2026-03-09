package org.ntqqrev.acidify

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.ntqqrev.acidify.event.AndroidSessionStoreUpdatedEvent
import org.ntqqrev.acidify.event.QRCodeGeneratedEvent
import org.ntqqrev.acidify.event.QRCodeStateQueryEvent
import org.ntqqrev.acidify.event.SessionStoreUpdatedEvent
import org.ntqqrev.acidify.exception.WtLoginException
import org.ntqqrev.acidify.internal.crypto.pow.POW
import org.ntqqrev.acidify.internal.crypto.tea.TEA
import org.ntqqrev.acidify.internal.json.pmhq.PmhqLoginQrCodePayload
import org.ntqqrev.acidify.common.PmhqSelfInfo
import org.ntqqrev.acidify.internal.pmhq.system.GetQRCodePicture
import org.ntqqrev.acidify.internal.pmhq.system.GetQuickLoginList
import org.ntqqrev.acidify.internal.pmhq.system.GetSelfInfo
import org.ntqqrev.acidify.internal.pmhq.listener.NodeIKernelLoginListener
import org.ntqqrev.acidify.internal.pmhq.system.QuickLoginWithUin
import org.ntqqrev.acidify.internal.proto.system.AndroidThirdPartyLoginResponse
import org.ntqqrev.acidify.internal.service.system.WtLogin
import org.ntqqrev.acidify.internal.util.*
import org.ntqqrev.acidify.struct.QRCodeState
import kotlin.io.encoding.Base64

private suspend fun Bot.awaitPmhqOnline(queryInterval: Long, maxAttempts: Int): PmhqSelfInfo? {
    repeat(maxAttempts) {
        delay(queryInterval)
        val selfInfo = client.packetContext.callService(GetSelfInfo)
        logger.d { "第 ${it + 1}/$maxAttempts 次查询登录状态，结果：${selfInfo.online}" }
        if (selfInfo.online) {
            return selfInfo
        }
    }
    return null
}

private suspend fun Bot.completePmhqLogin(selfInfo: PmhqSelfInfo, preloadContacts: Boolean) {
    val hasChanged = sessionStore.uin != selfInfo.uin || sessionStore.uid != selfInfo.uid
    sessionStore.uin = selfInfo.uin
    sessionStore.uid = selfInfo.uid
    if (hasChanged) {
        sharedEventFlow.emit(SessionStoreUpdatedEvent(sessionStore))
    }
    online(preloadContacts)
}

/**
 * 发起二维码登录请求。过程中会触发事件：
 * - [QRCodeGeneratedEvent]：当二维码生成时触发，包含二维码链接和 PNG 图片数据
 * - [QRCodeStateQueryEvent]：每次查询二维码状态时触发，包含当前二维码状态（例如未扫码、已扫码未确认、已确认等）
 * @param queryInterval 查询间隔（单位 ms），不能小于 `1000`
 * @param preloadContacts 是否在登录成功后预加载好友和群信息以初始化内存缓存
 * @throws org.ntqqrev.acidify.exception.WtLoginException 当二维码扫描成功，但后续登录失败时抛出
 * @throws IllegalStateException 当二维码过期或用户取消登录时抛出
 * @see QRCodeState
 */
suspend fun Bot.qrCodeLogin(queryInterval: Long = 3000L, preloadContacts: Boolean = false) {
    require(queryInterval >= 1000L) { "查询间隔不能小于 1000 毫秒" }

    val selfInfo = client.packetContext.callService(GetSelfInfo)
    if (selfInfo.online) {
        completePmhqLogin(selfInfo, preloadContacts)
        return
    }
    val listenerId = client.packetContext.addEventListener(object : NodeIKernelLoginListener() {
        override suspend fun onQRCodeGetPicture(payload: PmhqLoginQrCodePayload) {
            val base64 = payload.pngBase64QrcodeData.removePrefix("data:image/png;base64,")
            sharedEventFlow.emit(
                QRCodeGeneratedEvent(
                    url = payload.qrcodeUrl,
                    png = Base64.decode(base64),
                )
            )
        }
    })
    try {
        client.packetContext.callService(GetQRCodePicture)
        val polledSelfInfo = awaitPmhqOnline(queryInterval, maxAttempts = 300)
        if (polledSelfInfo != null) {
            completePmhqLogin(polledSelfInfo, preloadContacts)
        } else {
            throw IllegalStateException("二维码登录超时，用户可能未扫码或未确认登录")
        }
    } finally {
        client.packetContext.removeEventListener(listenerId)
    }
}

/**
 * 使用 PMHQ 提供的登录能力进行登录。
 * 当 [quickLoginUin] 非空时，会优先检查当前登录态和快速登录列表，再在必要时回落到二维码登录。
 */
suspend fun Bot.login(
    queryInterval: Long = 3000L,
    preloadContacts: Boolean = false,
    quickLoginUin: Long? = null,
) {
    require(queryInterval >= 1000L) { "查询间隔不能小于 1000 毫秒" }

    val currentSelfInfo = client.packetContext.callService(GetSelfInfo)
    if (currentSelfInfo.online) {
        if (quickLoginUin == null || currentSelfInfo.uin == quickLoginUin) {
            logger.d { "当前 QQ 实例已登录账号 ${currentSelfInfo.uin}" }
            completePmhqLogin(currentSelfInfo, preloadContacts)
            return
        }
        throw IllegalStateException(
            "当前 QQ 实例已登录账号 ${currentSelfInfo.uin}，与要求的 uin $quickLoginUin 不匹配"
        )
    }

    if (quickLoginUin != null) {
        val quickLoginList = client.packetContext.callService(GetQuickLoginList)
        val canQuickLogin = quickLoginList
            ?.LocalLoginInfoList
            ?.any { it.uin == quickLoginUin.toString() && it.isQuickLogin && !it.isUserLogin }
            ?: false

        if (canQuickLogin) {
            logger.d { "尝试快速登录 $quickLoginUin" }
            launch {
                client.packetContext.callService(QuickLoginWithUin, quickLoginUin)
            }
            val quickLoggedInSelfInfo = awaitPmhqOnline(queryInterval, maxAttempts = 300)
            if (quickLoggedInSelfInfo != null) {
                if (quickLoggedInSelfInfo.uin != quickLoginUin) {
                    throw IllegalStateException(
                        "快速登录后 PMHQ 返回了 uin ${quickLoggedInSelfInfo.uin}，与要求的 uin $quickLoginUin 不匹配"
                    )
                }
                completePmhqLogin(quickLoggedInSelfInfo, preloadContacts)
                return
            }
        } else {
            logger.w { "找不到 $quickLoginUin 的快速登录信息，将进行二维码登录" }
        }
    }

    qrCodeLogin(queryInterval, preloadContacts)
}

/**
 * 使用 [org.ntqqrev.acidify.common.android.AndroidSessionStore] 中的密码进行登录。
 * @param onRequireCaptchaTicket 当需要验证码时的回调，参数为验证码 URL，返回值为验证码 Ticket
 * @param onRequireSmsCode 当需要短信验证码时的回调，参数为国家码、手机号和短信验证 URL，返回值为短信验证码
 * @param preloadContacts 是否预加载好友和群信息以初始化内存缓存
 */
suspend fun AndroidBot.passwordLogin(
    onRequireCaptchaTicket: suspend (captchaUrl: String) -> String,
    onRequireSmsCode: suspend (countryCode: String, phone: String, smsUrl: String) -> String,
    preloadContacts: Boolean = false
) {
    var result: WtLogin.AndroidLogin.Resp = client.callService(
        WtLogin.AndroidLogin.Tgtgt,
        WtLogin.AndroidLogin.Tgtgt.Req(
            energy = client.getEnergyFor(WtLogin.AndroidLogin.Tgtgt),
            debugXwid = client.getDebugXwidFor(WtLogin.AndroidLogin.Tgtgt),
        )
    )

    suspend fun handleSms(countryCode: String, phone: String, smsUrl: String): WtLogin.AndroidLogin.Resp {
        val smsCode = onRequireSmsCode(countryCode, phone, smsUrl)
        return if (smsCode.isNotEmpty()) {
            client.callService(
                WtLogin.AndroidLogin.SubmitSMSCode,
                WtLogin.AndroidLogin.SubmitSMSCode.Req(
                    energy = client.getEnergyFor(WtLogin.AndroidLogin.SubmitSMSCode),
                    debugXwid = client.getDebugXwidFor(WtLogin.AndroidLogin.SubmitSMSCode),
                    smsCode = smsCode,
                )
            )
        } else {
            client.callService(
                WtLogin.AndroidLogin.Tgtgt,
                WtLogin.AndroidLogin.Tgtgt.Req(
                    energy = client.getEnergyFor(WtLogin.AndroidLogin.Tgtgt),
                    debugXwid = client.getDebugXwidFor(WtLogin.AndroidLogin.Tgtgt),
                )
            )
        }
    }

    if (result.state == 2u.toUByte()) { // Need captcha verify
        result.tlvPack[0x104u]?.let {
            sessionStore.state.tlv104 = it
        }
        result.tlvPack[0x546u]?.let {
            sessionStore.state.tlv547 = POW.generateTlv547(it)
        }
        val captchaUrl = result.tlvPack[0x192u]!!.decodeToString()
        val ticket = onRequireCaptchaTicket(captchaUrl)
        result = client.callService(
            WtLogin.AndroidLogin.SubmitCaptchaTicket,
            WtLogin.AndroidLogin.SubmitCaptchaTicket.Req(
                energy = client.getEnergyFor(WtLogin.AndroidLogin.SubmitCaptchaTicket),
                debugXwid = client.getDebugXwidFor(WtLogin.AndroidLogin.SubmitCaptchaTicket),
                ticket = ticket,
            )
        )
        if (result.state == 160u.toUByte()) { // SMS required
            result.tlvPack[0x104u]?.let {
                sessionStore.state.tlv104 = it
            }
            val (countryCode, phone, smsUrl) = result.readSmsInfo()
            result = handleSms(countryCode, phone, smsUrl)
        }
    }

    if (result.state == 239u.toUByte()) { // Device lock via SMS code
        result.tlvPack[0x104u]?.let {
            sessionStore.state.tlv104 = it
        }
        result.tlvPack[0x174u]?.let {
            sessionStore.state.tlv174 = it
        }
        val (countryCode, phone, smsUrl) = result.readSmsInfo()
        result = client.callService(
            WtLogin.AndroidLogin.FetchSMSCode,
            WtLogin.AndroidLogin.FetchSMSCode.Req(
                debugXwid = client.getDebugXwidFor(WtLogin.AndroidLogin.FetchSMSCode),
            )
        )
        if (result.state == 160u.toUByte()) { // SMS required
            result.tlvPack[0x104u]?.let {
                sessionStore.state.tlv104 = it
            }
            result = handleSms(countryCode, phone, smsUrl)
        }
    }

    if (result.state != 0u.toUByte()) { // fallback; the error should be in tlv 146
        throw WtLoginException(result.state.toInt(), "", "")
    }

    val internalTlvPack = TEA.decrypt(result.tlvPack[0x119u]!!, client.sessionStore.wloginSigs.tgtgtKey)
        .parseTlv()

    sessionStore.apply {
        internalTlvPack[0x103u]?.let { wloginSigs.stWeb = it }
        internalTlvPack[0x143u]?.let { wloginSigs.d2 = it }
        internalTlvPack[0x108u]?.let { wloginSigs.ksid = it }
        internalTlvPack[0x10Au]?.let { wloginSigs.a2 = it }
        internalTlvPack[0x10Cu]?.let { wloginSigs.a1Key = it }
        internalTlvPack[0x10Du]?.let { wloginSigs.a2Key = it }
        internalTlvPack[0x10Eu]?.let { wloginSigs.stKey = it }
        internalTlvPack[0x114u]?.let { wloginSigs.st = it }
        // internalTlvPack[0x11Au]?.let { /* save age, gender, nickname */ }
        internalTlvPack[0x120u]?.let { wloginSigs.sKey = it }
        internalTlvPack[0x133u]?.let { wloginSigs.wtSessionTicket = it }
        internalTlvPack[0x134u]?.let { wloginSigs.wtSessionTicketKey = it }
        internalTlvPack[0x305u]?.let { wloginSigs.d2Key = it }
        internalTlvPack[0x106u]?.let { wloginSigs.a1 = it }
        internalTlvPack[0x16Au]?.let { wloginSigs.noPicSig = it }
        internalTlvPack[0x16Du]?.let { wloginSigs.superKey = it }
        internalTlvPack[0x512u]?.let {
            wloginSigs.psKey = mutableMapOf<String, String>().apply {
                val tlv512Reader = it.reader()
                val domainCount = tlv512Reader.readUShort()
                repeat(domainCount.toInt()) {
                    val domain = tlv512Reader.readPrefixedString(Prefix.UINT_16 or Prefix.LENGTH_ONLY)
                    val key = tlv512Reader.readPrefixedString(Prefix.UINT_16 or Prefix.LENGTH_ONLY)
                    val pt4Token = tlv512Reader.readPrefixedString(Prefix.UINT_16 or Prefix.LENGTH_ONLY)
                    this[domain] = key
                }
            }
        }
        internalTlvPack[0x543u]?.let {
            uid = it.pbDecode<AndroidThirdPartyLoginResponse>().commonInfo.rspNT.uid
        }
    }
    sharedEventFlow.emit(AndroidSessionStoreUpdatedEvent(sessionStore))
    online(preloadContacts)
}

/**
 * 如果 Session 为空则调用 [passwordLogin] 进行登录。
 * 如果 Session 不为空则尝试使用现有的 Session 信息登录，若失败则调用 [passwordLogin] 重新登录。
 * @param onRequireCaptchaTicket 当需要验证码时的回调，参数为验证码 URL，返回值为验证码 Ticket
 * @param onRequireSmsCode 当需要短信验证码时的回调，参数为国家码、手机号和短信验证 URL，返回值为短信验证码
 * @param preloadContacts 是否预加载好友和群信息以初始化内存缓存
 */
suspend fun AndroidBot.login(
    onRequireCaptchaTicket: suspend (captchaUrl: String) -> String,
    onRequireSmsCode: suspend (countryCode: String, phone: String, smsUrl: String) -> String,
    preloadContacts: Boolean = false
) {
    if (sessionStore.wloginSigs.a2.isEmpty()) {
        logger.i { "Session 为空，尝试密码登录" }
        passwordLogin(onRequireCaptchaTicket, onRequireSmsCode, preloadContacts)
    } else {
        try {
            online(preloadContacts)
        } catch (e: Exception) {
            logger.w(e) { "使用现有 Session 登录失败，尝试密码登录" }
            sessionStore.clear()
            // sharedEventFlow.emit(AndroidSessionStoreUpdatedEvent(sessionStore))
            passwordLogin(onRequireCaptchaTicket, onRequireSmsCode, preloadContacts)
        }
    }
}
