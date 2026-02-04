package org.ntqqrev.yogurt.api

import io.ktor.server.application.*
import org.ntqqrev.acidify.Bot

class MilkyApiContext(
    val bot: Bot,
    val application: Application,
)