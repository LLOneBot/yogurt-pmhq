package org.ntqqrev.acidify.internal

import kotlinx.coroutines.CoroutineScope
import org.ntqqrev.acidify.common.AppInfo
import org.ntqqrev.acidify.common.SessionStore
import org.ntqqrev.acidify.common.SignProvider
import org.ntqqrev.acidify.exception.UrlSignException
import org.ntqqrev.acidify.internal.proto.system.SsoSecureInfo
import org.ntqqrev.acidify.internal.service.system.BotOnline
import org.ntqqrev.acidify.logging.Logger

internal class LagrangeClient(
    val appInfo: AppInfo,
    val sessionStore: SessionStore,
    val signProvider: SignProvider,
    loggerFactory: (Any) -> Logger,
    scope: CoroutineScope,
) : AbstractClient(loggerFactory, scope) {
    override val os: String
        get() = appInfo.os

    override val uin: Long
        get() = sessionStore.uin

    override val uid: String
        get() = sessionStore.uid

    override val appId: Int
        get() = appInfo.appId

    override val subAppId: Int
        get() = appInfo.subAppId

    override val currentVersion: String
        get() = appInfo.currentVersion

    override val appClientVersion: Int
        get() = appInfo.appClientVersion

    override val a2: ByteArray
        get() = sessionStore.a2

    override val d2: ByteArray
        get() = sessionStore.d2

    override val d2Key: ByteArray
        get() = sessionStore.d2Key

    override val guid: ByteArray
        get() = sessionStore.guid

    val signRequiredCommand = setOf(
        "MessageSvc.PbSendMsg",
        "wtlogin.trans_emp",
        "wtlogin.login",
        "trpc.login.ecdh.EcdhService.SsoKeyExchange",
        "trpc.login.ecdh.EcdhService.SsoNTLoginPasswordLogin",
        "trpc.login.ecdh.EcdhService.SsoNTLoginEasyLogin",
        "trpc.login.ecdh.EcdhService.SsoNTLoginPasswordLoginNewDevice",
        "trpc.login.ecdh.EcdhService.SsoNTLoginEasyLoginUnusualDevice",
        "trpc.login.ecdh.EcdhService.SsoNTLoginPasswordLoginUnusualDevice",
        "OidbSvcTrpcTcp.0x6d9_4"
    )

    override suspend fun getSsoSecureInfo(cmd: String, seq: Int, src: ByteArray): SsoSecureInfo? {
        return if (signRequiredCommand.contains(cmd)) {
            try {
                signProvider.sign(cmd, seq, src).let {
                    SsoSecureInfo(
                        sign = it.sign,
                        token = it.token,
                        extra = it.extra,
                    )
                }
            } catch (e: UrlSignException) {
                logger.w(e) { "没有成功获取 $cmd (seq=$seq) 的签名，该操作可能会失败" }
                null
            }
        } else {
            null
        }
    }

    override suspend fun sendOnlinePacket() = callService(BotOnline)
}