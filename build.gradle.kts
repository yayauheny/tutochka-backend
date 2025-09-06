plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktorPlugin)
    alias(libs.plugins.serializationPlugin)
}

group = "yayauheny.by"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

dependencies {
    implementation(libs.bundles.ktorServerBundle)
    implementation(libs.bundles.koinBundle)
    implementation(libs.bundles.exposedBundle)
    implementation(libs.logbackClassic)

    runtimeOnly(libs.postgresql)

    testImplementation(libs.bundles.testcontainersBundle)
    testImplementation(libs.ktorServerTestHost)
    testImplementation(libs.junitKotlin)
}
