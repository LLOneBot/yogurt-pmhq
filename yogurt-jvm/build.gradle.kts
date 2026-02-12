plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.ktor)
}

application {
    mainClass = "JvmMain"
}

tasks.withType<JavaExec> {
    standardInput = System.`in`
}

dependencies {
    implementation(project(":yogurt"))
}