package yayauheny.by.bot.service

import java.time.Instant
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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

class FormatterServiceTest {
    private val formatterService = FormatterService()
    private val now = Instant.parse("2025-01-01T00:00:00Z")

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
            building = sampleBuilding(workTime = mapOf("is_24x7" to true))
        )

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).contains("Круглосуточно")
    }

    @Test
    fun `toiletDetail shows neutral tags when no work schedule is available`() {
        val restroom = sampleRestroom().copy(
            workTime = null,
            inheritBuildingSchedule = false,
            building = null
        )

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).contains("<b>—</b>")
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
    fun `toiletDetail suppresses long direction guide`() {
        val restroom = sampleRestroom().copy(directionGuide = "a".repeat(101))

        val formatted = formatterService.toiletDetail(restroom, null)

        assertThat(formatted).doesNotContain("Как найти:")
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
    fun `toiletsFound substitutes count`() {
        val text = formatterService.toiletsFound(3)

        assertThat(text).contains("3")
    }

    private fun sampleRestroom(): RestroomResponseDto {
        return RestroomResponseDto(
            id = UUID.randomUUID(),
            cityId = UUID.randomUUID(),
            cityName = "Minsk",
            buildingId = UUID.randomUUID(),
            subwayStationId = UUID.randomUUID(),
            name = "<b>Bad</b>",
            address = "Main <script>alert(1)</script> Street",
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
            externalMaps = emptyMap(),
            accessNote = null,
            directionGuide = "Find it near <b>exit</b>",
            inheritBuildingSchedule = false,
            hasPhotos = false,
            locationType = LocationType.UNKNOWN,
            originProvider = ImportProvider.USER,
            originId = null,
            isHidden = false,
            createdAt = now,
            updatedAt = now,
            distanceMeters = 120,
            building =
                BuildingResponseDto(
                    id = UUID.randomUUID(),
                    cityId = UUID.randomUUID(),
                    name = "HQ <script>",
                    address = "Some <i>address</i>",
                    buildingType = PlaceType.OTHER,
                    workTime = emptyMap(),
                    coordinates = Coordinates(53.91, 27.57),
                    externalIds = emptyMap(),
                    isDeleted = false,
                    createdAt = now,
                    updatedAt = now
                ),
            subwayStation =
                sampleSubwayStation()
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
        isDeleted = false,
        createdAt = now,
        updatedAt = now
    )

    private fun sampleSubwayStation() =
        SubwayStationResponseDto(
            id = UUID.randomUUID(),
            subwayLineId = UUID.randomUUID(),
            nameRu = "Victory <b>Square</b>",
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

    private fun sampleNearestRestroom(feeType: FeeType) =
        yayauheny.by.model.restroom.NearestRestroomSlimDto(
            id = UUID.randomUUID(),
            displayName = "Nearest",
            distanceMeters = 120.0,
            feeType = feeType,
            queryCoordinates = Coordinates(53.9, 27.56),
            restroomCoordinates = Coordinates(53.91, 27.57)
        )
}
