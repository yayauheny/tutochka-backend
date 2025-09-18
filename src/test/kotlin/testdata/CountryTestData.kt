package yayauheny.by.testdata

import yayauheny.by.model.CountryCreateDto
import yayauheny.by.model.CountryResponseDto
import java.util.UUID

object CountryTestData {
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
        (1..count).map { index ->
            createCountryResponseDto(
                id = UUID.randomUUID(),
                nameRu = "Страна $index",
                nameEn = "Country $index",
                code = "C$index"
            )
        }
}
