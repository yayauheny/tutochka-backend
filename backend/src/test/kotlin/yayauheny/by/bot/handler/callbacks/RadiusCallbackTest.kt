package yayauheny.by.bot.handler.callbacks

import java.util.Optional
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.cache.BackListSnapshotCache
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.FormatterService
import yayauheny.by.bot.service.UserService
import yayauheny.by.bot.session.UserSession
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.service.RestroomService

class RadiusCallbackTest {
    private val sender = mock(MessageSender::class.java)
    private val userService = mock(UserService::class.java)
    private val searchService = mock(RestroomService::class.java)
    private val analyticsService = mock(AnalyticsService::class.java)
    private val backListSnapshotCache = mock(BackListSnapshotCache::class.java)
    private val formatterService = mock(FormatterService::class.java)
    private val inlineKeyboard = mock(InlineKeyboardFactory::class.java)
    private val replyKeyboard = mock(ReplyKeyboardFactory::class.java)
    private val handler =
        RadiusCallback(
            sender,
            userService,
            searchService,
            analyticsService,
            backListSnapshotCache,
            formatterService,
            inlineKeyboard,
            replyKeyboard
        )

    @BeforeEach
    fun setUp() {
        `when`(formatterService.toiletsFound(1)).thenReturn("Найдено 1 туалетов поблизости:")
    }

    @Test
    fun `handle radius callback with session location`() {
        val chatId = 123L
        val userId = 456L
        val radius = 1000
        val lat = 53.9
        val lon = 27.56

        val update = callbackUpdate(chatId, userId, "radius:$radius")
        val ctx = UpdateContext.from(update)
        val session = UserSession.withLocation(UserSession.Location(lat, lon), UserService.DEFAULT_RADIUS)
        `when`(userService.getSession(userId)).thenReturn(Optional.of(session))
        val results = listOf(sampleItem("Test restroom"))
        val keyboard = mock(org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup::class.java)
        `when`(searchService.findNearestRestrooms(lat, lon, 5, radius)).thenReturn(results)
        `when`(formatterService.toiletsFound(1)).thenReturn("Найдено 1 туалетов поблизости:")
        `when`(inlineKeyboard.toiletList(results)).thenReturn(keyboard)

        handler.handle(update, ctx)

        verify(userService).getSession(userId)
        verify(searchService).findNearestRestrooms(lat, lon, 5, radius)
        verify(sender).editOrReply(ctx, "Найдено 1 туалетов поблизости:", keyboard)
        verify(backListSnapshotCache).store(chatId, userId, radius, results)
        verify(analyticsService).trackNearestRestroomsSearch(
            AnalyticsIdentityHeaders(userId, chatId.toString(), ctx.username, AnalyticsSource.TELEGRAM_BOT),
            1,
            lat,
            lon
        )
    }

    @Test
    fun `handle asks for location when session is missing`() {
        val chatId = 123L
        val userId = 456L
        val update = callbackUpdate(chatId, userId, "radius:1000")
        val ctx = UpdateContext.from(update)
        val keyboard = mock(ReplyKeyboardMarkup::class.java)
        `when`(replyKeyboard.shareLocation()).thenReturn(keyboard)
        `when`(userService.getSession(userId)).thenReturn(Optional.empty())

        handler.handle(update, ctx)

        verify(sender).editOrReply(ctx, Messages.LOCATION_NOT_FOUND, null)
        verify(sender).sendText(chatId, Messages.LOCATION_REQUEST, keyboard)
        verifyNoInteractions(searchService)
    }

    @Test
    fun `handle invalid radius`() {
        val update = callbackUpdate(123L, 456L, "radius:invalid")
        val ctx = UpdateContext.from(update)

        handler.handle(update, ctx)

        verifyNoInteractions(userService)
        verify(sender).editOrReply(ctx, "❌ Ошибка: неверный радиус", null)
    }

    private fun callbackUpdate(chatId: Long, userId: Long, callbackData: String): Update {
        val update = Update()
        val callbackQuery = CallbackQuery()
        callbackQuery.data = callbackData
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
