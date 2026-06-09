package yayauheny.by.bot.keyboard

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.BotAdminAccessService
import yayauheny.by.common.config.TelegramBotProperties

class ReplyKeyboardFactoryTest {
    private val accessService = BotAdminAccessService(TelegramBotProperties(adminIds = listOf("42")))
    private val factory = ReplyKeyboardFactory(accessService)

    @Test
    fun `shareLocation adds stats buttons for admin`() {
        val keyboard = factory.shareLocation(42L, true)

        assertThat(keyboard.keyboard).hasSize(1)
        assertThat(keyboard.keyboard[0].map { it.text }).containsExactly(Messages.BUTTON_SHARE_LOCATION, Messages.BUTTON_ANALYTICS)
    }

    @Test
    fun `helpAndLocation keeps default keyboard for non admin`() {
        val keyboard = factory.helpAndLocation(7L, true)

        assertThat(keyboard.keyboard).hasSize(1)
        assertThat(keyboard.keyboard[0].map { it.text }).containsExactly(Messages.BUTTON_SHARE_LOCATION, "/help")
    }

    @Test
    fun `shareLocation hides stats buttons outside private chat`() {
        val keyboard = factory.shareLocation(42L, false)

        assertThat(keyboard.keyboard).hasSize(1)
        assertThat(keyboard.keyboard[0].map { it.text }).containsExactly(Messages.BUTTON_SHARE_LOCATION)
    }
}
