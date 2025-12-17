package org.ntqqrev.yogurt.api.message

import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import org.ntqqrev.acidify.Bot
import org.ntqqrev.milky.ApiEndpoint
import org.ntqqrev.milky.GetResourceTempUrlOutput
import org.ntqqrev.yogurt.util.define

val GetResourceTempUrl = ApiEndpoint.GetResourceTempUrl.define {
    val bot = application.dependencies.resolve<Bot>()
    GetResourceTempUrlOutput(
        url = bot.getDownloadUrl(it.resourceId)
    )
}