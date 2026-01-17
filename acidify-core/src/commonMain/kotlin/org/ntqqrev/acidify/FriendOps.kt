package org.ntqqrev.acidify

import org.ntqqrev.acidify.internal.service.friend.*
import org.ntqqrev.acidify.internal.service.message.SendFriendNudge
import org.ntqqrev.acidify.struct.BotFriendRequest
import org.ntqqrev.acidify.struct.BotFriendRequest.Companion.parseFilteredFriendRequest
import org.ntqqrev.acidify.struct.BotFriendRequest.Companion.parseFriendRequest

/**
 * 发送好友戳一戳
 * @param friendUin 好友 QQ 号
 * @param isSelf 是否戳自己（默认为 false）
 */
suspend fun Bot.sendFriendNudge(
    friendUin: Long,
    isSelf: Boolean = false
) = client.callService(
    SendFriendNudge,
    SendFriendNudge.Req(friendUin, isSelf)
)

/**
 * 给好友点赞
 * @param friendUin 好友 QQ 号
 * @param count 点赞次数（默认为 1）
 */
suspend fun Bot.sendProfileLike(
    friendUin: Long,
    count: Int = 1
) = client.callService(
    SendProfileLike,
    SendProfileLike.Req(getUidByUin(friendUin), count)
)

/**
 * 删除好友
 * @param friendUin 好友 QQ 号
 * @param block 是否将对方加入黑名单
 */
suspend fun Bot.deleteFriend(
    friendUin: Long,
    block: Boolean = false
) = client.callService(
    DeleteFriend,
    DeleteFriend.Req(getUidByUin(friendUin), block)
)

/**
 * 获取好友请求列表
 * @param isFiltered 是否只获取被过滤的请求（风险账号发起）
 * @param limit 获取的最大请求数量
 * @return 好友请求列表
 */
suspend fun Bot.getFriendRequests(isFiltered: Boolean = false, limit: Int = 20): List<BotFriendRequest> {
    return if (isFiltered) {
        client.callService(FetchFriendRequests.Filtered, limit)
            .map { parseFilteredFriendRequest(it) }
    } else {
        client.callService(FetchFriendRequests.Normal, limit)
            .map { parseFriendRequest(it) }
    }
}

/**
 * 处理好友请求（同意/拒绝）
 * @param initiatorUid 请求发起者 uid
 * @param accept 是否同意
 * @param isFiltered 是否是被过滤的请求
 */
suspend fun Bot.setFriendRequest(initiatorUid: String, accept: Boolean, isFiltered: Boolean = false) {
    if (isFiltered) {
        if (accept) {
            client.callService(SetFilteredFriendRequest, initiatorUid)
        }
    } else {
        client.callService(
            SetNormalFriendRequest,
            SetNormalFriendRequest.Req(initiatorUid, accept)
        )
    }
}
