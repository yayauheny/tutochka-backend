package integration.base

import java.sql.Connection
import java.sql.DriverManager
import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import yayauheny.by.helpers.DatabaseTestHelper

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
    }

    protected lateinit var dslContext: DSLContext
    private var connection: Connection? = null

    @BeforeAll
    fun runMigrationsOnce() {
        check(postgres.isRunning) { "PostgreSQL container is not running" }

        DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password).use { conn ->
            val database =
                DatabaseFactory
                    .getInstance()
                    .findCorrectDatabaseImplementation(JdbcConnection(conn))
            database.setDefaultSchemaName("public")
            database.setLiquibaseSchemaName("public")
            val liquibase =
                Liquibase(
                    "db/changelog/db.changelog-master.yml",
                    ClassLoaderResourceAccessor(),
                    database
                )
            liquibase.update(Contexts(), LabelExpression())
        }
    }

    @BeforeEach
    open fun openConnectionAndResetData() {
        connection = DriverManager.getConnection(postgres.jdbcUrl, postgres.username, postgres.password)
        dslContext = DSL.using(connection, SQLDialect.POSTGRES)

        DatabaseTestHelper.truncateAllTables(dslContext)
    }

    @AfterEach
    fun closeConnection() {
        runCatching {
            connection?.apply {
                autoCommit = true
                close()
            }
        }
        connection = null
    }
}
