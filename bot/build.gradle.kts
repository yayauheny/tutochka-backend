import org.gradle.api.file.FileTree
import org.gradle.api.tasks.testing.Test

plugins {
    java
    id("org.springframework.boot") version "3.5.6"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation(libs.caffeine)
    implementation(libs.telegrambotsClient)
    implementation(libs.telegrambotsWebhook)
    implementation(libs.resilience4jRetry)
    implementation("org.apache.httpcomponents.client5:httpclient5")
    implementation("org.apache.httpcomponents.core5:httpcore5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val integrationTestPattern = "**/*IntegrationTest.class"
val testsParallel = providers.gradleProperty("tests.parallel").map(String::toBoolean).orElse(false)
val jacocoToolVersion = providers.gradleProperty("jacoco.toolVersion").get()
val jacocoCoverageMinimum = providers.gradleProperty("jacoco.coverage.minimum").get()

tasks.test {
    useJUnitPlatform()
    exclude(integrationTestPattern)
    maxParallelForks = if (testsParallel.get()) Runtime.getRuntime().availableProcessors().coerceAtLeast(1) else 1

    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    useJUnitPlatform()
    include(integrationTestPattern)
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    systemProperty("spring.profiles.active", "test")
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    shouldRunAfter("test")

    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
}

tasks.check {
    dependsOn("integrationTest", "jacocoTestReport")
}

jacoco {
    toolVersion = jacocoToolVersion
}

val botJacocoExcludes =
    listOf(
        "**/TutochkaTgBotApplication*",
        "**/config/**",
        "**/dto/backend/**"
    )

fun FileTree.excludeBotJacocoPatterns(): FileTree =
    matching {
        exclude(botJacocoExcludes)
    }

tasks.jacocoTestReport {
    dependsOn(tasks.test, tasks.named("integrationTest"))

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    executionData.setFrom(fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec"))

    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it).excludeBotJacocoPatterns() })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it).excludeBotJacocoPatterns() })
    )

    violationRules {
        rule {
            limit {
                minimum = jacocoCoverageMinimum.toBigDecimal()
            }
        }
    }
}
