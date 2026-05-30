package yayauheny.by.model.imports

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID


import yayauheny.by.model.enums.ImportJobStatus


@Schema(description = "Response DTO for import operation")
data class ImportResponseDto(
    @field:Schema(
        description = "Import job ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    
    val importId: UUID,
    @field:Schema(
        description = "Created or updated restroom ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    
    val restroomId: UUID,
    @field:Schema(
        description = "Created or updated building ID (if applicable)",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    
    val buildingId: UUID?,
    @field:Schema(
        description = "Import job status",
        example = "SUCCESS"
    )
    val status: ImportJobStatus
)
