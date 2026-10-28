package yayauheny.by.model.subway

import java.time.Instant
import java.util.UUID




data class SubwayLineResponseDto(
     val id: UUID,
     val cityId: UUID,
    val code: String,
    val nameRu: String,
    val nameEn: String,
    val hexColor: String,
     val createdAt: Instant
)
