package yayauheny.by.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import yayauheny.by.util.EnvironmentConfig

data class DatabaseConfig(
    val host: String = EnvironmentConfig.getString("DB_HOST", "localhost"),
    val port: Int = EnvironmentConfig.getInt("DB_PORT", 5432),
    val name: String = EnvironmentConfig.getString("DB_NAME", "tutochka"),
    val user: String = EnvironmentConfig.getString("DB_USER", "postgres"),
    val password: String = EnvironmentConfig.getString("DB_PASSWORD", "password"),
    val maxPoolSize: Int = EnvironmentConfig.getInt("DB_MAX_POOL_SIZE", 10),
    val minIdle: Int = EnvironmentConfig.getInt("DB_MIN_IDLE", 2),
    val connectionTimeout: Long = EnvironmentConfig.getLong("DB_CONNECTION_TIMEOUT", 30000L),
    val idleTimeout: Long = EnvironmentConfig.getLong("DB_IDLE_TIMEOUT", 600000L),
    val maxLifetime: Long = EnvironmentConfig.getLong("DB_MAX_LIFETIME", 1800000L)
) {
    
    fun createDatabase(): Database = Database.connect(createDataSource())
    
    private fun createDataSource(): HikariDataSource = HikariDataSource(
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
}

