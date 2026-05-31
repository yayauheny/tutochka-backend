package yayauheny.by.bot.service

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.bot.metrics.BotMetrics
import yayauheny.by.bot.router.UpdateRouter

@Service
class UpdateHandlingService(
    private val router: UpdateRouter,
    private val botMetrics: BotMetrics
) {

    @Throws(Exception::class)
    fun handle(update: Update) {
        botMetrics.incrementTelegramUpdate(resolveUpdateType(update))
        router.route(update)
    }

    fun resolveUpdateType(update: Update): String {
        if (update.hasCallbackQuery()) {
            return "callback"
        }
        if (update.hasMessage()) {
            if (update.message.hasLocation()) {
                return "location"
            }
            if (update.message.hasText() && update.message.text.startsWith("/")) {
                return "command"
            }
            return "message"
        }
        return "other"
    }
}
