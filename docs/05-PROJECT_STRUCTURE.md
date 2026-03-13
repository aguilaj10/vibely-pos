# Project Structure - Modern KMP Setup

## Overview

This document defines the modern Kotlin Multiplatform (KMP) project structure for Vibely POS, including build configurations, code quality tools, and best practices.

## Module Structure

```
vibely-pos/
├── gradle/
│   └── libs.versions.toml          # Version catalog
├── shared/                          # Shared KMP code
│   ├── src/
│   │   ├── commonMain/
│   │   ├── commonTest/
│   │   ├── androidMain/
│   │   ├── iosMain/
│   │   ├── desktopMain/
│   │   └── wasmJsMain/
│   └── build.gradle.kts
├── composeApp/                      # Compose Multiplatform UI
│   ├── src/
│   │   ├── commonMain/
│   │   ├── androidMain/
│   │   ├── iosMain/
│   │   ├── desktopMain/
│   │   └── wasmJsMain/
│   └── build.gradle.kts
├── backend/                         # Optional: Ktor backend
│   ├── src/main/kotlin/
│   └── build.gradle.kts
├── build.gradle.kts                 # Root build script
├── settings.gradle.kts              # Project settings
├── gradle.properties                # Gradle properties
├── detekt.yml                       # Detekt configuration
└── .editorconfig                    # Code style configuration
```

## Version Catalog (libs.versions.toml)

```toml
[versions]
# Kotlin
kotlin = "2.1.0"
compose = "1.7.1"
compose-plugin = "1.7.1"

# KMP
kotlinx-coroutines = "1.9.0"
kotlinx-serialization = "1.7.3"
kotlinx-datetime = "0.6.1"
kotlinx-io = "0.6.0"
ktor = "3.0.1"

# Compose & UI
androidx-lifecycle = "2.8.7"
androidx-navigation = "2.8.0-alpha10"
coil = "3.0.0"
material3-window-size = "0.5.0"

# Database
sqldelight = "2.0.2"

# DI
koin = "4.0.0"
koin-compose = "4.0.0"

# Backend (Ktor Server)
logback = "1.5.11"
postgres = "42.7.4"
hikari = "6.1.0"

# Testing
junit = "4.13.2"
kotlin-test = "2.1.0"
turbine = "1.1.0"
kotest = "5.9.1"

# Code Quality
detekt = "1.23.7"
spotless = "6.25.0"

# Android
android-gradle-plugin = "8.7.2"
androidx-core = "1.15.0"
androidx-appcompat = "1.7.0"
androidx-activity-compose = "1.9.3"

# Build
gradle = "8.11"

[libraries]
# Kotlin
kotlin-test = { module = "org.jetbrains.kotlin:kotlin-test", version.ref = "kotlin" }
kotlin-test-junit = { module = "org.jetbrains.kotlin:kotlin-test-junit", version.ref = "kotlin" }

# Coroutines
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "kotlinx-coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines" }
kotlinx-coroutines-swing = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-swing", version.ref = "kotlinx-coroutines" }

# Serialization
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
kotlinx-serialization-core = { module = "org.jetbrains.kotlinx:kotlinx-serialization-core", version.ref = "kotlinx-serialization" }

# DateTime
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }

# IO
kotlinx-io = { module = "org.jetbrains.kotlinx:kotlinx-io-core", version.ref = "kotlinx-io" }

# Ktor Client
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-client-serialization = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging = { module = "io.ktor:ktor-client-logging", version.ref = "ktor" }
ktor-client-auth = { module = "io.ktor:ktor-client-auth", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-js = { module = "io.ktor:ktor-client-js", version.ref = "ktor" }
ktor-client-curl = { module = "io.ktor:ktor-client-curl", version.ref = "ktor" }

# Ktor Server
ktor-server-core = { module = "io.ktor:ktor-server-core-jvm", version.ref = "ktor" }
ktor-server-netty = { module = "io.ktor:ktor-server-netty-jvm", version.ref = "ktor" }
ktor-server-content-negotiation = { module = "io.ktor:ktor-server-content-negotiation-jvm", version.ref = "ktor" }
ktor-server-serialization = { module = "io.ktor:ktor-serialization-kotlinx-json-jvm", version.ref = "ktor" }
ktor-server-auth = { module = "io.ktor:ktor-server-auth-jvm", version.ref = "ktor" }
ktor-server-auth-jwt = { module = "io.ktor:ktor-server-auth-jwt-jvm", version.ref = "ktor" }
ktor-server-cors = { module = "io.ktor:ktor-server-cors-jvm", version.ref = "ktor" }
ktor-server-status-pages = { module = "io.ktor:ktor-server-status-pages-jvm", version.ref = "ktor" }
ktor-server-call-logging = { module = "io.ktor:ktor-server-call-logging-jvm", version.ref = "ktor" }
ktor-server-tests = { module = "io.ktor:ktor-server-tests-jvm", version.ref = "ktor" }
logback = { module = "ch.qos.logback:logback-classic", version.ref = "logback" }

# Database
sqldelight-driver-android = { module = "app.cash.sqldelight:android-driver", version.ref = "sqldelight" }
sqldelight-driver-native = { module = "app.cash.sqldelight:native-driver", version.ref = "sqldelight" }
sqldelight-driver-sqlite = { module = "app.cash.sqldelight:sqlite-driver", version.ref = "sqldelight" }
sqldelight-driver-web = { module = "app.cash.sqldelight:web-driver", version.ref = "sqldelight" }
sqldelight-coroutines = { module = "app.cash.sqldelight:coroutines-extensions", version.ref = "sqldelight" }
sqldelight-primitive-adapters = { module = "app.cash.sqldelight:primitive-adapters", version.ref = "sqldelight" }
postgres-driver = { module = "org.postgresql:postgresql", version.ref = "postgres" }
hikari = { module = "com.zaxxer:HikariCP", version.ref = "hikari" }

# DI - Koin
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin-compose" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin-compose" }
koin-android = { module = "io.insert-koin:koin-android", version.ref = "koin" }
koin-test = { module = "io.insert-koin:koin-test", version.ref = "koin" }

# Compose & UI
androidx-lifecycle-viewmodel = { module = "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel", version.ref = "androidx-lifecycle" }
androidx-lifecycle-runtime-compose = { module = "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose", version.ref = "androidx-lifecycle" }
androidx-navigation-compose = { module = "org.jetbrains.androidx.navigation:navigation-compose", version.ref = "androidx-navigation" }
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil" }
material3-window-size = { module = "dev.chrisbanes.material3:material3-window-size-class-multiplatform", version.ref = "material3-window-size" }

# Android
androidx-core = { module = "androidx.core:core-ktx", version.ref = "androidx-core" }
androidx-appcompat = { module = "androidx.appcompat:appcompat", version.ref = "androidx-appcompat" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "androidx-activity-compose" }

# Testing
junit = { module = "junit:junit", version.ref = "junit" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
kotest-assertions = { module = "io.kotest:kotest-assertions-core", version.ref = "kotest" }
kotest-property = { module = "io.kotest:kotest-property", version.ref = "kotest" }

[plugins]
# Kotlin
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }

# Compose
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-plugin" }

# Android
android-application = { id = "com.android.application", version.ref = "android-gradle-plugin" }
android-library = { id = "com.android.library", version.ref = "android-gradle-plugin" }

# Database
sqldelight = { id = "app.cash.sqldelight", version.ref = "sqldelight" }

# Code Quality
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }

# Build
ktor = { id = "io.ktor.plugin", version.ref = "ktor" }

[bundles]
ktor-client = [
    "ktor-client-core",
    "ktor-client-content-negotiation",
    "ktor-client-serialization",
    "ktor-client-logging",
    "ktor-client-auth"
]

ktor-server = [
    "ktor-server-core",
    "ktor-server-netty",
    "ktor-server-content-negotiation",
    "ktor-server-serialization",
    "ktor-server-auth",
    "ktor-server-auth-jwt",
    "ktor-server-cors",
    "ktor-server-status-pages",
    "ktor-server-call-logging",
    "logback"
]

koin = [
    "koin-core",
    "koin-compose",
    "koin-compose-viewmodel"
]

testing = [
    "kotlin-test",
    "kotlinx-coroutines-test",
    "turbine",
    "kotest-assertions",
    "koin-test"
]
```

## Root Build Script (build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

// Apply Detekt to all subprojects
allprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    detekt {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(files("$rootDir/detekt.yml"))
        baseline = file("$rootDir/detekt-baseline.xml")
    }

    dependencies {
        detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:${libs.versions.detekt.get()}")
    }
}

// Apply Spotless to all subprojects
subprojects {
    apply(plugin = "com.diffplug.spotless")

    spotless {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**/*.kt")
            ktlint("1.3.1")
                .editorConfigOverride(
                    mapOf(
                        "max_line_length" to "120",
                        "indent_size" to "4",
                        "ij_kotlin_allow_trailing_comma" to "true",
                        "ij_kotlin_allow_trailing_comma_on_call_site" to "true"
                    )
                )
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }

        kotlinGradle {
            target("*.gradle.kts")
            ktlint("1.3.1")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
```

## Settings (settings.gradle.kts)

```kotlin
rootProject.name = "vibely-pos"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

include(":shared")
include(":composeApp")
include(":backend")
```

## Gradle Properties (gradle.properties)

```properties
# Kotlin
kotlin.code.style=official
kotlin.mpp.stability.nowarn=true
kotlin.mpp.androidSourceSetLayoutVersion=2

# Android
android.useAndroidX=true
android.nonTransitiveRClass=true

# Gradle
org.gradle.jvmargs=-Xmx4g -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParallelGC
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.parallel=true

# Compose
compose.resources.always.generate.accessors=true

# Build
kotlin.daemon.jvmargs=-Xmx4g
```

## Shared Module (shared/build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // DateTime - Use kotlin.time.Clock
            implementation(libs.kotlinx.datetime)

            // Ktor Client
            implementation(libs.bundles.ktor.client)

            // SQLDelight
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.primitive.adapters)

            // DI
            implementation(libs.bundles.koin)
        }

        commonTest.dependencies {
            implementation(libs.bundles.testing)
        }

        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.sqldelight.driver.android)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.sqldelight.driver.native)
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.sqldelight.driver.sqlite)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.sqldelight.driver.web)
            }
        }
    }
}

android {
    namespace = "com.vibely.pos.shared"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

sqldelight {
    databases {
        create("VivelyPosDatabase") {
            packageName.set("com.vibely.pos.db")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/databases"))
            verifyMigrations.set(true)
        }
    }
}
```

## Compose App Module (composeApp/build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"
            }
        }
    }

    jvm("desktop")

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    wasmJs {
        moduleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(projects.shared)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)

            // Navigation
            implementation(libs.androidx.navigation.compose)

            // Image Loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Window Size
            implementation(libs.material3.window.size)

            // DI
            implementation(libs.bundles.koin)
        }

        androidMain.dependencies {
            implementation(libs.androidx.core)
            implementation(libs.androidx.appcompat)
            implementation(libs.androidx.activity.compose)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

android {
    namespace = "com.vibely.pos"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vibely.pos"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "com.vibely.pos.MainKt"

        nativeDistributions {
            targetFormats(
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi,
                org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb
            )
            packageName = "Vibely POS"
            packageVersion = "1.0.0"
        }
    }
}
```

## Backend Module (backend/build.gradle.kts)

```kotlin
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
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
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test.junit)
}
```

## Detekt Configuration (detekt.yml)

```yaml
build:
  maxIssues: 0
  excludeCorrectable: false
  weights:
    complexity: 2
    LongParameterList: 1
    style: 1
    comments: 1

config:
  validation: true
  warningsAsErrors: false
  checkExhaustiveness: false

processors:
  active: true
  exclude:
    - 'DetektProgressListener'

console-reports:
  active: true
  exclude:
    - 'ProjectStatisticsReport'
    - 'ComplexityReport'
    - 'NotificationReport'
    - 'FindingsReport'
    - 'FileBasedFindingsReport'

output-reports:
  active: true
  exclude:
    - 'TxtOutputReport'
    - 'XmlOutputReport'
    - 'HtmlOutputReport'
    - 'MdOutputReport'
    - 'SarifOutputReport'

comments:
  active: true
  AbsentOrWrongFileLicense:
    active: false
  CommentOverPrivateFunction:
    active: false
  CommentOverPrivateProperty:
    active: false
  DeprecatedBlockTag:
    active: false
  EndOfSentenceFormat:
    active: false
  OutdatedDocumentation:
    active: false
  UndocumentedPublicClass:
    active: false
  UndocumentedPublicFunction:
    active: false
  UndocumentedPublicProperty:
    active: false

complexity:
  active: true
  CognitiveComplexMethod:
    active: true
    threshold: 15
  ComplexCondition:
    active: true
    threshold: 4
  ComplexInterface:
    active: false
  CyclomaticComplexMethod:
    active: true
    threshold: 15
  LabeledExpression:
    active: false
  LargeClass:
    active: true
    threshold: 600
  LongMethod:
    active: true
    threshold: 60
  LongParameterList:
    active: true
    functionThreshold: 6
    constructorThreshold: 7
  MethodOverloading:
    active: false
  NamedArguments:
    active: false
  NestedBlockDepth:
    active: true
    threshold: 4
  NestedScopeFunctions:
    active: false
  ReplaceSafeCallChainWithRun:
    active: false
  StringLiteralDuplication:
    active: false
  TooManyFunctions:
    active: true
    thresholdInFiles: 11
    thresholdInClasses: 11
    thresholdInInterfaces: 11
    thresholdInObjects: 11
    thresholdInEnums: 11

coroutines:
  active: true
  GlobalCoroutineUsage:
    active: true
  InjectDispatcher:
    active: true
  RedundantSuspendModifier:
    active: true
  SleepInsteadOfDelay:
    active: true
  SuspendFunSwallowedCancellation:
    active: true
  SuspendFunWithCoroutineScopeReceiver:
    active: true
  SuspendFunWithFlowReturnType:
    active: true

empty-blocks:
  active: true
  EmptyCatchBlock:
    active: true
    allowedExceptionNameRegex: '_|(ignore|expected).*'
  EmptyClassBlock:
    active: true
  EmptyDefaultConstructor:
    active: true
  EmptyDoWhileBlock:
    active: true
  EmptyElseBlock:
    active: true
  EmptyFinallyBlock:
    active: true
  EmptyForBlock:
    active: true
  EmptyFunctionBlock:
    active: true
    ignoreOverridden: false
  EmptyIfBlock:
    active: true
  EmptyInitBlock:
    active: true
  EmptyKtFile:
    active: true
  EmptySecondaryConstructor:
    active: true
  EmptyTryBlock:
    active: true
  EmptyWhenBlock:
    active: true
  EmptyWhileBlock:
    active: true

exceptions:
  active: true
  ExceptionRaisedInUnexpectedLocation:
    active: true
  InstanceOfCheckForException:
    active: true
  NotImplementedDeclaration:
    active: false
  ObjectExtendsThrowable:
    active: false
  PrintStackTrace:
    active: true
  RethrowCaughtException:
    active: true
  ReturnFromFinally:
    active: true
  SwallowedException:
    active: false
  ThrowingExceptionFromFinally:
    active: true
  ThrowingExceptionInMain:
    active: false
  ThrowingExceptionsWithoutMessageOrCause:
    active: true
  ThrowingNewInstanceOfSameException:
    active: true
  TooGenericExceptionCaught:
    active: true
    exceptionNames:
      - 'ArrayIndexOutOfBoundsException'
      - 'Error'
      - 'Exception'
      - 'IllegalMonitorStateException'
      - 'IndexOutOfBoundsException'
      - 'NullPointerException'
      - 'RuntimeException'
      - 'Throwable'
  TooGenericExceptionThrown:
    active: true
    exceptionNames:
      - 'Error'
      - 'Exception'
      - 'RuntimeException'
      - 'Throwable'

naming:
  active: true
  BooleanPropertyNaming:
    active: false
  ClassNaming:
    active: true
    classPattern: '[A-Z][a-zA-Z0-9]*'
  ConstructorParameterNaming:
    active: true
    parameterPattern: '[a-z][A-Za-z0-9]*'
  EnumNaming:
    active: true
    enumEntryPattern: '[A-Z][_a-zA-Z0-9]*'
  ForbiddenClassName:
    active: false
  FunctionMaxLength:
    active: false
  FunctionMinLength:
    active: false
  FunctionNaming:
    active: true
    functionPattern: '[a-z][a-zA-Z0-9]*'
    excludeClassPattern: '$^'
  FunctionParameterNaming:
    active: true
    parameterPattern: '[a-z][A-Za-z0-9]*'
  InvalidPackageDeclaration:
    active: true
  LambdaParameterNaming:
    active: false
  MatchingDeclarationName:
    active: true
  MemberNameEqualsClassName:
    active: true
  NoNameShadowing:
    active: true
  NonBooleanPropertyPrefixedWithIs:
    active: false
  ObjectPropertyNaming:
    active: true
    constantPattern: '[A-Za-z][_A-Za-z0-9]*'
  PackageNaming:
    active: true
    packagePattern: '[a-z]+(\.[a-z][A-Za-z0-9]*)*'
  TopLevelPropertyNaming:
    active: true
    constantPattern: '[A-Z][_A-Z0-9]*'
  VariableMaxLength:
    active: false
  VariableMinLength:
    active: false
  VariableNaming:
    active: true
    variablePattern: '[a-z][A-Za-z0-9]*'

performance:
  active: true
  ArrayPrimitive:
    active: true
  CouldBeSequence:
    active: false
  ForEachOnRange:
    active: true
  SpreadOperator:
    active: false
  UnnecessaryPartOfBinaryExpression:
    active: false
  UnnecessaryTemporaryInstantiation:
    active: true

potential-bugs:
  active: true
  AvoidReferentialEquality:
    active: true
  CastNullableToNonNullableType:
    active: false
  CastToNullableType:
    active: false
  Deprecation:
    active: false
  DontDowncastCollectionTypes:
    active: false
  DoubleMutabilityForCollection:
    active: true
  ElseCaseInsteadOfExhaustiveWhen:
    active: false
  EqualsAlwaysReturnsTrueOrFalse:
    active: true
  EqualsWithHashCodeExist:
    active: true
  ExitOutsideMain:
    active: false
  ExplicitGarbageCollectionCall:
    active: true
  HasPlatformType:
    active: true
  IgnoredReturnValue:
    active: true
  ImplicitDefaultLocale:
    active: true
  ImplicitUnitReturnType:
    active: false
  InvalidRange:
    active: true
  IteratorHasNextCallsNextMethod:
    active: true
  IteratorNotThrowingNoSuchElementException:
    active: true
  LateinitUsage:
    active: false
  MapGetWithNotNullAssertionOperator:
    active: true
  MissingPackageDeclaration:
    active: false
  NullCheckOnMutableProperty:
    active: false
  NullableToStringCall:
    active: false
  UnconditionalJumpStatementInLoop:
    active: false
  UnnecessaryNotNullCheck:
    active: false
  UnnecessaryNotNullOperator:
    active: true
  UnnecessarySafeCall:
    active: true
  UnreachableCatchBlock:
    active: true
  UnreachableCode:
    active: true
  UnsafeCallOnNullableType:
    active: true
  UnsafeCast:
    active: false
  UnusedUnaryOperator:
    active: true
  UselessPostfixExpression:
    active: true
  WrongEqualsTypeParameter:
    active: true

style:
  active: true
  AlsoCouldBeApply:
    active: false
  BracesOnIfStatements:
    active: false
  BracesOnWhenStatements:
    active: false
  CanBeNonNullable:
    active: false
  CascadingCallWrapping:
    active: false
  ClassOrdering:
    active: false
  CollapsibleIfStatements:
    active: false
  DataClassContainsFunctions:
    active: false
  DataClassShouldBeImmutable:
    active: false
  DestructuringDeclarationWithTooManyEntries:
    active: false
  DoubleNegativeLambda:
    active: false
  EqualsNullCall:
    active: true
  EqualsOnSignatureLine:
    active: false
  ExplicitCollectionElementAccessMethod:
    active: false
  ExplicitItLambdaParameter:
    active: false
  ExpressionBodySyntax:
    active: false
  ForbiddenAnnotation:
    active: false
  ForbiddenComment:
    active: false
  ForbiddenImport:
    active: false
  ForbiddenMethodCall:
    active: false
  ForbiddenVoid:
    active: true
  FunctionOnlyReturningConstant:
    active: false
  LoopWithTooManyJumpStatements:
    active: false
  MagicNumber:
    active: false
  MandatoryBracesLoops:
    active: false
  MaxChainedCallsOnSameLine:
    active: false
  MaxLineLength:
    active: true
    maxLineLength: 120
  MayBeConst:
    active: true
  ModifierOrder:
    active: true
  MultilineLambdaItParameter:
    active: false
  MultilineRawStringIndentation:
    active: false
  NestedClassesVisibility:
    active: false
  NewLineAtEndOfFile:
    active: true
  NoTabs:
    active: false
  NullableBooleanCheck:
    active: false
  ObjectLiteralToLambda:
    active: true
  OptionalAbstractKeyword:
    active: true
  OptionalUnit:
    active: false
  PreferToOverPairSyntax:
    active: false
  ProtectedMemberInFinalClass:
    active: true
  RedundantExplicitType:
    active: false
  RedundantHigherOrderMapUsage:
    active: true
  RedundantVisibilityModifierRule:
    active: false
  ReturnCount:
    active: true
    max: 3
  SafeCast:
    active: true
  SerialVersionUIDInSerializableClass:
    active: false
  SpacingBetweenPackageAndImports:
    active: false
  StringShouldBeRawString:
    active: false
  ThrowsCount:
    active: true
    max: 2
  TrailingWhitespace:
    active: false
  TrimMultilineRawString:
    active: false
  UnderscoresInNumericLiterals:
    active: false
  UnnecessaryAbstractClass:
    active: false
  UnnecessaryAnnotationUseSiteTarget:
    active: false
  UnnecessaryApply:
    active: true
  UnnecessaryBackticks:
    active: false
  UnnecessaryBracesAroundTrailingLambda:
    active: false
  UnnecessaryFilter:
    active: true
  UnnecessaryInheritance:
    active: true
  UnnecessaryInnerClass:
    active: false
  UnnecessaryLet:
    active: false
  UnnecessaryParentheses:
    active: false
  UntilInsteadOfRangeTo:
    active: false
  UnusedImports:
    active: false
  UnusedParameter:
    active: false
  UnusedPrivateClass:
    active: true
  UnusedPrivateMember:
    active: true
  UnusedPrivateProperty:
    active: true
  UseAnyOrNoneInsteadOfFind:
    active: true
  UseArrayLiteralsInAnnotations:
    active: true
  UseCheckNotNull:
    active: true
  UseCheckOrError:
    active: true
  UseDataClass:
    active: false
  UseEmptyCounterpart:
    active: false
  UseIfEmptyOrIfBlank:
    active: false
  UseIfInsteadOfWhen:
    active: false
  UseIsNullOrEmpty:
    active: true
  UseOrEmpty:
    active: true
  UseRequire:
    active: true
  UseRequireNotNull:
    active: true
  UseSumOfInsteadOfFlatMapSize:
    active: false
  UselessCallOnNotNull:
    active: true
  UtilityClassWithPublicConstructor:
    active: true
  VarCouldBeVal:
    active: true
  WildcardImport:
    active: false
```

## Editor Config (.editorconfig)

```ini
root = true

[*]
charset = utf-8
end_of_line = lf
indent_size = 4
indent_style = space
insert_final_newline = true
max_line_length = 120
tab_width = 4
trim_trailing_whitespace = true

[*.{kt,kts}]
ij_kotlin_allow_trailing_comma = true
ij_kotlin_allow_trailing_comma_on_call_site = true

[*.{json,yml,yaml}]
indent_size = 2

[*.md]
max_line_length = off
trim_trailing_whitespace = false
```

## Time API Usage (kotlin.time.Clock)

```kotlin
// ✅ CORRECT - Use kotlin.time.Clock (not deprecated)
import kotlin.time.Clock
import kotlin.time.measureTime
import kotlinx.datetime.Instant
import kotlinx.datetime.Clock as DateTimeClock

// For monotonic time measurement
val mark = Clock.Monotonic.markNow()
val duration = mark.elapsedNow()

// For timestamps
val now: Instant = DateTimeClock.System.now()

// Measure execution time
val timeTaken = measureTime {
    // Your code here
}

// ❌ DEPRECATED - Don't use
// import kotlinx.datetime.Clock
// val now = Clock.System.now() // This Clock is deprecated
```

## Project Patterns

### 1. **Module Organization**
- `shared/` - Business logic, data layer, API clients
- `composeApp/` - UI layer with Compose Multiplatform
- `backend/` (optional) - Ktor server for backend services

### 2. **No Deprecated Resources**
- Use `compose.components.resources` for resources
- Leverage `Res.string.*` and `Res.drawable.*` generated accessors
- Set `compose.resources.always.generate.accessors=true` in gradle.properties

### 3. **Type-Safe Navigation**
- Use Navigation Compose with type-safe routes
- Define routes as sealed classes with serialization

### 4. **Dependency Injection**
- Use Koin with Compose integration
- Define modules per feature
- Use `koin-compose-viewmodel` for ViewModel injection

### 5. **Coroutines & Flow**
- Use structured concurrency
- Prefer `Flow` over `LiveData`
- Use `StateFlow` for UI state

### 6. **Database**
- SQLDelight for local persistence
- Define schema in `.sq` files
- Use coroutines extensions for async queries

### 7. **Testing**
- Unit tests in `commonTest`
- Use Turbine for Flow testing
- Use Kotest assertions for expressive tests

## Build Commands

```bash
# Clean build
./gradlew clean

# Check code quality
./gradlew detekt
./gradlew spotlessCheck

# Format code
./gradlew spotlessApply

# Run tests
./gradlew test

# Android
./gradlew :composeApp:assembleDebug
./gradlew :composeApp:installDebug

# Desktop
./gradlew :composeApp:run

# iOS
./gradlew :composeApp:iosX64Binaries

# Backend
./gradlew :backend:run
```

## CI/CD Integration

```yaml
# Example GitHub Actions workflow
name: CI

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Code Quality
        run: |
          ./gradlew detekt
          ./gradlew spotlessCheck

      - name: Test
        run: ./gradlew test

      - name: Build
        run: ./gradlew build
```

## References

- [Kotlin Multiplatform](https://www.jetbrains.com/kotlin-multiplatform/)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Ktor](https://ktor.io/)
- [SQLDelight](https://cashapp.github.io/sqldelight/)
- [Koin](https://insert-koin.io/)
- [Detekt](https://detekt.dev/)

---

**Last Updated:** 2026-03-12
