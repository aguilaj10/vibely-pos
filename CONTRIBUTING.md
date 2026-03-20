# Contributing to Vibely POS

Thank you for your interest in contributing to Vibely POS! This document provides guidelines and workflows for contributors.

> **📌 IMPORTANT**: Read [CODING_STANDARDS.md](CODING_STANDARDS.md) for detailed code quality requirements before making changes.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Code Standards](#code-standards)
- [Testing Requirements](#testing-requirements)
- [Pull Request Process](#pull-request-process)
- [Commit Guidelines](#commit-guidelines)

## Getting Started

### Prerequisites

1. **JDK 17** - Required for Kotlin 2.3.10
2. **Git** - Version control
3. **IDE** - IntelliJ IDEA recommended (with Kotlin plugin)
4. **Platform SDKs** (optional):
   - Android SDK for Android development
   - Xcode for iOS development (macOS only)

### Setup

```bash
# Fork and clone the repository
git clone https://github.com/YOUR_USERNAME/vibely-pos.git
cd vibely-pos

# Verify build works
./gradlew build

# Run tests
./gradlew test
```

## Development Workflow

We use **Git Flow** branching strategy:

### Branches

- **`main`** - Integration branch and production-ready code (protected)
- **`feature/*`** - Feature branches
- **`hotfix/*`** - Urgent production fixes

### Starting a Feature

```bash
# Update main branch
git checkout main
git pull origin main

# Create feature branch
git checkout -b feature/your-feature-name

# Make changes and commit
git add .
git commit -m "Description of changes"

# Push to your fork
git push origin feature/your-feature-name
```

### Creating a Pull Request

1. Push your feature branch to your fork
2. Open a PR from `feature/your-feature-name` → `main`
3. Fill out the PR template with:
   - Clear description of changes
   - Related issue numbers
   - Screenshots (if UI changes)
   - Testing performed
4. Wait for CI checks to pass
5. Request review from maintainers

### After PR is Merged

```bash
# Update your local main
git checkout main
git pull origin main

# Delete feature branch
git branch -d feature/your-feature-name
git push origin --delete feature/your-feature-name
```

## Code Standards

> **📖 Full standards**: See [CODING_STANDARDS.md](CODING_STANDARDS.md) for comprehensive coding standards including KDoc requirements, deprecated library checks, and the Boy Scout rule.

### Code Formatting

We use **Spotless** with ktlint for consistent formatting:

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-fix formatting issues
./gradlew spotlessApply
```

**Key formatting rules:**
- Maximum line length: 150 characters
- Indentation: 4 spaces
- No trailing whitespace
- Files must end with newline
- Composable functions can start with capital letters

### Static Analysis

We use **Detekt** for code quality:

```bash
# Run static analysis
./gradlew detekt
```

**Rules:**
- Warnings are treated as errors
- Maximum cyclomatic complexity: 10
- Maximum function length: 40 lines
- No magic numbers
- No unused code
- Public APIs must be documented

### Pre-commit Hooks

Pre-commit hooks automatically run `spotlessCheck` and `detekt`:

```bash
# Hooks run automatically on git commit
git commit -m "Your message"

# To skip hooks (use sparingly):
git commit --no-verify -m "Your message"
```

### Code Style

**Kotlin Conventions:**
- Use meaningful variable names
- Prefer `val` over `var`
- Use extension functions when appropriate
- Prefer sealed classes for state management
- Use data classes for DTOs

**Compose Best Practices:**
- Keep composables small and focused
- Extract reusable components
- Use `remember` and `LaunchedEffect` correctly
- Follow Material 3 guidelines
- Handle loading and error states

## Testing Requirements

### Unit Tests

All business logic must have unit tests:

```bash
# Run unit tests
./gradlew test

# Run with coverage
./gradlew koverXmlReport
```

**Requirements:**
- Minimum 70% code coverage for new code
- Test happy paths and error cases
- Use descriptive test names

**Example:**
```kotlin
@Test
fun `login with valid credentials returns success`() {
    // Arrange
    val useCase = LoginUseCase(mockRepo)

    // Act
    val result = useCase.execute("user@example.com", "password")

    // Assert
    assertTrue(result is Result.Success)
}
```

### Integration Tests

Test interactions between layers:

```kotlin
@Test
fun `repository fetches and maps data correctly`() {
    // Test repository with real DTOs
}
```

## Pull Request Process

### Before Submitting

1. ✅ All tests pass: `./gradlew test`
2. ✅ Code is formatted: `./gradlew spotlessApply`
3. ✅ No static analysis issues: `./gradlew detekt`
4. ✅ Build succeeds: `./gradlew build`
5. ✅ PR is based on latest `main`

### PR Title Format

Use conventional commit format:

- `feat: Add customer search functionality`
- `fix: Correct inventory calculation bug`
- `docs: Update API documentation`
- `refactor: Simplify authentication logic`
- `test: Add tests for checkout flow`
- `chore: Update dependencies`

### PR Description Template

```markdown
## Description
Brief description of changes

## Related Issue
Fixes #123

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
Describe testing performed

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex logic
- [ ] Tests added/updated
- [ ] Documentation updated
```

### Review Process

1. **Automated checks** must pass (CI/CD)
2. **Code review** from at least one maintainer
3. **Address feedback** and push changes
4. **Squash and merge** when approved

## Commit Guidelines

### Commit Message Format

```
<type>: <subject>

<body>

<footer>
```

**Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation changes
- `style` - Formatting (no code change)
- `refactor` - Code restructuring
- `test` - Adding tests
- `chore` - Maintenance tasks

**Example:**
```
feat: Add barcode scanner integration

Integrate with device camera for barcode scanning.
Supports QR codes and UPC-A formats.

Closes #45
```

### Commit Best Practices

- Write clear, descriptive commit messages
- Keep commits focused on single changes
- Reference issue numbers when applicable
- Use imperative mood ("Add feature" not "Added feature")

## Questions?

- 💬 [GitHub Discussions](https://github.com/yourusername/vibely-pos/discussions)
- 🐛 [Report a Bug](https://github.com/yourusername/vibely-pos/issues)
- 💡 [Request a Feature](https://github.com/yourusername/vibely-pos/issues)

---

Thank you for contributing to Vibely POS! 🙏
