package org.ntqqrev.acidify.struct.internal

import org.ntqqrev.acidify.Bot
import org.ntqqrev.acidify.getUinByUid
import org.ntqqrev.acidify.internal.proto.oidb.FilteredFriendRequestItem
import org.ntqqrev.acidify.internal.proto.oidb.FriendRequestItem
import org.ntqqrev.acidify.struct.BotFriendRequest
import org.ntqqrev.acidify.struct.RequestState

internal suspend fun Bot.parseFriendRequest(
    raw: FriendRequestItem
) = BotFriendRequest(
    time = raw.timestamp,
    initiatorUin = getUinByUid(raw.sourceUid),
    initiatorUid = raw.sourceUid,
    targetUserUin = getUinByUid(raw.targetUid),
    targetUserUid = raw.targetUid,
    state = when (raw.state) {
        1 -> RequestState.PENDING
        3 -> RequestState.ACCEPTED
        7 -> RequestState.REJECTED
        else -> RequestState.DEFAULT
    },
    comment = raw.comment,
    via = raw.source,
    isFiltered = false
)

internal suspend fun Bot.parseFilteredFriendRequest(
    raw: FilteredFriendRequestItem
) = BotFriendRequest(
    time = raw.timestamp,
    initiatorUin = getUinByUid(raw.sourceUid),
    initiatorUid = raw.sourceUid,
    targetUserUin = uin,
    targetUserUid = uid,
    state = RequestState.PENDING,
    comment = raw.comment,
    via = raw.source,
    isFiltered = true
)