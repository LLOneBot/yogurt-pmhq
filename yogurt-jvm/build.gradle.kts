plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.ktor)
}

application {
    mainClass = "JvmMain"
}

dependencies {
    implementation(project(":yogurt"))
}