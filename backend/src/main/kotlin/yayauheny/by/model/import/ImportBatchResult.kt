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
    val restroomId: UUID?,
    val buildingId: UUID?,
    val success: Boolean,
    val errorMessage: String? = null
)
