package org.ntqqrev.yogurt

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import org.ntqqrev.acidify.*
import org.ntqqrev.acidify.common.AppInfo
import org.ntqqrev.acidify.common.SessionStore
import org.ntqqrev.acidify.common.UrlSignProvider
import org.ntqqrev.acidify.common.android.AndroidAppInfo
import org.ntqqrev.acidify.common.android.AndroidSessionStore
import org.ntqqrev.acidify.common.android.AndroidSignProvider
import org.ntqqrev.acidify.common.android.AndroidUrlSignProvider
import org.ntqqrev.milky.Event
import org.ntqqrev.yogurt.YogurtApp.config
import org.ntqqrev.yogurt.YogurtApp.t
import org.ntqqrev.yogurt.transform.transformAcidifyEvent
import org.ntqqrev.yogurt.util.AndroidLegacyUrlSignProvider
import org.ntqqrev.yogurt.util.logHandler

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
    val signProvider: AndroidSignProvider = if (!config.androidUseLegacySign) {
        AndroidUrlSignProvider(config.signApiUrl)
    } else {
        AndroidLegacyUrlSignProvider(config.signApiUrl)
    }
    val sessionStore: AndroidSessionStore = if (SystemFileSystem.exists(androidSessionStorePath)) {
        SystemFileSystem.source(androidSessionStorePath).buffered().use {
            AndroidSessionStore.fromJson(it.readString())
        }.takeIf {
            it.uin == config.androidCredentials.uin && it.password == config.androidCredentials.password
        } ?: run {
            t.println("找到的 SessionStore 与配置的 uin 不匹配，正在创建新的 SessionStore...")
            AndroidSessionStore.empty(
                uin = config.androidCredentials.uin,
                password = config.androidCredentials.password
            )
        }
    } else AndroidSessionStore.empty(
        uin = config.androidCredentials.uin,
        password = config.androidCredentials.password
    ).also {
        t.println("未找到 Android SessionStore，正在创建新的 SessionStore 并保存到文件...")
        SystemFileSystem.sink(androidSessionStorePath).buffered().use { sink ->
            sink.writeString(it.toJson())
        }
    }
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
    if (signProvider is AndroidLegacyUrlSignProvider) {
        t.println("正在注册设备信息到 Sign API...")
        signProvider.registerDevice(
            qua = appInfo.qua,
            uin = sessionStore.uin,
            qimei = sessionStore.qimei,
            guid = sessionStore.guid.toHexString(),
        )
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
            onRequireSmsCode = { countryCode, phone, url ->
                t.println("短信已发送到 $countryCode-$phone，请输入收到的验证码，并按 Enter 提交。")
                t.println("如果未收到验证码，也可以进行通过下面的 URL 进行手动验证，然后直接按 Enter 以继续登录。")
                t.println(url)
                readln().trim()
            },
            preloadContacts = config.preloadContacts,
        )
    }
}