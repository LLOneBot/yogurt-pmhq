package org.ntqqrev.acidify.internal.pmhq.system

import kotlinx.io.IOException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import org.ntqqrev.acidify.internal.AbstractClient
import org.ntqqrev.acidify.common.PmhqSelfInfo
import org.ntqqrev.acidify.internal.json.pmhq.PmhqSelfInfoPayload
import org.ntqqrev.acidify.internal.pmhq.NoInputPmhqService
import org.ntqqrev.acidify.internal.pmhq.pmhqJson

internal object GetSelfInfo : NoInputPmhqService<PmhqSelfInfo>("getSelfInfo") {
    override fun parse(client: AbstractClient, payload: JsonElement?): PmhqSelfInfo {
        val selfInfo = pmhqJson.decodeFromJsonElement<PmhqSelfInfoPayload>(
            payload ?: throw IOException("PMHQ call getSelfInfo returned an empty result")
        )
        return PmhqSelfInfo(
            uin = selfInfo.uin.toLongOrNull() ?: 0L,
            uid = selfInfo.uid,
            online = selfInfo.online,
            nick = selfInfo.nick,
        )
    }
}
