package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable

@Serializable
internal class PmhqSelfInfoPayload(
    val uin: String = "",
    val uid: String = "",
    val online: Boolean = false,
    val nick: String = "",
)
