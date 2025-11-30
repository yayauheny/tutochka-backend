package yayauheny.by.config

import com.zaxxer.hikari.HikariDataSource
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.database.jvm.JdbcConnection
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("LiquibaseRunner")

fun runLiquibaseMigrations(dataSource: HikariDataSource) {
    try {
        logger.info("Starting Liquibase migrations...")

        dataSource.connection.use { conn ->
            val database =
                DatabaseFactory
                    .getInstance()
                    .findCorrectDatabaseImplementation(JdbcConnection(conn))

            val liquibase =
                Liquibase(
                    "db/changelog/db.changelog-master.yml",
                    ClassLoaderResourceAccessor(),
                    database
                )

            liquibase.update()
            logger.info("Liquibase migrations completed successfully")
        }
    } catch (e: Exception) {
        logger.error("Не удалось выполнить миграции Liquibase", e)
        throw e
    }
}
