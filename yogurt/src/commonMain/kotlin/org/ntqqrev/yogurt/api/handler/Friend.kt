package org.ntqqrev.yogurt.api.handler

import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import org.ntqqrev.acidify.*
import org.ntqqrev.milky.*
import org.ntqqrev.yogurt.api.define
import org.ntqqrev.yogurt.transform.toMilkyEntity

val SendFriendNudge = ApiEndpoint.SendFriendNudge.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.sendFriendNudge(it.userId, it.isSelf)
    SendFriendNudgeOutput()
}

val SendProfileLike = ApiEndpoint.SendProfileLike.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.sendProfileLike(it.userId, it.count)
    SendProfileLikeOutput()
}

val DeleteFriend = ApiEndpoint.DeleteFriend.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.deleteFriend(it.userId)
    DeleteFriendOutput()
}

val GetFriendRequests = ApiEndpoint.GetFriendRequests.define {
    val bot = application.dependencies.resolve<Bot>()
    val requests = bot.getFriendRequests(it.isFiltered, it.limit)
    GetFriendRequestsOutput(
        requests = requests.map { req -> req.toMilkyEntity() }
    )
}

val AcceptFriendRequest = ApiEndpoint.AcceptFriendRequest.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.setFriendRequest(
        initiatorUid = it.initiatorUid,
        accept = true,
        isFiltered = it.isFiltered
    )
    AcceptFriendRequestOutput()
}

val RejectFriendRequest = ApiEndpoint.RejectFriendRequest.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.setFriendRequest(
        initiatorUid = it.initiatorUid,
        accept = false,
        isFiltered = it.isFiltered
    )
    RejectFriendRequestOutput()
}
