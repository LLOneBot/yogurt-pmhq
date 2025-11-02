package org.ntqqrev.yogurt.util

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

val isDockerEnv: Boolean by lazy {
    SystemFileSystem.exists(Path("/.dockerenv"))
}