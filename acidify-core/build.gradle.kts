plugins {
    id("buildsrc.convention.kotlin-multiplatform")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlinx.atomicfu)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.dokka)
}

group = "org.ntqqrev"
version = "0.2.1"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.io)
            implementation(libs.bundles.ktor.client)
            implementation(libs.ktor.network)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.korlibs.compression)
            implementation(libs.bundles.xmlutil)
            implementation(libs.stately.concurrent.collections)
        }
        commonTest.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(kotlin("test"))
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
    coordinates(
        groupId = project.group.toString(),
        artifactId = project.name,
        version = project.version.toString()
    )

    pom {
        name = project.name
        description = "Kotlin NTQQ protocol implementation"
        url = "https://github.com/LagrangeDev/acidify"
        inceptionYear = "2025"
        licenses {
            license {
                name = "GNU General Public License v3.0"
                url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
            }
        }
        developers {
            developer {
                id = "Wesley-Young"
                name = "Wesley F. Young"
                email = "wesley.f.young@outlook.com"
            }
        }
        scm {
            connection = "scm:git:git://github.com/LagrangeDev/acidify.git"
            developerConnection = "scm:git:ssh://github.com/LagrangeDev/acidify.git"
            url = "https://github.com/LagrangeDev/acidify"
        }
    }
}

dokka {
    dokkaSourceSets.commonMain {
        sourceLink {
            localDirectory.set(projectDir.resolve("src"))
            remoteUrl("https://github.com/LagrangeDev/acidify/tree/main/acidify-core/src")
            remoteLineSuffix.set("#L")
        }
    }
    pluginsConfiguration.html {
        footerMessage = "(c) LagrangeDev"
    }
}