package yayauheny.by.bot.handler

import org.telegram.telegrambots.meta.api.objects.Update

interface MessageHandler {
    fun canHandle(update: Update, ctx: UpdateContext): Boolean
    @Throws(Exception::class)
    fun handle(update: Update, ctx: UpdateContext)
}
