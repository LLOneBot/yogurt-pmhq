package org.ntqqrev.acidify.internal.pmhq.listener

import kotlinx.serialization.json.decodeFromJsonElement
import org.ntqqrev.acidify.internal.json.pmhq.PmhqEventData
import org.ntqqrev.acidify.internal.json.pmhq.PmhqLoginQrCodePayload
import org.ntqqrev.acidify.internal.pmhq.PmhqListener
import org.ntqqrev.acidify.internal.pmhq.pmhqJson

internal abstract class NodeIKernelLoginListener : PmhqListener("nodeIKernelLoginListener") {
    final override suspend fun onEvent(event: PmhqEventData) {
        when (event.subType) {
            "onQRCodeGetPicture" -> {
                val payload = event.data?.let { pmhqJson.decodeFromJsonElement<PmhqLoginQrCodePayload>(it) } ?: return
                onQRCodeGetPicture(payload)
            }

            "onQRCodeLoginPollingStarted" -> onQRCodeLoginPollingStarted()
            "onQRCodeSessionUserScaned" -> onQRCodeSessionUserScaned()
            "onQRCodeLoginSucceed" -> onQRCodeLoginSucceed()
            "onUserLoggedIn" -> onUserLoggedIn()
            "onQRCodeSessionFailed" -> onQRCodeSessionFailed()
            "onLoginFailed" -> onLoginFailed()
            "onQRCodeSessionQuickLoginFailed" -> onQRCodeSessionQuickLoginFailed()
        }
    }

    open suspend fun onQRCodeGetPicture(payload: PmhqLoginQrCodePayload) {}

    open suspend fun onQRCodeLoginPollingStarted() {}

    open suspend fun onQRCodeSessionUserScaned() {}

    open suspend fun onQRCodeLoginSucceed() {}

    open suspend fun onUserLoggedIn() {}

    open suspend fun onQRCodeSessionFailed() {}

    open suspend fun onLoginFailed() {}

    open suspend fun onQRCodeSessionQuickLoginFailed() {}
}