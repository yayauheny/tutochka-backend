package yayauheny.by.model.enums

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Data source type indicating how the restroom data was obtained")
enum class DataSourceType {
    @Schema(description = "Manually entered by administrator")
    MANUAL,

    @Schema(description = "Submitted by user")
    USER,

    @Schema(description = "Retrieved from external API")
    API,

    @Schema(description = "Imported from data file")
    IMPORT
}
