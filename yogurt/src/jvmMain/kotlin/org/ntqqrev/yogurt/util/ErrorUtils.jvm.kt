package org.ntqqrev.yogurt.util

actual tailrec fun Throwable.isCausedByAddrInUse(): Boolean {
    if (this is java.net.BindException) {
        return true
    }
    return cause?.isCausedByAddrInUse() ?: false
}