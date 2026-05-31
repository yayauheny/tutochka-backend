package yayauheny.by.common.config

import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "telegram.bot", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class TelegramBotStartupValidator(
    private val properties: TelegramBotProperties
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) {
        require(properties.webhookSecretToken.isNotBlank()) {
            "telegram.bot.webhook-secret-token must be configured when bot is enabled"
        }
    }
}
