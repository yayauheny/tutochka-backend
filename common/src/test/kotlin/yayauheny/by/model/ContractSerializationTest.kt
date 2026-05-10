package yayauheny.by.model

import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.subway.SubwayLineResponseDto
import yayauheny.by.model.subway.SubwayStationResponseDto

class ContractSerializationTest {
    private val json =
        Json {
            serializersModule = serializersModule
            encodeDefaults = true
        }

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
                phones = jsonObject("main" to JsonPrimitive("+375291234567")),
                workTime = jsonObject("monday" to JsonPrimitive("08:00-20:00")),
                feeType = FeeType.FREE,
                genderType = null,
                accessibilityType = AccessibilityType.WHEELCHAIR,
                placeType = PlaceType.PUBLIC,
                coordinates = Coordinates(53.9, 27.56),
                dataSource = DataSourceType.MANUAL,
                status = RestroomStatus.ACTIVE,
                amenities = jsonObject("wifi" to JsonPrimitive(true)),
                externalMaps = jsonObject("2gis" to JsonPrimitive("abc123")),
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
                        workTime = jsonObject("sunday" to JsonPrimitive("closed")),
                        coordinates = Coordinates(53.91, 27.57),
                        externalIds = jsonObject("2gis" to JsonPrimitive("b-1")),
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

        val decoded = json.decodeFromString(RestroomResponseDto.serializer(), json.encodeToString(RestroomResponseDto.serializer(), restroom))

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

        val decoded = json.decodeFromString(NearestRestroomSlimDto.serializer(), json.encodeToString(NearestRestroomSlimDto.serializer(), dto))

        assertEquals(dto, decoded)
    }

    @Test
    fun placeType_uses_wire_code() {
        val encoded = json.encodeToString(PlaceType.serializer(), PlaceType.PUBLIC)

        assertEquals("\"public_toilet\"", encoded)
        assertEquals(PlaceType.PUBLIC, json.decodeFromString(PlaceType.serializer(), encoded))
        assertEquals(PlaceType.OTHER, PlaceType.fromCode("missing"))
    }

    private fun jsonObject(vararg pairs: Pair<String, JsonElement>): JsonObject =
        buildJsonObject {
            pairs.forEach { (key, value) -> put(key, value) }
        }

    private val serializersModule =
        SerializersModule {
            contextual(UUID::class, UuidAsStringSerializer)
            contextual(Instant::class, InstantAsStringSerializer)
        }

    private object UuidAsStringSerializer : KSerializer<UUID> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: UUID) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
    }

    private object InstantAsStringSerializer : KSerializer<Instant> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Instant) {
            encoder.encodeString(value.toString())
        }

        override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
    }
}
