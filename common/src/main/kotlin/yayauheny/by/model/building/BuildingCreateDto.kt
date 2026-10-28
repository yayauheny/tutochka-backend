package yayauheny.by.model.building



import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID


import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.PlaceType


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
    @field:Schema(description = "2GIS provider id")
    val twoGisId: String? = null,
    @field:Schema(description = "Yandex provider id")
    val yandexId: String? = null,
    @field:Schema(description = "Google provider id")
    val googleId: String? = null,
    @field:Schema(description = "Apple provider id")
    val appleId: String? = null
)
