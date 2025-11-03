plugins {
    id("buildsrc.convention.kotlin-multiplatform")
    alias(libs.plugins.kotlin.serialization)
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "org.ntqqrev"
version = "0.1.1"

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.io)
            implementation(libs.kotlinx.datetime)
            implementation(libs.bundles.ktor.client)
            implementation(libs.ktor.network)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.korlibs.compression)
            implementation(libs.kotlincrypto.hash.sha1)
            implementation(libs.bundles.xmlutil)
            implementation(libs.stately.concurrent.collections)
            implementation(libs.mordant)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
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