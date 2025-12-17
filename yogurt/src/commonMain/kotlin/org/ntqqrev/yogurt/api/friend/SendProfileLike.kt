package org.ntqqrev.yogurt.api.friend

import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import org.ntqqrev.acidify.Bot
import org.ntqqrev.milky.ApiEndpoint
import org.ntqqrev.milky.SendProfileLikeOutput
import org.ntqqrev.yogurt.util.define

val SendProfileLike = ApiEndpoint.SendProfileLike.define {
    val bot = application.dependencies.resolve<Bot>()

    bot.sendProfileLike(it.userId, it.count)

    SendProfileLikeOutput()
}