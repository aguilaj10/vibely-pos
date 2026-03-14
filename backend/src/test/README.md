# Backend Authentication API Tests

## Overview
This directory contains tests for the authentication API endpoints and services.

## Test Structure

### AuthServiceTest
Unit tests for the `AuthService` class:
- Password hashing with BCrypt
- Hash verification
- Special character handling
- Unicode support

### AuthRoutesTest
Integration tests for authentication endpoints:
- POST `/api/auth/login` - User authentication
- POST `/api/auth/logout` - Session termination
- GET `/api/auth/me` - Current user retrieval
- POST `/api/auth/refresh` - Token refresh

## Running Tests

```bash
# Run all backend tests
./gradlew :backend:test

# Run specific test class
./gradlew :backend:test --tests "AuthServiceTest"

# Run with coverage
./gradlew :backend:koverHtmlReport
```

## Test Database Setup

For integration tests that require database access, you'll need to:

1. Set up a test Supabase project or use a local PostgreSQL database
2. Run the migration script: `migrations/001_create_auth_tables.sql`
3. Create test users with proper password hashes
4. Configure environment variables:
   ```
   SUPABASE_URL=your-test-supabase-url
   SUPABASE_KEY=your-test-supabase-key
   JWT_SECRET=test-secret-key
   ```

## Manual API Testing

### 1. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "Password123!"}'
```

### 2. Get Current User
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <access_token>"
```

### 3. Refresh Token
```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refresh_token": "<refresh_token>"}'
```

### 4. Logout
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <access_token>"
```

## Test Data

### Create Test User (SQL)
```sql
INSERT INTO users (id, email, full_name, role, status, password_hash, created_at)
VALUES (
  gen_random_uuid(),
  'test@example.com',
  'Test User',
  'CASHIER',
  'ACTIVE',
  '$2a$12$hash_here', -- Use AuthService.hashPassword() to generate
  NOW()
);
```

## Notes

- Tests use MockK for mocking dependencies
- Integration tests require a running database
- JWT tokens expire after 15 minutes (access) / 7 days (refresh)
- Blacklisted tokens are automatically cleaned up when expired
