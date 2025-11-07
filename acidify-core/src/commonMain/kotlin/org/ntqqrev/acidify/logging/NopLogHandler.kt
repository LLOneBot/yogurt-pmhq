package org.ntqqrev.acidify.logging

/**
 * 不执行任何操作的日志处理器
 */
object NopLogHandler : LogHandler {
    override fun handleLog(
        level: LogLevel,
        tag: String,
        message: String,
        throwable: Throwable?
    ) {
        // do nothing
    }
}