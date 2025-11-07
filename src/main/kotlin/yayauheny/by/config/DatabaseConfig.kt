package yayauheny.by.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import yayauheny.by.util.env
import yayauheny.by.util.envInt
import yayauheny.by.util.envLong

data class DatabaseConfig(
    val host: String = "DB_HOST".env("localhost"),
    val port: Int = "DB_PORT".envInt(5432),
    val name: String = "DB_NAME".env("postgres"),
    val user: String = "DB_USER".env("admin"),
    val password: String = "DB_PASSWORD".env("admin"),
    val maxPoolSize: Int = "DB_MAX_POOL_SIZE".envInt(10),
    val minIdle: Int = "DB_MIN_IDLE".envInt(2),
    val connectionTimeout: Long = "DB_CONNECTION_TIMEOUT".envLong(30000L),
    val idleTimeout: Long = "DB_IDLE_TIMEOUT".envLong(600000L),
    val maxLifetime: Long = "DB_MAX_LIFETIME".envLong(1800000L)
) {
    fun createDataSource(): HikariDataSource =
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = "jdbc:postgresql://$host:$port/$name"
                username = user
                password = this@DatabaseConfig.password
                driverClassName = "org.postgresql.Driver"
                maximumPoolSize = maxPoolSize
                minimumIdle = minIdle
                connectionTimeout = connectionTimeout
                idleTimeout = idleTimeout
                maxLifetime = maxLifetime
                connectionTestQuery = "SELECT 1"
                validationTimeout = 5000L
                leakDetectionThreshold = 60000L
                isAutoCommit = true
                poolName = "TutochkaPool"
            }
        )

    fun createJooqConfiguration(dataSource: DataSource): Configuration {
        return DefaultConfiguration().apply {
            set(dataSource)
            set(SQLDialect.POSTGRES)
        }
    }

    fun createDSLContext(dataSource: DataSource): DSLContext {
        return DSL.using(createJooqConfiguration(dataSource))
    }
}
