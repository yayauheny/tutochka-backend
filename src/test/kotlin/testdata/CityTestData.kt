package yayauheny.by.testdata

import yayauheny.by.model.CityCreateDto
import yayauheny.by.model.CityResponseDto
import yayauheny.by.model.CountryResponseDto
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
    
    fun createCityList(count: Int): List<CityResponseDto> = 
        (1..count).map { index ->
            createCityResponseDto(
                id = UUID.randomUUID(),
                nameRu = "Город $index",
                nameEn = "City $index"
            )
        }
}
