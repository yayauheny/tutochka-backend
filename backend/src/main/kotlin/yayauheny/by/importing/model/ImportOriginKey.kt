package yayauheny.by.importing.model

import yayauheny.by.model.enums.ImportProvider

data class ImportOriginKey(
    val provider: ImportProvider,
    val originId: String
)
