package org.ntqqrev.yogurt.util

import io.ktor.utils.io.errors.PosixException

val addrInUsePosixCodes = setOf(
    48, // macOS - EADDRINUSE
    98, // Linux - EADDRINUSE
    10048, // Windows - WSAEADDRINUSE
)

actual tailrec fun Throwable.isCausedByAddrInUse(): Boolean {
    if (this is PosixException.PosixErrnoException) {
        if (errno in addrInUsePosixCodes) {
            return true
        }
    }
    return cause?.isCausedByAddrInUse() ?: false
}