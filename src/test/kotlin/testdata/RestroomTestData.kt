package yayauheny.by.testdata

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import yayauheny.by.enums.AccessibilityType
import yayauheny.by.enums.DataSourceType
import yayauheny.by.enums.FeeType
import yayauheny.by.model.RestroomCreateDto
import yayauheny.by.model.RestroomResponseDto
import java.time.Instant
import java.util.UUID

object RestroomTestData {
    
    fun createRestroomCreateDto(
        cityId: UUID = UUID.randomUUID(),
        code: String = "REST001",
        description: String = "Public restroom",
        name: String = "Central Park Restroom",
        workTime: String = "24/7",
        feeType: FeeType = FeeType.FREE,
        accessibilityType: AccessibilityType = AccessibilityType.DISABLED,
        lat: Double = 40.7829,
        lon: Double = -73.9654,
        dataSource: String = DataSourceType.MANUAL.name,
        amenities: JsonObject = createBasicAmenities()
    ) = RestroomCreateDto(
        cityId = cityId,
        code = code,
        description = description,
        name = name,
        workTime = workTime,
        feeType = feeType,
        accessibilityType = accessibilityType,
        lat = lat,
        lon = lon,
        dataSource = dataSource,
        amenities = amenities
    )
    
    fun createRestroomResponseDto(
        id: UUID = UUID.randomUUID(),
        cityId: UUID = UUID.randomUUID(),
        code: String = "REST001",
        description: String = "Public restroom",
        name: String = "Central Park Restroom",
        workTime: String = "24/7",
        feeType: FeeType = FeeType.FREE,
        accessibilityType: AccessibilityType = AccessibilityType.DISABLED,
        lat: Double = 40.7829,
        lon: Double = -73.9654,
        dataSource: String = DataSourceType.MANUAL.name,
        amenities: JsonObject = createBasicAmenities(),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now()
    ) = RestroomResponseDto(
        id = id,
        cityId = cityId,
        code = code,
        description = description,
        name = name,
        workTime = workTime,
        feeType = feeType,
        accessibilityType = accessibilityType,
        lat = lat,
        lon = lon,
        dataSource = dataSource,
        amenities = amenities,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    fun createRestroomList(count: Int): List<RestroomResponseDto> = 
        (1..count).map { index ->
            createRestroomResponseDto(
                id = UUID.randomUUID(),
                name = "Restroom $index",
                code = "REST${index.toString().padStart(3, '0')}"
            )
        }
    
    fun createAmenityConfigurations(): List<Pair<RestroomCreateDto, RestroomResponseDto>> = listOf(
        createBasicAmenityConfig(),
        createAccessibleAmenityConfig(),
        createPremiumAmenityConfig()
    )
    
    private fun createBasicAmenityConfig(): Pair<RestroomCreateDto, RestroomResponseDto> {
        val amenities = createBasicAmenities()
        val createDto = createRestroomCreateDto(amenities = amenities)
        val responseDto = createRestroomResponseDto(amenities = amenities)
        return createDto to responseDto
    }
    
    private fun createAccessibleAmenityConfig(): Pair<RestroomCreateDto, RestroomResponseDto> {
        val amenities = buildJsonObject {
            put("wheelchair_accessible", JsonPrimitive("true"))
            put("baby_changing", JsonPrimitive("true"))
            put("free", JsonPrimitive("true"))
        }
        val createDto = createRestroomCreateDto(amenities = amenities)
        val responseDto = createRestroomResponseDto(amenities = amenities)
        return createDto to responseDto
    }
    
    private fun createPremiumAmenityConfig(): Pair<RestroomCreateDto, RestroomResponseDto> {
        val amenities = buildJsonObject {
            put("wifi", JsonPrimitive("true"))
            put("air_conditioning", JsonPrimitive("true"))
            put("music", JsonPrimitive("true"))
            put("attendant", JsonPrimitive("true"))
        }
        val createDto = createRestroomCreateDto(amenities = amenities)
        val responseDto = createRestroomResponseDto(amenities = amenities)
        return createDto to responseDto
    }
    
    private fun createBasicAmenities(): JsonObject = buildJsonObject {
        put("wifi", JsonPrimitive("false"))
        put("free", JsonPrimitive("true"))
    }
}
