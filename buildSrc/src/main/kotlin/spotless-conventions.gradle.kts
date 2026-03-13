import com.diffplug.gradle.spotless.SpotlessExtension

plugins {
    id("com.diffplug.spotless")
}

configure<SpotlessExtension> {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", "**/generated/**", "**/.gradle/**")
        targetExcludeIfContentContains("ComposeUIViewController")

        ktlint("1.5.0")
            .editorConfigOverride(
                mapOf(
                    "max_line_length" to "150",
                    "ktlint_standard_max-line-length" to "enabled",
                    "indent_size" to "4",
                    "ij_kotlin_allow_trailing_comma" to "true",
                    "ij_kotlin_allow_trailing_comma_on_call_site" to "true",
                    "ktlint_standard_filename" to "disabled",
                    "ktlint_standard_value-parameter-comment" to "disabled",
                    "ktlint_standard_no-empty-file" to "disabled",
                    "ktlint_function_naming_ignore_when_annotated_with" to "Composable"
                )
            )

        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts", "buildSrc/**/*.gradle.kts")
        ktlint("1.5.0")
        trimTrailingWhitespace()
        endWithNewline()
    }

    format("misc") {
        target("**/*.md", "**/.gitignore")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
