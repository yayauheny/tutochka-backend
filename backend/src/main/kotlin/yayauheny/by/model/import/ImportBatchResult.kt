package yayauheny.by.model.import

import java.util.UUID

/**
 * Результат batch импорта с информацией о всех обработанных объектах.
 */
data class ImportBatchResult(
    val importId: UUID,
    val totalProcessed: Int,
    val successful: Int,
    val failed: Int,
    val results: List<ImportItemResult>
)

/**
 * Результат импорта одного элемента в batch.
 */
data class ImportItemResult(
    val index: Int,
    val outcome: ImportStatus,
    val providerExternalId: String? = null,
    val restroomId: UUID?,
    val buildingId: UUID?,
    val duplicateOfRestroomId: UUID? = null,
    val duplicateReason: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null
)
