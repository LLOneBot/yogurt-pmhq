plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
    alias(libs.plugins.shadow)
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