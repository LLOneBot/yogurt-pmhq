import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.io.File
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("buildsrc.convention.kotlin-multiplatform")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.build.konfig)
    alias(libs.plugins.kotlinx.atomicfu)
}

version = "0.1.0"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":acidify-core"))
            implementation(libs.kotlinx.datetime)
            implementation(libs.bundles.ktor.client)
            implementation(libs.bundles.ktor.server)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.milky.types)
            implementation(libs.acidify.codec)
            implementation(libs.qr.matrix)
            implementation(libs.quickjs.kt)
            implementation(libs.mordant)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(libs.logback.classic)
        }
        mingwMain.dependencies {
            implementation(libs.ktor.client.winhttp)
        }
        appleMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        linuxMain.dependencies {
            implementation(libs.ktor.client.curl)
        }
    }

    targets.withType<KotlinNativeTarget> {
        binaries {
            executable {
                entryPoint = "org.ntqqrev.yogurt.main"
            }
        }
    }

    mingwX64 {
        binaries.all {
            linkerOpts(
                "-Wl,-Bstatic",
                "-lstdc++",
                "-lgcc",
                "-Wl,-Bdynamic",
            )
        }
    }
}

fun resolveGitDir(rootDir: File): File? {
    val dotGit = rootDir.resolve(".git")
    if (dotGit.isDirectory) {
        return dotGit
    }
    if (!dotGit.isFile) {
        return null
    }
    val pointer = dotGit.readText().trim()
    val prefix = "gitdir:"
    if (!pointer.startsWith(prefix)) {
        return null
    }
    return rootDir.resolve(pointer.removePrefix(prefix).trim()).normalize().takeIf { it.exists() }
}

fun resolveGitHash(rootDir: File): String? {
    val gitDir = resolveGitDir(rootDir) ?: return null
    val headFile = gitDir.resolve("HEAD")
    if (!headFile.isFile) {
        return null
    }
    val head = headFile.readText().trim()
    if (!head.startsWith("ref: ")) {
        return head.takeIf { it.matches(Regex("^[0-9a-fA-F]{40}$")) }
    }

    val ref = head.removePrefix("ref: ").trim()
    val refFile = gitDir.resolve(ref)
    if (refFile.isFile) {
        return refFile.readText().trim()
    }

    val packedRefsFile = gitDir.resolve("packed-refs")
    if (!packedRefsFile.isFile) {
        return null
    }
    return packedRefsFile.useLines { lines ->
        lines
            .filterNot { it.startsWith("#") || it.startsWith("^") }
            .firstOrNull { it.endsWith(" $ref") }
            ?.substringBefore(' ')
            ?.trim()
    }
}

val gitHashProvider: Provider<String> = providers.gradleProperty("yogurtGitHash")
    .orElse(providers.environmentVariable("YOGURT_GIT_HASH"))
    .orElse(providers.environmentVariable("GITHUB_SHA"))
    .orElse(providers.provider { resolveGitHash(rootProject.rootDir) ?: "unknown" })

val gitShortHashProvider: Provider<String> = gitHashProvider.map { hash ->
    if (hash.length <= 7) hash else hash.substring(0, 7)
}

val buildTimeProvider: Provider<String> = providers.provider {
    ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
}

buildkonfig {
    packageName = "org.ntqqrev.yogurt"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "name", "Yogurt")
        buildConfigField(FieldSpec.Type.STRING, "version", "${project.version}+${gitShortHashProvider.get()}")
        buildConfigField(
            FieldSpec.Type.STRING,
            "coreVersion",
            project(":acidify-core").let {
                "${it.name} ${it.version}+${gitShortHashProvider.get()}"
            }
        )
        buildConfigField(
            FieldSpec.Type.STRING,
            "milkyVersion",
            libs.milky.types.get().let {
                "${it.module.name} ${it.version}"
            }
        )
        buildConfigField(FieldSpec.Type.STRING, "commitHash", gitHashProvider.get())
        buildConfigField(FieldSpec.Type.STRING, "buildTime", buildTimeProvider.get())
    }
}
