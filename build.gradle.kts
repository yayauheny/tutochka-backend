plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktorPlugin)
    alias(libs.plugins.serializationPlugin)
    alias(libs.plugins.liquibasePlugin)
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
    implementation(libs.postgis)
    implementation(libs.hikaricp)
    implementation(libs.logbackClassic)

    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.serializationJsonType)
    liquibaseRuntime(libs.bundles.liquibaseBundle)

    testImplementation(libs.bundles.testcontainersBundle)
    testImplementation(libs.bundles.testingBundle)
    testImplementation(libs.ktorServerTestHost)
}

tasks.test {
    useJUnitPlatform()
}

liquibase {
    activities.register("main") {
        this.arguments = mapOf(
            "defaultsFile" to "$projectDir/liquibase/liquibase.properties"
        )
    }
    runList = "main"
}
