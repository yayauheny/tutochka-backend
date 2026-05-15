package yayauheny.by.importing.model

import java.util.UUID

data class ImportBatchSummary(
    val importId: UUID,
    val totalProcessed: Int,
    val successful: Int,
    val failed: Int,
    val created: Int,
    val updated: Int,
    val linkedDuplicates: Int,
    val skippedDuplicates: Int,
    val warnings: Int,
    val results: List<ImportPipelineResult>
)
