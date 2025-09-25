plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktorPlugin)
    alias(libs.plugins.serializationPlugin)
    alias(libs.plugins.liquibasePlugin)
    alias(libs.plugins.ktlintPlugin)
    jacoco
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
    implementation(libs.bundles.swaggerBundle)
    implementation(libs.bundles.validationBundle)
    implementation(libs.postgis)
    implementation(libs.hikaricp)
    implementation(libs.logbackClassic)
    implementation(libs.bundles.botBundle)
    implementation(libs.bundles.databaseBundle)
    implementation(libs.liquibaseCore)

    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.serializationJsonType)
    liquibaseRuntime(libs.bundles.liquibaseBundle)

    testImplementation(libs.bundles.testingBundle)
    testImplementation(libs.bundles.testcontainersBundle)
    testImplementation(libs.ktorServerTestHost)
    testImplementation(libs.liquibaseCore)
    testRuntimeOnly(libs.postgresql)
}

tasks.test {
    useJUnitPlatform()
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    finalizedBy("jacocoTestReport")
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    useJUnitPlatform()
    testClassesDirs =
        sourceSets.test
            .get()
            .output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    include("**/integration/**")
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    shouldRunAfter("test")
    finalizedBy("jacocoTestReport")
}

tasks.test {
    exclude("**/integration/**")
}

tasks.check {
    dependsOn("integrationTest")
}

ktlint {
    version.set("1.3.1")
    debug.set(false)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)

    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
}

tasks.named("build") {
    if (System.getenv("SKIP_KTLINT") != "true") {
        dependsOn("ktlintCheck")
    }
}

tasks.named("compileKotlin") {
    if (System.getenv("SKIP_KTLINT") != "true") {
        dependsOn("ktlintFormat")
    }
}

liquibase {
    activities.register("main") {
        this.arguments =
            mapOf(
                "defaultsFile" to "$projectDir/liquibase/liquibase.properties",
            )
    }
    runList = "main"
}

jacoco {
    toolVersion = "0.8.11"
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
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "yayauheny/by/Application.class",
                        "yayauheny/by/model/**/*.class",
                        "yayauheny/by/entity/**/*.class",
                        "yayauheny/by/table/**/*.class",
                        "yayauheny/by/config/**/*.class",
                        "yayauheny/by/di/**/*.class",
                        "yayauheny/by/util/**/*.class",
                        "yayauheny/by/common/errors/ErrorResponse.class",
                        "yayauheny/by/common/errors/FieldError.class",
                        "yayauheny/by/service/validation/Validators.class",
                        "yayauheny/by/repository/type/**/*.class"
                    )
                }
            }
        )
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    enabled = true

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "yayauheny/by/Application.class",
                        "yayauheny/by/model/**/*.class",
                        "yayauheny/by/entity/**/*.class",
                        "yayauheny/by/table/**/*.class",
                        "yayauheny/by/config/**/*.class",
                        "yayauheny/by/di/**/*.class",
                        "yayauheny/by/util/**/*.class",
                        "yayauheny/by/common/errors/ErrorResponse.class",
                        "yayauheny/by/common/errors/FieldError.class",
                        "yayauheny/by/repository/type/**/*.class"
                    )
                }
            }
        )
    )

    violationRules {
        rule {
            limit {
                minimum = "0.75".toBigDecimal()
            }
        }
    }
}
