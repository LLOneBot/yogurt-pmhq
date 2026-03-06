package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal class PmhqIncomingFrame(
    val type: String = "",
    val data: JsonElement? = null,
    val code: Int = 0,
    val message: String? = null,
)
