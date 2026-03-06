package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal class PmhqEventData(
    val echo: String? = null,
    @SerialName("sub_type") val subType: String = "",
    val data: JsonElement? = null,
)
