package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable

@Serializable
internal class PmhqSendData(
    val echo: String = "",
    val cmd: String = "",
    val pb: String = "",
)
