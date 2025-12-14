FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts libs.versions.toml ./
COPY gradle.properties ./
COPY gradle/ gradle/
COPY gradlew gradlew.bat ./
COPY backend/build.gradle.kts backend/
COPY bot/build.gradle.kts bot/
RUN chmod +x ./gradlew

ENV GRADLE_OPTS="-Dorg.gradle.jvmargs=-Xmx2g -Dorg.gradle.workers.max=1"

RUN ./gradlew dependencies --no-daemon

COPY backend/src backend/src

RUN ./gradlew :backend:build --no-daemon -x test -x check

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

RUN groupadd -g 1001 appgroup && useradd -u 1001 -g appgroup -s /bin/bash -m appuser
COPY --from=builder /app/backend/build/libs/backend-all.jar app.jar
RUN mkdir -p /app/logs && chown -R appuser:appgroup /app

USER appuser
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]