package org.ntqqrev.acidify.internal.pmhq

import org.ntqqrev.acidify.internal.json.pmhq.PmhqEventData

internal abstract class PmhqListener(val type: String) {
    suspend fun handle(eventType: String, event: PmhqEventData) {
        if (eventType != type) return
        onEvent(event)
    }

    protected abstract suspend fun onEvent(event: PmhqEventData)
}
