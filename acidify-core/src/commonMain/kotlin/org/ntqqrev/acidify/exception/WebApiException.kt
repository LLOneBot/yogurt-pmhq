package org.ntqqrev.acidify.exception

import io.ktor.http.HttpStatusCode

/**
 * Web API 调用异常
 * @property statusCode HTTP 状态码
 */
class WebApiException(msg: String, val statusCode: HttpStatusCode) : Exception("$msg ($statusCode)")