package org.ntqqrev.yogurt.script

import com.dokar.quickjs.QuickJs
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import org.ntqqrev.acidify.Bot
import org.ntqqrev.milky.Event
import org.ntqqrev.milky.milkyJsonModule
import org.ntqqrev.yogurt.scriptsPath

suspend fun Application.loadScripts() {
    val bot = dependencies.resolve<Bot>()
    val qjs = dependencies.resolve<QuickJs>()
    val logger = bot.createLogger("ScriptLoader")
    val scripts = SystemFileSystem.list(scriptsPath)
        .filter { it.name.endsWith(".yogurtx.js") }
        .map {
            async {
                Script(
                    name = it.name,
                    content = SystemFileSystem.source(it).buffered().use { r ->
                        withContext(Dispatchers.IO) {
                            r.readString()
                        }
                    }
                )
            }
        }
        .awaitAll()
    if (scripts.isNotEmpty()) {
        scripts.forEach {
            qjs.evaluate<Any?>(
                code = it.content,
                filename = it.name,
                asModule = true,
            )
            logger.i { "脚本 ${it.name.removeSuffix(".yogurtx.js")} 加载完成" }
        }
        logger.i { "加载了 ${scripts.size} 个脚本" }
        val flow = dependencies.resolve<SharedFlow<Event>>()
        flow.collect {
            qjs.evaluate<Any?>("$internalEmitHandle(${milkyJsonModule.encodeToString(it)})")
        }
    }
}