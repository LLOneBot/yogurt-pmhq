package org.ntqqrev.acidify.js

import kotlinx.coroutines.await
import kotlinx.coroutines.promise
import org.ntqqrev.acidify.AndroidBot
import org.ntqqrev.acidify.common.android.AndroidAppInfo
import org.ntqqrev.acidify.common.android.AndroidSessionStore
import org.ntqqrev.acidify.common.android.AndroidSignProvider
import org.ntqqrev.acidify.logging.LogHandler
import org.ntqqrev.acidify.logging.LogLevel
import org.ntqqrev.acidify.login
import kotlin.js.Promise

@JsExport
@JsName("AndroidBot")
@AcidifyJsWrapper
class JsAndroidBot internal constructor(override val bot: AndroidBot) : JsAbstractBot(bot) {
    fun login(
        onRequireCaptchaTicket: (captchaUrl: String) -> Promise<String>,
        onRequireSmsCode: (countryCode: String, phone: String, smsUrl: String) -> Promise<String>,
        preloadContacts: Boolean = false
    ) = promise {
        bot.login(
            onRequireCaptchaTicket = { captchaUrl ->
                onRequireCaptchaTicket(captchaUrl).await()
            },
            onRequireSmsCode = { countryCode, phone, smsUrl ->
                onRequireSmsCode(countryCode, phone, smsUrl).await()
            },
            preloadContacts = preloadContacts
        )
    }

    companion object {
        @JsStatic
        fun create(
            appInfo: AndroidAppInfo,
            sessionStore: AndroidSessionStore,
            signProvider: JsAndroidSignProvider,
            jsScope: JsCoroutineScope,
            minLogLevel: LogLevel,
            logHandler: LogHandler
        ): Promise<JsAndroidBot> = jsScope.value.promise {
            JsAndroidBot(
                AndroidBot.create(
                    appInfo = appInfo,
                    sessionStore = sessionStore,
                    signProvider = object : AndroidSignProvider {
                        override suspend fun sign(
                            uin: Long,
                            cmd: String,
                            buffer: ByteArray,
                            guid: String,
                            seq: Int,
                            version: String,
                            qua: String,
                        ) = signProvider.sign(uin, cmd, buffer, guid, seq, version, qua).await()

                        override suspend fun energy(
                            uin: Long,
                            data: String,
                            guid: String,
                            ver: String,
                            version: String,
                            qua: String,
                        ) = signProvider.energy(uin, data, guid, ver, version, qua).await()

                        override suspend fun getDebugXwid(
                            uin: Long,
                            data: String,
                            guid: String,
                            version: String,
                            qua: String,
                        ) = signProvider.getDebugXwid(uin, data, guid, version, qua).await()
                    },
                    scope = jsScope.value,
                    minLogLevel = minLogLevel,
                    logHandler = logHandler,
                )
            )
        }
    }
}
