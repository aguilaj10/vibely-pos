# AI Agent Coding Standards for Vibely POS

> **🚨 MANDATORY FOR ALL AI AGENTS**: Read this file AND [/CODING_STANDARDS.md](../CODING_STANDARDS.md) before making any changes.

This file contains **workflow-specific rules for AI agents**. For general coding standards, see the root [CODING_STANDARDS.md](../CODING_STANDARDS.md).

---

## 🤖 Agent Operating Protocol

### 1. Pre-Work Checklist (ALWAYS)

Before starting ANY task:

- [ ] Read [/CODING_STANDARDS.md](../CODING_STANDARDS.md) to understand project standards
- [ ] Search codebase for similar patterns (use `explore` agent or grep)
- [ ] Verify any unfamiliar libraries are NOT deprecated (use `librarian` agent or web search)
- [ ] Identify which files will be modified
- [ ] Note any KDoc that must be preserved

### 2. During Work

- [ ] Preserve ALL existing KDoc on public APIs
- [ ] Apply Boy Scout rule - small improvements to code you touch
- [ ] Run Gradle tasks ONCE (use grep/tail for filtering, do NOT re-run)
- [ ] Follow existing codebase patterns (unless they're clearly wrong)
- [ ] Update tests for any behavior changes

### 3. Pre-Submission Checklist

- [ ] `./gradlew spotlessApply` - Auto-fix formatting
- [ ] `./gradlew detekt` - Pass static analysis (zero warnings)
- [ ] `./gradlew test` - Tests pass (or document pre-existing failures)
- [ ] All KDoc preserved on modified functions
- [ ] No deprecated libraries introduced
- [ ] No type safety violations (`as any`, `!!`, unsafe casts)

---

## 🔴 CRITICAL: Gradle Workflow

### After Making Code Changes:

**Run ONLY this single command:**
```bash
./gradlew assemble test spotlessApply detekt
```

**⚠️ IMPORTANT:**
- ✅ Use `assemble` (NOT `build`) - Faster, sufficient for development
- ✅ Run all quality checks in one command
- ❌ NEVER run full `build` unless explicitly required
- ❌ NEVER run tasks separately or multiple times

### Reading Build Output / Errors:

**Use grep/tail from the START - DO NOT re-run gradlew:**

```bash
# ✅ CORRECT - Filter output in first run
./gradlew test 2>&1 | grep -A 5 "FAILED"
./gradlew detekt 2>&1 | tail -50

# ✅ CORRECT - Read existing reports
cat backend/build/test-results/test/*.xml
cat */build/reports/detekt/detekt.xml
find . -name "*.xml" -path "*/test-results/*" -exec grep -l "failure" {} \;

# ❌ WRONG - Running multiple times
./gradlew test        # First run
./gradlew test        # Seeing errors, running again
./gradlew test --info # Still confused, running third time
```

**Why this matters:**
- Gradle builds can take 1-2 minutes
- Re-running wastes time and CI resources
- Reports are ALREADY THERE after first run

**Golden Rule:** Run once with proper filtering, or read the XML/HTML reports.

---

## 🚨 Critical Requirements

### 1. Preserve KDoc Documentation

**DO NOT REMOVE KDOC** from public API classes, functions, or properties.

```kotlin
// ✅ CORRECT - Preserve existing KDoc
/**
 * Authenticates a user with email and password.
 * 
 * @param email User's email address
 * @param password User's password
 * @return Result containing authenticated user or error
 */
suspend fun login(email: String, password: String): Result<User> {
    // Your changes here
}

// ❌ WRONG - Removing KDoc will fail detekt
suspend fun login(email: String, password: String): Result<User> {
    // Your changes here
}
```

**Why?** Detekt enforces `UndocumentedPublicClass`, `UndocumentedPublicFunction`, and `UndocumentedPublicProperty`. Missing KDoc = build failure.

**Before modifying any function:**
1. Read existing KDoc
2. Understand documented behavior
3. Preserve KDoc in your edits
4. Update KDoc if behavior changed

---

### 2. Verify Libraries Are NOT Deprecated

**ALWAYS verify library status** before using external dependencies.

**Known deprecated libraries (DO NOT USE):**
- `kotlinx.datetime.Clock` → Use `kotlin.time.Clock` instead

**How to verify:**
1. Check library GitHub repo for deprecation notices
2. Search for "migration guide" or "deprecated" in docs
3. Use `librarian` agent to search for recent usage examples
4. When in doubt, search popular KMP projects for current patterns

**Example - Clock API:**
```kotlin
// ❌ WRONG - kotlinx.datetime.Clock is DEPRECATED
import kotlinx.datetime.Clock
val now = Clock.System.now()

// ✅ CORRECT - Use kotlin.time.Clock from stdlib
import kotlin.time.Clock
val now = Clock.System.now()
```

**If you're uncertain about a library:**
- Fire a `librarian` agent to research it
- Search the existing codebase for usage patterns
- Check the library's GitHub repo and docs
- ASK before introducing new dependencies

---

### 3. Boy Scout Rule - Clean As You Go

**Leave code better than you found it** - Small improvements only, no big refactors.

**What to improve while working:**
- ✅ Add missing KDoc to functions you're modifying
- ✅ Extract magic numbers to named constants
- ✅ Simplify complex boolean expressions
- ✅ Remove unused imports
- ✅ Fix obvious typos in names
- ✅ Apply Spotless formatting

**What NOT to do:**
- ❌ Large-scale refactoring unrelated to your task
- ❌ Changing architecture patterns
- ❌ Renaming variables across multiple files
- ❌ Rewriting working code just for "style"

**Example:**
```kotlin
// BEFORE - Found while fixing a bug
fun calculate(items: List<Item>): Double {
    var t = 0.0
    for(i in items) {
        t = t + i.p * i.q  // Bad names
    }
    return t
}

// AFTER - Fixed bug AND improved readability
/**
 * Calculates total cost for all items including quantities.
 */
fun calculateTotal(items: List<Item>): Double {
    return items.sumOf { it.price * it.quantity }
}
```

**Balance:** Improve what you touch, but stay focused on your primary task.

---

## 🔍 Search & Exploration Strategy

### When to Use Direct Tools

Use grep/glob/read when:
- You know exactly what to search
- Single keyword/pattern suffices
- Known file location
- Simple file reads

### When to Use Explore Agent

Use `explore` agent (background) when:
- Multiple search angles needed
- Unfamiliar module structure
- Cross-layer pattern discovery
- Need to understand architectural patterns

```typescript
// ✅ CORRECT - Fire in background, continue working
task(
    subagent_type="explore",
    run_in_background=true,
    load_skills=[],
    description="Find auth patterns",
    prompt="Find all authentication implementations..."
)
// Continue working immediately
```

### When to Use Librarian Agent

Use `librarian` agent (background) when:
- Researching external libraries/frameworks
- Verifying if APIs are deprecated
- Finding official documentation
- Looking for OSS implementation examples

```typescript
// ✅ CORRECT - Research external resources
task(
    subagent_type="librarian",
    run_in_background=true,
    load_skills=[],
    description="Verify JWT library status",
    prompt="Check if kotlinx.jwt is deprecated..."
)
```

### Parallel Execution (MANDATORY)

**Run independent searches in parallel:**

```typescript
// ✅ CORRECT - Multiple parallel background tasks
task(subagent_type="explore", run_in_background=true, load_skills=[], ...)
task(subagent_type="explore", run_in_background=true, load_skills=[], ...)
task(subagent_type="librarian", run_in_background=true, load_skills=[], ...)
// Continue working, system will notify on completion
```

---

## 🎨 Design System (Agent-Specific Notes)

### Colors

**Grayscale theme** - Green ONLY for success indicators.

When generating UI code:
```kotlin
// ✅ Grayscale primary colors
primary = Color(0xFF424242)      // Dark gray
secondary = Color(0xFF757575)    // Medium gray
background = Color(0xFFFAFAFA)   // Light gray

// ✅ Green only for success
successColor = Color(0xFF4CAF50)
```

### Icons

**Use FontAwesome monochrome icons** - NO emoji icons.

```kotlin
// ✅ CORRECT - FontAwesome
Icon(FontAwesomeIcons.Solid.User, contentDescription = "User")

// ❌ WRONG - Emoji icons
Text("👤")  // Don't use emoji as icons
```

---

## 🏗️ Architecture Patterns (Quick Reference)

### Domain Layer
- Use `Result<T>` sealed class for error handling
- Value objects for validation (Email, Phone, etc.)
- Business logic in use cases

### Data Layer
- DTOs for serialization
- Entities for domain models
- Mappers for conversion

### Navigation
- Use **Navigation3 (1.1.0-alpha04)** - NOT androidx.navigation
- KMP-ready, future-proof choice

---

## 🧪 Testing Strategy (Agent-Specific)

### When Adding Tests

**Authentication tests use hybrid approach:**
- 80% route tests with mock auth (`configureTestAuthentication()`)
- 15% service tests (unit test JWT logic)
- 5% integration tests (full JWT flow)

```kotlin
// ✅ Preferred for most route tests
@Test
fun `GET products returns list`() = testApplication {
    application {
        configureTestAuthentication()  // Mock auth
        routing { productRoutes() }
    }
    
    client.get("/api/products") {
        bearerAuth("test-user-123")  // Simple token
    }
}
```

### Running Tests

```bash
# Run once, read reports if failures
./gradlew test 2>&1 | tee test-output.log

# Read specific test results
cat backend/build/test-results/test/TEST-*.xml
```

---

## 🚫 Anti-Patterns (Agent-Specific)

### Type Safety
```kotlin
// ❌ NEVER suppress type errors
val user = data as User
val items: Any = products

// ✅ Handle types properly
val user = data as? User ?: return Error("Invalid type")
```

### Error Handling
```kotlin
// ❌ NEVER silent failures
try { risky() } catch (e: Exception) {}

// ✅ Log or propagate errors
try { risky() } catch (e: Exception) {
    logger.error("Failed", e)
    return Result.Error(e)
}
```

### Gradle Tasks
```kotlin
// ❌ NEVER run multiple times
./gradlew test
./gradlew test  // Seeing errors, running again

// ✅ Run once with filtering
./gradlew test 2>&1 | grep -A 5 "FAILED"
```

---

## 🔧 Delegation Guidelines

### When to Delegate vs Work Directly

**Delegate to specialists when:**
- Frontend/UI work → `category="visual-engineering"`
- Complex logic → `category="ultrabrain"`
- Trivial changes → `category="quick"`
- Unfamiliar patterns → Fire `explore`/`librarian` first

**Work directly when:**
- Simple, known patterns
- Single file changes
- You have full context already

### Delegation Prompt Structure

**MANDATORY - Include ALL sections:**
```
1. TASK: [Atomic, specific goal]
2. EXPECTED OUTCOME: [Concrete deliverables]
3. REQUIRED TOOLS: [Explicit tool whitelist]
4. MUST DO: [Exhaustive requirements]
5. MUST NOT DO: [Forbidden actions]
6. CONTEXT: [File paths, patterns, constraints]
```

### Session Continuity

**ALWAYS use session_id for follow-ups:**

```typescript
// First delegation
task(category="quick", load_skills=[], run_in_background=false, ...)
// Returns: session_id="ses_abc123"

// Follow-up (use session_id)
task(session_id="ses_abc123", load_skills=[], run_in_background=false, prompt="Fix: [specific issue]")
```

---

## 📋 Agent Pre-Flight Checklist

Before making ANY code changes:

- [ ] Read [/CODING_STANDARDS.md](../CODING_STANDARDS.md)
- [ ] Read this file (`.claude/CODING_STANDARDS.md`)
- [ ] Search codebase for similar patterns
- [ ] Verify no deprecated libraries involved
- [ ] Identify KDoc to preserve
- [ ] Plan Boy Scout improvements

Before submitting changes:

- [ ] Run `./gradlew assemble test spotlessApply detekt` ONCE
- [ ] All KDoc preserved
- [ ] No deprecated libraries introduced
- [ ] Boy Scout improvements applied
- [ ] Tests pass or pre-existing failures documented
- [ ] No type safety violations
- [ ] No silent error handling

---

## 📚 Related Documentation

**MUST READ:**
- [/CODING_STANDARDS.md](../CODING_STANDARDS.md) - General project standards
- [/CONTRIBUTING.md](../CONTRIBUTING.md) - Workflow and conventions
- [/docs/ARCHITECTURE.md](../docs/ARCHITECTURE.md) - System design

**Reference:**
- [/docs/DEVELOPMENT.md](../docs/DEVELOPMENT.md) - Implementation roadmap
- [/detekt.yml](../detekt.yml) - Enforced rules
- [/.editorconfig](../.editorconfig) - Editor config

---

**Last Updated**: 2026-03-15  
**For**: AI agents working on Vibely POS

**Remember**: You are part of a team. Write code as if a human developer will maintain it tomorrow.
