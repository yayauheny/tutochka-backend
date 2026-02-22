package yayauheny.by.model.import

import kotlinx.serialization.json.JsonObject

data class ImportRequestDto(
    val items: List<JsonObject>
)
