import org.jooq.meta.jaxb.Logging
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktorPlugin)
    alias(libs.plugins.serializationPlugin)
    alias(libs.plugins.liquibasePlugin)
    alias(libs.plugins.ktlintPlugin)
    alias(libs.plugins.jooqPlugin)
    jacoco
}

group = "yayauheny.by"
version = "0.0.1"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

sourceSets {
    main {
        java {
            srcDir("build/generated-src/jooq")
        }
    }
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.testcontainersCore)
        classpath(libs.testcontainersPostgres)
    }
}

dependencies {
    jooqGenerator(libs.postgresql)
    jooqGenerator(libs.postgis)

    implementation(libs.bundles.ktorServerBundle)
    implementation(libs.bundles.koinBundle)
    implementation(libs.bundles.swaggerBundle)
    implementation(libs.bundles.validationBundle)
    implementation(libs.bundles.jooqBundle)
    implementation(libs.postgis)
    implementation(libs.hikaricp)
    implementation(libs.liquibaseCore)
    implementation(libs.logbackClassic)

    runtimeOnly(libs.postgresql)

    testImplementation(libs.bundles.testingBundle)
    testImplementation(libs.ktorServerTestHost)
    testImplementation(libs.bundles.testcontainersBundle)

    testRuntimeOnly(libs.postgresql)

    liquibaseRuntime(libs.bundles.liquibaseBundle)
}

val tcImage = providers.gradleProperty("testcontainers.postgis.image").get()
val tcDbName = providers.gradleProperty("testcontainers.db.name").get()

val containerInstance: PostgreSQLContainer<Nothing>? =
    if ("generateJooq" in project.gradle.startParameter.taskNames ||
        "update" in project.gradle.startParameter.taskNames ||
        "updateMain" in project.gradle.startParameter.taskNames
    ) {
        PostgreSQLContainer<Nothing>(
            DockerImageName
                .parse(tcImage)
                .asCompatibleSubstituteFor("postgres")
        ).apply {
            withDatabaseName(tcDbName)
            start()
        }
    } else {
        null
    }

val liquibaseLogLevel = providers.gradleProperty("liquibase.log.level").get()
val liquibaseChangelogPath = providers.gradleProperty("liquibase.changelog.path").get()
val liquibaseDriver = providers.gradleProperty("liquibase.driver").get()

liquibase {
    activities.register("main") {
        this.arguments =
            mapOf(
                "logLevel" to liquibaseLogLevel,
                "classpath" to "${project.rootDir}/src/main/",
                "changeLogFile" to liquibaseChangelogPath,
                "searchPath" to "${project.rootDir}/src/main/resources/",
                "url" to containerInstance?.jdbcUrl,
                "username" to containerInstance?.username,
                "password" to containerInstance?.password,
                "driver" to liquibaseDriver
            )
    }
    runList = "main"
}

val jooqLoggingLevel = providers.gradleProperty("jooq.logging.level").get()
val jooqDatabaseSchema = providers.gradleProperty("jooq.database.schema").get()
val jooqDatabaseExcludes: String? =
    providers
        .gradleProperty("jooq.database.excludes")
        .orNull
val jooqTargetPackage = providers.gradleProperty("jooq.target.package").get()
val jooqTargetDirectory = providers.gradleProperty("jooq.target.directory").get()

jooq {
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                logging = Logging.valueOf(jooqLoggingLevel)
                jdbc.apply {
                    driver = liquibaseDriver
                    url = containerInstance?.jdbcUrl
                    user = containerInstance?.username
                    password = containerInstance?.password
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        excludes = jooqDatabaseExcludes
                        inputSchema = jooqDatabaseSchema

                        withForcedTypes(
                            org.jooq.meta.jaxb
                                .ForcedType()
                                .withName(org.jooq.impl.SQLDataType.INSTANT.typeName)
                                .withIncludeTypes("(?i:TIMESTAMP\\ (WITH|WITHOUT)\\ TIME\\ ZONE)"),
                            org.jooq.meta.jaxb
                                .ForcedType()
                                .withName(org.jooq.impl.SQLDataType.JSONB.typeName)
                                .withIncludeTypes("jsonb"),
                        )
                    }
                    generate.apply {
                        withJavaTimeTypes(true)
                        withPojosAsKotlinDataClasses(true)
                        withKotlinSetterJvmNameAnnotationsOnIsPrefix(true)
                    }
                    target.apply {
                        packageName = jooqTargetPackage
                        directory = jooqTargetDirectory
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

tasks.named("generateJooq").configure {
    dependsOn("update")
    doLast {
        containerInstance?.stop()
    }
}

val testsParallel = providers.gradleProperty("tests.parallel").get()
val testsTagIntegration = providers.gradleProperty("tests.tags.integration").get()

tasks.test {
    useJUnitPlatform {
        excludeTags(testsTagIntegration)
    }
    systemProperty("junit.jupiter.execution.parallel.enabled", testsParallel)
    finalizedBy("jacocoTestReport")

    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    useJUnitPlatform {
        includeTags(testsTagIntegration)
    }
    testClassesDirs =
        sourceSets.test
            .get()
            .output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    // Отключаем параллельное выполнение для интеграционных тестов
    systemProperty("junit.jupiter.execution.parallel.enabled", "false")
    shouldRunAfter("test")
    finalizedBy("jacocoTestReport")

    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

tasks.check {
    dependsOn("integrationTest")
}

val ktlintVersion = providers.gradleProperty("ktlint.version").get()

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
    exclude { projectFileTree ->
        projectFileTree.file.absolutePath.contains("/build/")
    }
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask>().configureEach {
    exclude("**/build/**")
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask>().configureEach {
    exclude("**/build/**")
}

ktlint {
    version.set(ktlintVersion)
    debug.set(false)
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(false)
    enableExperimentalRules.set(false)

    filter {
        exclude { element ->
            element.file.path.contains(File.separator + "build" + File.separator) ||
                element.file.path.startsWith("build/") ||
                element.file.path.contains("generated-src")
        }

        include("src/main/kotlin/**/*.kt", "src/test/kotlin/**/*.kt")
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

val jacocoExcludes =
    listOf(
        "yayauheny/by/Application.class",
        "yayauheny/by/config/**",
        "yayauheny/by/di/**",
        "yayauheny/by/util/**",
        "yayauheny/by/constants/**",
        "yayauheny/by/model/**",
        "yayauheny/by/entity/**",
        "yayauheny/by/table/**",
        "yayauheny/by/repository/type/**",
        "yayauheny/by/keys/**",
        "yayauheny/by/common/errors/**",
        "yayauheny/by/service/validation/**"
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
        csv.required.set(true) // Enable CSV for programmatic analysis
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/html"))
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoTestReport.xml"))
        csv.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoTestReport.csv"))
    }

    // Collect execution data from both unit and integration tests
    executionData.setFrom(
        fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec")
    )

    // Configure class directories with exclusions
    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it).excludeJacocoPatterns() })
    )

    // Enable detailed reporting by package and class
    doLast {
        logger.lifecycle("JaCoCo HTML report: ${reports.html.outputLocation.get().asFile.absolutePath}/index.html")
        logger.lifecycle("JaCoCo XML report: ${reports.xml.outputLocation.get().asFile.absolutePath}")
        logger.lifecycle("JaCoCo CSV report: ${reports.csv.outputLocation.get().asFile.absolutePath}")
    }
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
repositories {
    mavenCentral()
}
