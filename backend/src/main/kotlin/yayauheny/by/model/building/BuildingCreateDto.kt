package yayauheny.by.model.building

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.dto.LatLon
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.import.BuildingImportStatus

@Serializable
@Schema(description = "Data for creating a new building")
data class BuildingCreateDto(
    @field:Schema(description = "City ID where the building is located", required = true)
    @Contextual
    val cityId: UUID,
    @field:Schema(description = "Building name")
    val name: String? = null,
    @field:Schema(description = "Address", required = true)
    val address: String,
    @field:Schema(description = "Building type", example = "mall")
    val buildingType: PlaceType? = null,
    @field:Schema(description = "Working hours JSONB")
    val workTime: JsonObject? = null,
    @field:Schema(description = "Coordinates", required = true)
    val coordinates: LatLon,
    @field:Schema(description = "External IDs JSONB map")
    val externalIds: JsonObject? = null,
    @field:Schema(description = "Import status", example = "COMPLETED")
    val importStatus: BuildingImportStatus = BuildingImportStatus.COMPLETED
)
