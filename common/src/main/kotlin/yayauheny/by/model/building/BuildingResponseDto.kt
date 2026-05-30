package yayauheny.by.model.building



import java.time.Instant
import java.util.UUID



import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.PlaceType


data class BuildingResponseDto(
     val id: UUID,
     val cityId: UUID,
    val name: String?,
    val address: String,
    val buildingType: PlaceType?,
    val workTime: Map<String, Any>?,
    val coordinates: Coordinates,
    val externalIds: Map<String, Any>?,
    val isDeleted: Boolean,
     val createdAt: Instant,
     val updatedAt: Instant
) {
    fun displayName(): String? =
        name?.takeIf { it.isNotBlank() }
            ?: address.takeIf { it.isNotBlank() }
}
