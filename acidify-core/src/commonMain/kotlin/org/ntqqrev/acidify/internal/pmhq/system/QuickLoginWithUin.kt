package org.ntqqrev.acidify.internal.pmhq.system

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import org.ntqqrev.acidify.internal.AbstractClient
import org.ntqqrev.acidify.internal.json.pmhq.PmhqQuickLoginResult
import org.ntqqrev.acidify.internal.pmhq.PmhqService
import org.ntqqrev.acidify.internal.pmhq.pmhqJson

internal object QuickLoginWithUin : PmhqService<Long, PmhqQuickLoginResult?>("loginService.quickLoginWithUin") {
    override fun build(client: AbstractClient, payload: Long): List<JsonElement> =
        listOf(JsonPrimitive(payload.toString()))

    override fun parse(client: AbstractClient, payload: JsonElement?): PmhqQuickLoginResult? =
        payload?.let { pmhqJson.decodeFromJsonElement<PmhqQuickLoginResult>(it) }
}
