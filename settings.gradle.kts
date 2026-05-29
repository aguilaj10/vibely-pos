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

buildCache {
    local {
        isEnabled = true
        isPush = true
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":backend")
include(":shared")

// Skip frontend / mobile modules when BACKEND_ONLY=true (used by the
// render.com Docker build so it doesn't need Android SDK or composeApp sources).
if (System.getenv("BACKEND_ONLY") != "true") {
    include(":composeApp")
    include(":androidApp")
}
