plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.serializationPlugin)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Minimal dependencies for serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
