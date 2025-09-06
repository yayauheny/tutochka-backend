package yayauheny.by.testdata

import yayauheny.by.model.CityCreateDto
import yayauheny.by.model.CityResponseDto
import yayauheny.by.model.CountryResponseDto
import java.time.Instant
import java.util.UUID

object CityTestData {
    
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
        lon: Double = -74.0060,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now()
    ) = CityResponseDto(
        id = id,
        countryId = countryId,
        nameRu = nameRu,
        nameEn = nameEn,
        region = region,
        lat = lat,
        lon = lon,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    fun createCountryResponseDto(
        id: UUID = UUID.randomUUID(),
        name: String = "United States",
        code: String = "US",
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now()
    ) = CountryResponseDto(
        id = id,
        name = name,
        code = code,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    fun createCityList(count: Int): List<CityResponseDto> = 
        (1..count).map { index ->
            createCityResponseDto(
                id = UUID.randomUUID(),
                nameRu = "Город $index",
                nameEn = "City $index"
            )
        }
}
