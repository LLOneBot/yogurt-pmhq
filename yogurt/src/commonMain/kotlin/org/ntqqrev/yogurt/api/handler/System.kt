package org.ntqqrev.yogurt.api.handler

import io.ktor.server.plugins.di.*
import io.ktor.server.routing.*
import org.ntqqrev.acidify.*
import org.ntqqrev.milky.*
import org.ntqqrev.yogurt.BuildKonfig
import org.ntqqrev.yogurt.api.MilkyApiException
import org.ntqqrev.yogurt.api.define
import org.ntqqrev.yogurt.transform.toMilkyEntity
import org.ntqqrev.yogurt.transform.toMilkyOutput
import org.ntqqrev.yogurt.util.resolveUri

val GetLoginInfo = ApiEndpoint.GetLoginInfo.define {
    val bot = application.dependencies.resolve<Bot>()
    GetLoginInfoOutput(
        uin = bot.uin,
        nickname = bot.fetchUserInfoByUid(bot.uid).nickname
    )
}

private fun String.toMilkyProtocolOs() = when (this) {
    "Windows" -> "windows"
    "Linux" -> "linux"
    "Mac" -> "macos"
    else -> "linux"
}

val GetImplInfo = ApiEndpoint.GetImplInfo.define {
    val bot = application.dependencies.resolve<Bot>()
    GetImplInfoOutput(
        implName = BuildKonfig.name,
        implVersion = BuildKonfig.version,
        qqProtocolVersion = bot.appInfo.currentVersion,
        qqProtocolType = bot.appInfo.os.toMilkyProtocolOs(),
        milkyVersion = milkyVersion,
    )
}

val GetUserProfile = ApiEndpoint.GetUserProfile.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.fetchUserInfoByUin(it.userId).toMilkyOutput()
}

val GetFriendList = ApiEndpoint.GetFriendList.define {
    val bot = application.dependencies.resolve<Bot>()
    val friends = bot.getFriends(forceUpdate = it.noCache)
    GetFriendListOutput(
        friends = friends.map { friend -> friend.toMilkyEntity() }
    )
}

val GetFriendInfo = ApiEndpoint.GetFriendInfo.define {
    val bot = application.dependencies.resolve<Bot>()
    val friend = bot.getFriend(it.userId, forceUpdate = it.noCache)
        ?: throw MilkyApiException(-404, "Friend not found")
    GetFriendInfoOutput(
        friend = friend.toMilkyEntity()
    )
}

val GetGroupList = ApiEndpoint.GetGroupList.define {
    val bot = application.dependencies.resolve<Bot>()
    val groups = bot.getGroups(forceUpdate = it.noCache)
    GetGroupListOutput(
        groups = groups.map { group -> group.toMilkyEntity() }
    )
}

val GetGroupInfo = ApiEndpoint.GetGroupInfo.define {
    val bot = application.dependencies.resolve<Bot>()
    val group = bot.getGroup(it.groupId, forceUpdate = it.noCache)
        ?: throw MilkyApiException(-404, "Group not found")
    GetGroupInfoOutput(
        group = group.toMilkyEntity()
    )
}

val GetGroupMemberList = ApiEndpoint.GetGroupMemberList.define {
    val bot = application.dependencies.resolve<Bot>()
    val group = bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    val members = group.getMembers(forceUpdate = it.noCache)
    GetGroupMemberListOutput(
        members = members.map { member -> member.toMilkyEntity() }
    )
}

val GetGroupMemberInfo = ApiEndpoint.GetGroupMemberInfo.define {
    val bot = application.dependencies.resolve<Bot>()
    val group = bot.getGroup(it.groupId)
        ?: throw MilkyApiException(-404, "Group not found")
    val member = group.getMember(it.userId, forceUpdate = it.noCache)
        ?: throw MilkyApiException(-404, "Group member not found")
    GetGroupMemberInfoOutput(
        member = member.toMilkyEntity()
    )
}

val SetAvatar = ApiEndpoint.SetAvatar.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.setAvatar(resolveUri(it.uri))
    SetAvatarOutput()
}

val SetNickname = ApiEndpoint.SetNickname.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.setNickname(it.newNickname)
    SetNicknameOutput()
}

val SetBio = ApiEndpoint.SetBio.define {
    val bot = application.dependencies.resolve<Bot>()
    bot.setBio(it.newBio)
    SetBioOutput()
}

val GetCustomFaceUrlList = ApiEndpoint.GetCustomFaceUrlList.define {
    val bot = application.dependencies.resolve<Bot>()
    GetCustomFaceUrlListOutput(bot.getCustomFaceUrl())
}

val GetCookies = ApiEndpoint.GetCookies.define {
    val bot = application.dependencies.resolve<Bot>()
    GetCookiesOutput(
        cookies = bot.getCookies(it.domain).entries
            .joinToString("; ") { "${it.key}=${it.value}" }
    )
}

val GetCsrfToken = ApiEndpoint.GetCsrfToken.define {
    val bot = application.dependencies.resolve<Bot>()
    GetCsrfTokenOutput(
        csrfToken = bot.getCsrfToken().toString()
    )
}