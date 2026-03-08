package org.ntqqrev.acidify.internal.pmhq.system

import kotlinx.serialization.json.JsonElement
import org.ntqqrev.acidify.internal.AbstractClient
import org.ntqqrev.acidify.internal.pmhq.NoInputPmhqService

internal object GetQRCodePicture : NoInputPmhqService<Unit>("loginService.getQRCodePicture") {
    override fun parse(client: AbstractClient, payload: JsonElement?) = Unit
}
