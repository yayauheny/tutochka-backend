package support.helpers

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import yayauheny.by.model.CityCreateDto
import yayauheny.by.model.CityResponseDto
import yayauheny.by.model.CountryCreateDto
import yayauheny.by.model.CountryResponseDto
import yayauheny.by.model.RestroomCreateDto
import yayauheny.by.model.RestroomResponseDto
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus

object TestDataHelpers {
    fun createCountryCreateDto(
        nameRu: String = "Соединенные Штаты",
        nameEn: String = "United States",
        code: String = "US"
    ) = CountryCreateDto(
        nameRu = nameRu,
        nameEn = nameEn,
        code = code
    )

    fun createCountryResponseDto(
        id: UUID = UUID.randomUUID(),
        nameRu: String = "Соединенные Штаты",
        nameEn: String = "United States",
        code: String = "US"
    ) = CountryResponseDto(
        id = id,
        nameRu = nameRu,
        nameEn = nameEn,
        code = code
    )

    fun createCountryList(count: Int): List<CountryResponseDto> =
        (1..count).map { i ->
            createCountryResponseDto(
                nameRu = "Страна $i",
                nameEn = "Country $i",
                code = "C$i"
            )
        }

    fun createCityCreateDto(
        countryId: UUID = UUID.randomUUID(),
        nameRu: String = "Нью-Йорк",
        nameEn: String = "New York",
        region: String? = "NY",
        lat: Double = 40.7128,
        lon: Double = -74.0060
    ) = CityCreateDto(
        countryId = countryId,
        nameRu = nameRu,
        nameEn = nameEn,
        region = region,
        lat = lat,
        lon = lon
    )

    fun createCityResponseDto(
        id: UUID = UUID.randomUUID(),
        countryId: UUID = UUID.randomUUID(),
        nameRu: String = "Нью-Йорк",
        nameEn: String = "New York",
        region: String? = "NY",
        lat: Double = 40.7128,
        lon: Double = -74.0060
    ) = CityResponseDto(
        id = id,
        countryId = countryId,
        nameRu = nameRu,
        nameEn = nameEn,
        region = region,
        lat = lat,
        lon = lon
    )

    fun createCityList(
        count: Int,
        countryId: UUID = UUID.randomUUID()
    ): List<CityResponseDto> =
        (1..count).map { i ->
            createCityResponseDto(
                countryId = countryId,
                nameRu = "Город $i",
                nameEn = "City $i",
                region = "R$i",
                lat = 40.0 + i,
                lon = -74.0 - i
            )
        }

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

    fun createRestroomList(
        count: Int,
        cityId: UUID = UUID.randomUUID()
    ): List<RestroomResponseDto> =
        (1..count).map { i ->
            createRestroomResponseDto(
                cityId = cityId,
                name = "Restroom $i",
                description = "Description for restroom $i",
                address = "Address $i",
                lat = 40.0 + i * 0.01,
                lon = -74.0 - i * 0.01
            )
        }

    fun createBasicPhones(): JsonObject =
        buildJsonObject {
            put("primary", JsonPrimitive("+1-555-0123"))
            put("secondary", JsonPrimitive("+1-555-0124"))
        }

    fun createBasicWorkTime(): JsonObject =
        buildJsonObject {
            put("monday", JsonPrimitive("09:00-18:00"))
            put("tuesday", JsonPrimitive("09:00-18:00"))
            put("wednesday", JsonPrimitive("09:00-18:00"))
            put("thursday", JsonPrimitive("09:00-18:00"))
            put("friday", JsonPrimitive("09:00-18:00"))
            put("saturday", JsonPrimitive("10:00-16:00"))
            put("sunday", JsonPrimitive("closed"))
        }

    fun createBasicAmenities(): JsonObject =
        buildJsonObject {
            put("wheelchair_accessible", JsonPrimitive(true))
            put("baby_changing", JsonPrimitive(false))
            put("hand_dryer", JsonPrimitive(true))
            put("soap", JsonPrimitive(true))
            put("toilet_paper", JsonPrimitive(true))
        }

    fun createPremiumAmenities(): JsonObject =
        buildJsonObject {
            put("wheelchair_accessible", JsonPrimitive(true))
            put("baby_changing", JsonPrimitive(true))
            put("hand_dryer", JsonPrimitive(true))
            put("soap", JsonPrimitive(true))
            put("toilet_paper", JsonPrimitive(true))
            put("air_conditioning", JsonPrimitive(true))
            put("music", JsonPrimitive(true))
            put("perfume", JsonPrimitive(true))
        }

    fun createInvalidCountryData(): Map<String, Any> =
        mapOf(
            "nameRu" to "", // Empty name
            "nameEn" to "", // Empty name
            "code" to "INVALID_CODE_TOO_LONG" // Too long code
        )

    fun createInvalidCityData(): Map<String, Any> =
        mapOf(
            "countryId" to "invalid-uuid",
            "nameRu" to "", // Empty name
            "nameEn" to "", // Empty name
            "lat" to 200.0, // Invalid latitude
            "lon" to 200.0 // Invalid longitude
        )

    fun createInvalidRestroomData(): Map<String, Any> =
        mapOf(
            "cityId" to "invalid-uuid",
            "name" to "", // Empty name
            "address" to "", // Empty address
            "lat" to 200.0, // Invalid latitude
            "lon" to 200.0 // Invalid longitude
        )
}
