package yayauheny.by.model.import

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import yayauheny.by.model.enums.ImportJobStatus

@Serializable
@Schema(description = "Response DTO for import operation")
data class ImportResponseDto(
    @field:Schema(
        description = "Import job ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual
    val importId: UUID,
    @field:Schema(
        description = "Created or updated restroom ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual
    val restroomId: UUID,
    @field:Schema(
        description = "Created or updated building ID (if applicable)",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual
    val buildingId: UUID?,
    @field:Schema(
        description = "Import job status",
        example = "SUCCESS"
    )
    val status: ImportJobStatus
)
