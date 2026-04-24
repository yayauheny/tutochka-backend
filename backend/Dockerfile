FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

COPY gradlew gradlew.bat ./
COPY gradle/ gradle/
COPY build.gradle.kts settings.gradle.kts libs.versions.toml gradle.properties ./
COPY backend/build.gradle.kts backend/
COPY bot/build.gradle.kts bot/

RUN chmod +x ./gradlew

RUN echo "=== JAVA / GRADLE VERSION ===" \
    && java -version \
    && ./gradlew --version

RUN echo "=== FILES BEFORE SOURCES COPY ===" \
    && pwd \
    && ls -la \
    && find . -maxdepth 3 -type f | sort

RUN echo "=== RESOLVE DEPENDENCIES ===" \
    && ./gradlew :backend:dependencies \
        --no-daemon \
        --stacktrace \
        --info \
        --warning-mode all

COPY backend/src backend/src

RUN echo "=== FILES AFTER SOURCES COPY ===" \
    && find backend -maxdepth 5 -type f | sort | sed -n '1,300p'

RUN echo "=== BUILD FAT JAR START ===" \
    && ./gradlew :backend:buildFatJar \
        --no-daemon \
        --stacktrace \
        --info \
        --warning-mode all \
        -x test \
        -x integrationTest \
        -x ktlintCheck \
        -x ktlintFormat \
        -x ktlintKotlinScriptCheck \
        -x ktlintMainSourceSetCheck \
        -x ktlintTestSourceSetCheck

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -g 1001 appgroup \
    && useradd -u 1001 -g appgroup -s /usr/sbin/nologin -m appuser

COPY --from=builder /app/backend/build/libs/backend-all.jar /app/app.jar
RUN mkdir -p /app/logs/archive \
    && chown -R appuser:appgroup /app

USER 1001:1001
EXPOSE 8080

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
