package yayauheny.by.testdata

import yayauheny.by.model.CountryCreateDto
import yayauheny.by.model.CountryResponseDto
import java.time.Instant
import java.util.UUID

object CountryTestData {
    
    fun createCountryCreateDto(
        name: String = "United States",
        code: String = "US"
    ) = CountryCreateDto(
        name = name,
        code = code
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
    
    fun createCountryList(count: Int): List<CountryResponseDto> = 
        (1..count).map { index ->
            createCountryResponseDto(
                id = UUID.randomUUID(),
                name = "Country $index",
                code = "C$index"
            )
        }
}
