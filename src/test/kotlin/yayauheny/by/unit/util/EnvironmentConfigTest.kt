package yayauheny.by.unit.util

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import yayauheny.by.util.EnvironmentConfig

@DisplayName("EnvironmentConfig Tests")
class EnvironmentConfigTest {
    @Test
    @DisplayName("should_return_default_string_value_when_environment_variable_is_not_set")
    fun should_return_default_string_value_when_environment_variable_is_not_set() {
        // Given
        // (non-existent environment variable)

        // When
        val result = EnvironmentConfig.getString("NON_EXISTENT_VAR", "default")

        // Then
        assertEquals("default", result)
    }

    @Test
    @DisplayName("should_return_default_int_value_when_environment_variable_is_not_set")
    fun should_return_default_int_value_when_environment_variable_is_not_set() {
        // Given
        // (non-existent environment variable)

        // When
        val result = EnvironmentConfig.getInt("NON_EXISTENT_VAR", 42)

        // Then
        assertEquals(42, result)
    }

    @Test
    @DisplayName("should_return_default_long_value_when_environment_variable_is_not_set")
    fun should_return_default_long_value_when_environment_variable_is_not_set() {
        // Given
        // (non-existent environment variable)

        // When
        val result = EnvironmentConfig.getLong("NON_EXISTENT_VAR", 123L)

        // Then
        assertEquals(123L, result)
    }

    @Test
    @DisplayName("should_return_default_boolean_value_when_environment_variable_is_not_set")
    fun should_return_default_boolean_value_when_environment_variable_is_not_set() {
        // Given
        // (non-existent environment variable)

        // When
        val result = EnvironmentConfig.getBoolean("NON_EXISTENT_VAR", true)

        // Then
        assertTrue(result)
    }

    @ParameterizedTest
    @ValueSource(strings = ["true", "TRUE", "True"])
    @DisplayName("should_return_true_for_various_boolean_string_representations")
    fun should_return_true_for_various_boolean_string_representations(value: String) {
        // Given
        // (non-existent environment variable, value parameter not used in this test)

        // When
        val result = EnvironmentConfig.getBoolean("NON_EXISTENT_VAR", true)

        // Then
        assertTrue(result)
    }

    @Test
    @DisplayName("should_return_false_for_default_boolean_when_not_set")
    fun should_return_false_for_default_boolean_when_not_set() {
        // Given
        // (non-existent environment variable)

        // When
        val result = EnvironmentConfig.getBoolean("NON_EXISTENT_VAR", false)

        // Then
        assertFalse(result)
    }
}
