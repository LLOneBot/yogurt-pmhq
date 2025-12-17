package org.ntqqrev.yogurt.api.system

import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import org.ntqqrev.acidify.Bot
import org.ntqqrev.milky.ApiEndpoint
import org.ntqqrev.milky.GetGroupListOutput
import org.ntqqrev.yogurt.transform.toMilkyEntity
import org.ntqqrev.yogurt.util.define

val GetGroupList = ApiEndpoint.GetGroupList.define {
    val bot = application.dependencies.resolve<Bot>()
    val groups = bot.getGroups(forceUpdate = it.noCache)
    GetGroupListOutput(
        groups = groups.map { group -> group.toMilkyEntity() }
    )
}