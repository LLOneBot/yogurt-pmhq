package org.ntqqrev.yogurt.api.group

import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import org.ntqqrev.acidify.Bot
import org.ntqqrev.milky.ApiEndpoint
import org.ntqqrev.milky.DeleteGroupAnnouncementOutput
import org.ntqqrev.yogurt.api.MilkyApiException

import org.ntqqrev.yogurt.util.define

val DeleteGroupAnnouncement = ApiEndpoint.DeleteGroupAnnouncement.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")

    bot.deleteGroupAnnouncement(it.groupId, it.announcementId)

    DeleteGroupAnnouncementOutput()
}

