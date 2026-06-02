import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.kotlinJvm)
    jacoco
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

dependencies {
    api(libs.jacksonAnnotations)
    api("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation(libs.swaggerAnnotations)

    testImplementation(libs.jacksonModuleKotlin)
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${libs.versions.jackson.get()}")
    testImplementation(libs.junitJupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }

    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
}

val jacocoToolVersion = providers.gradleProperty("jacoco.toolVersion").get()
val jacocoCoverageMinimumCommon = providers.gradleProperty("jacoco.coverage.minimum.common").get()

jacoco {
    toolVersion = jacocoToolVersion
}

val jacocoExcludes =
    listOf(
        "yayauheny/by/model/building/**",
        "yayauheny/by/model/city/**",
        "yayauheny/by/model/country/**",
        "yayauheny/by/model/subway/**",
        "yayauheny/by/model/dto/**",
        "yayauheny/by/model/enums/**"
    )

fun FileTree.excludeJacocoPatterns(): FileTree =
    matching {
        exclude(jacocoExcludes)
    }

tasks.jacocoTestReport {
    dependsOn(tasks.test)

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }

    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it).excludeJacocoPatterns() })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)

    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it).excludeJacocoPatterns() })
    )

    violationRules {
        rule {
            limit {
                minimum = jacocoCoverageMinimumCommon.toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
