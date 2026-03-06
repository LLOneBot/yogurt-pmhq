package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable

@Serializable
internal class PmhqQuickLoginAccount(
    val uin: String = "",
    val uid: String = "",
    val nickname: String = "",
    val faceUrl: String = "",
    val loginType: Int = 0,
    val isQuickLogin: Boolean = false,
    val isAutoLogin: Boolean = false,
    val isUserLogin: Boolean = false,
)
