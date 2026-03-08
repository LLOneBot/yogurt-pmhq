package org.ntqqrev.acidify.internal.pmhq

import kotlinx.serialization.json.Json

internal val pmhqJson = Json {
    encodeDefaults = true
    ignoreUnknownKeys = true
    explicitNulls = true
}