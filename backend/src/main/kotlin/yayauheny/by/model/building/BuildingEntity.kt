package yayauheny.by.model.building

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.JsonObject
import by.yayauheny.shared.dto.LatLon
import by.yayauheny.shared.enums.PlaceType

data class BuildingEntity(
    val id: UUID,
    val cityId: UUID,
    val name: String?,
    val address: String,
    val buildingType: PlaceType?,
    val workTime: JsonObject?,
    val coordinates: LatLon,
    val externalIds: JsonObject?,
    val isDeleted: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
