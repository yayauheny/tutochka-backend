package yayauheny.by.model.building



import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID



import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.BuildingImportStatus


@Schema(description = "Data for creating a new building")
data class BuildingCreateDto(
    @field:Schema(description = "City ID where the building is located", required = true)
    
    val cityId: UUID,
    @field:Schema(description = "Building name")
    val name: String? = null,
    @field:Schema(description = "Address", required = true)
    val address: String,
    @field:Schema(description = "Building type", example = "mall")
    val buildingType: PlaceType? = null,
    @field:Schema(description = "Working hours JSONB")
    val workTime: Map<String, Any>? = null,
    @field:Schema(description = "Coordinates", required = true)
    val coordinates: Coordinates,
    @field:Schema(description = "External IDs JSONB map")
    val externalIds: Map<String, Any>? = null,
    @field:Schema(description = "Import status", example = "COMPLETED")
    val importStatus: BuildingImportStatus = BuildingImportStatus.COMPLETED
)
