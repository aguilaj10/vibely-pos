# Architecture Overview

Vibely POS is built with Clean Architecture principles using Kotlin Multiplatform, ensuring code sharing across platforms while maintaining platform-specific optimizations.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Desktop  │  │ Android  │  │   iOS    │  │   Web    │   │
│  │  (JVM)   │  │          │  │          │  │ (JS/Wasm)│   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
│       └─────────────┴─────────────┴─────────────┘           │
│                         │                                    │
│                  Compose Multiplatform                       │
│                 (composeApp module)                          │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────┐
│                    Domain Layer                              │
│  ┌────────────┐  ┌──────────┐  ┌────────────────┐          │
│  │  Entities  │  │Use Cases │  │  Repositories  │          │
│  │  (Models)  │  │ (Logic)  │  │  (Interfaces)  │          │
│  └────────────┘  └──────────┘  └────────────────┘          │
│                   (shared module)                            │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────┐
│                    Data Layer                                │
│  ┌──────────────┐  ┌─────────────┐  ┌──────────────┐       │
│  │ Repositories │  │     DTOs    │  │ Data Sources │       │
│  │     Impl     │  │   Mappers   │  │   (Remote)   │       │
│  └──────────────┘  └─────────────┘  └──────────────┘       │
│                   (shared module)                            │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────┴───────────────────────────────────┐
│                    Backend (Optional)                        │
│  ┌──────────┐  ┌──────────┐  ┌────────────┐               │
│  │   Ktor   │  │   Auth   │  │  Supabase  │               │
│  │   API    │  │   JWT    │  │ PostgreSQL │               │
│  └──────────┘  └──────────┘  └────────────┘               │
│                   (backend module)                           │
└─────────────────────────────────────────────────────────────┘
```

## Module Structure

### composeApp (Presentation)

UI layer using Compose Multiplatform with MVVM pattern.

**Structure:**
```
composeApp/
├── commonMain/
│   ├── ui/
│   │   ├── screens/        # Screen composables
│   │   ├── components/     # Reusable UI components
│   │   ├── theme/          # Theme system (colors, typography, shapes)
│   │   └── navigation/     # Navigation logic
│   └── viewmodel/          # ViewModels (state management)
├── androidMain/            # Android-specific UI
├── iosMain/                # iOS-specific UI
├── desktopMain/            # Desktop-specific UI
└── wasmJsMain/             # Web-specific UI
```

**Key Patterns:**
- **MVVM** - ViewModels manage UI state
- **Unidirectional Data Flow** - State flows down, events flow up
- **StateFlow** - Reactive state management
- **Navigation** - Type-safe navigation with sealed classes

### shared (Domain + Data)

Business logic and data management shared across platforms.

**Structure:**
```
shared/
├── commonMain/
│   ├── domain/
│   │   ├── model/          # Domain entities
│   │   ├── usecase/        # Business logic
│   │   └── repository/     # Repository interfaces
│   ├── data/
│   │   ├── repository/     # Repository implementations
│   │   ├── dto/            # Data transfer objects
│   │   ├── mapper/         # DTO ↔ Entity mappers
│   │   └── remote/         # API clients (Ktor)
│   └── di/                 # Dependency injection (Koin)
├── androidMain/            # Android-specific data sources
├── iosMain/                # iOS-specific data sources
└── jvmMain/                # JVM-specific data sources
```

**Key Patterns:**
- **Clean Architecture** - Separation of concerns
- **Repository Pattern** - Abstract data sources
- **Use Cases** - Single responsibility business logic
- **Result Type** - Functional error handling

### backend (Optional)

Ktor server providing REST API (optional if using Supabase directly).

**Structure:**
```
backend/
└── src/main/kotlin/
    ├── Application.kt      # Server entry point
    ├── routes/             # API endpoints
    ├── auth/               # JWT authentication
    ├── data/               # Database access
    └── config/             # Configuration
```

## Design Principles

### 1. Clean Architecture

**Dependency Rule:** Dependencies point inward toward domain layer.

- **Domain Layer** - No dependencies on outer layers
- **Data Layer** - Depends on domain interfaces
- **Presentation Layer** - Depends on domain use cases

### 2. Separation of Concerns

Each layer has a single responsibility:

- **Presentation** - UI rendering and user interaction
- **Domain** - Business rules and logic
- **Data** - Data access and persistence

### 3. Platform Independence

Shared code is platform-agnostic with platform-specific implementations using `expect`/`actual`:

```kotlin
// commonMain
expect class PlatformLogger {
    fun log(message: String)
}

// androidMain
actual class PlatformLogger {
    actual fun log(message: String) {
        Log.d("App", message)
    }
}
```

## Data Flow

### Read Flow (Query)

```
UI Screen → ViewModel → Use Case → Repository → Data Source → API/DB
                                                                  │
UI Screen ← ViewModel ← Use Case ← Repository ← DTO Mapper ← ────┘
```

### Write Flow (Command)

```
UI Event → ViewModel → Use Case → Repository → Data Source → API/DB
                                                                 │
UI State ← ViewModel ← Use Case ← Repository ← Result ←─────────┘
```

## State Management

### ViewModel Pattern

```kotlin
class DashboardViewModel(
    private val getDashboardSummary: GetDashboardSummaryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    fun loadDashboard() {
        viewModelScope.launch {
            when (val result = getDashboardSummary()) {
                is Result.Success -> _state.value = DashboardState.Success(result.data)
                is Result.Error -> _state.value = DashboardState.Error(result.message)
            }
        }
    }
}
```

### State Types

```kotlin
sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(val summary: DashboardSummary) : DashboardState()
    data class Error(val message: String) : DashboardState()
}
```

## Error Handling

### Result Type

```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
}
```

### Usage in Use Cases

```kotlin
class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        return try {
            // Validate input
            if (email.isBlank()) return Result.Error("Email is required")
            if (password.isBlank()) return Result.Error("Password is required")

            // Execute business logic
            authRepository.login(email, password)
        } catch (e: Exception) {
            Result.Error("Login failed", e)
        }
    }
}
```

## Dependency Injection

Using Koin for DI across platforms:

```kotlin
val domainModule = module {
    factory { LoginUseCase(get()) }
    factory { GetDashboardSummaryUseCase(get()) }
}

val dataModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single { HttpClient { /* config */ } }
}

val presentationModule = module {
    viewModel { LoginViewModel(get()) }
    viewModel { DashboardViewModel(get()) }
}
```

## Navigation

Type-safe navigation using sealed classes:

```kotlin
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    data class ProductDetail(val productId: String) : Screen("product/{productId}")
}

// Usage
navController.navigate(Screen.Dashboard.route)
```

## Theme System

Material 3 design system with platform adaptations:

```kotlin
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme else lightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
```

## Performance Considerations

### 1. Lazy Loading

```kotlin
@Composable
fun ProductList(products: List<Product>) {
    LazyColumn {
        items(products) { product ->
            ProductItem(product)
        }
    }
}
```

### 2. Remember & Derived State

```kotlin
@Composable
fun FilteredProducts(products: List<Product>, query: String) {
    val filtered = remember(products, query) {
        products.filter { it.name.contains(query, ignoreCase = true) }
    }
}
```

### 3. Flow Optimization

```kotlin
val searchResults = searchQuery
    .debounce(300)  // Wait 300ms after typing stops
    .distinctUntilChanged()  // Only emit when value changes
    .flatMapLatest { query -> searchProducts(query) }
```

## Security

### 1. Authentication

- JWT tokens stored securely (encrypted preferences)
- Token refresh logic
- Automatic session expiration

### 2. Data Validation

- Input validation in domain layer
- SQL injection prevention (parameterized queries)
- XSS prevention (no raw HTML rendering)

### 3. Network Security

- HTTPS only
- Certificate pinning (optional)
- Request/response encryption

## Testing Strategy

### Unit Tests (Domain Layer)

```kotlin
@Test
fun `calculate total should sum item prices`() {
    val cart = Cart(items = listOf(
        CartItem(price = 10.0, quantity = 2),
        CartItem(price = 5.0, quantity = 1)
    ))
    assertEquals(25.0, cart.calculateTotal())
}
```

### Integration Tests (Data Layer)

```kotlin
@Test
fun `repository should fetch and map products correctly`() = runTest {
    val repository = ProductRepositoryImpl(mockApi)
    val result = repository.getProducts()
    assertTrue(result is Result.Success)
}
```

### UI Tests (Presentation Layer)

```kotlin
@Test
fun `login screen should show error for invalid credentials`() {
    composeTestRule.setContent {
        LoginScreen(viewModel = mockViewModel)
    }
    composeTestRule.onNodeWithText("Login").performClick()
    composeTestRule.onNodeWithText("Invalid credentials").assertIsDisplayed()
}
```

## Build Configuration

### Multi-platform Targets

- **JVM** - Desktop applications
- **Android** - Mobile devices (API 24+)
- **iOS** - iPhone and iPad (arm64, simulator)
- **JS** - Browser (legacy compatibility)
- **Wasm** - Browser (modern, performance-optimized)

### Gradle Modules

```kotlin
// Root build.gradle.kts
plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
}

// Module dependencies
// composeApp depends on shared
// backend is independent
```

## Resources

- [Kotlin Multiplatform Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Material 3 Design](https://m3.material.io/)

---

## Backend Authentication

### AuthService

JWT-based authentication with BCrypt password hashing:

- **Token Types:** Access (15 min) + Refresh (7 days)
- **Password:** BCrypt with cost factor 12
- **Security:** Token blacklisting, user status validation

### API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/login | Authenticate with email/password |
| POST | /api/auth/logout | Invalidate session |
| GET | /api/auth/me | Get authenticated user |
| POST | /api/auth/refresh | Refresh access token |

### Database Tables

- `refresh_tokens` - Store refresh tokens with expiration
- `token_blacklist` - Store invalidated tokens

---

## Backend Plugins

Ktor plugins configured in Application.kt:

| Plugin | Purpose |
|--------|---------|
| ContentNegotiation | JSON serialization (kotlinx.serialization) |
| CORS | Cross-origin requests |
| CallLogging | HTTP request logging |
| StatusPages | Global error handling |
| Authentication | JWT validation (HMAC256) |

### Supabase Client

- Lazy-initialized singleton
- Postgrest module for database operations
- CIO HTTP engine

### Environment Variables

```bash
SUPABASE_URL=https://xxx.supabase.co
SUPABASE_SERVICE_ROLE_KEY=your-key
JWT_SECRET=your-secret-key
```

---

## Theme System

Material 3-based design system for retail environments:

### AppColors

- **Primary:** Indigo (0xFF6366F1) - Trust, professionalism
- **Secondary:** Emerald (0xFF10B981) - Success, growth
- **Tertiary:** Amber (0xFFF59E0B) - Energy, attention
- **Status:** Success, Warning, Error, Info variants

### AppTypography

- Display (57/45/36sp), Headline (32/28/24sp), Title (22/16/14sp)
- Body (16/14/12sp), Label (14/12/11sp)
- POS-specific: PriceDisplay (48sp), NumericInput (32sp)

### AppShapes

- ExtraSmall (4dp), Small (8dp), Medium (12dp), Large (16dp), ExtraLarge (24dp)
- POS-specific: ProductCard, ActionButton, InputField

---

## Code Quality

### Tools

- **Spotless:** ktlint formatting
- **Detekt:** Static analysis
- **Kover:** Code coverage

### Koin-Detekt Rules

58 specialized rules across 6 categories:

| Category | Rules | Purpose |
|----------|-------|---------|
| Service Locator | 6 | Prevent KoinComponent anti-pattern |
| Module DSL | 14 | Best practices for module definitions |
| Scope Management | 8 | Memory leak prevention |
| Platform-Specific | 8 | Android/Compose patterns |
| Architecture | 4 | API usage correctness |
| Annotations | 18 | Koin annotations validation |

### Common Fixes

```kotlin
// ❌ Bad - KoinComponent
class MyRepo : KoinComponent {
    private val api: Api by inject()
}

// ✅ Good - Constructor injection
class MyRepo(private val api: Api)

// ❌ Bad - UseCase as singleton
single { LoginUseCase(get()) }

// ✅ Good - UseCase as factory
factory { LoginUseCase(get()) }
```

### Run Checks

```bash
./gradlew spotlessApply detekt
```
