# CI/CD Workflows

This directory contains GitHub Actions workflows for automated testing, building, and deployment.

## Workflows

### CI Workflow (`ci.yml`)

Main continuous integration pipeline that runs on every push and pull request to `main` and `develop` branches.

#### Jobs Overview

```
Code Quality (2 min)
     ├─> Build Desktop (5 min)
     ├─> Build Android (5 min)
     ├─> Build iOS (6 min)
     └─> Build Web (4 min)
              └─> Test Coverage (3 min)
```

#### Job Details

**1. Code Quality** (`ubuntu-latest`)
- ✅ Spotless formatting check
- ✅ Detekt static analysis
- 📤 Uploads Detekt reports

**2. Build Desktop (JVM)** (`ubuntu-latest`)
- Builds JVM targets (composeApp, backend)
- Runs JVM tests
- 📤 Uploads JAR artifacts

**3. Build Android** (`ubuntu-latest`)
- Builds Android debug APK
- Runs Android unit tests
- Validates Android compatibility

**4. Build iOS** (`macos-latest`)
- Builds iOS framework for Simulator ARM64
- Runs iOS tests
- Uses Kotlin Native cache

**5. Build Web (JS/Wasm)** (`ubuntu-latest`)
- Builds JavaScript distribution
- Builds WebAssembly distribution
- Runs JS tests (can fail)

**6. Test Coverage** (`ubuntu-latest`)
- Runs all tests
- Generates Kover coverage reports (Backend)
- Creates coverage badge
- 💬 Comments coverage on PRs
- 📤 Uploads coverage reports (30 days)

## Running Locally

Simulate CI checks before pushing:

```bash
# Quick check
./gradlew spotlessCheck detekt

# Full build
./gradlew build

# Platform-specific
./gradlew jvmTest                    # Desktop
./gradlew composeApp:assembleDebug   # Android
./gradlew iosSimulatorArm64Test      # iOS
./gradlew jsTest                     # Web

# Coverage
./gradlew backend:koverXmlReport backend:koverHtmlReport
open backend/build/reports/kover/html/index.html
```

## Caching Strategy

### Gradle Cache
- **Path:** `~/.gradle/caches`, `~/.gradle/wrapper`
- **Key:** OS + Gradle files hash
- **Benefit:** ~2-3 minutes faster builds

### Kotlin Native Cache (iOS)
- **Path:** `~/.konan`
- **Key:** OS + Gradle wrapper hash
- **Benefit:** ~1-2 minutes faster iOS builds

## Environment Variables

```yaml
GRADLE_OPTS: "-Dorg.gradle.jvmargs=-Xmx4096m -Dorg.gradle.daemon=false -Dkotlin.incremental=false"
```

- **Xmx4096m:** 4GB heap for large builds
- **daemon=false:** No daemon in CI
- **incremental=false:** Full compilation for reliability

## Artifacts

### Detekt Reports
- **Retention:** 7 days
- **Formats:** HTML, XML, TXT, SARIF
- **Path:** `**/build/reports/detekt/`

### JVM Artifacts
- **Retention:** 7 days
- **Files:** `*.jar`
- **Path:** `composeApp/build/libs/`, `backend/build/libs/`

### Coverage Reports
- **Retention:** 30 days
- **Formats:** XML, HTML
- **Path:** `backend/build/reports/kover/`

## Coverage Requirements

- **Overall:** 40% minimum
- **Changed files:** 60% minimum
- **Badge:** Auto-generated in `.github/badges/`

## Troubleshooting

### Build Timeout
Increase `timeout-minutes` in job definition.

### Out of Memory
Increase `GRADLE_OPTS` Xmx value.

### iOS Build Fails
- Ensure running on `macos-latest`
- Check Xcode version compatibility

### Coverage Badge Not Updating
- Check `.github/badges/` directory exists
- Verify `jacoco-badge-generator` action ran successfully

## Future Enhancements

- [ ] Deploy preview environments
- [ ] Performance benchmarking
- [ ] Visual regression testing
- [ ] Automated releases
- [ ] Docker image publishing
- [ ] Integration test suite

## Performance Targets

- Code quality: < 2 minutes
- Platform builds: < 5 minutes each
- Full pipeline: < 15 minutes
- Test coverage: < 3 minutes

## Links

- [CI Documentation](../../docs/CI_CD.md)
- [Contributing Guide](../../CONTRIBUTING.md)
- [Kover Plugin](https://github.com/Kotlin/kotlinx-kover)
- [Detekt](https://detekt.dev/)
