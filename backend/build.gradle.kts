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
    implementation(project(":shared"))

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
        val container =
            PostgreSQLContainer<Nothing>(
                DockerImageName
                    .parse(tcImage)
                    .asCompatibleSubstituteFor("postgres")
            ).apply {
                withDatabaseName(tcDbName)
                start()
                while (!isRunning) {
                    Thread.sleep(100)
                }
            }
        container
    } else {
        null
    }

val liquibaseLogLevel = providers.gradleProperty("liquibase.log.level").get()
val liquibaseChangelogPath = providers.gradleProperty("liquibase.changelog.path").get()
val liquibaseDriver = providers.gradleProperty("liquibase.driver").get()

liquibase {
    activities.register("main") {
        val args =
            mutableMapOf<String, String>(
                "logLevel" to liquibaseLogLevel,
                "classpath" to "${project.projectDir}/src/main/",
                "changelogFile" to liquibaseChangelogPath,
                "searchPath" to "${project.projectDir}/src/main/resources/",
                "driver" to liquibaseDriver
            )

        val container = containerInstance
        if (container != null && container.isRunning) {
            args["url"] = container.jdbcUrl
            args["username"] = container.username
            args["password"] = container.password
        }

        this.arguments = args
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
                    containerInstance?.let { container ->
                        url = container.jdbcUrl
                        user = container.username
                        password = container.password
                    }
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
                        directory = "${layout.buildDirectory.get()}/generated-src/jooq"
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

val jooqExcludePatterns =
    listOf(
        "**/build/**",
        "**/tables/**",
        "**/keys/**",
        "**/routines/**",
        "**/udts/**",
        "**/udt/**",
        "**/indexes/**",
        "**/jooq/**",
        "**/Public.kt",
        "**/DefaultCatalog.kt",
        "**/yayauheny/by/Public.kt",
        "**/yayauheny/by/DefaultCatalog.kt"
    )

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
    exclude { projectFileTree ->
        val file = projectFileTree.file
        val absolutePath = file.absolutePath
        val name = file.name

        absolutePath.contains("/build/") ||
            absolutePath.contains("/tables/") ||
            absolutePath.contains("/keys/") ||
            absolutePath.contains("/routines/") ||
            absolutePath.contains("/udts/") ||
            absolutePath.contains("/udt/") ||
            absolutePath.contains("/indexes/") ||
            absolutePath.contains("/jooq/") ||
            name == "Public.kt" ||
            name == "DefaultCatalog.kt" ||
            absolutePath.contains("/Public.kt") ||
            absolutePath.contains("/DefaultCatalog.kt") ||
            absolutePath.endsWith("yayauheny/by/Public.kt") ||
            absolutePath.endsWith("yayauheny/by/DefaultCatalog.kt")
    }
}

tasks.named("runKtlintCheckOverMainSourceSet", org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask::class.java).configure {
    setSource(
        sourceSets.main.get().allSource.asFileTree.filter { file ->
            val absolutePath = file.absolutePath
            val name = file.name
            !(
                absolutePath.contains("/build/") ||
                    absolutePath.contains("/tables/") ||
                    absolutePath.contains("/keys/") ||
                    absolutePath.contains("/routines/") ||
                    absolutePath.contains("/udts/") ||
                    absolutePath.contains("/udt/") ||
                    absolutePath.contains("/indexes/") ||
                    absolutePath.contains("/jooq/") ||
                    name == "Public.kt" ||
                    name == "DefaultCatalog.kt" ||
                    absolutePath.contains("/Public.kt") ||
                    absolutePath.contains("/DefaultCatalog.kt") ||
                    absolutePath.endsWith("yayauheny/by/Public.kt") ||
                    absolutePath.endsWith("yayauheny/by/DefaultCatalog.kt")
            )
        }
    )
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintCheckTask>().configureEach {
    exclude(*jooqExcludePatterns.toTypedArray())
}

tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.KtLintFormatTask>().configureEach {
    exclude(*jooqExcludePatterns.toTypedArray())
}

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

            absolutePath.contains("/build/") ||
                absolutePath.contains("/tables/") ||
                absolutePath.contains("/keys/") ||
                absolutePath.contains("/routines/") ||
                absolutePath.contains("/udts/") ||
                absolutePath.contains("/udt/") ||
                absolutePath.contains("/indexes/") ||
                absolutePath.contains("/jooq/") ||
                name == "Public.kt" ||
                name == "DefaultCatalog.kt" ||
                absolutePath.contains("/Public.kt") ||
                absolutePath.contains("/DefaultCatalog.kt") ||
                absolutePath.endsWith("yayauheny/by/Public.kt") ||
                absolutePath.endsWith("yayauheny/by/DefaultCatalog.kt")
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

    executionData.setFrom(
        fileTree(layout.buildDirectory.dir("jacoco")).include("**/*.exec")
    )

    classDirectories.setFrom(
        files(classDirectories.files.map { fileTree(it).excludeJacocoPatterns() })
    )
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
