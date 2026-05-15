package yayauheny.by.importing.model

import java.util.UUID
import yayauheny.by.model.enums.ImportPayloadType

data class ImportAdapterContext(
    val payloadType: ImportPayloadType,
    val cityId: UUID
)
