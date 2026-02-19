package org.ntqqrev.yogurt.api.handler

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import org.ntqqrev.acidify.*
import org.ntqqrev.milky.*
import org.ntqqrev.yogurt.api.MilkyApiException
import org.ntqqrev.yogurt.api.define
import org.ntqqrev.yogurt.transform.toEventType
import org.ntqqrev.yogurt.transform.toIntReactionType
import org.ntqqrev.yogurt.transform.toMilkyEntity
import org.ntqqrev.yogurt.transform.transformEssenceMessage
import org.ntqqrev.yogurt.util.resolveUri

val SetGroupName = ApiEndpoint.SetGroupName.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.setGroupName(it.groupId, it.newGroupName)
    SetGroupNameOutput()
}

val SetGroupAvatar = ApiEndpoint.SetGroupAvatar.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    val imageData = resolveUri(it.imageUri)
    bot.setGroupAvatar(it.groupId, imageData)
    SetGroupAvatarOutput()
}

val SetGroupMemberCard = ApiEndpoint.SetGroupMemberCard.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.setGroupMemberCard(it.groupId, it.userId, it.card)
    SetGroupMemberCardOutput()
}

val SetGroupMemberSpecialTitle = ApiEndpoint.SetGroupMemberSpecialTitle.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.setGroupMemberSpecialTitle(it.groupId, it.userId, it.specialTitle)
    SetGroupMemberSpecialTitleOutput()
}

val SetGroupMemberAdmin = ApiEndpoint.SetGroupMemberAdmin.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.setGroupMemberAdmin(it.groupId, it.userId, it.isSet)
    SetGroupMemberAdminOutput()
}

val SetGroupMemberMute = ApiEndpoint.SetGroupMemberMute.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.setGroupMemberMute(it.groupId, it.userId, it.duration)
    SetGroupMemberMuteOutput()
}

val SetGroupWholeMute = ApiEndpoint.SetGroupWholeMute.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.setGroupWholeMute(it.groupId, it.isMute)
    SetGroupWholeMuteOutput()
}

val KickGroupMember = ApiEndpoint.KickGroupMember.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.kickGroupMember(it.groupId, it.userId, it.rejectAddRequest)
    KickGroupMemberOutput()
}

val GetGroupAnnouncements = ApiEndpoint.GetGroupAnnouncements.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    val announcements = bot.getGroupAnnouncements(it.groupId)
    GetGroupAnnouncementsOutput(
        announcements = announcements.map { announcement ->
            GroupAnnouncementEntity(
                groupId = announcement.groupUin,
                announcementId = announcement.announcementId,
                userId = announcement.senderId,
                time = announcement.time,
                content = announcement.content,
                imageUrl = announcement.imageUrl
            )
        }
    )
}

val SendGroupAnnouncement = ApiEndpoint.SendGroupAnnouncement.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.sendGroupAnnouncement(it.groupId, it.content, null)
    SendGroupAnnouncementOutput()
}

val DeleteGroupAnnouncement = ApiEndpoint.DeleteGroupAnnouncement.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.deleteGroupAnnouncement(it.groupId, it.announcementId)
    DeleteGroupAnnouncementOutput()
}

val GetGroupEssenceMessages = ApiEndpoint.GetGroupEssenceMessages.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    val essenceMessageResult = bot.getGroupEssenceMessages(
        groupUin = it.groupId,
        pageIndex = it.pageIndex,
        pageSize = it.pageSize
    )
    GetGroupEssenceMessagesOutput(
        messages = essenceMessageResult.messages.map {
            application.async { application.transformEssenceMessage(it) }
        }.awaitAll(),
        isEnd = essenceMessageResult.isEnd
    )
}

val SetGroupEssenceMessage = ApiEndpoint.SetGroupEssenceMessage.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.setGroupEssenceMessage(it.groupId, it.messageSeq, it.isSet)
    SetGroupEssenceMessageOutput()
}

val QuitGroup = ApiEndpoint.QuitGroup.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.quitGroup(it.groupId)
    QuitGroupOutput()
}

val SendGroupMessageReaction = ApiEndpoint.SendGroupMessageReaction.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.setGroupMessageReaction(
        it.groupId,
        it.messageSeq,
        it.reaction,
        it.reactionType.toIntReactionType(),
        it.isAdd
    )
    SendGroupMessageReactionOutput()
}

val SendGroupNudge = ApiEndpoint.SendGroupNudge.define {
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.sendGroupNudge(it.groupId, it.userId)
    SendGroupNudgeOutput()
}

val GetGroupNotifications = ApiEndpoint.GetGroupNotifications.define {
    val (notifications, nextSeq) = bot.getGroupNotifications(
        startSequence = it.startNotificationSeq,
        isFiltered = it.isFiltered,
        count = it.limit
    )
    GetGroupNotificationsOutput(
        notifications = notifications.map { it.toMilkyEntity() },
        nextNotificationSeq = nextSeq
    )
}

val AcceptGroupRequest = ApiEndpoint.AcceptGroupRequest.define {
    bot.setGroupRequest(
        groupUin = it.groupId,
        sequence = it.notificationSeq,
        eventType = it.notificationType.toEventType(),
        accept = true,
        isFiltered = it.isFiltered
    )
    AcceptGroupRequestOutput()
}

val RejectGroupRequest = ApiEndpoint.RejectGroupRequest.define {
    bot.setGroupRequest(
        groupUin = it.groupId,
        sequence = it.notificationSeq,
        eventType = it.notificationType.toEventType(),
        accept = false,
        isFiltered = it.isFiltered,
        reason = it.reason ?: ""
    )
    RejectGroupRequestOutput()
}

val AcceptGroupInvitation = ApiEndpoint.AcceptGroupInvitation.define {
    bot.setGroupInvitation(
        groupUin = it.groupId,
        invitationSeq = it.invitationSeq,
        accept = true
    )
    AcceptGroupInvitationOutput()
}

val RejectGroupInvitation = ApiEndpoint.RejectGroupInvitation.define {
    bot.setGroupInvitation(
        groupUin = it.groupId,
        invitationSeq = it.invitationSeq,
        accept = false
    )
    RejectGroupInvitationOutput()
}
