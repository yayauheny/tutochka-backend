# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

COPY gradlew ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts libs.versions.toml gradle.properties ./
COPY bot/build.gradle.kts bot/
# Keep only if Gradle configuration for :bot touches :backend
COPY backend/build.gradle.kts backend/

RUN chmod +x ./gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :bot:dependencies --no-daemon

COPY bot/src bot/src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :bot:bootJar --no-daemon --build-cache -x test \
    && cp "$(find /app/bot/build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)" /app/app.jar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -g 1001 appgroup \
    && useradd -u 1001 -g appgroup -s /usr/sbin/nologin -m appuser

COPY --from=builder /app/app.jar /app/app.jar

USER 1001:1001
EXPOSE 8081

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]