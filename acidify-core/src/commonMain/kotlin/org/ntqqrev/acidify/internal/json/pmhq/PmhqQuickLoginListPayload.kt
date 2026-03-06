package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable

@Serializable
internal class PmhqQuickLoginListPayload(
    val result: Int = 0,
    val LocalLoginInfoList: List<PmhqQuickLoginAccount> = emptyList(),
)
