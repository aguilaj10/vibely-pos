# Build stage
FROM gradle:9.3.1-jdk21 AS build

# Set working directory
WORKDIR /app

# Tell settings.gradle.kts to skip composeApp/androidApp so this image
# does not need their sources or the Android SDK to build.
ENV BACKEND_ONLY=true

# Copy Gradle configuration files
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY buildSrc ./buildSrc
COPY gradle ./gradle
COPY gradlew gradlew.bat ./

# Copy source code (only modules required by :backend)
COPY shared ./shared
COPY backend ./backend

# Build the application.
# --no-configuration-cache: the cache assumes settings/projects don't depend
# on env vars; BACKEND_ONLY makes the project set vary, so we must disable it.
RUN gradle :backend:installDist --no-daemon --no-configuration-cache

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the built application from build stage
COPY --from=build /app/backend/build/install/backend /app

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
RUN chown -R appuser:appgroup /app
USER appuser

# Expose port (Render.com will use PORT environment variable)
EXPOSE 8080

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

# Run the application
CMD ["sh", "-c", "/app/bin/backend"]
