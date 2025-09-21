package yayauheny.by.model.enums

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Current status of the restroom")
enum class RestroomStatus {
    @Schema(description = "Restroom is active and available for use")
    ACTIVE,

    @Schema(description = "Restroom is permanently inactive")
    INACTIVE,

    @Schema(description = "Restroom data is pending verification")
    PENDING,

    @Schema(description = "Restroom is temporarily closed for maintenance")
    TEMP_CLOSED
}
