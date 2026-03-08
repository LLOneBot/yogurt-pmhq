package org.ntqqrev.acidify.common

import kotlinx.serialization.json.JsonElement

class PmhqCallResponse(
    val code: Int,
    val message: String?,
    val result: JsonElement?,
)
