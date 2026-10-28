package yayauheny.by.bot.service

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.subway.SubwayLineResponseDto
import yayauheny.by.model.subway.SubwayStationResponseDto
import yayauheny.by.service.import.schedule.OsmScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter
import yayauheny.by.service.import.schedule.YandexScheduleAdapter

class FormatterServiceTest {
    private val now = Instant.parse("2024-12-30T09:00:00Z")
    private val formatterService = formatterServiceAt(now)

    @Test
    fun `toiletDetail escapes html in dynamic content`() {
        val restroom = sampleRestroom()

        val formatted = formatterService.toiletDetail(restroom, 42.0)

        assertThat(formatted).contains("&lt;b&gt;Bad&lt;/b&gt;")
        assertThat(formatted).contains("Main &lt;script&gt;alert(1)&lt;/script&gt; Street")
        assertThat(formatted).contains("Find it near &lt;b&gt;exit&lt;/b&gt;")
        assertThat(formatted).contains("HQ &lt;script&gt;")
        assertThat(formatted).contains("Victory &lt;b&gt;Square&lt;/b&gt;")
        assertThat(formatted).doesNotContain("<script>alert(1)</script>")
        assertThat(formatted).doesNotContain("<b>Bad</b>")
    }

    @Test
    fun `toiletDetail falls back to building display name when restroom name is blank`() {
        val restroom = sampleRestroom().copy(name = "Туалет")

        val formatted = formatterService.toiletDetail(restroom, 42.0)

        assertThat(formatted).contains("HQ &lt;script&gt;")
        assertThat(formatted).doesNotContain(">Туалет<")
    }

    @Test
    fun `toiletDetail falls back to restroom default when neither restroom name nor building display name exists`() {
        val restroom = sampleRestroom().copy(
            name = "",
            building = sampleBuilding(name = "", address = "")
        )

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).contains("Туалет")
    }

    @Test
    fun `toiletDetail falls back to building address when restroom address is blank`() {
        val restroom = sampleRestroom().copy(address = " ", building = sampleBuilding(address = "Building address"))

        val formatted = formatterService.toiletDetail(restroom, 42.0)

        assertThat(formatted).contains("Building address")
    }

    @Test
    fun `toiletDetail uses building schedule when inheritBuildingSchedule is true`() {
        val restroom = sampleRestroom().copy(
            workTime = null,
            inheritBuildingSchedule = true,
            building = sampleBuilding(workTime = sampleTwoGisSchedule())
        )

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).contains("🟢 Туалет открыт сейчас")
        assertThat(formatted).contains("Пн: 09:00-18:00")
    }

    @Test
    fun `toiletDetail shows yellow unknown text when no work schedule is available`() {
        val restroom = sampleRestroom().copy(
            workTime = null,
            inheritBuildingSchedule = false,
            building = null
        )

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).contains("<b>—</b>")
        assertThat(formatted).contains("🟡 Время работы неизвестно")
        assertThat(formatted).contains("Время работы не указано")
        assertThat(formatted).doesNotContain("Круглосуточно")
    }

    @Test
    fun `toiletDetail formats fee and accessibility tags`() {
        val restroom = sampleRestroom().copy(
            feeType = FeeType.PAID,
            accessibilityType = AccessibilityType.WHEELCHAIR
        )

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).contains("Платно")
        assertThat(formatted).contains("МГН")
    }

    @Test
    fun `toiletDetail shows green status for open schedule`() {
        val restroom = sampleRestroom().copy(workTime = sampleTwoGisSchedule())

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).contains("🟢 Туалет открыт сейчас")
        assertThat(formatted).contains("<b>Время работы:</b>")
        assertThat(formatted).contains("Пн: 09:00-18:00")
    }

    @Test
    fun `toiletDetail shows red status for closed schedule`() {
        val closedFormatter = formatterServiceAt(Instant.parse("2024-12-30T20:00:00Z"))
        val restroom = sampleRestroom().copy(workTime = sampleTwoGisSchedule())

        val formatted = closedFormatter.toiletDetail(restroom, null)

        assertThat(formatted).contains("🔴 Туалет закрыт")
    }

    @Test
    fun `toiletDetail shows yellow unknown text for unsupported osm schedule`() {
        val restroom = sampleRestroom(
            originProvider = ImportProvider.OSM,
            workTime = mapOf("opening_hours" to "Mo-Fr 09:00-18:00")
        )

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).contains("🟡 Время работы неизвестно")
        assertThat(formatted).contains("Mo-Fr 09:00-18:00")
    }

    @Test
    fun `toiletDetail suppresses long direction guide`() {
        val restroom = sampleRestroom().copy(directionGuide = "a".repeat(101))

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).doesNotContain("Как найти:")
    }

    @Test
    fun `colorToEmoji maps known colors and falls back for unknown`() {
        assertThat(formatterService.colorToEmoji("#FF0000")).isEqualTo("🔴")
        assertThat(formatterService.colorToEmoji("#does-not-exist")).isEqualTo("🚇")
        assertThat(formatterService.colorToEmoji(null)).isEqualTo("🚇")
    }

    @Test
    fun `toiletDetail renders landmark line for subway only`() {
        val restroom = sampleRestroom().copy(building = null)

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).contains("Ориентир:")
        assertThat(formatted).contains("Метро Victory &lt;b&gt;Square&lt;/b&gt;")
    }

    @Test
    fun `toiletListItem includes fee icon only when fee type is known`() {
        val paid = formatterService.toiletListItem(sampleNearestRestroom(feeType = FeeType.PAID))
        val unknown = formatterService.toiletListItem(sampleNearestRestroom(feeType = FeeType.UNKNOWN))

        assertThat(paid).contains("💰")
        assertThat(unknown).doesNotContain("💰")
        assertThat(unknown).doesNotContain("🆓")
    }

    @Test
    fun `toiletListItem formats fallback name and distance without fee separator for unknown fee`() {
        val item = formatterService.toiletListItem(
            sampleNearestRestroom(feeType = FeeType.UNKNOWN).copy(
                displayName = "   ",
                distanceMeters = 120.0
            )
        )

        assertThat(item).isEqualTo("🚶 120 м  Туалет")
    }

    @Test
    fun `toiletListItem formats kilometers and free icon`() {
        val item = formatterService.toiletListItem(
            sampleNearestRestroom(feeType = FeeType.FREE).copy(
                displayName = "Nearest",
                distanceMeters = 1500.0
            )
        )

        assertThat(item).isEqualTo("🚶 1,5 км  🆓  Nearest")
    }

    @Test
    fun `toiletDetail ignores building schedule when schedule inheritance is disabled`() {
        val restroom = sampleRestroom().copy(
            workTime = null,
            inheritBuildingSchedule = false,
            building = sampleBuilding(workTime = sampleTwoGisSchedule())
        )

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).contains("🟡 Время работы неизвестно")
        assertThat(formatted).doesNotContain("Пн: 09:00-18:00")
    }

    @Test
    fun `toiletsFound substitutes count`() {
        val text = formatterService.toiletsFound(3)

        assertThat(text).contains("3")
    }

    private fun formatterServiceAt(now: Instant): FormatterService {
        val resolver = RestroomAvailabilityResolver(
            ScheduleMappingService(listOf(TwoGisScheduleAdapter(), YandexScheduleAdapter(), OsmScheduleAdapter())),
            Clock.fixed(now, ZoneOffset.UTC)
        )
        return FormatterService(resolver)
    }

    private fun sampleRestroom(
        originProvider: ImportProvider = ImportProvider.TWO_GIS,
        workTime: Map<String, Any>? = emptyMap()
    ): RestroomResponseDto {
        return RestroomResponseDto(
            id = UUID.randomUUID(),
            cityId = UUID.randomUUID(),
            cityName = "Minsk",
            buildingId = UUID.randomUUID(),
            subwayStationId = UUID.randomUUID(),
            name = "<b>Bad</b>",
            address = "Main <script>alert(1)</script> Street",
            phones = emptyMap(),
            workTime = workTime,
            feeType = FeeType.UNKNOWN,
            genderType = null,
            accessibilityType = AccessibilityType.UNKNOWN,
            placeType = PlaceType.OTHER,
            coordinates = Coordinates(53.9, 27.56),
            dataSource = DataSourceType.USER,
            status = RestroomStatus.ACTIVE,
            amenities = emptyMap(),
            externalMaps = emptyMap(),
            accessNote = null,
            directionGuide = "Find it near <b>exit</b>",
            inheritBuildingSchedule = false,
            hasPhotos = false,
            locationType = LocationType.UNKNOWN,
            originProvider = originProvider,
            originId = null,
            isHidden = false,
            moderationStatus = "PENDING",
            createdAt = now,
            updatedAt = now,
            distanceMeters = 120,
            building = sampleBuilding(),
            subwayStation = sampleSubwayStation()
        )
    }

    private fun sampleBuilding(
        name: String = "HQ <script>",
        address: String = "Some <i>address</i>",
        workTime: Map<String, Any>? = emptyMap()
    ) = BuildingResponseDto(
        id = UUID.randomUUID(),
        cityId = UUID.randomUUID(),
        name = name,
            address = address,
            buildingType = PlaceType.OTHER,
            workTime = workTime,
            coordinates = Coordinates(53.91, 27.57),
            externalIds = emptyMap(),
            createdAt = now,
            updatedAt = now
        )

    private fun sampleSubwayStation() =
        SubwayStationResponseDto(
            id = UUID.randomUUID(),
            subwayLineId = UUID.randomUUID(),
            lineOrder = 1,
            slug = "victory-square",
            nameRu = "Victory <b>Square</b>",
            nameEn = "Victory Square",
            isTransfer = false,
            coordinates = Coordinates(53.92, 27.58),
            createdAt = now,
            line = SubwayLineResponseDto(
                id = UUID.randomUUID(),
                cityId = UUID.randomUUID(),
                code = "1",
                nameRu = "Red",
                nameEn = "Red",
                hexColor = "#FF0000",
                createdAt = now
            )
        )

    private fun sampleNearestRestroom(feeType: FeeType) =
        NearestRestroomSlimDto(
            id = UUID.randomUUID(),
            displayName = "Nearest",
            distanceMeters = 120.0,
            feeType = feeType,
            queryCoordinates = Coordinates(53.9, 27.56),
            restroomCoordinates = Coordinates(53.91, 27.57)
        )

    private fun sampleTwoGisSchedule(): Map<String, Any> {
        return mapOf(
            "Mon" to mapOf(
                "working_hours" to listOf(
                    mapOf(
                        "from" to "09:00",
                        "to" to "18:00"
                    )
                )
            )
        )
    }
}
