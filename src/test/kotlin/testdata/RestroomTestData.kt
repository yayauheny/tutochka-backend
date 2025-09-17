package yayauheny.by.testdata

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonPrimitive
import yayauheny.by.enums.AccessibilityType
import yayauheny.by.enums.DataSourceType
import yayauheny.by.enums.FeeType
import yayauheny.by.model.RestroomCreateDto
import yayauheny.by.model.RestroomResponseDto
import yayauheny.by.model.enums.RestroomStatus
import java.time.Instant
import java.util.UUID

object RestroomTestData {
    
    fun createRestroomCreateDto(
        cityId: UUID = UUID.randomUUID(),
        name: String = "Central Park Restroom",
        description: String = "Public restroom in Central Park",
        address: String = "123 Central Park, New York, NY",
        phones: JsonObject? = createBasicPhones(),
        workTime: JsonObject? = createBasicWorkTime(),
        feeType: FeeType = FeeType.FREE,
        accessibilityType: AccessibilityType = AccessibilityType.DISABLED,
        lat: Double = 40.7829,
        lon: Double = -73.9654,
        dataSource: DataSourceType = DataSourceType.MANUAL,
        amenities: JsonObject = createBasicAmenities()
    ) = RestroomCreateDto(
        cityId = cityId,
        name = name,
        description = description,
        address = address,
        phones = phones,
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
        name: String = "Central Park Restroom",
        description: String = "Public restroom in Central Park",
        address: String = "123 Central Park, New York, NY",
        phones: JsonObject? = createBasicPhones(),
        workTime: JsonObject? = createBasicWorkTime(),
        feeType: FeeType = FeeType.FREE,
        accessibilityType: AccessibilityType = AccessibilityType.DISABLED,
        lat: Double = 40.7829,
        lon: Double = -73.9654,
        dataSource: DataSourceType = DataSourceType.MANUAL,
        status: RestroomStatus = RestroomStatus.ACTIVE,
        amenities: JsonObject = createBasicAmenities(),
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now()
    ) = RestroomResponseDto(
        id = id,
        cityId = cityId,
        name = name,
        description = description,
        address = address,
        phones = phones,
        workTime = workTime,
        feeType = feeType,
        accessibilityType = accessibilityType,
        lat = lat,
        lon = lon,
        dataSource = dataSource,
        status = status,
        amenities = amenities,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    fun createRestroomList(count: Int): List<RestroomResponseDto> = 
        (1..count).map { index ->
            createRestroomResponseDto(
                id = UUID.randomUUID(),
                name = "Restroom $index",
                address = "Address $index"
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
    
    fun createBasicAmenities(): JsonObject = buildJsonObject {
        put("wifi", JsonPrimitive("false"))
        put("free", JsonPrimitive("true"))
    }
    
    private fun createBasicPhones(): JsonObject = buildJsonObject {
        put("main", JsonPrimitive("+1-555-123-4567"))
        put("emergency", JsonPrimitive("+1-555-911"))
    }
    
    private fun createBasicWorkTime(): JsonObject = buildJsonObject {
        put("monday", JsonPrimitive("09:00-18:00"))
        put("tuesday", JsonPrimitive("09:00-18:00"))
        put("wednesday", JsonPrimitive("09:00-18:00"))
        put("thursday", JsonPrimitive("09:00-18:00"))
        put("friday", JsonPrimitive("09:00-18:00"))
        put("saturday", JsonPrimitive("10:00-16:00"))
        put("sunday", JsonPrimitive("closed"))
    }
}
