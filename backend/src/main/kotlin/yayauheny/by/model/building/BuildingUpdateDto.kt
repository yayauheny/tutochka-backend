package yayauheny.by.model.building

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import by.yayauheny.shared.dto.LatLon
import by.yayauheny.shared.enums.PlaceType

@Serializable
@Schema(description = "Data for updating a building")
data class BuildingUpdateDto(
    @field:Schema(description = "City ID where the building is located")
    @Contextual
    val cityId: UUID?,
    @field:Schema(description = "Building name")
    val name: String?,
    @field:Schema(description = "Address")
    val address: String?,
    @field:Schema(description = "Building type", example = "mall")
    val buildingType: PlaceType?,
    @field:Schema(description = "Working hours JSONB")
    val workTime: JsonObject?,
    @field:Schema(description = "Coordinates")
    val coordinates: LatLon?,
    @field:Schema(description = "External IDs JSONB map")
    val externalIds: JsonObject?
)
