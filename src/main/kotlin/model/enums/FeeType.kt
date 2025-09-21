package yayauheny.by.model.enums

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Fee type for using the restroom")
enum class FeeType {
    @Schema(description = "Free to use")
    FREE,

    @Schema(description = "Paid restroom")
    PAID
}
