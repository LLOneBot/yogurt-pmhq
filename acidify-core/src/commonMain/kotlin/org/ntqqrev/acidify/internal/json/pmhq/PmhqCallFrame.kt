package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable

@Serializable
internal class PmhqCallFrame(
    val type: String = "call",
    val data: PmhqCallData = PmhqCallData(),
)
