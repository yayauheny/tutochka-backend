# Multi-stage build for lightweight production image
FROM gradle:8.5-jdk17 AS builder

# Set working directory
WORKDIR /app

# Copy gradle files first for better caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat ./
COPY build.gradle.kts settings.gradle.kts libs.versions.toml ./

# Download dependencies (this layer will be cached if gradle files don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/
COPY liquibase/ liquibase/

# Build the application (skip ktlint for Docker build)
ENV SKIP_KTLINT=true
RUN ./gradlew build --no-daemon \
    -x test -x integrationTest \
    -x ktlintCheck -x ktlintFormat -x ktlintKotlinScriptCheck -x ktlintMainSourceSetCheck -x ktlintTestSourceSetCheck

# Production stage with lightweight JRE
FROM eclipse-temurin:17-jre-jammy AS production

# Install necessary packages for production server
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    htop \
    nano \
    vim \
    net-tools \
    procps \
    && rm -rf /var/lib/apt/lists/* \
    && apt-get clean

# Create non-root user for security
RUN groupadd -g 1001 appgroup && \
    useradd -u 1001 -g appgroup -s /bin/bash -m appuser

# Set working directory
WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/build/libs/tutochka-backend-*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Set JVM options for containerized environment
ENV JAVA_OPTS="-XX:+UseContainerSupport \
    -XX:MaxRAMPercentage=75.0 \
    -XX:+UseG1GC \
    -XX:+UseStringDeduplication \
    -Djava.security.egd=file:/dev/./urandom"

# Set application environment
ENV APP_ENV=production
ENV TZ=UTC

# Add labels for better container management
LABEL maintainer="TuTochka Team" \
      version="0.1.0" \
      description="TuTochka Backend API - Public Restroom Finder"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
