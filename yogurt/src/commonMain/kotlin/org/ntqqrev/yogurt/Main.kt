@file:JvmName("Main")

package org.ntqqrev.yogurt

import org.ntqqrev.yogurt.util.addSigIntHandler
import kotlin.jvm.JvmName

fun main() {
    YogurtApp.createServer()
        .addSigIntHandler()
        .start(wait = true)
}