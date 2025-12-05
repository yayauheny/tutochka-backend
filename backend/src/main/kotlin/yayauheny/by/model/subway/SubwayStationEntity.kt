package yayauheny.by.model.subway

import java.time.Instant
import java.util.UUID
import by.yayauheny.shared.dto.LatLon

data class SubwayStationEntity(
    val id: UUID,
    val subwayLineId: UUID,
    val nameRu: String,
    val nameEn: String,
    val coordinates: LatLon,
    val isDeleted: Boolean,
    val createdAt: Instant
)
