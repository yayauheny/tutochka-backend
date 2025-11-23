package integration.base

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
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.OutputStream
import java.sql.DriverManager
import java.sql.SQLException

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
                        val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
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

    protected lateinit var dslContext: DSLContext

    @BeforeEach
    fun setupDatabase() {
        dslContext =
            DSL.using(
                DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password),
                SQLDialect.POSTGRES
            )

        dslContext.transaction { configuration ->
            DSL
                .using(configuration)
                .execute(
                    """
                    TRUNCATE TABLE restrooms, cities, countries
                    RESTART IDENTITY CASCADE
                    """.trimIndent()
                )
        }
    }

    @Deprecated("Use dslContext instead", ReplaceWith("dslContext"))
    protected val testDatabase: DSLContext
        get() = dslContext
}
