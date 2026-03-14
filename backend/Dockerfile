FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts libs.versions.toml gradle.properties ./
COPY backend/build.gradle.kts backend/
COPY bot/build.gradle.kts bot/

RUN chmod +x ./gradlew

RUN ./gradlew :backend:dependencies --no-daemon

COPY backend/src backend/src

RUN ./gradlew :backend:buildFatJar --no-daemon

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -g 1001 appgroup \
    && useradd -u 1001 -g appgroup -s /usr/sbin/nologin -m appuser \
    && mkdir -p /app/logs

COPY --from=builder /app/backend/build/libs/backend-all.jar /app/app.jar
RUN chown -R appuser:appgroup /app

USER 1001:1001
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]