package yayauheny.by.importing.model

import java.util.UUID
import yayauheny.by.model.import.ImportStatus

data class ImportPipelineResult(
    val outcome: ImportStatus,
    val providerExternalId: String? = null,
    val restroomId: UUID? = null,
    val buildingId: UUID? = null,
    val duplicateOfRestroomId: UUID? = null,
    val duplicateReason: String? = null,
    val errorCode: String? = null,
    val errorMessage: String? = null
)
