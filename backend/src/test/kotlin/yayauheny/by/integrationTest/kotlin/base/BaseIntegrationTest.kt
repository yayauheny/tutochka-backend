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
import yayauheny.by.helpers.DatabaseTestHelper

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseIntegrationTest {
    protected lateinit var dslContext: DSLContext
    private var connection: Connection? = null
    protected val postgres
        get() = TestPostgresContainer.postgres

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
