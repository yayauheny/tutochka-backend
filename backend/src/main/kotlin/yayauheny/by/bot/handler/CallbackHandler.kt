package yayauheny.by.bot.handler

import org.telegram.telegrambots.meta.api.objects.Update

interface CallbackHandler {
    fun prefix(): String
    fun canHandle(callbackData: String): Boolean
    @Throws(Exception::class)
    fun handle(update: Update, ctx: UpdateContext)
}
