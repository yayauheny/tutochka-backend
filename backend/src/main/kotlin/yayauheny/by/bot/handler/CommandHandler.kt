package yayauheny.by.bot.handler

import org.telegram.telegrambots.meta.api.objects.Update

interface CommandHandler {
    fun command(): String
    fun canHandle(update: Update): Boolean
    @Throws(Exception::class)
    fun handle(update: Update, ctx: UpdateContext)
}
