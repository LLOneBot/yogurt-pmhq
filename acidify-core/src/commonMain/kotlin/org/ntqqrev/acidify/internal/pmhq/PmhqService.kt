package org.ntqqrev.acidify.internal.pmhq

import kotlinx.serialization.json.JsonElement
import org.ntqqrev.acidify.internal.AbstractClient

internal abstract class PmhqService<T, R>(val func: String) {
    open fun build(client: AbstractClient, payload: T): List<JsonElement> = emptyList()

    abstract fun parse(client: AbstractClient, payload: JsonElement?): R
}

internal abstract class NoInputPmhqService<R>(func: String) : PmhqService<Unit, R>(func)

internal abstract class NoOutputPmhqService<T>(func: String) : PmhqService<T, Unit>(func) {
    override fun parse(client: AbstractClient, payload: JsonElement?) = Unit
}
