package yayauheny.by.model.country

import java.util.UUID

data class CountryEntity(
    val id: UUID,
    val code: String,
    val nameRu: String,
    val nameEn: String
)
