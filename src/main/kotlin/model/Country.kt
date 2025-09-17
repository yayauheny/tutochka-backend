package yayauheny.by.model

import java.util.UUID

data class Country(
    val id: UUID,
    val code: String,
    val nameRu: String,
    val nameEn: String
)
