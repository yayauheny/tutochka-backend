package yayauheny.by.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "telegram.bot.search")
data class TelegramBotSearchProperties(
    val defaultRadiusMeters: Int = 15_000,
    val nearestLimit: Int = 5
)
