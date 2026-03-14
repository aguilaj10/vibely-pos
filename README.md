# Vibely POS

A modern, cross-platform Point of Sale system built with Kotlin Multiplatform and Compose Multiplatform.

[![CI](https://github.com/yourusername/vibely-pos/workflows/CI/badge.svg)](https://github.com/yourusername/vibely-pos/actions)
[![Coverage](.github/badges/jacoco.svg)](https://github.com/yourusername/vibely-pos/actions)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.10-blue.svg)](https://kotlinlang.org)

## Features

- 🛒 **Point of Sale** - Fast checkout with barcode scanning and product search
- 📊 **Inventory Management** - Real-time stock tracking and low-stock alerts
- 👥 **Customer Management** - Track purchases and loyalty programs
- 📈 **Sales Analytics** - Comprehensive reporting and insights
- 💼 **Multi-user Support** - Role-based access control
- 🌍 **Cross-platform** - Desktop, Android, iOS, and Web from a single codebase

## Technology Stack

- **Kotlin** 2.3.10 - Modern, type-safe programming language
- **Compose Multiplatform** 1.10.1 - Declarative UI framework
- **Ktor** 3.4.0 - Async HTTP client & server
- **Supabase** - PostgreSQL database and authentication
- **Koin** 4.1.1 - Dependency injection
- **Material 3** - Modern design system

## Supported Platforms

| Platform | Status | Target |
|----------|--------|--------|
| Desktop (JVM) | ✅ Ready | Windows, macOS, Linux |
| Android | ✅ Ready | API 24+ (Android 7.0+) |
| iOS | ✅ Ready | iOS 14+ |
| Web | ✅ Ready | Modern browsers (JS/Wasm) |

## Quick Start

### Prerequisites

- JDK 17 or higher
- Android SDK (for Android builds)
- Xcode (for iOS builds, macOS only)

### Build & Run

```bash
# Clone the repository
git clone https://github.com/yourusername/vibely-pos.git
cd vibely-pos

# Build all platforms
./gradlew build

# Run desktop application
./gradlew :composeApp:run

# Run backend server
./gradlew :backend:run
```

### Android

```bash
# Build and install on connected device
./gradlew :composeApp:installDebug
```

### Web

```bash
# Run development server
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

## Project Structure

```
vibely-pos/
├── composeApp/          # UI layer (Compose Multiplatform)
├── shared/              # Business logic (domain & data layers)
├── backend/             # Ktor server (REST API)
├── docs/                # Documentation
└── gradle/              # Build configuration
```

## Documentation

- [Architecture](docs/ARCHITECTURE.md) - System design and technical decisions
- [Database Schema](docs/DATABASE.md) - Data model and relationships
- [Contributing](CONTRIBUTING.md) - Development workflow and guidelines
- [Development Guide](docs/DEVELOPMENT.md) - Implementation plan and tasks

## Development

### Debug Mode (Skip Authentication)

For development convenience, you can bypass the login screen and start directly at the dashboard with a mock admin user:

#### Desktop (JVM)
```bash
# Using --skip-auth flag
./gradlew :composeApp:run --args="--skip-auth"

# Using shorthand -d flag
./gradlew :composeApp:run --args="-d"
```

#### Environment Variable (All Platforms)
```bash
# Set environment variable
DEBUG_MODE=true ./gradlew :composeApp:run
```

#### Android
Add to `local.properties`:
```properties
debug.mode.enabled=true
```

Or set via adb:
```bash
adb shell setprop debug.my.app.debug_mode true
```

#### iOS
1. Edit scheme in Xcode
2. Add launch argument: `-skip-auth`

**Debug Mode Features:**
- ✅ Auto-login with mock admin user (dev@vibely.pos)
- ✅ Skip authentication entirely
- ✅ Shows "🔧 DEBUG MODE" badge in UI
- ✅ Logs warning on startup
- ✅ Automatically disabled in production builds

**⚠️ IMPORTANT:** Debug mode is automatically disabled in production builds.

### Code Quality

This project maintains high code quality standards:

- **Spotless** - Automatic code formatting with ktlint
- **Detekt** - Static analysis (warnings treated as errors)
- **Pre-commit hooks** - Quality checks before each commit
- **CI/CD** - Automated testing on all platforms

```bash
# Check code formatting
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply

# Run static analysis
./gradlew detekt

# Run all checks
./gradlew check
```

### Testing

```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew koverXmlReport koverHtmlReport

# View HTML coverage report
open build/reports/kover/html/index.html

# Run tests for specific platform
./gradlew jvmTest           # JVM/Desktop tests
./gradlew jsTest            # JavaScript tests
./gradlew iosSimulatorArm64Test  # iOS tests
```

### Continuous Integration

The project uses GitHub Actions for CI/CD with the following checks:

- **Code Quality** - Spotless formatting and Detekt static analysis
- **Multi-platform Builds** - Desktop (JVM), Android, iOS, Web (JS/Wasm)
- **Automated Testing** - Unit tests on all platforms
- **Code Coverage** - Kover reports with badge generation
- **Parallel Execution** - Fast feedback with parallel jobs

All checks must pass before merging.

## Contributing

We welcome contributions! Please read our [Contributing Guide](CONTRIBUTING.md) for details on:

- Git workflow (Git Flow)
- Code style and conventions
- Pull request process
- Testing requirements

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [JetBrains](https://www.jetbrains.com/) for Kotlin and Compose Multiplatform
- [Ktor](https://ktor.io/) for networking framework
- [Supabase](https://supabase.com/) for backend infrastructure
- [Koin](https://insert-koin.io/) for dependency injection

---

**Status**: 🚧 In Active Development

For questions or support, please [open an issue](https://github.com/yourusername/vibely-pos/issues).
