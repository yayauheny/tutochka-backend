package yayauheny.by.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.service.UpdateHandlingService

@Service
class BotUpdateServiceImpl(
    private val updateHandlingService: UpdateHandlingService
) : BotUpdateService {
    private val log = LoggerFactory.getLogger(BotUpdateServiceImpl::class.java)

    override fun process(update: Update) {
        val ctx = UpdateContext.from(update)
        val updateType = updateHandlingService.resolveUpdateType(update)
        val startedAt = System.nanoTime()
        log.info(
            "Webhook update received: updateId={}, type={}, chatId={}, userId={}",
            update.updateId,
            updateType,
            ctx.chatId,
            ctx.userId
        )
        
        try {
            updateHandlingService.handle(update)
        } catch (e: Exception) {
            log.error("Error processing update", e)
            throw e
        }
        
        val durationMs = (System.nanoTime() - startedAt) / 1_000_000
        log.info(
            "Webhook update processed: updateId={}, type={}, chatId={}, userId={}, durationMs={}",
            update.updateId,
            updateType,
            ctx.chatId,
            ctx.userId,
            durationMs
        )
    }
}
