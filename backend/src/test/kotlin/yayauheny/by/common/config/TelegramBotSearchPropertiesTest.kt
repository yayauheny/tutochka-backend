package yayauheny.by.common.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class TelegramBotSearchPropertiesTest {
    private val contextRunner =
        ApplicationContextRunner()
            .withUserConfiguration(TestConfig::class.java)

    @EnableConfigurationProperties(TelegramBotSearchProperties::class)
    class TestConfig

    @Test
    fun `binds search properties from configuration`() {
        contextRunner
            .withPropertyValues(
                "telegram.bot.search.default-radius-meters=12000",
                "telegram.bot.search.nearest-limit=7"
            ).run { context ->
                val properties = context.getBean(TelegramBotSearchProperties::class.java)

                assertThat(properties.defaultRadiusMeters).isEqualTo(12000)
                assertThat(properties.nearestLimit).isEqualTo(7)
            }
    }

    @Test
    fun `uses defaults when search properties are absent`() {
        contextRunner.run { context ->
            val properties = context.getBean(TelegramBotSearchProperties::class.java)

            assertThat(properties.defaultRadiusMeters).isEqualTo(15000)
            assertThat(properties.nearestLimit).isEqualTo(5)
        }
    }
}
