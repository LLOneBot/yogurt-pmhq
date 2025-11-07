package org.ntqqrev.acidify.internal.util

internal val builtinHttpEntities = mapOf(
    "nbsp" to "\u00A0",
    "lt" to "<",
    "gt" to ">",
    "amp" to "&",
    "quot" to "\"",
    "apos" to "'",
)

internal fun String.escapeHttp(entities: Map<String, String> = builtinHttpEntities): String {
    val sb = StringBuilder()
    for (c in this) {
        val entity = entities.entries.find { it.value == c.toString() }?.key
        if (entity != null) {
            sb.append("&").append(entity).append(";")
        } else {
            sb.append(c)
        }
    }
    return sb.toString()
}

internal fun String.unescapeHttp(entities: Map<String, String> = builtinHttpEntities): String {
    val sb = StringBuilder()
    var i = 0
    while (i < length) {
        val c = this[i]
        if (c == '&') {
            val semicolon = indexOf(';', i + 1)
            if (semicolon > 0) {
                val entity = substring(i + 1, semicolon)
                val decoded = when {
                    entity.startsWith("#x") || entity.startsWith("#X") -> {
                        // 处理十六进制 &#xA0;
                        entity.drop(2).toIntOrNull(16)?.toChar()?.toString()
                    }
                    entity.startsWith("#") -> {
                        // 处理十进制 &#160;
                        entity.drop(1).toIntOrNull()?.toChar()?.toString()
                    }
                    else -> entities[entity]
                }
                if (decoded != null) {
                    sb.append(decoded)
                    i = semicolon + 1
                    continue
                }
            }
        }
        sb.append(c)
        i++
    }
    return sb.toString()
}