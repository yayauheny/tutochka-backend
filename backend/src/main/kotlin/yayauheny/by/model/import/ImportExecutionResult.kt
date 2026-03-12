package yayauheny.by.model.import

import java.util.UUID
import yayauheny.by.model.enums.ImportJobStatus

/**
 * Результат выполнения импорта с информацией о созданных сущностях и статусе.
 */
data class ImportExecutionResult(
    val importId: UUID,
    val restroomId: UUID,
    val buildingId: UUID?,
    val status: ImportJobStatus
)
