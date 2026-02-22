package yayauheny.by.model.import

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider

@Serializable
@Schema(description = "Request DTO for importing restroom data from external providers")
data class ImportRequestDto(
    @field:Schema(
        description = "Import provider (2GIS, Yandex Maps, Google Maps, OSM)",
        example = "TWO_GIS",
        required = true
    )
    val provider: ImportProvider,
    @field:Schema(
        description = "Type of payload format",
        example = "TWO_GIS_SCRAPED_PLACE_JSON",
        required = true
    )
    val payloadType: ImportPayloadType,
    @field:Schema(
        description = "City ID where the import is happening",
        example = "123e4567-e89b-12d3-a456-426614174000",
        required = true
    )
    @Contextual
    val cityId: UUID,
    @field:Schema(
        description = "JSON payload from the provider (either full response or single item)",
        required = true
    )
    val payload: JsonObject
)
