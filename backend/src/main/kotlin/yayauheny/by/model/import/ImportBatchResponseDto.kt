package yayauheny.by.model.import

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Response DTO for batch import operation")
data class ImportBatchResponseDto(
    @field:Schema(
        description = "Import job ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual
    val importId: UUID,
    @field:Schema(
        description = "Total number of items processed",
        example = "10"
    )
    val totalProcessed: Int,
    @field:Schema(
        description = "Number of successfully imported items",
        example = "9"
    )
    val successful: Int,
    @field:Schema(
        description = "Number of failed items",
        example = "1"
    )
    val failed: Int
)

@Serializable
@Schema(description = "Result for a single item in batch import")
data class ImportItemResultDto(
    @field:Schema(
        description = "Index of the item in the batch",
        example = "0"
    )
    val index: Int,
    @field:Schema(
        description = "Created or updated restroom ID",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual
    val restroomId: UUID?,
    @field:Schema(
        description = "Created or updated building ID (if applicable)",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual
    val buildingId: UUID?,
    @field:Schema(
        description = "Whether the import was successful",
        example = "true"
    )
    val success: Boolean,
    @field:Schema(
        description = "Error message if import failed",
        example = "Missing required field: id"
    )
    val errorMessage: String? = null
)
