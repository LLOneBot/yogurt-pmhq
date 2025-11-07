package org.ntqqrev.acidify.exception

import io.ktor.http.HttpStatusCode

class WebApiException(msg: String, val statusCode: HttpStatusCode) : Exception("$msg ($statusCode)")