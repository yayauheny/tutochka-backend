package yayauheny.by.model.import

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ImportRequestDto(
    val items: List<JsonObject>
)
