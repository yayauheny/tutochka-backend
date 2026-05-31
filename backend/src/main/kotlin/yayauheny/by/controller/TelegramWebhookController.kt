package yayauheny.by.controller

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.service.BotUpdateService

@RestController
@ConditionalOnProperty(prefix = "telegram.bot", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class TelegramWebhookController(
    private val botUpdateService: BotUpdateService
) {

    @PostMapping("\${telegram.bot.webhook-path}")
    fun handleWebhook(@RequestBody update: Update) {
        botUpdateService.process(update)
    }
}
