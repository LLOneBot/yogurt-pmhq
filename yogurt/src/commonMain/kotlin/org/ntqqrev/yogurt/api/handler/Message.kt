package org.ntqqrev.yogurt.api.handler

import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import org.ntqqrev.acidify.*
import org.ntqqrev.acidify.message.MessageScene
import org.ntqqrev.milky.*
import org.ntqqrev.yogurt.api.MilkyApiException
import org.ntqqrev.yogurt.api.define
import org.ntqqrev.yogurt.transform.*

val SendPrivateMessage = ApiEndpoint.SendPrivateMessage.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.getFriend(it.userId)
        ?: throw MilkyApiException(-404, "Friend not found")
    val result = bot.sendFriendMessage(it.userId) {
        with(
            YogurtMessageBuildingContext(
                application,
                this,
                MessageScene.FRIEND,
                it.userId
            )
        ) {
            it.message.forEach { segment ->
                applySegment(segment)
            }
        }
    }
    SendPrivateMessageOutput(
        messageSeq = result.sequence,
        time = result.sendTime
    )
}

val SendGroupMessage = ApiEndpoint.SendGroupMessage.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    val result = bot.sendGroupMessage(it.groupId) {
        with(
            YogurtMessageBuildingContext(
                application,
                this,
                MessageScene.GROUP,
                it.groupId
            )
        ) {
            it.message.forEach { segment ->
                applySegment(segment)
            }
        }
    }
    SendGroupMessageOutput(
        messageSeq = result.sequence,
        time = result.sendTime
    )
}

val RecallPrivateMessage = ApiEndpoint.RecallPrivateMessage.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.getFriend(it.userId)
        ?: throw MilkyApiException(-404, "Friend not found")
    bot.recallFriendMessage(
        friendUin = it.userId,
        sequence = it.messageSeq
    )
    RecallPrivateMessageOutput()
}

val RecallGroupMessage = ApiEndpoint.RecallGroupMessage.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    bot.recallGroupMessage(
        groupUin = it.groupId,
        sequence = it.messageSeq
    )
    RecallGroupMessageOutput()
}

val GetMessage = ApiEndpoint.GetMessage.define {
    val bot = application.dependencies.resolve<Bot>()
    val messages = when (it.messageScene.toMessageScene()) {
        MessageScene.FRIEND -> {
            bot.getFriend(it.peerId)
                ?: throw MilkyApiException(-404, "Friend not found")
            bot.getFriendHistoryMessages(
                friendUin = it.peerId,
                limit = 1,
                startSequence = it.messageSeq
            )
        }

        MessageScene.GROUP -> {
            bot.getGroup(it.peerId)
                ?: throw MilkyApiException(-404, "Group not found")
            bot.getGroupHistoryMessages(
                groupUin = it.peerId,
                limit = 1,
                startSequence = it.messageSeq
            )
        }

        else -> throw MilkyApiException(-400, "Unsupported message scene")
    }
    val message = messages.messages.firstOrNull()
        ?: throw MilkyApiException(-404, "Message not found")
    val transformedMessage = application.transformMessage(message)
        ?: throw MilkyApiException(-404, "Message transformation failed")
    GetMessageOutput(
        message = transformedMessage
    )
}

val GetHistoryMessages = ApiEndpoint.GetHistoryMessages.define {
    val bot = application.dependencies.resolve<Bot>()
    if (it.limit !in 1..30) {
        throw MilkyApiException(-400, "Limit must be between 1 and 30")
    }
    val historyMessages = when (it.messageScene.toMessageScene()) {
        MessageScene.FRIEND -> {
            bot.getFriend(it.peerId) ?: throw MilkyApiException(-404, "Friend not found")
            bot.getFriendHistoryMessages(
                friendUin = it.peerId,
                limit = it.limit,
                startSequence = it.startMessageSeq
            )
        }

        MessageScene.GROUP -> {
            bot.getGroup(it.peerId) ?: throw MilkyApiException(-404, "Group not found")
            bot.getGroupHistoryMessages(
                groupUin = it.peerId,
                limit = it.limit,
                startSequence = it.startMessageSeq
            )
        }

        else -> throw MilkyApiException(-400, "Unsupported message scene")
    }
    val transformedMessages = historyMessages.messages.mapNotNull { msg ->
        application.transformMessage(msg)
    }
    GetHistoryMessagesOutput(
        messages = transformedMessages,
        nextMessageSeq = historyMessages.nextStartSequence
    )
}

val GetResourceTempUrl = ApiEndpoint.GetResourceTempUrl.define {
    val bot = application.dependencies.resolve<Bot>()
    GetResourceTempUrlOutput(
        url = bot.getDownloadUrl(it.resourceId)
    )
}

val GetForwardedMessages = ApiEndpoint.GetForwardedMessages.define {
    val bot = application.dependencies.resolve<Bot>()
    val forwardedMessages = bot.getForwardedMessages(it.forwardId)
    val transformedMessages = forwardedMessages.map { msg ->
        IncomingForwardedMessage(
            senderName = msg.senderName,
            avatarUrl = msg.avatarUrl,
            time = msg.timestamp,
            segments = msg.segments.map { segment ->
                application.transformSegment(segment)
            }
        )
    }
    GetForwardedMessagesOutput(
        messages = transformedMessages
    )
}

val MarkMessageAsRead = ApiEndpoint.MarkMessageAsRead.define {
    val bot = application.dependencies.resolve<Bot>()
    when (it.messageScene.toMessageScene()) {
        MessageScene.FRIEND -> {
            bot.getFriend(it.peerId) ?: throw MilkyApiException(-404, "Friend not found")
            // Get the message time from history
            val messages = bot.getFriendHistoryMessages(
                friendUin = it.peerId,
                limit = 1,
                startSequence = it.messageSeq
            )
            val message = messages.messages.firstOrNull()
                ?: throw MilkyApiException(-404, "Message not found")
            bot.markFriendMessagesAsRead(
                friendUin = it.peerId,
                startSequence = it.messageSeq,
                startTime = message.timestamp
            )
        }

        MessageScene.GROUP -> {
            bot.getGroup(it.peerId) ?: throw MilkyApiException(-404, "Group not found")
            bot.markGroupMessagesAsRead(
                groupUin = it.peerId,
                startSequence = it.messageSeq
            )
        }

        else -> throw MilkyApiException(-400, "Unsupported message scene")
    }
    MarkMessageAsReadOutput()
}
