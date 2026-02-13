@file:OptIn(ExperimentalSerializationApi::class)

package org.ntqqrev.yogurt

import com.github.ajalt.mordant.rendering.AnsiLevel
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink
import org.ntqqrev.acidify.logging.LogLevel

@Serializable
class YogurtConfig(
    val signApiUrl: String = "",
    val protocol: Protocol = Protocol(),
    val androidCredentials: AndroidCredentials = AndroidCredentials(),
    val androidUseLegacySign: Boolean = false,
    val reportSelfMessage: Boolean = true,
    val preloadContacts: Boolean = false,
    val transformIncomingMFaceToImage: Boolean = false,
    val httpConfig: MilkyHttpConfig = MilkyHttpConfig(),
    val webhookConfig: MilkyWebhookConfig = MilkyWebhookConfig(),
    val logging: LoggingConfig = LoggingConfig(),
    val skipSecurityCheck: Boolean = false,
) {
    @Serializable
    class Protocol(
        val os: String = "Linux",
        val version: String = "fetched",
    )

    @Serializable
    class AndroidCredentials(
        val uin: Long = 0L,
        val password: String = "",
    )

    @Serializable
    class LoggingConfig(
        val ansiLevel: AnsiLevel = AnsiLevel.ANSI256,
        val coreLogLevel: LogLevel = LogLevel.DEBUG,
    )

    @Serializable
    class MilkyHttpConfig(
        val host: String = "127.0.0.1",
        val port: Int = 3000,
        val accessToken: String = "",
        val corsOrigins: List<String> = listOf(),
    )

    @Serializable
    class MilkyWebhookConfig(
        val url: List<String> = emptyList(),
        val accessToken: String = "",
    )

    companion object {
        val jsonModule = Json {
            prettyPrint = true
            encodeDefaults = true
            allowComments = true
            allowTrailingComma = true
            ignoreUnknownKeys = true
        }

        fun loadFromFile(): YogurtConfig {
            if (!SystemFileSystem.exists(configPath)) {
                val defaultConfig = YogurtConfig()
                SystemFileSystem.sink(configPath).buffered().use {
                    jsonModule.encodeToSink(defaultConfig, it)
                }
                println("配置文件已生成于 ${SystemFileSystem.resolve(configPath)}")
                println("请根据需要进行修改，修改完成后按 Enter 键继续...")
                readln()
            }
            return SystemFileSystem.source(configPath).buffered().use {
                jsonModule.decodeFromSource<YogurtConfig>(it)
            }.also {
                SystemFileSystem.sink(configPath).buffered().use { sink ->
                    jsonModule.encodeToSink(it, sink)
                    // rewrite it to format, and add new fields if any
                }
            }
        }
    }
}