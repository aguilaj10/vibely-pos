# Vibely POS - Kotlin Multiplatform Point of Sale System

A modern, cross-platform Point of Sale system built with Kotlin Multiplatform and Compose Multiplatform.

## 🚀 Tech Stack

- **Kotlin**: 2.1.0
- **Compose Multiplatform**: 1.7.1
- **Ktor**: 3.0.1 (Client & Server)
- **SQLDelight**: 2.0.2
- **Koin**: 4.0.0
- **Gradle**: 8.11

## 🏗️ Project Structure

```
vibely-pos/
├── shared/                  # Shared KMP code (business logic, data layer)
│   ├── commonMain/         # Common Kotlin code
│   ├── androidMain/        # Android-specific code
│   ├── desktopMain/        # Desktop (JVM) specific code
│   └── wasmJsMain/         # Web (Wasm) specific code
├── composeApp/             # Compose Multiplatform UI
│   ├── commonMain/         # Common UI code
│   ├── androidMain/        # Android app entry point
│   ├── desktopMain/        # Desktop app entry point
│   └── wasmJsMain/         # Web app entry point
├── backend/                # Ktor backend server
│   └── src/main/kotlin/    # Backend API code
├── gradle/                 # Gradle wrapper and version catalog
│   └── libs.versions.toml  # Centralized dependency versions
├── build.gradle.kts        # Root build script
├── settings.gradle.kts     # Project settings
├── gradle.properties       # Gradle configuration
├── detekt.yml             # Static analysis config
└── .editorconfig          # Code style config
```

## 🎯 Supported Platforms

- ✅ **Desktop** (Windows, macOS, Linux via JVM) - Primary development platform
- ✅ **Android** (API 24+) - Mobile POS terminals
- ⏸️ **Web (Wasm)** - Configured but awaiting SQLDelight Wasm support

## 🛠️ Build Commands

### Desktop Application
```bash
# Build desktop JAR
./gradlew :composeApp:desktopJar

# Run desktop application
./gradlew :composeApp:run
```

### Android Application
```bash
# Build debug APK
./gradlew :composeApp:assembleDebug

# Build release APK
./gradlew :composeApp:assembleRelease

# Install on connected device
./gradlew :composeApp:installDebug
```

### Backend Server
```bash
# Build backend
./gradlew :backend:build

# Run backend server (http://localhost:8080)
./gradlew :backend:run
```

### Web Application (Wasm)
```bash
# Note: Web build currently blocked by SQLDelight not supporting Wasm yet
# Structure is ready, will work once SQLDelight adds Wasm support
# ./gradlew :composeApp:wasmJsBrowserDevelopmentWebpack
```

## 🧪 Testing & Quality

### Run Tests
```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :shared:test
```

### Code Quality
```bash
# Check code style
./gradlew spotlessCheck

# Format code automatically
./gradlew spotlessApply

# Run static analysis
./gradlew detekt

# Run all quality checks
./gradlew check
```

## 📋 Prerequisites

- **JDK 11+** (JDK 17+ recommended)
- **Android SDK** (for Android builds)
  - Set `ANDROID_HOME` or create `local.properties` with `sdk.dir=/path/to/android/sdk`
- **Git**

## 🚦 Getting Started

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd vibely-pos
   ```

2. **Configure Android SDK** (if building for Android)
   ```bash
   echo "sdk.dir=/path/to/your/Android/Sdk" > local.properties
   ```

3. **Build the project**
   ```bash
   ./gradlew build
   ```

4. **Run desktop application**
   ```bash
   ./gradlew :composeApp:run
   ```

5. **Run backend server**
   ```bash
   ./gradlew :backend:run
   ```

## 📦 Module Details

### shared/
Contains shared business logic, domain models, repositories, and data sources that are used across all platforms.

**Key Features:**
- Cross-platform networking (Ktor Client)
- Local database (SQLDelight)
- Dependency injection (Koin)
- Common business logic

### composeApp/
UI layer built with Compose Multiplatform. Contains screens, ViewModels, and UI components.

**Key Features:**
- Material 3 Design System
- Navigation (Compose Navigation)
- Platform-specific UI adaptations
- Responsive layouts

### backend/
Ktor-based backend server providing REST API endpoints.

**Key Features:**
- JWT Authentication
- PostgreSQL database
- CORS support
- API endpoints for POS operations

## 🔧 Configuration

### Gradle Properties
Key configurations in `gradle.properties`:
- `org.gradle.jvmargs=-Xmx4g` - Maximum heap size for Gradle
- `kotlin.native.ignoreDisabledTargets=true` - Ignore iOS targets on non-macOS

### Code Quality
- **Spotless** with ktlint for code formatting (120 char line length)
- **Detekt** for static code analysis
- **EditorConfig** for IDE consistency

## 📚 Documentation

Additional documentation available in the `docs/` directory:
- Architecture Overview
- API Documentation
- UI/UX Guidelines
- Database Schema
- Deployment Guide

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Run `./gradlew spotlessApply` before committing
- Ensure all tests pass: `./gradlew check`

## 📄 License

[Add your license information here]

## 🙏 Acknowledgments

- JetBrains for Kotlin Multiplatform and Compose Multiplatform
- Ktor for networking framework
- SQLDelight for database management
- Koin for dependency injection

---

**Status**: ✅ Project Setup Complete | 🚧 In Active Development

For questions or support, please open an issue on GitHub.
