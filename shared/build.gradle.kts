
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.buildkonfig)
    alias(libs.plugins.detekt)
    id("spotless-conventions")
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "com.vibely.pos.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()

    jvm()

    js {
        browser()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            // DI - Koin
            api(libs.koin.core)

            // Ktor Client - Core
            implementation(libs.ktor.clientCore)
            implementation(libs.ktor.clientContentNegotiation)
            implementation(libs.ktor.clientSerialization)
            implementation(libs.ktor.clientLogging)

            // Ktor Client - Auth (para manejo de tokens)
            implementation(libs.ktor.clientAuth)

            // Supabase
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.storage)

            // Cryptography KMP - for encryption across platforms
            implementation(libs.cryptography.core)
            implementation(libs.cryptography.provider.optimal)

            // Network Connectivity Monitoring
            implementation(libs.connectivity.core)
            implementation(libs.connectivity.http)
        }

        androidMain.dependencies {
            implementation(libs.ktor.clientAndroid)

            // DataStore for encrypted storage
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.core)

            // Navigation 3 - UI y ViewModel (solo Android)
            implementation(libs.androidx.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodel.navigation3)
        }

        iosMain.dependencies {
            implementation(libs.ktor.clientDarwin)

            // DataStore for encrypted storage
            implementation(libs.androidx.datastore.core)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.clientCio)

            // DataStore for encrypted storage
            implementation(libs.androidx.datastore.core)
        }

        jsMain.dependencies {
            implementation(libs.ktor.clientJs)
        }

        wasmJsMain.dependencies {
            implementation(libs.ktor.clientJs)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.ktor.clientMock)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.test)
        }
    }
}

// BuildKonfig configuration
buildkonfig {
    packageName = "com.vibely.pos.shared"

    defaultConfigs {
        buildConfigField(
            com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING,
            "API_BASE_URL",
            System.getenv("API_BASE_URL") ?: "http://localhost:8080",
        )
    }
}

// Detekt configuration
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/detekt.yml"))
    baseline = file("$rootDir/detekt-baseline.xml")
}

dependencies {
    detektPlugins(libs.detekt.koin.rules)
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
