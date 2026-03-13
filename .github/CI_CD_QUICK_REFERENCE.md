# CI/CD Quick Reference

## Pipeline Status
✅ **Multi-platform builds** - Desktop, Android, iOS, Web
✅ **Automated testing** - All platforms
✅ **Static analysis** - Detekt with fail-on-error
✅ **Code coverage** - Kover with badges
✅ **Optimized** - Caching & parallel execution

## Local Commands

### Quick Checks
```bash
# Code quality only (1-2 min)
./gradlew spotlessCheck detekt

# Fix formatting
./gradlew spotlessApply
```

### Platform Builds
```bash
# Desktop (JVM)
./gradlew :composeApp:jvmJar :backend:build

# Android
./gradlew :composeApp:assembleDebug

# iOS (macOS only)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Web
./gradlew :composeApp:jsBrowserDistribution
./gradlew :composeApp:wasmJsBrowserDistribution
```

### Testing
```bash
# All tests
./gradlew test

# Platform-specific
./gradlew jvmTest
./gradlew composeApp:testDebugUnitTest
./gradlew iosSimulatorArm64Test
./gradlew jsTest
```

### Coverage
```bash
# Generate reports
./gradlew backend:koverXmlReport backend:koverHtmlReport

# View HTML report
open backend/build/reports/kover/html/index.html
```

### Full CI Simulation
```bash
# Everything CI runs
./gradlew spotlessCheck detekt build test backend:koverXmlReport
```

## GitHub Actions Jobs

| Job | Platform | Time | Dependencies |
|-----|----------|------|--------------|
| Code Quality | ubuntu-latest | 2m | - |
| Build Desktop | ubuntu-latest | 5m | Code Quality |
| Build Android | ubuntu-latest | 5m | Code Quality |
| Build iOS | macos-latest | 6m | Code Quality |
| Build Web | ubuntu-latest | 4m | Code Quality |
| Coverage | ubuntu-latest | 3m | All builds |

**Total Pipeline:** ~15 minutes

## Artifacts

| Type | Retention | Path |
|------|-----------|------|
| Detekt Reports | 7 days | `**/build/reports/detekt/` |
| JVM JARs | 7 days | `{composeApp,backend}/build/libs/*.jar` |
| Coverage | 30 days | `backend/build/reports/kover/` |

## Coverage Thresholds

- **Overall:** 40% minimum
- **Changed files:** 60% minimum
- **Badge:** Auto-updated in `.github/badges/jacoco.svg`

## Documentation

- **CI/CD Guide:** `docs/CI_CD.md`
- **Workflow Docs:** `.github/workflows/README.md`
- **Summary:** `CI_CD_ENHANCEMENTS.md`

## Troubleshooting

### Build Failed
```bash
# Get detailed output
./gradlew <task> --stacktrace --info

# Clear cache
./gradlew clean
rm -rf ~/.gradle/caches
```

### Formatting Errors
```bash
./gradlew spotlessApply
```

### Coverage Not Generated
```bash
# Ensure tests ran first
./gradlew backend:test
./gradlew backend:koverXmlReport
```

## Performance Tips

- Use `--no-daemon` in CI
- Use `--parallel` for local builds
- Cache `~/.gradle` and `~/.konan`
- Run code quality first (fast fail)

## Contact

Issues? See:
- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Kover Docs](https://github.com/Kotlin/kotlinx-kover)
- [Detekt Docs](https://detekt.dev/)
