package org.ntqqrev.acidify.exception

import kotlin.js.JsExport

/**
 * URL 签名 API 调用异常
 * @property code HTTP 状态码，或签名服务返回的错误码
 */
@JsExport
class UrlSignException(
    msg: String,
    val code: Int,
) : Exception("$msg ($code)")