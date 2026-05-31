package yayauheny.by.common.config

import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.TelegramClient

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@ConditionalOnProperty(prefix = "telegram.bot", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class TelegramWebhookRegistrar(
    private val telegramClient: TelegramClient,
    private val properties: TelegramBotProperties
) : ApplicationRunner {
    private val log = LoggerFactory.getLogger(TelegramWebhookRegistrar::class.java)

    override fun run(args: ApplicationArguments) {
        val webhookUrl = properties.webhookPublicUrl.trim()
        if (webhookUrl.isBlank()) {
            log.info("Webhook URL not configured (telegram.bot.webhook-public-url), skipping webhook registration")
            return
        }

        try {
            val setWebhook = SetWebhook.builder()
                .url(webhookUrl)
                .secretToken(properties.webhookSecretToken)
                .build()
            telegramClient.execute(setWebhook)
            log.info("Webhook registered successfully: {}", webhookUrl)
        } catch (e: TelegramApiException) {
            log.error("Failed to register webhook: {}", webhookUrl, e)
            throw e
        }
    }
}
