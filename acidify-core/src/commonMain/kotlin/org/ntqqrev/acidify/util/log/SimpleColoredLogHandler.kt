package org.ntqqrev.acidify.util.log

import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles.bold
import com.github.ajalt.mordant.terminal.Terminal
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class SimpleColoredLogHandler(val t: Terminal) : LogHandler {
    override fun handleLog(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        t.println(formatColoredLog(level, tag, message, throwable?.stackTraceToString()))
    }

    companion object {
        val timeFormat = LocalDateTime.Format {
            hour()
            char(':')
            minute()
            char(':')
            second()
        }

        @OptIn(ExperimentalTime::class)
        fun formatColoredLog(
            level: LogLevel,
            tag: String,
            message: String,
            stackTrace: String?
        ): String {
            val b = StringBuilder()
            val now: Instant = Clock.System.now()
            val localNow: LocalDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
            b.append(bold(green(timeFormat.format(localNow))))
            b.append(" ")
            b.append(
                when (level) {
                    LogLevel.VERBOSE -> gray("TRACE")
                    LogLevel.DEBUG -> brightBlue("DEBUG")
                    LogLevel.INFO -> brightGreen(" INFO")
                    LogLevel.WARN -> brightYellow(" WARN")
                    LogLevel.ERROR -> brightRed("ERROR")
                }
            )
            b.append(" ")
            b.append(
                when (level) {
                    LogLevel.VERBOSE -> gray
                    LogLevel.ERROR -> brightRed
                    else -> cyan
                }(shortenPackageName(tag))
            )
            b.append(" ")
            b.append(
                when (level) {
                    LogLevel.VERBOSE -> gray(message)
                    LogLevel.ERROR -> brightRed(message)
                    else -> message
                }
            )
            if (stackTrace != null) {
                b.append("\n")
                b.append(
                    when (level) {
                        LogLevel.ERROR -> brightRed
                        LogLevel.WARN -> brightYellow
                        else -> gray
                    }(stackTrace)
                )
            }
            return b.toString()
        }
    }
}