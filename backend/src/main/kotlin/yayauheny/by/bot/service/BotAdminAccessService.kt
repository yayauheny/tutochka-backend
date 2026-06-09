package yayauheny.by.bot.service

import org.springframework.stereotype.Service
import yayauheny.by.common.config.TelegramBotProperties

@Service
class BotAdminAccessService(
    properties: TelegramBotProperties
) {
    private val adminIds =
        properties.adminIds
            .asSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .mapNotNull(String::toLongOrNull)
            .toSet()

    fun isAdmin(userId: Long): Boolean = userId in adminIds
}
