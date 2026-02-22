package yayauheny.by.model.building

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.import.BuildingImportStatus

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
    val coordinates: Coordinates?,
    @field:Schema(description = "External IDs JSONB map")
    val externalIds: JsonObject?,
    @field:Schema(description = "Import status", example = "COMPLETED")
    val importStatus: BuildingImportStatus? = null
)
