package buildsrc.convention

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    // val isBuildingCoreLibrary = System.getenv("GITHUB_ACTIONS_BUILD_CORE_LIBRARY") == "true"
    val actionsTarget = System.getenv("GITHUB_ACTIONS_BUILD_TARGET")
    // val hostOs = System.getProperty("os.name")
    // val arch = System.getProperty("os.arch")
    when {
        // is run from GitHub Actions - build Yogurt, improving dependency pulling time
        actionsTarget == "mingwX64" -> mingwX64()
        actionsTarget == "macosX64" -> macosX64()
        actionsTarget == "macosArm64" -> macosArm64()
        actionsTarget == "linuxX64" -> linuxX64()
        actionsTarget == "linuxArm64" -> linuxArm64()

        // is run locally
        else -> {
            mingwX64()
            macosX64()
            macosArm64()
            linuxX64()
            linuxArm64()
        }
    }

    jvmToolchain(21)
}
