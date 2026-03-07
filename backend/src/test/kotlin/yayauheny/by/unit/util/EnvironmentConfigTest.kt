package yayauheny.by.unit.util

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.util.EnvironmentConfig

@DisplayName("EnvironmentConfig Tests")
class EnvironmentConfigTest {
    @Test
    @DisplayName("Returns default when environment variable is not set")
    fun returns_default_when_environment_variable_is_not_set() {
        assertEquals("default", EnvironmentConfig.getString("NON_EXISTENT_VAR", "default"))
        assertEquals(42, EnvironmentConfig.getInt("NON_EXISTENT_VAR", 42))
    }

    @Test
    @DisplayName("Returns default boolean when environment variable is not set")
    fun returns_default_boolean_when_not_set() {
        assertTrue(EnvironmentConfig.getBoolean("NON_EXISTENT_VAR", true))
        assertFalse(EnvironmentConfig.getBoolean("NON_EXISTENT_VAR", false))
    }
}
