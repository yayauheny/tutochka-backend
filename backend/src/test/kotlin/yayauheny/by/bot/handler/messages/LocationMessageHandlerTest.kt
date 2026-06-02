package yayauheny.by.bot.handler.messages

import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
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

class LocationMessageHandlerTest {
    private val sender = mock(MessageSender::class.java)
    private val searchService = mock(RestroomService::class.java)
    private val analyticsService = mock(AnalyticsService::class.java)
    private val userService = mock(UserService::class.java)
    private val backListSnapshotCache = mock(BackListSnapshotCache::class.java)
    private val formatterService = FormatterService()
    private val inlineKeyboard = InlineKeyboardFactory(formatterService)
    private val replyKeyboard = ReplyKeyboardFactory()
    private val handler =
        LocationMessageHandler(
            sender,
            searchService,
            analyticsService,
            userService,
            backListSnapshotCache,
            formatterService,
            inlineKeyboard,
            replyKeyboard
        )

    private val ctx =
        UpdateContext(
            chatId = 123L,
            userId = 456L,
            username = "test-user-10",
            text = null,
            hasLocation = true,
            latitude = 53.9,
            longitude = 27.56,
            isCallback = false,
            callbackData = null,
            messageId = 1
        )

    @BeforeEach
    fun setUp() {
        `when`(userService.saveLocation(456L, 53.9, 27.56))
            .thenReturn(UserSession.withLocation(UserSession.Location(53.9, 27.56), UserService.DEFAULT_RADIUS))
    }

    @Test
    fun `handle saves location before searching and stores results`() {
        val results = listOf(sampleItem("Test restroom"))
        `when`(searchService.findNearestRestrooms(53.9, 27.56, 5, UserService.DEFAULT_RADIUS))
            .thenReturn(results)
        val expectedKeyboard = inlineKeyboard.toiletList(results)

        handler.handle(Update(), ctx)

        val order = inOrder(userService, searchService, analyticsService, sender, backListSnapshotCache)
        order.verify(userService).saveLocation(456L, 53.9, 27.56)
        order.verify(searchService).findNearestRestrooms(53.9, 27.56, 5, UserService.DEFAULT_RADIUS)
        order.verify(analyticsService).trackNearestRestroomsSearch(
            AnalyticsIdentityHeaders(456L, "123", "test-user-10", AnalyticsSource.TELEGRAM_BOT),
            1,
            53.9,
            27.56
        )

        order.verify(sender).sendText(123L, formatterService.toiletsFound(1), expectedKeyboard)
        order.verify(backListSnapshotCache).store(123L, 456L, UserService.DEFAULT_RADIUS, results)
        assertThat(expectedKeyboard.keyboard).isNotEmpty()
    }

    @Test
    fun `handle sends no results flow without storing cache`() {
        `when`(searchService.findNearestRestrooms(53.9, 27.56, 5, UserService.DEFAULT_RADIUS))
            .thenReturn(emptyList())
        val expectedInlineKeyboard = inlineKeyboard.radiusSelection()
        val expectedReplyKeyboard = replyKeyboard.shareLocation()

        handler.handle(Update(), ctx)

        verify(userService).saveLocation(456L, 53.9, 27.56)
        verify(searchService).findNearestRestrooms(53.9, 27.56, 5, UserService.DEFAULT_RADIUS)
        verify(analyticsService).trackNearestRestroomsSearch(
            AnalyticsIdentityHeaders(456L, "123", "test-user-10", AnalyticsSource.TELEGRAM_BOT),
            0,
            53.9,
            27.56
        )
        verify(sender).sendText(123L, Messages.NO_TOILETS_FOUND, expectedInlineKeyboard)
        verify(sender).sendText(123L, "Или попробуйте другую точку:", expectedReplyKeyboard)
        assertThat(expectedInlineKeyboard.keyboard).isNotEmpty()
        assertThat(expectedReplyKeyboard.keyboard).isNotEmpty()
        verify(backListSnapshotCache, never()).store(123L, 456L, UserService.DEFAULT_RADIUS, emptyList())
    }

    @Test
    fun `handle sends error when coordinates are missing without searching`() {
        val missingCtx = ctx.copy(latitude = null, longitude = null, hasLocation = false)

        handler.handle(Update(), missingCtx)

        verify(sender).sendText(123L, Messages.SOMETHING_WENT_WRONG)
        verify(userService, never()).saveLocation(456L, 53.9, 27.56)
        verify(searchService, never()).findNearestRestrooms(53.9, 27.56, 5, UserService.DEFAULT_RADIUS)
        verify(backListSnapshotCache, never()).store(123L, 456L, UserService.DEFAULT_RADIUS, emptyList())
    }

    @Test
    fun `handle uses stored session radius after user expanded search`() {
        val expandedRadius = 2000
        val expandedSession = UserSession.withLocation(UserSession.Location(53.9, 27.56), expandedRadius)
        val results = listOf(sampleItem("Test restroom"))
        `when`(userService.saveLocation(456L, 53.9, 27.56)).thenReturn(expandedSession)
        `when`(searchService.findNearestRestrooms(53.9, 27.56, 5, expandedRadius))
            .thenReturn(results)
        val expectedKeyboard = inlineKeyboard.toiletList(results)

        handler.handle(Update(), ctx)

        verify(userService).saveLocation(456L, 53.9, 27.56)
        verify(searchService).findNearestRestrooms(53.9, 27.56, 5, expandedRadius)
        verify(sender).sendText(123L, formatterService.toiletsFound(1), expectedKeyboard)
        verify(backListSnapshotCache).store(123L, 456L, expandedRadius, results)
    }

    @Test
    fun `handle propagates search exception without partial response`() {
        `when`(searchService.findNearestRestrooms(53.9, 27.56, 5, UserService.DEFAULT_RADIUS))
            .thenThrow(RuntimeException("boom"))

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException::class.java) {
            handler.handle(Update(), ctx)
        }

        verify(userService).saveLocation(456L, 53.9, 27.56)
        verify(searchService).findNearestRestrooms(53.9, 27.56, 5, UserService.DEFAULT_RADIUS)
        verifyNoInteractions(sender)
        verify(backListSnapshotCache, never()).store(123L, 456L, UserService.DEFAULT_RADIUS, listOf(sampleItem("Test restroom")))
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
