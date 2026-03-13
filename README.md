# Vibely POS - Kotlin Multiplatform Point of Sale System

A modern, cross-platform Point of Sale system built with Kotlin Multiplatform and Compose Multiplatform.

## 🚀 Tech Stack

- **Kotlin**: 2.3.10
- **Compose Multiplatform**: 1.10.1
- **Ktor**: 3.4.0 (Client & Server)
- **Koin**: 4.1.1
- **Gradle**: 9.3.1
- **Android Gradle Plugin**: 9.1.0

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
- ✅ **iOS** (arm64, simulator) - Mobile POS terminals
- ✅ **Web** (JavaScript & WebAssembly) - Browser-based POS

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
# Build and run Web application
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Production build
./gradlew :composeApp:wasmJsBrowserDistribution
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

- **JDK 17** (required for Kotlin 2.3.10)
- **Android SDK** (for Android builds)
  - Set `ANDROID_HOME` or create `local.properties` with `sdk.dir=/path/to/android/sdk`
- **Xcode** (for iOS builds, macOS only)
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
- **Spotless** with ktlint for code formatting (150 char line length)
- **Detekt** for static code analysis (strict rules, warnings as errors)
- **EditorConfig** for IDE consistency
- **Pre-commit hooks** for automatic quality checks
- **CI/CD** with GitHub Actions for continuous validation

## 📚 Documentation

Additional documentation available in the `docs/` directory:
- Architecture Overview
- API Documentation
- UI/UX Guidelines
- Database Schema
- Deployment Guide

## 🤝 Contributing

### Git Workflow

This project uses a **Git Flow** branching strategy:

- **`main`** - Production-ready code. Protected branch, all changes via PR.
- **`develop`** - Integration branch for features. Base for all feature branches.
- **`feature/*`** - Feature branches (e.g., `feature/login-screen`)
  - Branch from: `develop`
  - Merge back to: `develop` via PR
- **`hotfix/*`** - Urgent production fixes (e.g., `hotfix/critical-bug`)
  - Branch from: `main`
  - Merge to: `main` AND `develop`

### Development Workflow

1. **Start a new feature**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. **Make changes and commit**
   ```bash
   # Pre-commit hooks will run automatically
   git add .
   git commit -m "Description of changes"

   # To skip hooks (not recommended):
   # git commit --no-verify
   ```

3. **Push and create PR**
   ```bash
   git push origin feature/your-feature-name
   # Open Pull Request on GitHub: feature/your-feature-name → develop
   ```

4. **After PR is merged**
   ```bash
   git checkout develop
   git pull origin develop
   git branch -d feature/your-feature-name  # Delete local branch
   ```

### Pre-commit Hooks

This project uses Git pre-commit hooks to ensure code quality:

- **Spotless Check**: Verifies code formatting
- **Detekt**: Runs static analysis

If checks fail, the commit will be rejected. Fix issues before committing:
```bash
# Fix formatting automatically
./gradlew spotlessApply

# Check and fix Detekt issues
./gradlew detekt
```

To temporarily skip hooks (use sparingly):
```bash
git commit --no-verify
```

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
