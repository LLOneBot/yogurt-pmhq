package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable

@Serializable
internal class PmhqLoginQrCodePayload(
    val pngBase64QrcodeData: String = "",
    val qrcodeUrl: String = "",
    val expireTime: Int = 0,
    val pollTimeInterval: Int = 0,
)
