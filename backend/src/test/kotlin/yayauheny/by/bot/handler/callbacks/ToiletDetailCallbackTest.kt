package yayauheny.by.bot.handler.callbacks

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.FormatterService
import yayauheny.by.bot.service.RestroomAvailabilityResolver
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.subway.SubwayLineResponseDto
import yayauheny.by.model.subway.SubwayStationResponseDto
import yayauheny.by.service.RestroomService
import yayauheny.by.service.import.schedule.OsmScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter
import yayauheny.by.service.import.schedule.YandexScheduleAdapter

class ToiletDetailCallbackTest {
    private val sender = mock(MessageSender::class.java)
    private val searchService = mock(RestroomService::class.java)
    private val analyticsService = mock(AnalyticsService::class.java)
    private val formatterService = createFormatterService()
    private val inlineKeyboard = InlineKeyboardFactory(formatterService)
    private val handler = ToiletDetailCallback(sender, searchService, analyticsService, formatterService, inlineKeyboard)

    @Test
    fun `should render detail with keyboard for valid id`() {
        val restroomId = UUID.randomUUID()
        val expectedRestroom = sampleRestroom(restroomId)
        `when`(searchService.getRestroomById(restroomId)).thenReturn(expectedRestroom)

        val update = detailUpdate("detail:$restroomId")
        val ctx = UpdateContext.from(update)
        val expectedKeyboard = inlineKeyboard.toiletDetail(expectedRestroom)
        val expectedText = formatterService.toiletDetail(expectedRestroom, expectedRestroom.distanceMeters?.toDouble())

        handler.handle(update, ctx)

        verify(searchService).getRestroomById(restroomId)
        verify(sender).editOrReply(ctx, expectedText, expectedKeyboard)
        verify(analyticsService).trackProductEvent(
            AnalyticsEventCommand(
                event = ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED,
                tgUserId = 10L,
                tgChatId = "123",
                username = "test-user-10",
                source = AnalyticsSource.TELEGRAM_BOT,
                metadata = mapOf("restroom_id" to restroomId.toString())
            )
        )

        assertThat(expectedKeyboard.keyboard).isNotEmpty
    }

    @Test
    fun `should reject empty id without building keyboard`() {
        val update = detailUpdate("detail:")
        val ctx = UpdateContext.from(update)

        handler.handle(update, ctx)

        verifyNoInteractions(searchService)
        verify(sender).editOrReply(ctx, Messages.SOMETHING_WENT_WRONG, null)
    }

    @Test
    fun `should reject malformed id without building keyboard`() {
        val update = detailUpdate("detail:not-a-uuid")
        val ctx = UpdateContext.from(update)

        handler.handle(update, ctx)

        verifyNoInteractions(searchService)
        verify(sender).editOrReply(ctx, Messages.SOMETHING_WENT_WRONG, null)
    }

    private fun detailUpdate(callbackData: String): Update {
        val update = Update()
        val callbackQuery = CallbackQuery()
        callbackQuery.data = callbackData
        callbackQuery.id = "query-id"
        val message = Message()
        message.messageId = 1
        message.chat = Chat.builder().id(123L).type("private").build()
        callbackQuery.message = message
        callbackQuery.from = User.builder().id(10L).isBot(false).firstName("Test").userName("test-user-10").build()
        update.callbackQuery = callbackQuery
        return update
    }

    private fun sampleRestroom(restroomId: UUID): RestroomResponseDto {
        val now = Instant.parse("2025-01-01T00:00:00Z")
        return RestroomResponseDto(
            id = restroomId,
            cityId = UUID.randomUUID(),
            cityName = "Minsk",
            buildingId = UUID.randomUUID(),
            subwayStationId = UUID.randomUUID(),
            name = "Test restroom",
            address = "Address",
            phones = emptyMap(),
            workTime = emptyMap(),
            feeType = FeeType.FREE,
            genderType = null,
            accessibilityType = AccessibilityType.UNKNOWN,
            placeType = PlaceType.OTHER,
            coordinates = Coordinates(53.9, 27.56),
            dataSource = DataSourceType.USER,
            status = RestroomStatus.ACTIVE,
            amenities = emptyMap(),
            externalMaps = mapOf("2gis" to "abc123"),
            accessNote = null,
            directionGuide = null,
            inheritBuildingSchedule = false,
            hasPhotos = false,
            locationType = LocationType.UNKNOWN,
            originProvider = ImportProvider.USER,
            originId = null,
            isHidden = false,
            createdAt = now,
            updatedAt = now,
            distanceMeters = 100,
            building =
                BuildingResponseDto(
                    id = UUID.randomUUID(),
                    cityId = UUID.randomUUID(),
                    name = "HQ",
                    address = "Some address",
                    buildingType = PlaceType.OTHER,
                    workTime = emptyMap(),
                    coordinates = Coordinates(53.91, 27.57),
                    externalIds = emptyMap(),
                    isDeleted = false,
                    createdAt = now,
                    updatedAt = now
                ),
            subwayStation =
                SubwayStationResponseDto(
                    id = UUID.randomUUID(),
                    subwayLineId = UUID.randomUUID(),
                    nameRu = "Victory Square",
                    nameEn = "Victory Square",
                    isTransfer = false,
                    coordinates = Coordinates(53.92, 27.58),
                    isDeleted = false,
                    createdAt = now,
                    line =
                        SubwayLineResponseDto(
                            id = UUID.randomUUID(),
                            cityId = UUID.randomUUID(),
                            nameRu = "Red",
                            nameEn = "Red",
                            shortCode = "1",
                            hexColor = "#FF0000",
                            isDeleted = false,
                            createdAt = now
                        )
                )
        )
    }

    private fun createFormatterService(): FormatterService {
        val resolver = RestroomAvailabilityResolver(
            ScheduleMappingService(listOf(TwoGisScheduleAdapter(), YandexScheduleAdapter(), OsmScheduleAdapter())),
            Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC)
        )
        return FormatterService(resolver)
    }
}
