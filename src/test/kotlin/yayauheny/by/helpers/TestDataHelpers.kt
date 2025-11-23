package yayauheny.by.helpers

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import yayauheny.by.model.restroom.NearestRestroomResponseDto
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.country.CountryResponseDto
import yayauheny.by.model.country.CountryUpdateDto
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

    fun createCountryUpdateDto(
        nameRu: String = "Соединенные Штаты",
        nameEn: String = "United States"
    ) = CountryUpdateDto(
        nameRu = nameRu,
        nameEn = nameEn
    )

    fun createCityCreateDto(
        countryId: UUID = UUID.randomUUID(),
        nameRu: String = "Минск",
        nameEn: String = "Minsk",
        region: String? = "Минская область",
        lat: Double = 53.9006,
        lon: Double = 27.5590
    ) = CityCreateDto(
        countryId = countryId,
        nameRu = nameRu,
        nameEn = nameEn,
        region = region,
        coordinates = yayauheny.by.model.LatLon(lat = lat, lon = lon)
    )

    fun createCityUpdateDto(
        countryId: UUID = UUID.randomUUID(),
        nameRu: String = "Минск",
        nameEn: String = "Minsk",
        region: String? = "Минская область",
        coordinates: yayauheny.by.model.LatLon = yayauheny.by.model.LatLon(lat = 53.9006, lon = 27.5590),
        cityBounds: String? = null
    ) = CityUpdateDto(
        countryId = countryId,
        nameRu = nameRu,
        nameEn = nameEn,
        region = region,
        coordinates = coordinates,
        cityBounds = cityBounds
    )

    fun createCityResponseDto(
        id: UUID = UUID.randomUUID(),
        countryId: UUID = UUID.randomUUID(),
        nameRu: String = "Минск",
        nameEn: String = "Minsk",
        region: String? = "Минская область",
        lat: Double = 53.9006,
        lon: Double = 27.5590
    ) = CityResponseDto(
        id = id,
        countryId = countryId,
        nameRu = nameRu,
        nameEn = nameEn,
        region = region,
        coordinates = yayauheny.by.model.LatLon(lat = lat, lon = lon),
        cityBounds = null
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
        name: String = "Minsk Central Restroom",
        description: String = "Public restroom in Minsk center",
        address: String = "123 Independence Avenue, Minsk, Belarus",
        phones: JsonObject? = createBasicPhones(),
        workTime: JsonObject? = createBasicWorkTime(),
        feeType: FeeType = FeeType.FREE,
        accessibilityType: AccessibilityType = AccessibilityType.DISABLED,
        lat: Double = 40.7829,
        lon: Double = -73.9654,
        dataSource: DataSourceType = DataSourceType.MANUAL,
        status: RestroomStatus = RestroomStatus.ACTIVE,
        amenities: JsonObject = createBasicAmenities(),
        parentPlaceName: String? = null,
        parentPlaceType: String? = null,
        inheritParentSchedule: Boolean = false
    ) = RestroomCreateDto(
        cityId = cityId,
        status = status,
        name = name,
        description = description,
        address = address,
        phones = phones,
        workTime = workTime,
        feeType = feeType,
        accessibilityType = accessibilityType,
        coordinates = yayauheny.by.model.LatLon(lat = lat, lon = lon),
        dataSource = dataSource,
        amenities = amenities,
        parentPlaceName = parentPlaceName,
        parentPlaceType = parentPlaceType,
        inheritParentSchedule = inheritParentSchedule
    )

    fun createRestroomUpdateDto(
        cityId: UUID? = UUID.randomUUID(),
        name: String? = "Updated Restroom",
        description: String? = "Updated description",
        address: String = "Updated Address",
        phones: JsonObject? = createBasicPhones(),
        workTime: JsonObject? = createBasicWorkTime(),
        feeType: FeeType = FeeType.FREE,
        accessibilityType: AccessibilityType = AccessibilityType.DISABLED,
        coordinates: yayauheny.by.model.LatLon = yayauheny.by.model.LatLon(lat = 40.7829, lon = -73.9654),
        status: RestroomStatus = RestroomStatus.ACTIVE,
        amenities: JsonObject? = createBasicAmenities(),
        parentPlaceName: String? = null,
        parentPlaceType: String? = null,
        inheritParentSchedule: Boolean = false
    ) = RestroomUpdateDto(
        cityId = cityId,
        name = name,
        description = description,
        address = address,
        phones = phones,
        workTime = workTime,
        feeType = feeType,
        accessibilityType = accessibilityType,
        coordinates = coordinates,
        status = status,
        amenities = amenities,
        parentPlaceName = parentPlaceName,
        parentPlaceType = parentPlaceType,
        inheritParentSchedule = inheritParentSchedule
    )

    fun createRestroomResponseDto(
        id: UUID = UUID.randomUUID(),
        cityId: UUID = UUID.randomUUID(),
        name: String = "Minsk Central Restroom",
        description: String = "Public restroom in Minsk center",
        address: String = "123 Independence Avenue, Minsk, Belarus",
        phones: JsonObject? = createBasicPhones(),
        workTime: JsonObject? = createBasicWorkTime(),
        feeType: FeeType = FeeType.FREE,
        accessibilityType: AccessibilityType = AccessibilityType.DISABLED,
        lat: Double = 40.7829,
        lon: Double = -73.9654,
        dataSource: DataSourceType = DataSourceType.MANUAL,
        status: RestroomStatus = RestroomStatus.ACTIVE,
        amenities: JsonObject = createBasicAmenities(),
        parentPlaceName: String? = null,
        parentPlaceType: String? = null,
        inheritParentSchedule: Boolean = false,
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
        coordinates = yayauheny.by.model.LatLon(lat = lat, lon = lon),
        dataSource = dataSource,
        status = status,
        amenities = amenities,
        parentPlaceName = parentPlaceName,
        parentPlaceType = parentPlaceType,
        inheritParentSchedule = inheritParentSchedule,
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

    fun createNearestRestroomResponseDto(
        id: UUID = UUID.randomUUID(),
        name: String = "Minsk Central Restroom",
        address: String = "123 Independence Avenue, Minsk, Belarus",
        feeType: FeeType = FeeType.FREE,
        lat: Double = 40.7829,
        lon: Double = -73.9654,
        distanceMeters: Double = 100.0,
        isOpen: Boolean? = null
    ) = NearestRestroomResponseDto(
        id = id,
        name = name,
        address = address,
        coordinates = yayauheny.by.model.LatLon(lat = lat, lon = lon),
        distanceMeters = distanceMeters,
        feeType = feeType,
        isOpen = isOpen
    )

    fun createNearestRestroomList(
        count: Int,
        cityId: UUID = UUID.randomUUID(),
        baseLat: Double = 40.7829,
        baseLon: Double = -73.9654
    ): List<NearestRestroomResponseDto> =
        (1..count).map { i ->
            createNearestRestroomResponseDto(
                name = "Restroom $i",
                address = "Address $i",
                lat = baseLat + i * 0.01,
                lon = baseLon - i * 0.01,
                distanceMeters = i * 50.0 // Simulate increasing distance
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
