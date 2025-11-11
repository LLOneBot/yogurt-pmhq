package org.ntqqrev.acidify.internal

import co.touchlab.stately.concurrency.AtomicBoolean
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.ntqqrev.acidify.Bot
import org.ntqqrev.acidify.entity.BotEntity

internal class CacheUtility<K, V : BotEntity<D>, D>(
    val bot: Bot,
    private val updateCache: suspend (bot: Bot) -> Map<K, D>,
    private val entityFactory: (bot: Bot, data: D) -> V,
) {
    private val mutex = Mutex()
    private var map = mutableMapOf<K, V>()
    private val updating = AtomicBoolean(false)
    private val logger = bot.createLogger(this)

    suspend fun get(key: K, forceUpdate: Boolean = false): V? {
        if (!map.containsKey(key) || forceUpdate) {
            logger.v { "缓存中 $key 不存在，请求刷新" }
            update()
        }
        return map[key]
    }

    suspend fun getAll(forceUpdate: Boolean = false): List<V> {
        if (forceUpdate || map.isEmpty()) {
            update()
        }
        return map.values.toList()
    }

    suspend fun update() {
        if (updating.value) {
            logger.v { "重复的刷新请求，已忽略" }
            mutex.withLock { } // 等待正在进行的更新完成
        } else {
            updating.value = true
            mutex.withLock {
                try {
                    val data = updateCache(bot)
                    acceptData(data)
                } catch (e: Exception) {
                    logger.w(e) { "缓存刷新失败" }
                } finally {
                    updating.value = false
                }
            }
        }
    }

    fun acceptData(data: Map<K, D>) {
        val newMap = mutableMapOf<K, V>()
        for ((key, value) in data.entries) {
            val entity = map[key]
            if (entity != null) {
                entity.updateBinding(value)
                newMap[key] = entity
            } else {
                newMap[key] = entityFactory(bot, value)
            }
        }
        map = newMap
    }
}