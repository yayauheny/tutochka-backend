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

    private fun sampleRestroom(): RestroomResponseDto {
        val now = Instant.parse("2025-01-01T00:00:00Z")
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
        )
    }
}
