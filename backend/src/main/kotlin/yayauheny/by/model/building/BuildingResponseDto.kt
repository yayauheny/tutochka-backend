package yayauheny.by.model.building

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.PlaceType

@Serializable
data class BuildingResponseDto(
    @Contextual val id: UUID,
    @Contextual val cityId: UUID,
    val name: String?,
    val address: String,
    val buildingType: PlaceType?,
    val workTime: JsonObject?,
    val coordinates: Coordinates,
    val externalIds: JsonObject?,
    val isDeleted: Boolean,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant
) {
    fun displayName(): String? =
        name?.takeIf { it.isNotBlank() }
            ?: address.takeIf { it.isNotBlank() }
}
