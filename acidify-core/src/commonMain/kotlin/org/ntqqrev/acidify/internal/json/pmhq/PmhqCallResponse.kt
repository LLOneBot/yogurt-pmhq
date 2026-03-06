package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.json.JsonElement

internal class PmhqCallResponse(
    val code: Int,
    val message: String?,
    val result: JsonElement?,
)
