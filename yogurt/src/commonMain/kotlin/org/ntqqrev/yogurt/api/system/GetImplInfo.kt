package org.ntqqrev.yogurt.api.system

import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import org.ntqqrev.acidify.Bot
import org.ntqqrev.milky.ApiEndpoint
import org.ntqqrev.milky.GetImplInfoOutput
import org.ntqqrev.milky.milkyVersion
import org.ntqqrev.yogurt.BuildKonfig
import org.ntqqrev.yogurt.util.define

private fun String.toMilkyProtocolOs() = when (this) {
    "Windows" -> "windows"
    "Linux" -> "linux"
    "Mac" -> "macos"
    else -> "linux"
}

val GetImplInfo = ApiEndpoint.GetImplInfo.define {
    val bot = application.dependencies.resolve<Bot>()
    GetImplInfoOutput(
        implName = BuildKonfig.name,
        implVersion = BuildKonfig.version,
        qqProtocolVersion = bot.appInfo.currentVersion,
        qqProtocolType = bot.appInfo.os.toMilkyProtocolOs(),
        milkyVersion = milkyVersion,
    )
}