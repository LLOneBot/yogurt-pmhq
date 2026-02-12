package org.ntqqrev.yogurt.util

import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.writeString
import org.ntqqrev.acidify.AbstractBot
import org.ntqqrev.acidify.event.AndroidSessionStoreUpdatedEvent
import org.ntqqrev.acidify.event.SessionStoreUpdatedEvent
import org.ntqqrev.yogurt.androidSessionStorePath
import org.ntqqrev.yogurt.sessionStorePath

fun Application.configureSessionStoreAutoSave() = launch {
    val bot = dependencies.resolve<AbstractBot>()
    val logger = bot.createLogger("SessionStoreUtils")
    bot.eventFlow.filterIsInstance<SessionStoreUpdatedEvent>().collect {
        logger.i { "SessionStore 已更新，正在保存至文件..." }
        SystemFileSystem.sink(sessionStorePath).buffered().use { source ->
            source.writeString(it.sessionStore.toJson())
        }
    }
    bot.eventFlow.filterIsInstance<AndroidSessionStoreUpdatedEvent>().collect {
        logger.i { "Android SessionStore 已更新，正在保存至文件..." }
        SystemFileSystem.sink(androidSessionStorePath).buffered().use { source ->
            source.writeString(it.sessionStore.toJson())
        }
    }
}