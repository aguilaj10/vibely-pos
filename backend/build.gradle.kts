plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
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

    // Supabase
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.clientCio)
    implementation(libs.ktor.clientContentNegotiation)
    implementation(libs.ktor.clientLogging)

    // Database
    implementation(libs.postgres.driver)
    implementation(libs.hikari)

    // Authentication
    implementation(libs.jwt)
    implementation(libs.bcrypt)

    // DI
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    // Testing
    testImplementation(libs.kotlin.testJunit)
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.mockk)
    testImplementation(libs.koin.test)

    // Detekt Plugins
    detektPlugins(libs.detekt.koin.rules)
}

// Detekt configuration
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/detekt.yml"))
    baseline = file("$rootDir/detekt-baseline.xml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
    }
}
