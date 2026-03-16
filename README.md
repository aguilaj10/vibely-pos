# Vibely POS

A modern, cross-platform Point of Sale system built with Kotlin Multiplatform and Compose Multiplatform. Designed for small to medium businesses with features for sales, inventory, customer management, and reporting.

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.20-blue.svg)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/compose-1.7.1-blue.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)

## Features

### ✅ Implemented

- **🔐 Authentication** - JWT-based login with role-based access control (admin, manager, cashier, warehouse, viewer)
- **📊 Dashboard** - Real-time metrics, quick actions, and recent transactions overview
- **🛒 Point of Sale** - Complete checkout flow with product search, cart management, and payment processing
- **📦 Inventory Management** - Product catalog, stock tracking, and low-stock monitoring
- **📋 Categories** - Hierarchical product categorization system
- **👥 Customer Management** - Customer database with purchase history and loyalty tracking
- **🏪 Supplier Management** - Vendor information and contact management
- **💰 Sales History** - Transaction records with refund support
- **📈 Reports** - Sales analytics with date range filtering, category breakdowns, and trends
- **💼 Purchase Orders** - Inventory procurement workflow
- **💵 Cash Shifts** - Shift management with opening/closing balance tracking
- **⚙️ Settings** - Store configuration, tax rates, currencies, and receipt customization
- **👤 User Management** - Multi-user support with role assignment
- **🎨 Modern UI** - Material Design 3 with custom grayscale-dominant theme
- **🔧 Debug Mode** - Development mode with authentication bypass for faster testing

### 🚧 In Progress

- **📱 Mobile Optimization** - Platform-specific UI enhancements for Android/iOS
- **🌐 Web Platform** - WebAssembly and JavaScript builds
- **📧 Email Notifications** - Automated receipts and low-stock alerts
- **📊 Advanced Analytics** - Predictive inventory and customer insights

### 📋 Planned

- **🔍 Barcode Scanning** - Product lookup via barcode scanner
- **🖨️ Receipt Printing** - Direct printer integration
- **☁️ Cloud Sync** - Multi-location synchronization
- **🔄 Offline Mode** - Local-first data with sync when online
- **📱 Mobile Apps** - Native iOS and Android builds

## Technology Stack

- **Kotlin** 2.3.10 - Modern, type-safe programming language
- **Compose Multiplatform** 1.10.2 - Declarative UI framework
- **Ktor** 3.4.1 - Async HTTP client & server
- **Supabase** - PostgreSQL database with Row Level Security (RLS)
- **Koin** 4.1.1 - Dependency injection
- **Material 3** - Modern design system with custom theme
- **JWT Authentication** - Secure token-based auth with BCrypt password hashing
- **Kotlinx Serialization** - Type-safe JSON handling
- **Coroutines & Flow** - Reactive state management

## Supported Platforms

| Platform | Status | Target |
|----------|--------|--------|
| Desktop (JVM) | ✅ Production Ready | Windows, macOS, Linux |
| Android | 🚧 In Development | API 24+ (Android 7.0+) |
| iOS | 🚧 In Development | iOS 14+ |
| Web | 🚧 In Development | Modern browsers (JS/Wasm) |

## Quick Start

### Prerequisites

- JDK 17 or higher
- Android SDK (for Android builds)
- Xcode (for iOS builds, macOS only)

### Build & Run

```bash
# Clone the repository
git clone https://github.com/your-username/vibely-pos.git
cd vibely-pos

# Set up environment variables
cp .env.example .env
# Edit .env with your Supabase credentials

# Apply database schema
# Run migrations in /migrations/ directory on your Supabase project

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

- [Coding Standards](CODING_STANDARDS.md) - Code quality requirements and best practices
- [Architecture](docs/ARCHITECTURE.md) - System design and technical decisions
- [Database Schema](docs/DATABASE.md) - Data model and relationships
- [Contributing](CONTRIBUTING.md) - Development workflow and guidelines
- [Development Guide](docs/DEVELOPMENT.md) - Implementation plan and tasks

## Development

### Debug Mode (Development)

For development convenience, you can bypass authentication and start directly at the dashboard with a pre-configured debug user:

**Backend Debug Mode:**
```bash
# Enable debug mode for backend API
export DEBUG_MODE=true
./gradlew :backend:run
```

When `DEBUG_MODE=true`, the backend accepts `Authorization: Bearer debug-access-token` and automatically injects a debug user principal.

**Frontend Debug Mode:**

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
- ✅ Backend: Accepts debug bearer token for authentication bypass
- ✅ Frontend: Auto-login with debug user (`dev@vibely.pos`, admin role)
- ✅ Skip authentication flows entirely
- ✅ Shows "🔧 DEBUG MODE" badge in UI
- ✅ Logs warning on startup
- ⚠️ **Automatically disabled in production builds**

> **Security Note:** Debug mode uses a hardcoded user in the database (`UUID: a2259bb8-d02d-4384-bf2f-bbfca16bade5`). The debug user is created automatically when you apply the database migrations.

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

The project uses GitHub Actions for CI/CD:

| Job | Purpose | Status |
|-----|---------|--------|
| Code Quality | Spotless + Detekt checks | ✅ Implemented |
| Build Desktop | JVM/JAR builds | ✅ Implemented |
| Build Android | Debug APK | ✅ Implemented |
| Build iOS | ARM64 Simulator | ✅ Implemented |
| Build Web | JS/Wasm bundles | ✅ Implemented |
| Test Coverage | Kover reports + badge generation | ✅ Implemented |

**Pipeline Features:**
- Parallel execution for fast feedback
- Gradle caching for faster builds
- Artifact retention (7-30 days)
- Automatic quality checks on PR

**Local CI Simulation:**
```bash
# Full CI check
./gradlew spotlessCheck detekt build test koverXmlReport

# Quick pre-commit check
./gradlew spotlessCheck detekt
```

All checks must pass before merging.

## Contributing

We welcome contributions! Please read:

- [**Coding Standards**](CODING_STANDARDS.md) - Code quality requirements (KDoc, deprecated libraries, Boy Scout rule)
- [**Contributing Guide**](CONTRIBUTING.md) - Git workflow, PR process, commit conventions

Key standards:
- Preserve KDoc on public APIs
- Verify libraries are NOT deprecated before use
- Clean code as you touch it (Boy Scout rule)
- Run `./gradlew spotlessApply detekt` before committing

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [JetBrains](https://www.jetbrains.com/) for Kotlin and Compose Multiplatform
- [Ktor](https://ktor.io/) for networking framework
- [Supabase](https://supabase.com/) for backend infrastructure
- [Koin](https://insert-koin.io/) for dependency injection

---

**Status**: 🚧 Active Development - Desktop platform production-ready, mobile/web in progress

For questions or support, please [open an issue](https://github.com/your-username/vibely-pos/issues).
