package yayauheny.by.bot.service

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.location.Location
import org.telegram.telegrambots.meta.api.objects.message.Message
import yayauheny.by.bot.metrics.BotMetrics
import yayauheny.by.bot.router.UpdateRouter

class UpdateHandlingServiceTest {
    private val router = mock(UpdateRouter::class.java)
    private val botMetrics = mock(BotMetrics::class.java)
    private val service = UpdateHandlingService(router, botMetrics)

    @Test
    fun `resolveUpdateType returns callback`() {
        val update = Update()
        update.callbackQuery =
            CallbackQuery().apply {
                data = "detail:abc"
            }

        org.assertj.core.api.Assertions.assertThat(service.resolveUpdateType(update)).isEqualTo("callback")
    }

    @Test
    fun `resolveUpdateType returns location`() {
        val update = Update()
        update.message =
            Message().apply {
                chat = Chat.builder().id(1L).type("private").build()
                location = Location.builder().latitude(53.9).longitude(27.56).build()
            }

        org.assertj.core.api.Assertions.assertThat(service.resolveUpdateType(update)).isEqualTo("location")
    }

    @Test
    fun `resolveUpdateType returns command`() {
        val update = Update()
        update.message =
            Message().apply {
                chat = Chat.builder().id(1L).type("private").build()
                text = "/start"
            }

        org.assertj.core.api.Assertions.assertThat(service.resolveUpdateType(update)).isEqualTo("command")
    }

    @Test
    fun `resolveUpdateType returns message`() {
        val update = Update()
        update.message =
            Message().apply {
                chat = Chat.builder().id(1L).type("private").build()
                text = "hello"
                from = User.builder().id(1L).isBot(false).firstName("Test").build()
            }

        org.assertj.core.api.Assertions.assertThat(service.resolveUpdateType(update)).isEqualTo("message")
    }

    @Test
    fun `resolveUpdateType returns other`() {
        org.assertj.core.api.Assertions.assertThat(service.resolveUpdateType(Update())).isEqualTo("other")
    }

    @Test
    fun `handle increments metrics and propagates router errors`() {
        val update = Update()
        update.message =
            Message().apply {
                chat = Chat.builder().id(1L).type("private").build()
                text = "hello"
                from = User.builder().id(1L).isBot(false).firstName("Test").build()
            }

        org.mockito.Mockito.doThrow(RuntimeException("boom")).`when`(router).route(update)

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException::class.java) {
            service.handle(update)
        }

        verify(botMetrics).incrementTelegramUpdate("message")
    }
}
