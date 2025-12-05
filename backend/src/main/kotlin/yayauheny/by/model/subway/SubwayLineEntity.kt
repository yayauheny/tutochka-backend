package yayauheny.by.model.subway

import java.time.Instant
import java.util.UUID

data class SubwayLineEntity(
    val id: UUID,
    val cityId: UUID,
    val nameRu: String,
    val nameEn: String,
    val hexColor: String,
    val isDeleted: Boolean,
    val createdAt: Instant
)
