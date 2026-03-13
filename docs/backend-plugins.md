# Backend Plugin Configuration

## Overview
This document describes the Ktor backend plugins and Supabase client configuration for the Vibely POS backend.

## Implemented Plugins

### 1. ContentNegotiation
- **Purpose**: JSON serialization/deserialization
- **Configuration**: kotlinx.serialization with pretty print and lenient mode
- **Features**: Ignores unknown keys for forward compatibility

### 2. CORS (Cross-Origin Resource Sharing)
- **Purpose**: Enable cross-origin requests from frontend applications
- **Allowed Methods**: GET, POST, PUT, DELETE, PATCH, OPTIONS
- **Allowed Headers**: Authorization, ContentType, AccessControlAllowOrigin
- **Configuration**: Currently allows any host (should be restricted in production)

### 3. CallLogging
- **Purpose**: Log all incoming HTTP requests
- **Level**: INFO
- **Filter**: Logs all requests starting with "/"

### 4. StatusPages (Error Handling)
- **Purpose**: Global exception handling
- **Handlers**:
  - `Throwable` → 500 Internal Server Error
  - `IllegalArgumentException` → 400 Bad Request
- **Response Format**: JSON with error message

### 5. Authentication (JWT)
- **Purpose**: JWT-based authentication
- **Algorithm**: HMAC256
- **Configuration**: Uses `JWT_SECRET` environment variable
- **Validation**: Checks for `userId` claim in JWT payload

## Supabase Client Configuration

### SupabaseConfig.kt
- **Location**: `backend/src/main/kotlin/com/vibely/pos/backend/config/`
- **Features**:
  - Lazy-initialized singleton client
  - Postgrest module for database operations
  - CIO HTTP engine
- **Environment Variables**:
  - `SUPABASE_URL`: Supabase project URL
  - `SUPABASE_SERVICE_ROLE_KEY`: Service role key for backend operations

## Logging Configuration

### logback.xml
- **Location**: `backend/src/main/resources/`
- **Configuration**:
  - Console appender with timestamp and log level
  - INFO level for Ktor framework
  - DEBUG level for application code (`com.vibely.pos`)

## Dependencies Added

### Ktor Plugins
- `ktor-server-content-negotiation`
- `ktor-server-cors`
- `ktor-server-call-logging`
- `ktor-server-status-pages`
- `ktor-server-auth`
- `ktor-server-auth-jwt`
- `ktor-serialization-kotlinx-json`

### Supabase
- `supabase-postgrest-kt`
- `ktor-client-cio` (HTTP engine)
- `ktor-client-content-negotiation`
- `ktor-client-logging`

### Dependency Injection
- `koin-ktor`
- `koin-logger-slf4j`

### Logging
- `logback-classic`

## Environment Variables

Create a `.env` file based on `.env.example`:

```bash
SUPABASE_URL=http://localhost:54321
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key-here
JWT_SECRET=your-secret-key-here-change-in-production
```

## Testing

### Build
```bash
./gradlew :backend:build
```

### Run
```bash
cd backend
export SUPABASE_URL="http://localhost:54321"
export SUPABASE_SERVICE_ROLE_KEY="your-key"
export JWT_SECRET="your-secret"
../gradlew run
```

### Test Endpoints
```bash
# Root endpoint
curl http://localhost:8080/

# Health check
curl http://localhost:8080/health
```

## Next Steps

1. Configure Koin dependency injection (Task #3)
2. Implement authentication routes
3. Add database repositories
4. Implement business logic endpoints
5. Add comprehensive error handling
6. Configure production CORS settings
7. Set up environment-specific configurations
