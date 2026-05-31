package yayauheny.by.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import yayauheny.by.model.analytics.AcquisitionTag
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.imports.ImportBatchResponseDto
import yayauheny.by.model.imports.ImportItemResultDto
import yayauheny.by.model.imports.ImportStatus
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.subway.SubwayLineResponseDto
import yayauheny.by.model.subway.SubwayStationResponseDto

class ContractSerializationTest {
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun restroomResponseDto_roundTrips_with_json_fields() {
        val restroom =
            RestroomResponseDto(
                id = UUID.randomUUID(),
                cityId = UUID.randomUUID(),
                cityName = "Minsk",
                buildingId = UUID.randomUUID(),
                subwayStationId = UUID.randomUUID(),
                name = "Central restroom",
                address = "Main street",
                phones = mapOf("main" to "+375291234567"),
                workTime = mapOf("monday" to "08:00-20:00"),
                feeType = FeeType.FREE,
                genderType = null,
                accessibilityType = AccessibilityType.WHEELCHAIR,
                placeType = PlaceType.PUBLIC,
                coordinates = Coordinates(53.9, 27.56),
                dataSource = DataSourceType.MANUAL,
                status = RestroomStatus.ACTIVE,
                amenities = mapOf("wifi" to true),
                externalMaps = mapOf("2gis" to "abc123"),
                accessNote = "Lift available",
                directionGuide = "Near the lobby",
                inheritBuildingSchedule = true,
                hasPhotos = false,
                locationType = yayauheny.by.model.enums.LocationType.UNKNOWN,
                originProvider = ImportProvider.MANUAL,
                originId = "origin-1",
                isHidden = false,
                createdAt = Instant.parse("2025-01-01T00:00:00Z"),
                updatedAt = Instant.parse("2025-01-02T00:00:00Z"),
                distanceMeters = 42,
                building =
                    BuildingResponseDto(
                        id = UUID.randomUUID(),
                        cityId = UUID.randomUUID(),
                        name = "Mall",
                        address = "Main street",
                        buildingType = PlaceType.MALL,
                        workTime = mapOf("sunday" to "closed"),
                        coordinates = Coordinates(53.91, 27.57),
                        externalIds = mapOf("2gis" to "b-1"),
                        isDeleted = false,
                        createdAt = Instant.parse("2025-01-01T00:00:00Z"),
                        updatedAt = Instant.parse("2025-01-02T00:00:00Z")
                    ),
                subwayStation =
                    SubwayStationResponseDto(
                        id = UUID.randomUUID(),
                        subwayLineId = UUID.randomUUID(),
                        nameRu = "Площадь Победы",
                        nameEn = "Victory Square",
                        isTransfer = false,
                        coordinates = Coordinates(53.92, 27.58),
                        isDeleted = false,
                        createdAt = Instant.parse("2025-01-01T00:00:00Z"),
                        line =
                            SubwayLineResponseDto(
                                id = UUID.randomUUID(),
                                cityId = UUID.randomUUID(),
                                nameRu = "Красная",
                                nameEn = "Red",
                                shortCode = "1",
                                hexColor = "#FF0000",
                                isDeleted = false,
                                createdAt = Instant.parse("2025-01-01T00:00:00Z")
                            )
                    )
            )

        val decoded = objectMapper.readValue<RestroomResponseDto>(objectMapper.writeValueAsString(restroom))

        assertEquals(restroom, decoded)
    }

    @Test
    fun nearestRestroomSlimDto_roundTrips() {
        val dto =
            NearestRestroomSlimDto(
                id = UUID.randomUUID(),
                displayName = "Central restroom",
                distanceMeters = 123.5,
                feeType = FeeType.PAID,
                queryCoordinates = Coordinates(53.9, 27.56),
                restroomCoordinates = Coordinates(53.9005, 27.5605)
            )

        val decoded = objectMapper.readValue<NearestRestroomSlimDto>(objectMapper.writeValueAsString(dto))

        assertEquals(dto, decoded)
    }

    @Test
    fun placeType_uses_wire_code() {
        val encoded = objectMapper.writeValueAsString(PlaceType.PUBLIC)

        assertEquals("\"public_toilet\"", encoded)
        assertEquals(PlaceType.PUBLIC, objectMapper.readValue(encoded))
        assertEquals(PlaceType.OTHER, PlaceType.fromCode("missing"))
    }

    @Test
    fun acquisitionTag_uses_wire_code() {
        val encoded = objectMapper.writeValueAsString(AcquisitionTag.WEBSITE)

        assertEquals("\"website\"", encoded)
        assertEquals(AcquisitionTag.WEBSITE, objectMapper.readValue(encoded))
        assertEquals(AcquisitionTag.OTHER, AcquisitionTag.fromValue("other"))
    }

    @Test
    fun importBatchResponseDto_roundTrips_with_item_results() {
        val dto =
            ImportBatchResponseDto(
                importId = UUID.randomUUID(),
                totalProcessed = 2,
                successful = 1,
                failed = 1,
                results =
                    listOf(
                        ImportItemResultDto(
                            index = 0,
                            outcome = ImportStatus.CREATED,
                            providerExternalId = "70000001062416076",
                            restroomId = UUID.randomUUID(),
                            buildingId = UUID.randomUUID()
                        ),
                        ImportItemResultDto(
                            index = 1,
                            outcome = ImportStatus.FAILED,
                            providerExternalId = "broken-item",
                            restroomId = null,
                            buildingId = null,
                            errorCode = "INVALID_PAYLOAD",
                            errorMessage = "Missing required field: id"
                        )
                    )
            )

        val decoded = objectMapper.readValue<ImportBatchResponseDto>(objectMapper.writeValueAsString(dto))

        assertEquals(dto, decoded)
    }
}
