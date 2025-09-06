package yayauheny.by.util

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("EnvironmentConfig Tests")
class EnvironmentConfigTest {
    
    @Test
    @DisplayName("should_return_default_string_value_when_environment_variable_is_not_set")
    fun should_return_default_string_value_when_environment_variable_is_not_set() {
        val result = EnvironmentConfig.getString("NON_EXISTENT_VAR", "default")
        assertEquals("default", result)
    }
    
    @Test
    @DisplayName("should_return_default_int_value_when_environment_variable_is_not_set")
    fun should_return_default_int_value_when_environment_variable_is_not_set() {
        val result = EnvironmentConfig.getInt("NON_EXISTENT_VAR", 42)
        assertEquals(42, result)
    }
    
    @Test
    @DisplayName("should_return_default_long_value_when_environment_variable_is_not_set")
    fun should_return_default_long_value_when_environment_variable_is_not_set() {
        val result = EnvironmentConfig.getLong("NON_EXISTENT_VAR", 123L)
        assertEquals(123L, result)
    }
    
    @Test
    @DisplayName("should_return_default_boolean_value_when_environment_variable_is_not_set")
    fun should_return_default_boolean_value_when_environment_variable_is_not_set() {
        val result = EnvironmentConfig.getBoolean("NON_EXISTENT_VAR", true)
        assertTrue(result)
    }
    
    @ParameterizedTest
    @ValueSource(strings = ["true", "TRUE", "True"])
    @DisplayName("should_return_true_for_various_boolean_string_representations")
    fun should_return_true_for_various_boolean_string_representations(value: String) {
        val result = EnvironmentConfig.getBoolean("NON_EXISTENT_VAR", true)
        assertTrue(result)
    }
    
    @Test
    @DisplayName("should_return_false_for_default_boolean_when_not_set")
    fun should_return_false_for_default_boolean_when_not_set() {
        val result = EnvironmentConfig.getBoolean("NON_EXISTENT_VAR", false)
        assertFalse(result)
    }
}