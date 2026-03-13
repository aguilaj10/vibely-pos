plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    application
}

group = "com.vibely.pos"
version = "1.0.0"

application {
    mainClass.set("com.vibely.pos.backend.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)

    // Ktor Server
    implementation(libs.bundles.ktor.server)

    // Database
    implementation(libs.postgres.driver)
    implementation(libs.hikari)

    // DI
    implementation(libs.koin.core)

    // Testing
    testImplementation(libs.kotlin.testJunit)
}
