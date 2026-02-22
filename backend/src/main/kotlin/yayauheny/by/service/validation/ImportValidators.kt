package yayauheny.by.service.validation

import io.konform.validation.Validation
import kotlinx.serialization.json.JsonObject

data class ImportItemsParams(
    val items: List<JsonObject>,
    val isBatch: Boolean
) {
    val validSize: Boolean get() = isBatch || items.size == 1
}

val validateImportItemsParams =
    Validation<ImportItemsParams> {
        ImportItemsParams::items {
            constrain("Items cannot be empty") { it.isNotEmpty() }
        }
        ImportItemsParams::validSize {
            constrain("Expected exactly one item") { it }
        }
    }
