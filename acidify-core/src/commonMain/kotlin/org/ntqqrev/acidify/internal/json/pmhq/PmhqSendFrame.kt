package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable

@Serializable
internal class PmhqSendFrame(
    val type: String = "send",
    val data: PmhqSendData = PmhqSendData(),
)
