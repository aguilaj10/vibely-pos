# Coding Standards for Vibely POS

## Critical Requirements

### ⚠️ DO NOT USE `kotlinx.datetime.Clock` - IT'S DEPRECATED

**CORRECT**: Use `kotlin.time.Clock` from the Kotlin standard library
```kotlin
import kotlin.time.Clock

val now = Clock.System.now()
```

**INCORRECT**: ❌ DO NOT use `kotlinx.datetime.Clock`
```kotlin
// ❌ WRONG - This is deprecated!
import kotlinx.datetime.Clock
```

### Why?
- `kotlinx.datetime.Clock` is deprecated
- `kotlin.time.Clock` is the standard library replacement
- All new code must use `kotlin.time.Clock`

---

## General Standards

### Code Style
- **ALWAYS use imports, NEVER use fully qualified names in code**
  ```kotlin
  // ✅ CORRECT
  import io.ktor.client.plugins.contentnegotiation.ContentNegotiation

  install(ContentNegotiation) { ... }

  // ❌ WRONG - Don't use full namespace
  install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) { ... }
  ```

### Domain Layer
- Use `Result<T>` sealed class for error handling
- Use value objects for validation (Email, Phone, Money, etc.)
- Follow existing domain patterns in `shared/domain/`
- Business logic belongs in use cases

### Data Layer
- DTOs for serialization
- Mappers for DTO ↔ Entity conversion
- Repository pattern for data access

### Dependency Injection
- Use Koin 4.0.0
- Register modules in appropriate module files

### Testing
- Unit tests for use cases and domain logic
- Integration tests for repositories
- Target: >80% code coverage

**Authentication Testing Strategy (Hybrid Approach):**
1. **80% Route Tests**: Use `configureTestAuthentication()` for fast tests
   - Tests business logic without JWT overhead
   - Use bearer tokens like `"test-user-123"` in tests
2. **15% Service Tests**: Unit test JWT logic in `TokenService`
   - Test token generation, validation, expiration
   - No Ktor dependencies
3. **5% Integration Tests**: Test real JWT flow in `AuthenticationIntegrationTest`
   - Verify actual token generation and validation
   - High confidence in production behavior

Example:
```kotlin
@Test
fun `test protected route`() = testApplication {
    application {
        configureTestAuthentication()  // Test auth provider
        routing { myRoutes() }
    }

    client.get("/protected") {
        bearerAuth("test-user-123")  // Simple test token
    }
}
```

### Code Quality
- Run Spotless for formatting
- Pass Detekt checks
- No compiler warnings
- No TODO/FIXME in production code

---

**Last Updated**: 2026-03-13
