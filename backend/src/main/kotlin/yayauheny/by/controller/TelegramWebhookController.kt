package yayauheny.by.controller

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.service.BotUpdateService
import yayauheny.by.common.config.TelegramBotProperties

@RestController
class TelegramWebhookController(
    private val botUpdateService: BotUpdateService,
    private val properties: TelegramBotProperties
) {

    @PostMapping("\${telegram.bot.webhook-path}")
    fun handleWebhook(@RequestBody update: Update) {
        botUpdateService.process(update)
    }
}
