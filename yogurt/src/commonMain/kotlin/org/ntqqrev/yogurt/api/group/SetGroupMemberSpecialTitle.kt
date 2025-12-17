package org.ntqqrev.yogurt.api.group

import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import org.ntqqrev.acidify.Bot
import org.ntqqrev.milky.ApiEndpoint
import org.ntqqrev.milky.SetGroupMemberSpecialTitleOutput
import org.ntqqrev.yogurt.api.MilkyApiException

import org.ntqqrev.yogurt.util.define

val SetGroupMemberSpecialTitle = ApiEndpoint.SetGroupMemberSpecialTitle.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")

    bot.setGroupMemberSpecialTitle(it.groupId, it.userId, it.specialTitle)

    SetGroupMemberSpecialTitleOutput()
}