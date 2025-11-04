package org.ntqqrev.acidify.internal.protobuf.util

internal typealias MultiMap<K, V> = MutableMap<K, MutableList<V>>

internal fun <K, V> multiMapOf(): MultiMap<K, V> = mutableMapOf<K, MutableList<V>>()

internal fun <K, V> MultiMap<K, V>.put(key: K, value: V) {
    this.getOrPut(key) { mutableListOf() }.add(value)
}

internal fun <K, V> MultiMap<K, V>.putAll(key: K, values: Collection<V>) {
    if (values.isEmpty()) return
    this.getOrPut(key) { mutableListOf() }.addAll(values)
}

internal fun <K, V> MultiMap<K, V>.remove(key: K): MutableList<V>? {
    return this.remove(key)
}