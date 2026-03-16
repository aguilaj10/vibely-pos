# Vibely POS Backend

This is the backend server for the Vibely POS system, built with Ktor and Kotlin.

## Features

- **Ktor Server**: High-performance async web framework
- **Supabase Integration**: PostgreSQL database access via Supabase
- **JWT Authentication**: Secure token-based authentication
- **Koin DI**: Dependency injection framework
- **JSON Serialization**: kotlinx.serialization for JSON handling
- **CORS Support**: Cross-origin resource sharing enabled
- **Call Logging**: Request/response logging via Logback
- **Error Handling**: Global error handling with StatusPages

## Prerequisites

- JDK 17 or higher
- Gradle 9.3+
- Supabase account with a project set up

## Environment Variables

Create a `.env` file in the project root with the following variables:

```env
# Supabase Configuration
SUPABASE_URL=https://your-project-id.supabase.co
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key-here

# JWT Configuration
JWT_SECRET=your-jwt-secret-here

# Server Configuration
PORT=8080
HOST=0.0.0.0

# Debug Mode (Optional - bypasses authentication for development)
DEBUG_MODE=false
```

### Getting Supabase Credentials

1. Go to your [Supabase Dashboard](https://app.supabase.com/)
2. Create a new project or select your existing project
3. Navigate to **Settings** → **API**
4. Copy the following:
   - **Project URL**: Use as `SUPABASE_URL` (format: `https://your-project-id.supabase.co`)
   - **service_role key**: Use as `SUPABASE_SERVICE_ROLE_KEY` (⚠️ Keep this secret!)

> **Note:** Apply the database schema from `/migrations/` or `/database_schema.sql` to your Supabase project before running the backend.

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── kotlin/com/vibely/pos/backend/
│   │   │   ├── Application.kt          # Main application entry point
│   │   │   ├── config/
│   │   │   │   └── SupabaseConfig.kt   # Supabase client configuration
│   │   │   └── di/
│   │   │       └── BackendModule.kt     # Koin dependency injection module
│   │   └── resources/
│   │       └── logback.xml              # Logging configuration
│   └── test/
└── build.gradle.kts                     # Build configuration
```

## Building

```bash
# Build the project
./gradlew :backend:build

# Build the fat JAR (includes all dependencies)
./gradlew :backend:shadowJar
```

## Running

### Development Mode

```bash
# Set environment variables and run
export SUPABASE_URL="https://your-project-id.supabase.co"
export SUPABASE_SERVICE_ROLE_KEY="your-key-here"
export JWT_SECRET="your-secret-here"

./gradlew :backend:run
```

### Debug Mode (Skip Authentication)

For development, you can enable debug mode to bypass authentication:

```bash
export DEBUG_MODE=true
./gradlew :backend:run
```

**Debug Mode Features:**
- Accepts `Authorization: Bearer debug-access-token` header
- Automatically injects debug user principal (`dev@vibely.pos`)
- Shows warning logs on startup
- ⚠️ **NEVER enable in production**

### Production Mode

```bash
# Run the fat JAR
export SUPABASE_URL="https://your-project-id.supabase.co"
export SUPABASE_SERVICE_ROLE_KEY="your-key-here"
export JWT_SECRET="your-secret-here"

java -jar backend/build/libs/backend-all.jar
```

## API Endpoints

### Health Check

```bash
curl http://localhost:8080/health
```

Response:
```json
{
  "status": "healthy",
  "service": "vibely-pos-backend",
  "supabase": "connected"
}
```

### Database Connection Test

```bash
curl http://localhost:8080/api/test/database
```

Response (success):
```json
{
  "status": "success",
  "message": "Database connection successful",
  "database": "connected"
}
```

## Configuration Details

### Ktor Plugins

The application configures the following Ktor plugins:

1. **ContentNegotiation**: JSON serialization with kotlinx.serialization
2. **Authentication**: JWT-based authentication
3. **CORS**: Allows all origins in development (should be restricted in production)
4. **CallLogging**: Logs all requests at INFO level
5. **StatusPages**: Global error handling for exceptions
6. **Koin**: Dependency injection

### Logging

Logback is configured to log:
- All application logs at DEBUG level (`com.vibely.pos`)
- Ktor framework logs at INFO level
- Ktor application and plugins at DEBUG level

Log pattern: `%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n`

### CORS Configuration

Currently configured to allow:
- All hosts (development mode)
- Methods: OPTIONS, GET, POST, PUT, DELETE, PATCH
- Headers: Authorization, Content-Type, Access-Control-Allow-Origin

**⚠️ Important**: Restrict `anyHost()` to specific domains in production!

### JWT Authentication

JWT authentication is configured with:
- Algorithm: HMAC256
- Secret: From `JWT_SECRET` environment variable
- Token validation: Checks for `userId` claim

## Dependencies

Key dependencies (see `build.gradle.kts` for versions):

- `ktor-server-core`: Ktor server framework
- `ktor-server-netty`: Netty engine for Ktor
- `ktor-server-content-negotiation`: JSON serialization support
- `ktor-server-auth-jwt`: JWT authentication
- `ktor-server-call-logging`: Request logging
- `ktor-server-status-pages`: Error handling
- `ktor-server-cors`: CORS support
- `supabase-postgrest-kt`: Supabase Kotlin client
- `koin-ktor`: Dependency injection
- `logback-classic`: Logging implementation

## Security Notes

⚠️ **Important Security Considerations**:

1. **Never commit** `.env` file or expose service role keys
2. The `SUPABASE_SERVICE_ROLE_KEY` bypasses Row Level Security (RLS) - use carefully
3. Change `JWT_SECRET` to a strong random string in production
4. Restrict CORS `anyHost()` to specific domains in production
5. Use HTTPS in production
6. Implement proper rate limiting for production

## Testing

```bash
# Run tests
./gradlew :backend:test

# Run with coverage
./gradlew :backend:koverHtmlReport
```

## Troubleshooting

### Server won't start

1. Check that all environment variables are set
2. Verify Supabase credentials are correct
3. Check if port 8080 is already in use: `lsof -i :8080`
4. Review logs in console output

### Database connection fails

1. Verify `SUPABASE_URL` is correct
2. Verify `SUPABASE_SERVICE_ROLE_KEY` is valid
3. Check network connectivity to Supabase
4. Ensure your Supabase project is active and healthy

### Build fails

1. Ensure you're using JDK 17+: `java -version`
2. Clean and rebuild: `./gradlew clean :backend:build`
3. Check for dependency conflicts in `build.gradle.kts`

## Next Steps

1. ✅ Configure Ktor plugins
2. ✅ Set up Supabase client
3. ✅ Add health check endpoints
4. ✅ Configure logging
5. 🔄 Implement authentication endpoints
6. 🔄 Add business logic routes
7. 🔄 Implement error handling middleware
8. 🔄 Add request validation
9. 🔄 Set up database migrations
10. 🔄 Add comprehensive tests

## License

See the main project LICENSE file.
