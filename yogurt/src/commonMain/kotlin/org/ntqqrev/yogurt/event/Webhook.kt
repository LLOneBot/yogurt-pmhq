package org.ntqqrev.yogurt.event

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*
import kotlinx.coroutines.launch
import org.ntqqrev.acidify.Bot
import org.ntqqrev.milky.milkyJsonModule
import org.ntqqrev.yogurt.YogurtApp.config
import org.ntqqrev.yogurt.transform.transformAcidifyEvent

val webhookClient = HttpClient {
    install(ContentNegotiation) {
        json(milkyJsonModule)
    }
}

fun Application.configureMilkyEventWebhook() = launch {
    val bot = dependencies.resolve<Bot>()
    val logger = bot.createLogger("WebhookModule")
    bot.eventFlow.collect { event ->
        transformAcidifyEvent(event)?.let {
            config.webhookConfig.url.forEach { webhookUrl ->
                launch {
                    try {
                        webhookClient.post(webhookUrl) {
                            if (config.webhookConfig.accessToken.isNotEmpty()) {
                                bearerAuth(config.webhookConfig.accessToken)
                            }
                            contentType(ContentType.Application.Json)
                            setBody(it)
                        }
                    } catch (e: Exception) {
                        logger.w(e) { "发送事件到 Webhook URL $webhookUrl 失败" }
                    }
                }
            }
        }
    }
}