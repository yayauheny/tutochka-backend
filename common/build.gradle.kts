import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serializationPlugin)
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
    api(libs.kotlinxSerializationJson)

    implementation(libs.jacksonAnnotations)
    implementation(libs.swaggerAnnotations)

    testImplementation(libs.junitJupiter)
    testImplementation(libs.assertj)
    testRuntimeOnly(libs.junitPlatformLauncher)
}
