package yayauheny.by.service

import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class BotUpdateServiceImpl : BotUpdateService {
    override fun process(update: Update) {
        // To be implemented in Phase 5: Migrating Telegram Bot Update Processing
    }
}
