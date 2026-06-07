package integration.telegram

import integration.base.BaseIntegrationTest
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.test.context.TestPropertySource

@Tag("integration")
@TestPropertySource(
    properties = [
        "telegram.bot.enabled=false"
    ]
)
class BotDisabledApplicationContextIntegrationTest : BaseIntegrationTest() {

    @Test
    fun given_bot_disabled_when_application_context_starts_then_spring_context_loads() {
    }
}
