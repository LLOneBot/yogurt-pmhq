@file:JvmName("Main")

package org.ntqqrev.yogurt

import io.ktor.server.engine.addShutdownHook
import kotlin.jvm.JvmName

fun main() {
    val server = YogurtApp.createServer()
    server.addShutdownHook {
        server.stop(gracePeriodMillis = 2000L, timeoutMillis = 5000L)
    }
    server.start(wait = true)
}