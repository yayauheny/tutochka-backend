package yayauheny.by.unit.config

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import yayauheny.by.config.DatabaseConfig
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("DatabaseConfig Validation Tests")
class DatabaseConfigTest {
    @Test
    @DisplayName("Valid configuration should be created successfully")
    fun valid_configuration_should_be_created_successfully() {
        // Given
        val config =
            DatabaseConfig(
                host = "localhost",
                port = 5432,
                name = "testdb",
                user = "testuser",
                password = "testpass",
                maxPoolSize = 10,
                minIdle = 2,
                connectionTimeout = 30000L,
                idleTimeout = 600000L,
                maxLifetime = 1800000L
            )

        // Then
        assertNotNull(config)
        assertEquals("localhost", config.host)
        assertEquals(5432, config.port)
    }

    @Test
    @DisplayName("Blank host should throw IllegalArgumentException")
    fun blank_host_should_throw_exception() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(host = "")
        }
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(host = "   ")
        }
    }

    @Test
    @DisplayName("Invalid port should throw IllegalArgumentException")
    fun invalid_port_should_throw_exception() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(port = 0)
        }
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(port = -1)
        }
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(port = 65536)
        }
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(port = 70000)
        }
    }

    @Test
    @DisplayName("Valid port boundaries should pass")
    fun valid_port_boundaries_should_pass() {
        // Given & When
        val config1 = DatabaseConfig(port = 1)
        val config2 = DatabaseConfig(port = 65535)

        // Then
        assertEquals(1, config1.port)
        assertEquals(65535, config2.port)
    }

    @Test
    @DisplayName("Blank database name should throw IllegalArgumentException")
    fun blank_database_name_should_throw_exception() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(name = "")
        }
    }

    @Test
    @DisplayName("Blank user should throw IllegalArgumentException")
    fun blank_user_should_throw_exception() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(user = "")
        }
    }

    @Test
    @DisplayName("Blank password should throw IllegalArgumentException")
    fun blank_password_should_throw_exception() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(password = "")
        }
    }

    @Test
    @DisplayName("Zero or negative maxPoolSize should throw IllegalArgumentException")
    fun invalid_max_pool_size_should_throw_exception() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(maxPoolSize = 0)
        }
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(maxPoolSize = -1)
        }
    }

    @Test
    @DisplayName("Negative minIdle should throw IllegalArgumentException")
    fun negative_min_idle_should_throw_exception() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(minIdle = -1)
        }
    }

    @Test
    @DisplayName("minIdle greater than maxPoolSize should throw IllegalArgumentException")
    fun min_idle_greater_than_max_pool_size_should_throw_exception() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(maxPoolSize = 5, minIdle = 10)
        }
    }

    @Test
    @DisplayName("minIdle equal to maxPoolSize should pass")
    fun min_idle_equal_to_max_pool_size_should_pass() {
        // Given & When
        val config = DatabaseConfig(maxPoolSize = 10, minIdle = 10)

        // Then
        assertEquals(10, config.maxPoolSize)
        assertEquals(10, config.minIdle)
    }

    @Test
    @DisplayName("Zero or negative connectionTimeout should throw IllegalArgumentException")
    fun invalid_connection_timeout_should_throw_exception() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(connectionTimeout = 0)
        }
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(connectionTimeout = -1)
        }
    }

    @Test
    @DisplayName("Zero or negative idleTimeout should throw IllegalArgumentException")
    fun invalid_idle_timeout_should_throw_exception() {
        // When & Then
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(idleTimeout = 0)
        }
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(idleTimeout = -1)
        }
    }

    @Test
    @DisplayName("Zero or negative maxLifetime should throw IllegalArgumentException")
    fun invalid_max_lifetime_should_throw_exception() {
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(maxLifetime = 0)
        }
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(maxLifetime = -1)
        }
    }

    @Test
    @DisplayName("idleTimeout greater than maxLifetime should throw IllegalArgumentException")
    fun idle_timeout_greater_than_max_lifetime_should_throw_exception() {
        assertThrows<IllegalArgumentException> {
            DatabaseConfig(idleTimeout = 2000000L, maxLifetime = 1000000L)
        }
    }

    @Test
    @DisplayName("idleTimeout equal to maxLifetime should pass")
    fun idle_timeout_equal_to_max_lifetime_should_pass() {
        val config = DatabaseConfig(idleTimeout = 1800000L, maxLifetime = 1800000L)
        assertEquals(1800000L, config.idleTimeout)
        assertEquals(1800000L, config.maxLifetime)
    }

    @Test
    @DisplayName("Very large valid values should pass")
    fun very_large_valid_values_should_pass() {
        val config =
            DatabaseConfig(
                maxPoolSize = Int.MAX_VALUE,
                minIdle = Int.MAX_VALUE,
                connectionTimeout = Long.MAX_VALUE,
                idleTimeout = Long.MAX_VALUE,
                maxLifetime = Long.MAX_VALUE
            )
        assertEquals(Int.MAX_VALUE, config.maxPoolSize)
        assertEquals(Long.MAX_VALUE, config.connectionTimeout)
    }
}
