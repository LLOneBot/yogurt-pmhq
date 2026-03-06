package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal class PmhqCallData(
    val echo: String = "",
    val func: String = "",
    val args: List<JsonElement> = emptyList(),
)
