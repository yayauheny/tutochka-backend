import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.kotlinJvm)
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
    testImplementation(libs.junitJupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly(libs.junitPlatformLauncher)
}
