package yayauheny.by.model.building



import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID


import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.PlaceType


@Schema(description = "Data for updating a building")
data class BuildingUpdateDto(
    @field:Schema(description = "City ID where the building is located")
    
    val cityId: UUID?,
    @field:Schema(description = "Building name")
    val name: String?,
    @field:Schema(description = "Address")
    val address: String?,
    @field:Schema(description = "Building type", example = "mall")
    val buildingType: PlaceType?,
    @field:Schema(description = "Working hours JSONB")
    val workTime: Map<String, Any>?,
    @field:Schema(description = "Coordinates")
    val coordinates: Coordinates?,
    @field:Schema(description = "2GIS provider id")
    val twoGisId: String?,
    @field:Schema(description = "Yandex provider id")
    val yandexId: String?,
    @field:Schema(description = "Google provider id")
    val googleId: String?,
    @field:Schema(description = "Apple provider id")
    val appleId: String?
)
