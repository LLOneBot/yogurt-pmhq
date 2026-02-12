package org.ntqqrev.yogurt

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import org.ntqqrev.acidify.*
import org.ntqqrev.acidify.common.AppInfo
import org.ntqqrev.acidify.common.SessionStore
import org.ntqqrev.acidify.common.UrlSignProvider
import org.ntqqrev.acidify.common.android.AndroidAppInfo
import org.ntqqrev.acidify.common.android.AndroidAppInfo.Sig
import org.ntqqrev.acidify.common.android.AndroidAppInfo.WtLoginSdkInfo
import org.ntqqrev.acidify.common.android.AndroidSessionStore
import org.ntqqrev.acidify.common.android.AndroidUrlSignProvider
import org.ntqqrev.milky.Event
import org.ntqqrev.yogurt.YogurtApp.config
import org.ntqqrev.yogurt.YogurtApp.t
import org.ntqqrev.yogurt.transform.transformAcidifyEvent
import org.ntqqrev.yogurt.util.logHandler

val bundledPCAppInfo = mapOf(
    "Linux/39038" to AppInfo(
        os = "Linux",
        kernel = "Linux",
        vendorOs = "linux",
        currentVersion = "3.2.19-39038",
        miscBitmap = 32764,
        ptVersion = "2.0.0",
        ssoVersion = 19,
        packageName = "com.tencent.qq",
        wtLoginSdk = "nt.wtlogin.0.0.1",
        appId = 1600001615,
        subAppId = 537313942,
        appClientVersion = 39038,
        mainSigMap = 169742560,
        subSigMap = 0,
        ntLoginType = 1
    ),
)

val bundledAndroidAppInfo = mapOf(
    "AndroidPhone/9.1.60" to AndroidAppInfo(
        os = "Android",
        vendorOs = "android",
        qua = "V1_AND_SQ_9.1.60_11520_YYB_D",
        currentVersion = "9.1.60.045f5d19",
        ptVersion = "9.1.60",
        ssoVersion = 22,
        packageName = "com.tencent.mobileqq",
        apkSignatureMd5 = "a6b745bf24a2c277527716f6f36eb68d".hexToByteArray(),
        sdkInfo = WtLoginSdkInfo(
            sdkBuildTime = 1740483688,
            sdkVersion = "6.0.0.2568",
            miscBitMap = 150470524,
            subSigMap = 66560,
            mainSigMap = Sig.WLOGIN_A5 or Sig.WLOGIN_RESERVED or Sig.WLOGIN_STWEB or Sig.WLOGIN_A2 or Sig.WLOGIN_ST
                    or Sig.WLOGIN_LSKEY or Sig.WLOGIN_SKEY or Sig.WLOGIN_SIG64 or Sig.WLOGIN_VKEY or Sig.WLOGIN_D2
                    or Sig.WLOGIN_SID or Sig.WLOGIN_PSKEY or Sig.WLOGIN_AQSIG or Sig.WLOGIN_LHSIG or Sig.WLOGIN_PAYTOKEN
                    or 65536L
        ),
        appId = 16,
        subAppId = 537275636,
        appClientVersion = 0
    ),
    "AndroidPad/9.2.20" to AndroidAppInfo(
        os = "ANDROID",
        vendorOs = "android",
        qua = "V1_AND_SQ_9.2.20_11650_YYB_D",
        currentVersion = "9.2.20.777b5929",
        ptVersion = "9.2.20",
        ssoVersion = 22,
        packageName = "com.tencent.mobileqq",
        apkSignatureMd5 = "a6b745bf24a2c277527716f6f36eb68d".hexToByteArray(),
        sdkInfo = WtLoginSdkInfo(
            sdkBuildTime = 1757058014,
            sdkVersion = "6.0.0.2589",
            miscBitMap = 150470524,
            subSigMap = 66560,
            mainSigMap = Sig.WLOGIN_A5 or Sig.WLOGIN_RESERVED or Sig.WLOGIN_STWEB or Sig.WLOGIN_A2 or Sig.WLOGIN_ST
                    or Sig.WLOGIN_LSKEY or Sig.WLOGIN_SKEY or Sig.WLOGIN_SIG64 or Sig.WLOGIN_VKEY or Sig.WLOGIN_D2
                    or Sig.WLOGIN_SID or Sig.WLOGIN_PSKEY or Sig.WLOGIN_AQSIG or Sig.WLOGIN_LHSIG or Sig.WLOGIN_PAYTOKEN
                    or 65536L
        ),
        appId = 16,
        subAppId = 537315825,
        appClientVersion = 0
    )
)

suspend fun Application.initializePC(): Bot {
    val signProvider = UrlSignProvider(config.signApiUrl)
    val sessionStore: SessionStore = if (SystemFileSystem.exists(sessionStorePath)) {
        SystemFileSystem.source(sessionStorePath).buffered().use {
            SessionStore.fromJson(it.readString())
        }
    } else SessionStore.empty()
    val appInfo: AppInfo = when (config.protocol.version) {
        "fetched" -> signProvider.getAppInfo()
            ?: throw IllegalStateException("通过 Sign API 获取 AppInfo 失败，请检查地址是否正确并且支持获取 AppInfo 功能")

        "custom" -> if (SystemFileSystem.exists(customAppInfoPath)) {
            SystemFileSystem.source(customAppInfoPath).buffered().use {
                AppInfo.fromJson(it.readString())
            }
        } else {
            throw IllegalStateException("未在 $customAppInfoPath 下找到自定义 AppInfo 文件")
        }

        else -> bundledPCAppInfo["${config.protocol.os}/${config.protocol.version}"]
            ?: throw IllegalStateException("未找到匹配的内置 AppInfo，请检查配置的 OS 和 Version 是否正确")
    }
    t.println("使用协议 ${appInfo.os} ${appInfo.currentVersion} (AppId: ${appInfo.subAppId})")
    val bot = Bot.create(
        appInfo = appInfo,
        sessionStore = sessionStore,
        signProvider = signProvider,
        scope = this@initializePC, // application is a CoroutineScope
        minLogLevel = config.logging.coreLogLevel,
        logHandler = YogurtApp.logHandler,
    )
    dependencies {
        provide<AbstractBot> { bot } cleanup { runBlocking { it.offline() } }
        provide<SharedFlow<Event>> {
            bot.eventFlow
                .map(this@initializePC::transformAcidifyEvent)
                .filterNotNull()
                .shareIn(
                    scope = this@initializePC,
                    started = SharingStarted.Lazily,
                )
        }
    }
    return bot
}

suspend fun Application.initializeAndroid(): AndroidBot {
    require(config.androidCredentials.uin != 0L && config.androidCredentials.password.isNotEmpty()) {
        "请在配置文件中填写 androidCredentials 的 uin 和 password 字段"
    }
    val signProvider = AndroidUrlSignProvider(config.signApiUrl)
    val sessionStore: AndroidSessionStore = if (SystemFileSystem.exists(androidSessionStorePath)) {
        SystemFileSystem.source(androidSessionStorePath).buffered().use {
            AndroidSessionStore.fromJson(it.readString())
        }
    } else AndroidSessionStore.empty(
        uin = config.androidCredentials.uin,
        password = config.androidCredentials.password
    )
    val appInfo: AndroidAppInfo = when (config.protocol.version) {
        "fetched" -> throw IllegalStateException("Android 协议不支持通过 Sign API 获取 AppInfo，请使用内置版本或自定义版本")

        "custom" -> if (SystemFileSystem.exists(customAppInfoPath)) {
            SystemFileSystem.source(customAppInfoPath).buffered().use {
                AndroidAppInfo.fromJson(it.readString())
            }
        } else {
            throw IllegalStateException("未在 $customAppInfoPath 下找到自定义 AppInfo 文件")
        }

        else -> bundledAndroidAppInfo["${config.protocol.os}/${config.protocol.version}"]
            ?: throw IllegalStateException("未找到匹配的内置 AppInfo，请检查配置的 OS 和 Version 是否正确")
    }
    t.println("使用协议 ${appInfo.os} ${appInfo.currentVersion} (AppId: ${appInfo.subAppId})")
    val androidBot = AndroidBot.create(
        appInfo = appInfo,
        sessionStore = sessionStore,
        signProvider = signProvider,
        scope = this@initializeAndroid, // application is a CoroutineScope
        minLogLevel = config.logging.coreLogLevel,
        logHandler = YogurtApp.logHandler,
    )
    dependencies {
        provide<AbstractBot> { androidBot } cleanup { runBlocking { it.offline() } }
        provide<SharedFlow<Event>> {
            androidBot.eventFlow
                .map(this@initializeAndroid::transformAcidifyEvent)
                .filterNotNull()
                .shareIn(
                    scope = this@initializeAndroid,
                    started = SharingStarted.Lazily,
                )
        }
        // TODO: provide captcha and SMS code source refs
    }
    return androidBot
}

suspend fun Application.botLogin() {
    when (val bot = dependencies.resolve<AbstractBot>()) {
        is Bot -> bot.login(preloadContacts = config.preloadContacts)
        is AndroidBot -> bot.login(
            // TODO: use WebUI to submit captcha and SMS code
            onRequireCaptchaTicket = { captchaUrl ->
                val queryParams = captchaUrl.split("?")[1].replace("uin=0", "uin=${bot.uin}")
                t.println("https://captcha.lagrangecore.org/?$queryParams")
                t.println("请打开网页用 F12 抓取 ticket 后输入，并按 Enter 提交：")
                readln().trim()
            },
            onRequireSmsCode = { countryCode, phone, _ ->
                // println("Manual verify URL: $_")
                t.println("短信已发送到 $countryCode-$phone，请输入收到的验证码，并按 Enter 提交：")
                readln().trim()
            },
            preloadContacts = config.preloadContacts,
        )
    }
}