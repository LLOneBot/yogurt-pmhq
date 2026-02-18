package org.ntqqrev.acidify.logging

import org.ntqqrev.acidify.AbstractBot
import kotlin.js.JsName

typealias MessageSupplier = () -> String

/**
 * 日志记录器，将日志消息发送到 Bot 的共享日志流
 * @property bot 关联的 Bot 实例
 * @property tag 日志标签，通常为完整类名
 */
class Logger internal constructor(val bot: AbstractBot, val tag: String) {
    inline fun v(messageSupplier: MessageSupplier) {
        if (bot.minLogLevel <= LogLevel.VERBOSE) {
            bot.logHandler.handleLog(
                level = LogLevel.VERBOSE,
                tag = tag,
                message = messageSupplier.invoke(),
                throwable = null
            )
        }
    }

    inline fun d(messageSupplier: MessageSupplier) {
        if (bot.minLogLevel <= LogLevel.DEBUG) {
            bot.logHandler.handleLog(
                level = LogLevel.DEBUG,
                tag = tag,
                message = messageSupplier.invoke(),
                throwable = null
            )
        }
    }

    inline fun i(messageSupplier: MessageSupplier) {
        if (bot.minLogLevel <= LogLevel.INFO) {
            bot.logHandler.handleLog(
                level = LogLevel.INFO,
                tag = tag,
                message = messageSupplier.invoke(),
                throwable = null
            )
        }
    }

    @JsName("wNoThrowable")
    inline fun w(messageSupplier: MessageSupplier) {
        if (bot.minLogLevel <= LogLevel.WARN) {
            bot.logHandler.handleLog(
                level = LogLevel.WARN,
                tag = tag,
                message = messageSupplier.invoke(),
                throwable = null
            )
        }
    }

    inline fun w(t: Throwable, messageSupplier: MessageSupplier) {
        if (bot.minLogLevel <= LogLevel.WARN) {
            bot.logHandler.handleLog(
                level = LogLevel.WARN,
                tag = tag,
                message = messageSupplier.invoke(),
                throwable = t
            )
        }
    }

    @JsName("eNoThrowable")
    inline fun e(messageSupplier: MessageSupplier) {
        if (bot.minLogLevel <= LogLevel.ERROR) {
            bot.logHandler.handleLog(
                level = LogLevel.ERROR,
                tag = tag,
                message = messageSupplier.invoke(),
                throwable = null
            )
        }
    }

    inline fun e(t: Throwable, messageSupplier: MessageSupplier) {
        if (bot.minLogLevel <= LogLevel.ERROR) {
            bot.logHandler.handleLog(
                level = LogLevel.ERROR,
                tag = tag,
                message = messageSupplier.invoke(),
                throwable = t
            )
        }
    }
}