package org.ntqqrev.acidify.internal.pmhq.system

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.ntqqrev.acidify.internal.AbstractClient
import org.ntqqrev.acidify.internal.json.pmhq.PmhqQuickLoginListPayload
import org.ntqqrev.acidify.internal.pmhq.NoInputPmhqService
import org.ntqqrev.acidify.internal.pmhq.pmhqJson

internal object GetQuickLoginList : NoInputPmhqService<PmhqQuickLoginListPayload?>("loginService.getLoginList") {
    override fun parse(client: AbstractClient, payload: JsonElement?): PmhqQuickLoginListPayload? =
        payload?.let { pmhqJson.decodeFromJsonElement<PmhqQuickLoginListPayload>(it) }
}
