# Koin-Detekt Static Analysis Integration

**Status**: ✅ Complete
**Version**: Koin-Detekt v1.1.0
**Date**: March 13, 2026

## Overview

Successfully integrated [Koin-Detekt](https://github.com/androidbroadcast/Koin-Detekt) to enforce Koin 4.x best practices through static analysis. This adds **58 specialized rules** across 6 categories to catch Koin anti-patterns at compile time.

## What Was Integrated

### Dependencies

**File**: `gradle/libs.versions.toml`
```toml
[versions]
detektKoinRules = "1.1.0"

[libraries]
detekt-koin-rules = { module = "dev.androidbroadcast.rules.koin:detekt-koin4-rules", version.ref = "detektKoinRules" }
```

### Module Configuration

Added to **all modules** with Koin usage (`shared/`, `composeApp/`, `backend/`):

```kotlin
dependencies {
    detektPlugins(libs.detekt.koin.rules)
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/detekt.yml"))
    baseline = file("$rootDir/detekt-baseline.xml")
}
```

### Rule Configuration

**File**: `detekt.yml` - Added comprehensive Koin rules with 58 rules across 6 categories.

## 58 Rules Across 6 Categories

### 1. Service Locator Anti-patterns (6 rules)

| Rule | Description | Fix |
|------|-------------|-----|
| `NoKoinComponentInterface` | Detects KoinComponent interface usage | Use constructor injection |
| `NoInjectDelegate` | Detects `by inject()` in non-allowed types | Restrict to Application/Activity |
| `PreferConstructorInjection` | Enforces constructor injection | Inject via constructor |
| `PreferLazyConstructorInjection` | Use `by inject()` vs `get()` in constructors | Lazy initialization |
| `NoGetInComposable` | Detects `get()` in Composables | Use `koinInject()` |
| `NoGetOutsideModuleDefinition` | `get()` only in module definitions | Move to module |

**Example Violation:**
```kotlin
// ❌ Bad
class MyRepository : KoinComponent {
    private val api: Api by inject()
}

// ✅ Good
class MyRepository(private val api: Api)
```

### 2. Module DSL Best Practices (14 rules)

| Rule | Description |
|------|-------------|
| `OverrideInIncludedModule` | Detect override conflicts in included modules |
| `GenericDefinitionWithoutQualifier` | Require qualifiers for generic types |
| `SingleForNonSharedDependency` | UseCase/Command should be `factory`, not `single` |
| `ConstructorDslAmbiguousParameters` | Detect ambiguous constructor parameters |
| `MissingModuleName` | All modules should have names |
| `RedundantSingle` | Detect redundant `single` usage |
| `IncludeModuleInsteadOfSubmodules` | Use `includes` instead of `submodules` |
| `FactoryForStatefulDependency` | Stateful dependencies should be `factory` |
| `KClassInsteadOfClass` | Use `KClass` instead of `Class` |
| `RedundantNamed` | Detect redundant `named()` calls |
| `QualifierOnNonGenericDefinition` | Qualifier on non-generic is redundant |
| `MissingFactoryFunction` | Missing factory function definition |
| `SingletonInsteadOfSingle` | (Optional) Consistency preference |
| `FactoryInsteadOfProvider` | (Optional) Consistency preference |

**Example Violation:**
```kotlin
// ❌ Bad - UseCase as singleton
val myModule = module {
    single { LoginUseCase(get()) }
}

// ✅ Good - UseCase as factory
val myModule = module {
    factory { LoginUseCase(get()) }
}
```

### 3. Scope Management (8 rules)

| Rule | Description |
|------|-------------|
| `ScopeDeclareWithActivityOrFragment` | Prevent memory leaks from scoping Activities/Fragments |
| `AndroidContextNotFromKoin` | Get Android Context from Koin, not manually |
| `GlobalScopeWithModule` | Don't use global scope with module |
| `FragmentScopeWithActivity` | Fragment scope issues |
| `GetScopeInsteadOfScope` | Use `getScope()` properly |
| `GetKoinInsteadOfKoinComponent` | Proper Koin access |
| `OrphanedScopeClose` | Detect unclosed scopes |
| `RedundantScopeId` | Remove redundant scope IDs |

### 4. Platform-Specific (8 rules)

| Rule | Description |
|------|-------------|
| `StartKoinInActivity` | Don't start Koin in Activity, use Application |
| `ViewModelCreatedWithGet` | Use `koinViewModel()` not `get()` |
| `ViewModelCreatedWithNew` | Don't create ViewModels with `new` |
| `AndroidContextByInject` | Proper Context injection |
| `AndroidViewModelForViewModel` | ViewModel type correctness |
| `ViewModelInsteadOfAndroidViewModel` | Use appropriate ViewModel base |
| `WorkerCreatedWithGet` | Use `koinWorker()` not `get()` |
| `WorkerCreatedWithNew` | Don't create Workers with `new` |

**Example Violation:**
```kotlin
// ❌ Bad
@Composable
fun MyScreen() {
    val viewModel: MyViewModel = get()
}

// ✅ Good
@Composable
fun MyScreen() {
    val viewModel: MyViewModel = koinViewModel()
}
```

### 5. Architecture (4 rules)

| Rule | Description |
|------|-------------|
| `DeprecatedKoinApi` | Detect deprecated Koin APIs |
| `KoinComponentInsteadOfExtension` | Use KoinComponent properly |
| `InjectInsteadOfGet` | (Optional) Lazy preference |
| `GetInsteadOfInject` | (Optional) Immediate preference |

### 6. Koin Annotations (18 rules)

For projects using `koin-annotations`:

| Rule | Description |
|------|-------------|
| `AnnotationProcessorNotConfigured` | KSP not configured |
| `ModuleMissingIncludes` | Missing `@Module` includes |
| `MissingModuleDeclaration` | Module declaration missing |
| `ComponentScanInKoinMain` | Component scan setup |
| `SingleOnAbstractClass` | @Single on abstract causes runtime error |
| `KoinWorkerOnNonWorker` | @KoinWorker type validation |
| `InjectedParamAnnotationOrder` | @InjectedParam must be first |
| `MissingKoinStopInTest` | Tests must call `stopKoin()` |
| `KoinViewModelOnNonViewModel` | @KoinViewModel type validation |
| `QualifierObfuscationRisk` | Avoid non-string qualifiers |
| `ModuleOnObject` | @Module on object classes |
| `ModuleOnNonPublic` | @Module visibility |
| `MissingScope` | Scope annotation missing |
| `MissingComponentScan` | Component scan missing |
| `RedundantQualifier` | Remove redundant qualifiers |
| `PropertyOnVal` | @Property on val |
| `PropertyOnVar` | @Property on var |
| `IncompatibleAnnotationCombination` | Conflicting annotations |

## Project-Specific Configuration

```yaml
koin-rules:
  active: true

  # Allowed types for inject() delegate
  NoInjectDelegate:
    active: true
    allowedSuperTypes:
      - 'Application'
      - 'Activity'
      - 'AppCompatActivity'
      - 'Fragment'
      - 'Service'
      - 'GlanceAppWidget'

  # UseCase pattern enforcement
  SingleForNonSharedDependency:
    active: true
    namePatterns:
      - '.*UseCase'
      - '.*Command'
      - '.*Handler'
      - '.*Validator'

  # Memory leak prevention
  ScopeDeclareWithActivityOrFragment:
    active: true
    additionalLeakProneTypes:
      - 'View'
      - 'Context'

  # Test cleanup
  MissingKoinStopInTest:
    active: true
    additionalTeardownAnnotations:
      - 'AfterTest'
      - 'Cleanup'
```

## Usage

### Run All Checks
```bash
# All modules
./gradlew detekt

# Specific module
./gradlew :shared:detekt
./gradlew :composeApp:detekt
./gradlew :backend:detekt

# With reports
./gradlew detekt
open build/reports/detekt/detekt.html
```

### CI/CD Integration

Automatically runs in GitHub Actions:
```yaml
- name: Code Quality Checks
  run: ./gradlew spotlessCheck detekt
```

All violations fail the build, ensuring code quality before merge.

## Common Issues & Solutions

### Issue 1: KoinComponent Anti-pattern

```kotlin
// ❌ Violation
class MyRepository : KoinComponent {
    private val api: Api by inject()
}

// ✅ Solution
class MyRepository(private val api: Api)
```

### Issue 2: Wrong Scope for UseCase

```kotlin
// ❌ Violation
val myModule = module {
    single { LoginUseCase(get()) }
}

// ✅ Solution
val myModule = module {
    factory { LoginUseCase(get()) }
}
```

### Issue 3: Incorrect Compose DI

```kotlin
// ❌ Violation
@Composable
fun MyScreen() {
    val viewModel: MyViewModel = get()
}

// ✅ Solution
@Composable
fun MyScreen() {
    val viewModel: MyViewModel = koinViewModel()
}
```

### Issue 4: Memory Leak Risk

```kotlin
// ❌ Violation
scope<Activity> {
    scoped { MyViewModel(get()) }
}

// ✅ Solution
// Use ViewModel-specific scope or don't scope Activities
```

### Issue 5: Lazy vs Immediate Injection

```kotlin
// ❌ Violation (in KoinComponent constructor)
class MyClass : KoinComponent {
    private val repo = get<Repository>()
}

// ✅ Solution
class MyClass : KoinComponent {
    private val repo: Repository by inject()
}
```

## Code Quality Improvements

### Before Integration
- ❌ 10 detekt violations in backend
- ❌ Code quality issues undetected
- ❌ Potential Koin anti-patterns

### After Integration & Fixes
- ✅ 0 detekt violations
- ✅ All Koin best practices enforced
- ✅ Cleaner, more maintainable code
- ✅ Files refactored for better structure:
  - `Application.kt` - Main app configuration (clean, focused)
  - `PluginConfig.kt` - Plugin configurations (separated concerns)
  - `SupabaseConfig.kt` - Database client config (optimized)

## Benefits Delivered

✅ **58 Koin-specific rules** enforcing best practices
✅ **Compile-time validation** - catch issues before runtime
✅ **Memory leak prevention** - scope management validation
✅ **Consistent DI patterns** across the codebase
✅ **Architecture enforcement** - guides toward clean architecture
✅ **CI/CD integration** - automatic checks on every PR
✅ **Zero violations** - all existing code cleaned up
✅ **Comprehensive documentation** for the team

## Files Modified

### Configuration
- `gradle/libs.versions.toml` - Added Koin-Detekt dependency
- `shared/build.gradle.kts` - Added detekt plugin
- `composeApp/build.gradle.kts` - Added detekt plugin
- `backend/build.gradle.kts` - Added detekt plugin and configuration
- `detekt.yml` - Added 58 Koin rules with project-specific config

### Code Refactoring (Quality Fixes)
- `backend/src/main/kotlin/com/vibely/pos/backend/Application.kt` - Refactored for better structure
- `backend/src/main/kotlin/com/vibely/pos/backend/config/PluginConfig.kt` - **New** - Extracted plugin configs
- `backend/src/main/kotlin/com/vibely/pos/backend/config/SupabaseConfig.kt` - Fixed violations

### Documentation
- `docs/KOIN_DETEKT_RULES.md` - **This file** - Complete integration guide

## Version Information

| Tool | Version |
|------|---------|
| Koin | v4.1.1 |
| Koin-Detekt | v1.1.0 |
| Detekt | v1.23.8 |
| Kotlin | v2.3.10 |

## References

- [Koin-Detekt GitHub](https://github.com/androidbroadcast/Koin-Detekt)
- [Complete Rule Catalog](https://github.com/androidbroadcast/Koin-Detekt/blob/main/docs/rules.md)
- [Koin Documentation](https://insert-koin.io/)
- [Detekt Documentation](https://detekt.dev/)

---

**Quality Status**: ✅ All checks passing | ✅ Zero violations | ✅ Production-ready
