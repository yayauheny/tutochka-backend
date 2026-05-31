package yayauheny.by.unit.service

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import yayauheny.by.bot.service.UpdateHandlingService
import yayauheny.by.service.BotUpdateServiceImpl

class BotUpdateServiceImplTest {
    private val updateHandlingService = mock(UpdateHandlingService::class.java)
    private val service = BotUpdateServiceImpl(updateHandlingService)

    @Test
    fun `process resolves update type and delegates to updateHandlingService`() {
        val update = messageUpdate("/start")
        `when`(updateHandlingService.resolveUpdateType(update)).thenReturn("command")

        service.process(update)

        verify(updateHandlingService).resolveUpdateType(update)
        verify(updateHandlingService).handle(update)
    }

    @Test
    fun `process rethrows handler exceptions`() {
        val update = messageUpdate("hello")
        `when`(updateHandlingService.resolveUpdateType(update)).thenReturn("message")
        doThrow(RuntimeException("boom")).`when`(updateHandlingService).handle(update)

        assertThrows(RuntimeException::class.java) {
            service.process(update)
        }

        verify(updateHandlingService).handle(update)
    }

    private fun messageUpdate(text: String): Update {
        val update = Update()
        update.updateId = 11
        update.message =
            Message().apply {
                chat = Chat.builder().id(123L).type("private").build()
                this.text = text
                from = User.builder().id(77L).firstName("Test").isBot(false).build()
                messageId = 10
            }
        return update
    }
}
