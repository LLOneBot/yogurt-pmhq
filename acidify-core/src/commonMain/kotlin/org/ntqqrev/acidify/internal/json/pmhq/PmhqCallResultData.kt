package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal class PmhqCallResultData(
    val echo: String? = null,
    val result: JsonElement? = null,
)
