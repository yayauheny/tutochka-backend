package yayauheny.by.bot.keyboard

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import yayauheny.by.bot.service.FormatterService
import yayauheny.by.bot.service.RestroomAvailabilityResolver
import yayauheny.by.bot.util.Links
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
import yayauheny.by.service.import.schedule.OsmScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter
import yayauheny.by.service.import.schedule.YandexScheduleAdapter

class InlineKeyboardFactoryTest {
    private val factory = InlineKeyboardFactory(createFormatterService())

    @Test
    fun `toiletDetail renders url buttons with same layout`() {
        val keyboard = factory.toiletDetail(sampleRestroom(emptyMap()))

        assertThat(keyboard.keyboard).hasSize(3)
        assertThat(keyboard.keyboard[0]).hasSize(2)
        assertThat(keyboard.keyboard[1]).hasSize(2)
        assertThat(keyboard.keyboard[2]).hasSize(1)

        assertMapButton(keyboard.keyboard[0][0], "🗺 Яндекс Карты")
        assertMapButton(keyboard.keyboard[0][1], "🗺 Google Maps")
        assertMapButton(keyboard.keyboard[1][0], "🗺 2ГИС")
        assertMapButton(keyboard.keyboard[1][1], "🗺 Apple Maps")
        assertThat(keyboard.keyboard[2][0].text).isEqualTo("← Назад")
        assertThat(keyboard.keyboard[2][0].callbackData).isEqualTo("back:list")
    }

    @Test
    fun `twoGis button uses branch id when available`() {
        val keyboard = factory.toiletDetail(sampleRestroom(mapOf("2gis" to "abc123")))
        val button = keyboard.keyboard[1][0]

        assertThat(button.text).isEqualTo("🗺 2ГИС")
        assertThat(button.url).isEqualTo(Links.twoGisById("abc123"))
        assertThat(button.callbackData).isNull()
    }

    private fun assertMapButton(button: org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton, expectedText: String) {
        assertThat(button.text).isEqualTo(expectedText)
        assertThat(button.url).isNotBlank()
        assertThat(button.callbackData).isNull()
    }

    private fun sampleRestroom(externalMaps: Map<String, Any>): RestroomResponseDto {
        val now = Instant.parse("2025-01-01T00:00:00Z")
        return RestroomResponseDto(
            id = UUID.randomUUID(),
            cityId = UUID.randomUUID(),
            cityName = "Minsk",
            buildingId = UUID.randomUUID(),
            subwayStationId = UUID.randomUUID(),
            name = "Test restroom",
            address = "Address",
            phones = emptyMap(),
            workTime = emptyMap(),
            feeType = FeeType.UNKNOWN,
            genderType = null,
            accessibilityType = AccessibilityType.UNKNOWN,
            placeType = PlaceType.OTHER,
            coordinates = Coordinates(53.9, 27.56),
            dataSource = DataSourceType.USER,
            status = RestroomStatus.ACTIVE,
            amenities = emptyMap(),
            externalMaps = externalMaps,
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
