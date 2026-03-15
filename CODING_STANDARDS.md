# Coding Standards for Vibely POS

> **📌 MANDATORY FOR ALL CONTRIBUTORS**: Human developers AND AI agents must follow these standards.  
> See also: [CONTRIBUTING.md](CONTRIBUTING.md) for workflow

---

## 🚨 Critical Rules (Never Violate)

### 1. DRY Principle - Don't Repeat Yourself

**✅ ALWAYS extract duplicated code** into shared utilities, extension functions, or common modules.

```kotlin
// ❌ WRONG - Duplicated formatCurrency across multiple files
// InventoryScreen.kt
private fun formatCurrency(amount: Double): String {
    val wholePart = amount.toInt()
    val decimalPart = ((amount - wholePart) * 100).toInt()
    return "$$wholePart.${decimalPart.toString().padStart(2, '0')}"
}

// ShiftsScreen.kt
private fun formatCurrency(amount: Double): String {  // DUPLICATE!
    val wholePart = amount.toInt()
    val decimalPart = ((amount - wholePart) * 100).toInt()
    return "$$wholePart.${decimalPart.toString().padStart(2, '0')}"
}

// ✅ CORRECT - Extract to shared utility
// shared/src/commonMain/kotlin/com/vibely/pos/shared/utils/FormatUtils.kt
object FormatUtils {
    fun formatCurrency(amount: Double): String {
        val wholePart = amount.toInt()
        val decimalPart = ((amount - wholePart) * 100).toInt()
        return "$$wholePart.${decimalPart.toString().padStart(2, '0')}"
    }
}

// Usage in screens
import com.vibely.pos.shared.utils.FormatUtils.formatCurrency
```

**When you find duplicated code:**
1. Extract to appropriate shared location (shared/utils, domain/extensions, ui/components)
2. Consider if it's domain logic (→ shared module) or UI helper (→ ui/utils)
3. Update all usages to reference the shared implementation
4. Run tests to ensure behavior is preserved

**Common extraction locations:**
- **shared/utils/** - Business logic utilities (formatting, validation, calculations)
- **composeApp/ui/utils/** - UI-only helpers (color manipulation, layout)
- **shared/domain/extensions/** - Extension functions for domain entities
- **composeApp/ui/components/** - Reusable UI components

---

### 2. Architecture Principles - Clean Architecture & Unidirectional Data Flow

**✅ ALWAYS follow these architectural principles:**

#### Single Responsibility Principle (SRP)
Each class/function should have ONE reason to change.

```kotlin
// ❌ WRONG - God class doing everything
class ProductManager {
    fun getProducts(): List<Product> { /* fetch from API */ }
    fun validateProduct(product: Product): Boolean { /* validation */ }
    fun formatPrice(price: Double): String { /* formatting */ }
    fun calculateDiscount(price: Double): Double { /* business logic */ }
}

// ✅ CORRECT - Separated concerns
class ProductRepository {
    suspend fun getProducts(): Result<List<Product>> { /* data layer */ }
}

class ProductValidator {
    fun validate(product: Product): ValidationResult { /* domain logic */ }
}

object FormatUtils {
    fun formatCurrency(amount: Double): String { /* presentation */ }
}

class DiscountCalculator {
    fun calculate(price: Double, discountRate: Double): Double { /* domain logic */ }
}
```

#### Unidirectional Data Flow (UDF)
Data flows in ONE direction: UI → ViewModel → Repository → DataSource

```kotlin
// ✅ CORRECT - Unidirectional flow
@Composable
fun ProductScreen(viewModel: ProductViewModel) {
    val state by viewModel.state.collectAsState()
    
    // 1. User action flows DOWN
    Button(onClick = { viewModel.loadProducts() }) {
        Text("Refresh")
    }
    
    // 2. State flows UP from ViewModel
    when (state) {
        is Loading -> LoadingIndicator()
        is Success -> ProductList(state.products)
        is Error -> ErrorMessage(state.message)
    }
}

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _state = MutableStateFlow<ProductState>(Loading)
    val state: StateFlow<ProductState> = _state.asStateFlow()
    
    // 3. ViewModel calls Repository
    fun loadProducts() {
        viewModelScope.launch {
            _state.value = Loading
            when (val result = repository.getProducts()) {
                is Success -> _state.value = Success(result.data)
                is Error -> _state.value = Error(result.message)
            }
        }
    }
}
```

#### Layer Separation
**UI Layer** (composeApp) → **Domain Layer** (shared/domain) → **Data Layer** (shared/data)

```kotlin
// ❌ WRONG - UI directly accessing data source
@Composable
fun ProductScreen() {
    val products = remember { mutableStateOf<List<Product>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        // UI should NOT know about HTTP clients or databases!
        val response = httpClient.get("/api/products")
        products.value = response.body()
    }
}

// ✅ CORRECT - Proper layer separation
// UI → ViewModel
@Composable
fun ProductScreen(viewModel: ProductViewModel) {
    val state by viewModel.state.collectAsState()
    ProductList(state.products)
}

// ViewModel → Use Case (optional) → Repository
class ProductViewModel(private val getProductsUseCase: GetProductsUseCase) {
    fun loadProducts() = viewModelScope.launch {
        getProductsUseCase().collect { result ->
            _state.value = result
        }
    }
}

// Use Case → Repository
class GetProductsUseCase(private val repository: ProductRepository) {
    operator fun invoke(): Flow<Result<List<Product>>> = flow {
        emit(Loading)
        emit(repository.getProducts())
    }
}

// Repository → Data Source
class ProductRepositoryImpl(
    private val remoteDataSource: RemoteProductDataSource,
    private val localDataSource: LocalProductDataSource
) : ProductRepository {
    override suspend fun getProducts(): Result<List<Product>> {
        return remoteDataSource.getProducts()
            .map { dtos -> dtos.map { it.toDomain() } }
    }
}
```

**Layer rules:**
- ❌ UI must NOT import data sources (HttpClient, database DAOs)
- ❌ Domain must NOT import UI (Composables, ViewModels) or framework code (Ktor, Supabase)
- ❌ Data sources must NOT contain business logic
- ✅ Dependencies flow inward: UI → Domain ← Data

---

### 3. Preserve KDoc Documentation

**✅ ALWAYS preserve KDoc** on public API classes, functions, and properties.

```kotlin
// ✅ CORRECT - Keep existing KDoc
/**
 * Authenticates a user with email and password.
 * 
 * @param email User's email address
 * @param password User's password
 * @return Result containing authenticated user or error
 */
suspend fun login(email: String, password: String): Result<User>

// ❌ WRONG - Removing KDoc will fail Detekt checks
suspend fun login(email: String, password: String): Result<User>
```

**Why?** Detekt enforces `UndocumentedPublicClass`, `UndocumentedPublicFunction`, and `UndocumentedPublicProperty` rules. Missing documentation = build failure.

**When modifying code:**
- Read existing KDoc before making changes
- Update KDoc if behavior changes
- Add KDoc if creating new public APIs
- Never delete KDoc to "simplify" code

---

### 2. Verify Library Status Before Use

**✅ ALWAYS verify libraries are NOT deprecated** before using them.

**BAD EXAMPLE - kotlinx.datetime.Clock:**
```kotlin
// ❌ WRONG - kotlinx.datetime.Clock is DEPRECATED
import kotlinx.datetime.Clock
val now = Clock.System.now()
```

**CORRECT - kotlin.time:**
```kotlin
// ✅ CORRECT - Use kotlin.time from stdlib
import kotlin.time.Clock
val now = Clock.System.now()
```

**Before using any external library:**
1. Check the library's GitHub repo for deprecation notices
2. Check the library's documentation for "deprecated" warnings
3. Search for "migration guide" or "replacement" if unsure
4. When in doubt, search for recent examples in popular open-source projects

**Common deprecated libraries to avoid:**
- `kotlinx.datetime.Clock` → Use `kotlin.time.Clock`
- Old Ktor plugins → Check Ktor 3.x migration guide
- Legacy Compose APIs → Check Compose Multiplatform docs

---

### 3. Boy Scout Rule - Leave Code Better Than You Found It

**✅ ALWAYS clean code as you touch it** - Small improvements, no big refactors.

**What to improve while working:**
- Add missing KDoc to functions you're modifying
- Extract magic numbers to named constants
- Simplify complex boolean expressions
- Remove unused imports
- Fix obvious typos in variable names
- Apply Spotless formatting to files you touch

**What NOT to do:**
- ❌ Large-scale refactoring unrelated to your task
- ❌ Changing architecture patterns
- ❌ Renaming variables across multiple files
- ❌ Rewriting working code for "style"

**Example:**
```kotlin
// BEFORE - Found this while fixing a bug
fun calculateTotal(items: List<Item>): Double {
    var t = 0.0  // Bad name
    for(i in items) {
        t = t + i.price * i.quantity  // Verbose
    }
    return t
}

// AFTER - Fixed bug AND applied Boy Scout rule
/**
 * Calculates total cost for all items including quantities.
 */
fun calculateTotal(items: List<Item>): Double {
    return items.sumOf { it.price * it.quantity }
}
```

**Balance:** Improve what you touch, but stay focused on your primary task.

---

### 4. Efficient Gradle Usage

**✅ DO NOT run Gradle tasks repeatedly** to see output.

**CORRECT workflow:**
```bash
# Run once with all checks
./gradlew assemble test spotlessApply detekt

# If errors, read reports (do NOT re-run)
cat backend/build/test-results/test/*.xml
cat */build/reports/detekt/detekt.xml

# Or use grep/tail from the start
./gradlew test 2>&1 | grep -A 5 "FAILED"
./gradlew detekt 2>&1 | tail -50
```

**WRONG workflow:**
```bash
# ❌ Running tasks multiple times wastes time
./gradlew test
# See error, run again to read it
./gradlew test
# Still confused, run again with --info
./gradlew test --info
```

**Why?** Gradle builds are slow. Running multiple times wastes developer time and CI resources.

**Golden rule:** Run once, read the XML/HTML reports or use grep/tail for filtering.

---

## 🎨 Design System

### Theme & Colors

**Grayscale-dominant theme** - Reserve green ONLY for success indicators.

```kotlin
// ✅ CORRECT - Grayscale for primary UI
MaterialTheme(
    colorScheme = if (darkMode) darkColorScheme(
        primary = Color(0xFFE0E0E0),
        secondary = Color(0xFFBDBDBD),
        background = Color(0xFF121212),
    ) else lightColorScheme(
        primary = Color(0xFF424242),
        secondary = Color(0xFF757575),
        background = Color(0xFFFAFAFA),
    )
)

// ✅ Green only for success states
if (saveSuccessful) {
    Icon(Icons.Default.Check, tint = Color(0xFF4CAF50))
}

// ❌ WRONG - Don't use green as primary color
Button(colors = ButtonDefaults.buttonColors(
    containerColor = Color(0xFF4CAF50) // ❌ No green buttons
))
```

**Icon standards:**
- Use **FontAwesome monochrome icons** (NOT emoji icons)
- Match icon color to theme (gray in light mode, light gray in dark mode)
- Reserve color only for semantic states (red = error, green = success, yellow = warning)

---

## 🏗️ Architecture Patterns

### Domain Layer (Clean Architecture)

```kotlin
// Use Result<T> sealed class for error handling
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Exception) : Result<Nothing>()
}

// Value objects for validation
@JvmInline
value class Email(val value: String) {
    init {
        require(value.matches(EMAIL_REGEX))
    }
}

// Business logic in use cases
class LoginUseCase(private val authRepo: AuthRepository) {
    suspend operator fun invoke(email: Email, password: String): Result<User> {
        // Domain logic here
    }
}
```

### Data Layer

```kotlin
// DTOs for serialization (API contracts)
@Serializable
data class UserDTO(val id: String, val email: String)

// Entities for domain models (business objects)
data class User(val id: UserId, val email: Email)

// Mappers for conversion
fun UserDTO.toDomain() = User(
    id = UserId(id),
    email = Email(email)
)
```

### Dependency Injection (Koin 4.0.0)

```kotlin
// Module definition
val dataModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    factory { LoginUseCase(get()) }
}

// Usage in Compose
@Composable
fun LoginScreen() {
    val viewModel: LoginViewModel = koinViewModel()
}
```

---

## 📱 Navigation (Navigation3 - KMP Ready)

**Use Navigation3 version 1.1.0-alpha04** (NOT legacy androidx.navigation).

```kotlin
// ✅ CORRECT - Navigation3
import org.jetbrains.androidx.navigation3.navigation3.*

val navController = rememberNavController()

NavHost(navController, startDestination = "dashboard") {
    composable("dashboard") { DashboardScreen() }
    composable("products") { ProductsScreen() }
}

// ❌ WRONG - Old androidx.navigation
import androidx.navigation.compose.*
```

**Why?** Navigation3 is Kotlin Multiplatform ready and will be the standard going forward.

---

## 🧪 Testing Standards

### Unit Tests

**Minimum 70% code coverage for new code.**

```kotlin
@Test
fun `login with valid credentials returns success`() {
    // Arrange
    val useCase = LoginUseCase(mockRepository)
    
    // Act
    val result = runBlocking {
        useCase(Email("test@example.com"), "password123")
    }
    
    // Assert
    assertTrue(result is Result.Success)
    assertEquals("test@example.com", (result as Result.Success).data.email.value)
}
```

**Test naming:**
- Use backticks for readable test names
- Format: `function name with scenario returns expected result`
- Example: `` `login with invalid email returns validation error` ``

### Authentication Testing (Hybrid Approach)

**80% Route Tests** - Fast tests without JWT overhead:
```kotlin
@Test
fun `GET products returns list`() = testApplication {
    application {
        configureTestAuthentication()  // Mock auth
        routing { productRoutes() }
    }
    
    client.get("/api/products") {
        bearerAuth("test-user-123")  // Simple test token
    }.apply {
        assertEquals(HttpStatusCode.OK, status)
    }
}
```

**15% Service Tests** - Unit test JWT logic in isolation

**5% Integration Tests** - Full JWT flow verification

---

## 🛠️ Code Quality Tools

### Spotless (Auto-formatting)

```bash
# Check formatting
./gradlew spotlessCheck

# Fix formatting
./gradlew spotlessApply
```

**Key rules:**
- Maximum line length: **150 characters** (Kotlin files)
- Indentation: **4 spaces**
- Files must end with newline
- No trailing whitespace

### Detekt (Static Analysis)

```bash
# Run static analysis
./gradlew detekt
```

**Key rules (warnings = errors):**
- Max cyclomatic complexity: 10
- Max function length: 40 lines
- No magic numbers (extract to constants)
- No unused code
- **Public APIs must have KDoc**

### Pre-commit Hooks

Hooks automatically run on `git commit`:
- `spotlessCheck` - Formatting validation
- `detekt` - Static analysis

**To skip (use sparingly):**
```bash
git commit --no-verify -m "Emergency hotfix"
```

---

## 📏 Code Style Guidelines

### Kotlin Conventions

```kotlin
// ✅ Use meaningful names
val activeUsers = users.filter { it.isActive }

// ❌ Avoid cryptic abbreviations
val au = u.filter { it.ia }

// ✅ Prefer val over var
val userName = user.name

// ❌ Avoid mutable state when possible
var userName = user.name

// ✅ Use extension functions
fun String.isValidEmail() = matches(EMAIL_REGEX)

// ✅ Prefer sealed classes for state
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: List<Product>) : UiState()
    data class Error(val message: String) : UiState()
}
```

### Import Style

**✅ ALWAYS use imports** - Never use fully qualified names in code:

```kotlin
// ✅ CORRECT
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation

install(ContentNegotiation) { ... }

// ❌ WRONG - Don't use full namespace
install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) { ... }
```

### Compose Best Practices

```kotlin
// ✅ Keep composables small and focused
@Composable
fun ProductCard(product: Product) {
    Card {
        ProductImage(product.imageUrl)
        ProductDetails(product)
    }
}

// ✅ Extract reusable components
@Composable
fun LoadingIndicator() {
    CircularProgressIndicator()
}

// ✅ Use remember for expensive calculations
val sortedProducts = remember(products, sortOrder) {
    products.sortedBy { it.name }
}

// ✅ Handle all UI states
when (uiState) {
    is UiState.Loading -> LoadingIndicator()
    is UiState.Success -> ProductList(uiState.data)
    is UiState.Error -> ErrorMessage(uiState.message)
}
```

---

## 🚫 Anti-Patterns (Never Do This)

### Type Safety Violations

```kotlin
// ❌ NEVER suppress type errors
val user = response.body() as User  // Unsafe cast
val items: List<Any> = products  // Type erasure

// ✅ CORRECT - Handle types properly
val user = response.body<User>()
val items: List<Product> = products
```

### Error Handling

```kotlin
// ❌ NEVER use empty catch blocks
try {
    riskyOperation()
} catch (e: Exception) {
    // Silent failure
}

// ✅ CORRECT - Handle or log errors
try {
    riskyOperation()
} catch (e: Exception) {
    logger.error("Operation failed", e)
    return Result.Error(e)
}
```

### Testing

```kotlin
// ❌ NEVER delete failing tests to make CI pass
// @Test  // Commented out because it fails
fun `important feature works`() { ... }

// ✅ CORRECT - Fix the test or fix the code
@Test
fun `important feature works`() {
    // Fixed implementation
}
```

### Code Smells

- **Magic Numbers**: Extract to named constants
- **Long Functions**: Break into smaller functions (max 40 lines)
- **Deep Nesting**: Extract to helper functions (max 4 levels)
- **God Classes**: Follow Single Responsibility Principle
- **TODO/FIXME**: Fix before committing (or create tracked issues)

---

## 📋 Pre-Flight Checklist

**Before committing code, verify:**

- [ ] All existing KDoc preserved on modified functions
- [ ] No deprecated libraries introduced (verified status)
- [ ] Boy Scout improvements applied to touched code
- [ ] Tests added/updated for changes
- [ ] `./gradlew spotlessApply` run successfully
- [ ] `./gradlew detekt` passes with zero warnings
- [ ] `./gradlew test` passes (or pre-existing failures documented)
- [ ] Design system colors respected (grayscale + semantic colors only)
- [ ] No type safety violations (`as any`, `!!`, unsafe casts)
- [ ] No empty catch blocks or silent error swallowing

---

## 🤖 For AI Agents

**If you are an AI agent working on this codebase:**

1. **Read this file FIRST** before making any code changes
2. **Read `.claude/CODING_STANDARDS.md`** for workflow-specific rules
3. **Verify library status** using web search if unfamiliar with a dependency
4. **Preserve all existing KDoc** - treat it as sacred
5. **Apply Boy Scout rule** to code you touch
6. **Run Gradle tasks ONCE** and read reports
7. **Follow the Pre-Flight Checklist** before submitting changes

**When uncertain:**
- Search existing codebase for similar patterns
- Check `docs/ARCHITECTURE.md` for architectural decisions
- Consult maintainers rather than guessing

---

## 📚 Additional Resources

- [CONTRIBUTING.md](CONTRIBUTING.md) - Git workflow, PR process, commit conventions
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - System design and patterns
- [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) - Implementation roadmap
- [detekt.yml](detekt.yml) - Enforced static analysis rules
- [.editorconfig](.editorconfig) - Editor configuration

---

**Last Updated**: 2026-03-15  
**Maintained By**: Vibely POS Team
