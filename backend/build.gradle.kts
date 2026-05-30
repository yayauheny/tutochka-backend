import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.springBootPlugin)
    alias(libs.plugins.springDependencyManagementPlugin)
    alias(libs.plugins.kotlinSpringPlugin)
    alias(libs.plugins.kotlinJpaPlugin)
    alias(libs.plugins.ktlintPlugin)
    jacoco
}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(project(":common"))
    implementation(libs.tink)

    implementation(libs.bundles.springBootBundle)
    implementation(libs.bundles.swaggerBundle)
    
    // Database
    implementation(libs.postgresql)
    implementation(libs.postgis)
    implementation(libs.liquibaseCore)

    testImplementation(libs.bundles.testingBundle)
    testImplementation(libs.bundles.testcontainersBundle)
    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.assertj)
}

val testsParallel = providers.gradleProperty("tests.parallel").map(String::toBoolean).orElse(false)
val testsTagIntegration = providers.gradleProperty("tests.tags.integration").get()

tasks.test {
    useJUnitPlatform {
        excludeTags(testsTagIntegration)
    }
    systemProperty("junit.jupiter.execution.parallel.enabled", testsParallel)

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
    useJUnitPlatform {
        includeTags(testsTagIntegration)
    }
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
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

val ktlintVersion = providers.gradleProperty("ktlint.version").get()

ktlint {
    version.set(ktlintVersion)
    debug.set(false)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    enableExperimentalRules.set(false)

    filter {
        exclude { element ->
            val file = element.file
            val absolutePath = file.absolutePath
            val name = file.name

            absolutePath.contains("/build/")
        }
        include("src/main/kotlin/**/*.kt", "src/test/kotlin/**/*.kt")
    }
}

tasks.named("build") {
    dependsOn("ktlintCheck")
}

tasks.named("compileKotlin") {
    dependsOn("ktlintFormat")
}

val jacocoExcludes =
    listOf(
        "yayauheny/by/Application*",
        "yayauheny/by/config/**",
        "yayauheny/by/model/**",
        "yayauheny/by/entity/**",
        "yayauheny/by/common/errors/**"
    )

fun FileTree.excludeJacocoPatterns(): FileTree =
    matching {
        exclude(jacocoExcludes)
    }

val jacocoToolVersion = providers.gradleProperty("jacoco.toolVersion").get()
val jacocoCoverageMinimum = providers.gradleProperty("jacoco.coverage.minimum").get()

jacoco {
    toolVersion = jacocoToolVersion
}

tasks.jacocoTestReport {
    dependsOn(tasks.test, tasks.named("integrationTest"))

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }

    executionData.setFrom(
        fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec")
    )

    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it).excludeJacocoPatterns() })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    enabled = true

    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it).excludeJacocoPatterns() })
    )

    violationRules {
        rule {
            limit {
                minimum = jacocoCoverageMinimum.toBigDecimal()
            }
        }
    }
}
