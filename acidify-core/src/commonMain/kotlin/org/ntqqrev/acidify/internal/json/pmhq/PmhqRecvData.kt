package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable

@Serializable
internal class PmhqRecvData(
    val echo: String? = null,
    val cmd: String = "",
    val pb: String = "",
)
