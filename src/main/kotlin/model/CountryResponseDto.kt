package yayauheny.by.model

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Country response data")
data class CountryResponseDto(
    @Schema(description = "Unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    @Contextual
    val id: UUID,
    @Schema(description = "Country name in Russian", example = "Соединенные Штаты")
    val nameRu: String,
    @Schema(description = "Country name in English", example = "United States")
    val nameEn: String,
    @Schema(description = "Country code", example = "US")
    val code: String
)
