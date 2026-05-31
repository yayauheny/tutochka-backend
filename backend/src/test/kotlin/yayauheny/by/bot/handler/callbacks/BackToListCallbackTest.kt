package yayauheny.by.bot.handler.callbacks

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.cache.BackListSnapshotCache
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.metrics.BotMetrics
import yayauheny.by.bot.service.FormatterService
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.restroom.NearestRestroomSlimDto

class BackToListCallbackTest {
    private val sender = mock(MessageSender::class.java)
    private val replyKeyboard = mock(ReplyKeyboardFactory::class.java)
    private val formatterService = FormatterService()
    private val inlineKeyboardFactory = InlineKeyboardFactory(formatterService)
    private lateinit var cache: BackListSnapshotCache
    private lateinit var handler: BackToListCallback

    @BeforeEach
    fun setUp() {
        cache =
            BackListSnapshotCache.forTest(
                BotMetrics(SimpleMeterRegistry()),
                com.github.benmanes.caffeine.cache.Ticker.systemTicker(),
                object : Clock() {
                    override fun getZone() = java.time.ZoneId.of("UTC")
                    override fun withZone(zone: java.time.ZoneId) = this
                    override fun instant(): Instant = Instant.parse("2025-01-01T00:00:00Z")
                },
                Duration.ofMinutes(5),
                10
            )
        handler = BackToListCallback(sender, cache, formatterService, inlineKeyboardFactory, replyKeyboard)
    }

    @Test
    fun `handle restores cached list without backend`() {
        val chatId = 123L
        val userId = 456L
        val update = backUpdate(chatId, userId)
        val ctx = UpdateContext.from(update)
        val items = listOf(sampleItem("Test restroom"))
        cache.store(chatId, userId, 500, items)
        val expectedKeyboard = inlineKeyboardFactory.toiletList(items)

        handler.handle(update, ctx)

        verify(sender).editOrReply(ctx, formatterService.toiletsFound(1), expectedKeyboard)
        verify(replyKeyboard, never()).shareLocation()
        assertThat(expectedKeyboard.keyboard).isNotEmpty()
    }

    @Test
    fun `handle asks for location on cache miss`() {
        val chatId = 123L
        val userId = 456L
        val update = backUpdate(chatId, userId)
        val ctx = UpdateContext.from(update)
        val keyboard = mock(ReplyKeyboardMarkup::class.java)
        `when`(replyKeyboard.shareLocation()).thenReturn(keyboard)

        handler.handle(update, ctx)

        verify(sender).sendText(chatId, Messages.LOCATION_REQUEST, keyboard)
    }

    private fun backUpdate(chatId: Long, userId: Long): Update {
        val update = Update()
        val callbackQuery = CallbackQuery()
        callbackQuery.data = "back:list"
        callbackQuery.id = "query-id"
        val message = Message()
        message.messageId = 1
        message.chat = Chat.builder().id(chatId).type("private").build()
        callbackQuery.message = message
        callbackQuery.from = User.builder().id(userId).isBot(false).firstName("Test").build()
        update.callbackQuery = callbackQuery
        return update
    }

    private fun sampleItem(name: String): NearestRestroomSlimDto =
        NearestRestroomSlimDto(
            UUID.randomUUID(),
            name,
            123.0,
            FeeType.FREE,
            Coordinates(53.9, 27.56),
            Coordinates(53.9001, 27.5601)
        )
}
