package org.ntqqrev.yogurt.util

import org.ntqqrev.acidify.util.log.LogHandler
import org.ntqqrev.acidify.util.log.SimpleColoredLogHandler
import org.ntqqrev.yogurt.YogurtApp
import org.ntqqrev.yogurt.YogurtApp.t

actual val YogurtApp.logHandler: LogHandler by lazy {
    SimpleColoredLogHandler(t)
}