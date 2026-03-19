# Build stage
FROM gradle:9.3.1-jdk21 AS build

# Set working directory
WORKDIR /app

# Copy Gradle configuration files
COPY build.gradle.kts settings.gradle.kts gradle.properties ./
COPY buildSrc ./buildSrc
COPY gradle ./gradle
COPY gradlew gradlew.bat ./

# Copy source code
COPY shared ./shared
COPY backend ./backend
COPY composeApp ./composeApp

# Build the application
RUN gradle :backend:installDist --no-daemon

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
