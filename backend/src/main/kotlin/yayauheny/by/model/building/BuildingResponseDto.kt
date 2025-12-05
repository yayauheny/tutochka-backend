package yayauheny.by.model.building

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import by.yayauheny.shared.dto.LatLon
import by.yayauheny.shared.enums.PlaceType

@Serializable
@Schema(description = "Building response DTO")
data class BuildingResponseDto(
    @Contextual val id: UUID,
    @Contextual val cityId: UUID,
    val name: String?,
    val address: String,
    val buildingType: PlaceType?,
    val workTime: JsonObject?,
    val coordinates: LatLon,
    val externalIds: JsonObject?,
    val isDeleted: Boolean,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant
)
