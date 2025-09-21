package yayauheny.by.model.enums

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Restroom accessibility type")
enum class AccessibilityType {
    @Schema(description = "Men's restroom")
    MEN,

    @Schema(description = "Women's restroom")
    WOMEN,

    @Schema(description = "Unisex restroom for all genders")
    UNISEX,

    @Schema(description = "Family restroom with baby changing facilities")
    FAMILY,

    @Schema(description = "Restroom accessible for disabled people")
    DISABLED
}
