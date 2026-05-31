package yayauheny.by.service

import org.telegram.telegrambots.meta.api.objects.Update

interface BotUpdateService {
    fun process(update: Update)
}
