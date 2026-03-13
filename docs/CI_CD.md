# CI/CD Pipeline

This document describes the Continuous Integration and Continuous Deployment (CI/CD) setup for Vibely POS.

## Overview

The CI/CD pipeline is implemented using GitHub Actions and consists of multiple parallel jobs that ensure code quality, build all platform targets, and generate comprehensive test coverage reports.

## Pipeline Jobs

### 1. Code Quality

**Runs on:** `ubuntu-latest`
**Triggers:** Every push and pull request
**Purpose:** Fast feedback on code quality issues

Checks:
- ✅ Code formatting (Spotless + ktlint)
- ✅ Static analysis (Detekt)
- ✅ Uploads Detekt reports as artifacts

**Fails if:** Any formatting violations or Detekt issues are found

### 2. Build Desktop (JVM)

**Runs on:** `ubuntu-latest`
**Depends on:** Code Quality
**Purpose:** Build and test JVM targets

Steps:
- Builds composeApp JVM target
- Builds backend server
- Runs JVM tests
- Uploads JAR artifacts

### 3. Build Android

**Runs on:** `ubuntu-latest`
**Depends on:** Code Quality
**Purpose:** Build and test Android targets

Steps:
- Builds Android debug APK
- Runs Android unit tests
- Verifies Android compatibility

### 4. Build iOS

**Runs on:** `macos-latest`
**Depends on:** Code Quality
**Purpose:** Build and test iOS targets

Steps:
- Builds iOS framework for Simulator (ARM64)
- Runs iOS tests
- Caches Kotlin Native compiler

**Note:** Requires macOS runner due to Apple toolchain requirements

### 5. Build Web (JS/Wasm)

**Runs on:** `ubuntu-latest`
**Depends on:** Code Quality
**Purpose:** Build and test web targets

Steps:
- Builds JavaScript browser distribution
- Builds WebAssembly browser distribution
- Runs JavaScript tests

### 6. Test Coverage

**Runs on:** `ubuntu-latest`
**Depends on:** All build jobs
**Purpose:** Generate comprehensive coverage reports

Steps:
- Runs all tests across platforms
- Generates Kover XML and HTML reports
- Creates coverage badges
- Uploads coverage reports (30-day retention)
- Comments coverage on pull requests

**Thresholds:**
- Overall coverage: 40% minimum
- Changed files: 60% minimum

### 7. Integration Tests

**Status:** Currently disabled (enabled with `if: false`)
**Purpose:** Run integration tests when configured

This job is ready to be enabled once integration tests are implemented.

## Caching Strategy

The pipeline uses aggressive caching to speed up builds:

### Gradle Cache
```yaml
~/.gradle/caches
~/.gradle/wrapper
```
**Key:** Based on Gradle files hash
**Benefit:** Faster dependency resolution

### Kotlin Native Cache (iOS builds)
```yaml
~/.konan
```
**Key:** Based on Gradle wrapper properties
**Benefit:** Faster native compilation

## Optimization Features

### 1. Parallel Execution
All platform builds run in parallel after code quality checks pass, reducing total CI time.

### 2. Gradle Daemon Configuration
```bash
GRADLE_OPTS: "-Dorg.gradle.jvmargs=-Xmx4096m -Dorg.gradle.daemon=false -Dkotlin.incremental=false"
```
- 4GB heap for large builds
- Daemon disabled for CI (fresh state)
- Incremental compilation disabled (reliability)

### 3. No-Daemon Builds
All Gradle commands use `--no-daemon` flag for:
- Consistent build environment
- Better memory management
- Cleaner CI logs

### 4. Stacktraces
All builds use `--stacktrace` for better debugging when issues occur.

## Code Coverage Setup

### Kover Configuration

The project uses [Kover](https://github.com/Kotlin/kotlinx-kover) for code coverage:

**Root build.gradle.kts:**
```kotlin
plugins {
    alias(libs.plugins.kover)
}

dependencies {
    kover(projects.shared)
    kover(projects.composeApp)
    kover(projects.backend)
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*BuildConfig",
                    "*_Factory",
                    "*.databinding.*",
                    "*.generated.*"
                )
            }
        }
        total {
            xml { reportFile.set(file("$buildDir/reports/kover/report.xml")) }
            html { reportDir.set(file("$buildDir/reports/kover/html")) }
        }
    }
}
```

**Module build files:**
```kotlin
plugins {
    alias(libs.plugins.kover)
}
```

### Coverage Reports

**XML Report:**
```bash
./gradlew koverXmlReport
# Output: build/reports/kover/report.xml
```

**HTML Report:**
```bash
./gradlew koverHtmlReport
# Output: build/reports/kover/html/index.html
```

**View locally:**
```bash
open build/reports/kover/html/index.html
```

## Coverage Badge

The pipeline generates a coverage badge automatically:

1. Coverage is calculated from Kover XML report
2. Badge is generated using `cicirello/jacoco-badge-generator`
3. Badge is saved to `.github/badges/jacoco.svg`
4. Badge is displayed in README.md

**Badge URL:**
```markdown
[![Coverage](.github/badges/jacoco.svg)](https://github.com/yourusername/vibely-pos/actions)
```

## Artifacts

### Detekt Reports
- **Retention:** 7 days
- **Path:** `**/build/reports/detekt/`
- **Formats:** HTML, XML, TXT, SARIF

### JVM Artifacts
- **Retention:** 7 days
- **Path:** `composeApp/build/libs/*.jar`, `backend/build/libs/*.jar`

### Coverage Reports
- **Retention:** 30 days
- **Path:** `**/build/reports/kover/`
- **Formats:** XML, HTML

## Pull Request Checks

### Required Checks
All pull requests must pass:
- ✅ Code formatting
- ✅ Static analysis (Detekt)
- ✅ All platform builds
- ✅ All tests

### Coverage Comments
PRs automatically receive coverage comments with:
- Overall coverage percentage
- Changed files coverage
- Comparison with base branch

## Local Testing

Before pushing, run the same checks locally:

```bash
# Full CI simulation
./gradlew spotlessCheck detekt build test koverXmlReport

# Quick check
./gradlew spotlessCheck detekt

# Platform-specific
./gradlew jvmTest                    # Desktop
./gradlew composeApp:assembleDebug   # Android
./gradlew jsTest                     # Web
```

## Troubleshooting

### Build Fails on iOS
- **Cause:** iOS builds require macOS runner
- **Solution:** Check if running on macos-latest

### Out of Memory
- **Cause:** Large builds with insufficient heap
- **Solution:** Increase GRADLE_OPTS Xmx value

### Cache Miss
- **Cause:** Gradle files changed
- **Solution:** Normal behavior, cache will rebuild

### Coverage Job Skipped
- **Cause:** Platform build failed
- **Solution:** Fix failing platform build first

## Future Improvements

### Planned Enhancements
- [ ] Deploy preview builds for PRs
- [ ] Performance benchmarking
- [ ] Visual regression testing
- [ ] Automated releases
- [ ] Docker image publishing (backend)
- [ ] Integration test suite

### Performance Targets
- Code quality: < 2 minutes
- Platform builds: < 5 minutes each
- Full pipeline: < 15 minutes
- Test coverage generation: < 3 minutes

## Version Requirements

- **Java:** JDK 17
- **Gradle:** 8.x (wrapper)
- **Kotlin:** 2.3.10
- **GitHub Actions:** v4 (checkout, cache, upload-artifact)

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Gradle Build Cache](https://docs.gradle.org/current/userguide/build_cache.html)
- [Kover Documentation](https://github.com/Kotlin/kotlinx-kover)
- [Detekt](https://detekt.dev/)
- [Spotless](https://github.com/diffplug/spotless)
