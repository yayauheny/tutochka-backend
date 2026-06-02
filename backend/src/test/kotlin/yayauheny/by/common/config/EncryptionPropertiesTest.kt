package yayauheny.by.common.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class EncryptionPropertiesTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java)

    @EnableConfigurationProperties(EncryptionProperties::class)
    class TestConfig

    @Test
    fun `binds encryption properties from configuration`() {
        contextRunner
            .withPropertyValues("encryption.keyset.json=test-keyset")
            .run { context ->
                val properties = context.getBean(EncryptionProperties::class.java)

                assertThat(properties.keyset.json).isEqualTo("test-keyset")
            }
    }

    @Test
    fun `uses default encryption properties when absent`() {
        contextRunner.run { context ->
            val properties = context.getBean(EncryptionProperties::class.java)

            assertThat(properties.keyset.json).isEmpty()
        }
    }
}
