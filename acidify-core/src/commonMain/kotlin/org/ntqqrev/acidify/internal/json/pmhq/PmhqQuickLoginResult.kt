package org.ntqqrev.acidify.internal.json.pmhq

import kotlinx.serialization.Serializable

@Serializable
internal class PmhqQuickLoginResult(
    val result: String = "",
    val loginErrorInfo: PmhqQuickLoginErrorInfo = PmhqQuickLoginErrorInfo(),
)
