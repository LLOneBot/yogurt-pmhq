package org.ntqqrev.acidify.internal.util

import kotlinx.io.Buffer
import org.ntqqrev.acidify.exception.ServiceInternalException
import org.ntqqrev.acidify.internal.KuromeClient
import org.ntqqrev.acidify.internal.LagrangeClient
import org.ntqqrev.acidify.internal.service.Service
import org.ntqqrev.acidify.internal.service.system.WtLogin
import org.ntqqrev.acidify.internal.tlv.AndroidTlv
import org.ntqqrev.acidify.internal.tlv.Tlv
import org.ntqqrev.acidify.internal.tlv.TlvQRCode

internal fun Service<*, *>.checkRetCode(retCode: Int, retMsg: String? = null) {
    if (retCode != 0) {
        throw ServiceInternalException(this, retCode, retMsg ?: "")
    }
}

internal inline fun LagrangeClient.buildTlv(block: Tlv.() -> Unit): Buffer {
    val tlv = Tlv(this)
    tlv.block()
    return tlv.build()
}

internal inline fun LagrangeClient.buildTlvQRCode(block: TlvQRCode.() -> Unit): Buffer {
    val tlv = TlvQRCode(this)
    block(tlv)
    return tlv.build()
}

internal inline fun KuromeClient.buildTlv(block: AndroidTlv.() -> Unit): Buffer {
    val tlv = AndroidTlv(this)
    tlv.block()
    return tlv.build()
}

internal suspend fun KuromeClient.getEnergyFor(service: WtLogin.AndroidLogin<*>): ByteArray {
    return signProvider.energy(
        uin = uin,
        data = "${service.wtLoginSubCmd.toString(16)}_${service.internalCmd.toString(16)}",
        guid = sessionStore.guid.toHexString(),
        ver = appInfo.sdkInfo.sdkVersion,
        version = appInfo.ptVersion,
        qua = appInfo.qua,
    )
}

internal suspend fun KuromeClient.getDebugXwidFor(service: WtLogin.AndroidLogin<*>): ByteArray {
    return signProvider.getDebugXwid(
        uin = uin,
        data = "${service.wtLoginSubCmd.toString(16)}_${service.internalCmd.toString(16)}",
        guid = sessionStore.guid.toHexString(),
        version = appInfo.ptVersion,
        qua = appInfo.qua,
    )
}

/**
 * Triple of country code, mobile number and manual verify URL
 */
internal fun WtLogin.AndroidLogin.Resp.readSmsInfo(): Triple<String, String, String> {
    val smsUrl = tlvPack[0x204u]?.decodeToString()
    val tlv178Reader = tlvPack[0x178u]!!.reader()
    val countryCode = tlv178Reader.readPrefixedString(Prefix.UINT_16 or Prefix.LENGTH_ONLY)
    val phone = tlv178Reader.readPrefixedString(Prefix.UINT_16 or Prefix.LENGTH_ONLY)
    return Triple(countryCode, phone, smsUrl ?: "")
}