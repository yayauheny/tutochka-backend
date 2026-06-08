package yayauheny.by.bot.handler.messages

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.analytics.service.BusinessAnalyticsPublisher
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.cache.BackListSnapshotCache
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.common.config.TelegramBotSearchProperties
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.FormatterService
import yayauheny.by.bot.service.RestroomAvailabilityResolver
import yayauheny.by.bot.service.UserService
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.service.RestroomService
import yayauheny.by.service.import.schedule.OsmScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter
import yayauheny.by.service.import.schedule.YandexScheduleAdapter

class LocationMessageHandlerTest {
    private val sender = mock(MessageSender::class.java)
    private val searchService = mock(RestroomService::class.java)
    private val businessAnalyticsPublisher = mock(BusinessAnalyticsPublisher::class.java)
    private val userService = mock(UserService::class.java)
    private val backListSnapshotCache = mock(BackListSnapshotCache::class.java)
    private val formatterService = createFormatterService()
    private val inlineKeyboard = InlineKeyboardFactory(formatterService)
    private val botSearchProperties = TelegramBotSearchProperties(defaultRadiusMeters = 15000, nearestLimit = 5)
    private val handler =
        LocationMessageHandler(
            sender,
            searchService,
            businessAnalyticsPublisher,
            userService,
            backListSnapshotCache,
            formatterService,
            inlineKeyboard,
            botSearchProperties
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

    @Test
    fun `handle saves location before searching and stores results`() {
        val results = listOf(sampleItem("Test restroom"))
        `when`(searchService.findNearestRestrooms(53.9, 27.56, 5, 15000))
            .thenReturn(results)
        val expectedKeyboard = inlineKeyboard.toiletList(results)

        handler.handle(Update(), ctx)

        val order = inOrder(userService, searchService, businessAnalyticsPublisher, sender, backListSnapshotCache)
        order.verify(userService).saveLocation(456L, 53.9, 27.56)
        order.verify(searchService).findNearestRestrooms(53.9, 27.56, 5, 15000)
        order.verify(businessAnalyticsPublisher).trackNearestRestroomsSearch(
            AnalyticsIdentityHeaders(456L, "123", "test-user-10", AnalyticsSource.TELEGRAM_BOT),
            1,
            53.9,
            27.56
        )

        order.verify(sender).sendText(123L, formatterService.toiletsFound(1), expectedKeyboard)
        order.verify(backListSnapshotCache).store(123L, 456L, 15000, results)
        assertThat(expectedKeyboard.keyboard).isNotEmpty()
    }

    @Test
    fun `handle sends no results flow without storing cache`() {
        `when`(searchService.findNearestRestrooms(53.9, 27.56, 5, 15000))
            .thenReturn(emptyList())

        handler.handle(Update(), ctx)

        verify(userService).saveLocation(456L, 53.9, 27.56)
        verify(searchService).findNearestRestrooms(53.9, 27.56, 5, 15000)
        verify(businessAnalyticsPublisher).trackNearestRestroomsSearch(
            AnalyticsIdentityHeaders(456L, "123", "test-user-10", AnalyticsSource.TELEGRAM_BOT),
            0,
            53.9,
            27.56
        )
        verify(sender).sendText(123L, Messages.NO_TOILETS_FOUND)
        verify(backListSnapshotCache, never()).store(123L, 456L, 15000, emptyList())
    }

    @Test
    fun `handle sends error when coordinates are missing without searching`() {
        val missingCtx = ctx.copy(latitude = null, longitude = null, hasLocation = false)

        handler.handle(Update(), missingCtx)

        verify(sender).sendText(123L, Messages.SOMETHING_WENT_WRONG)
        verify(userService, never()).saveLocation(456L, 53.9, 27.56)
        verify(searchService, never()).findNearestRestrooms(53.9, 27.56, 5, 15000)
        verify(backListSnapshotCache, never()).store(123L, 456L, 15000, emptyList())
    }

    @Test
    fun `handle propagates search exception without partial response`() {
        `when`(searchService.findNearestRestrooms(53.9, 27.56, 5, 15000))
            .thenThrow(RuntimeException("boom"))

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException::class.java) {
            handler.handle(Update(), ctx)
        }

        verify(userService).saveLocation(456L, 53.9, 27.56)
        verify(searchService).findNearestRestrooms(53.9, 27.56, 5, 15000)
        verifyNoInteractions(sender)
        verify(backListSnapshotCache, never()).store(123L, 456L, 15000, listOf(sampleItem("Test restroom")))
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

    private fun createFormatterService(): FormatterService {
        val resolver = RestroomAvailabilityResolver(
            ScheduleMappingService(listOf(TwoGisScheduleAdapter(), YandexScheduleAdapter(), OsmScheduleAdapter())),
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)
        )
        return FormatterService(resolver)
    }
}
