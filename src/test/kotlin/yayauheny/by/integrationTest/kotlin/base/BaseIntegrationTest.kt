package integration.base

import java.io.OutputStream
import java.sql.DriverManager
import java.sql.SQLException
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Scope
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.visitor.ChangeExecListener
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.ChangeExecListenerCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.CommandExecutionException
import liquibase.resource.ClassLoaderResourceAccessor
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {
    companion object {
        private const val IMAGE = "postgis/postgis:17-3.5"

        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer(
                DockerImageName
                    .parse(IMAGE)
                    .asCompatibleSubstituteFor("postgres")
            ).withDatabaseName("testdb")
                .withUsername("postgres")
                .withPassword("postgres")
                .withReuse(true)

        @JvmStatic
        @BeforeAll
        fun migrate() {
            try {
                DriverManager
                    .getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
                    .use { connection ->
                        val database =
                            DatabaseFactory
                                .getInstance()
                                .findCorrectDatabaseImplementation(JdbcConnection(connection))
                        val changeLogFile = "db/changelog/db.changelog-master.yml"
                        val scopeObjects =
                            mapOf(
                                Scope.Attr.database.name to database,
                                Scope.Attr.resourceAccessor.name to ClassLoaderResourceAccessor()
                            )

                        Scope.child(scopeObjects) {
                            val updateCommand =
                                CommandScope(*UpdateCommandStep.COMMAND_NAME).apply {
                                    addArgumentValue(
                                        DbUrlConnectionCommandStep.DATABASE_ARG,
                                        database
                                    )
                                    addArgumentValue(
                                        UpdateCommandStep.CHANGELOG_FILE_ARG,
                                        changeLogFile
                                    )
                                    addArgumentValue(
                                        UpdateCommandStep.CONTEXTS_ARG,
                                        Contexts().toString()
                                    )
                                    addArgumentValue(
                                        UpdateCommandStep.LABEL_FILTER_ARG,
                                        LabelExpression().originalString
                                    )
                                    addArgumentValue(
                                        ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG,
                                        null as ChangeExecListener?
                                    )
                                    addArgumentValue(
                                        DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS,
                                        ChangeLogParameters(database)
                                    )

                                    setOutput(OutputStream.nullOutputStream())
                                }
                            updateCommand.execute()
                        }
                    }
            } catch (e: CommandExecutionException) {
                println("Liquibase migration failed: ${e.message}")
                e.printStackTrace()
                throw e
            } catch (e: SQLException) {
                println("Database connection failed: ${e.message}")
                e.printStackTrace()
                throw e
            }
        }
    }

    protected lateinit var testDatabase: Database

    @BeforeEach
    fun setupDatabase() {
        testDatabase =
            Database.connect(
                url = postgres.jdbcUrl,
                driver = "org.postgresql.Driver",
                user = postgres.username,
                password = postgres.password
            )
        transaction(testDatabase) {
            exec(
                """
                TRUNCATE TABLE restrooms, cities, countries
                RESTART IDENTITY CASCADE
                """.trimIndent()
            )
        }
    }
}
