package yayauheny.by.service.validation

import io.konform.validation.Validation
import kotlinx.serialization.json.JsonObject
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.importing.service.ImportService

data class ImportItemsParams(
    val items: List<JsonObject>
)

val validateImportItemsParams =
    Validation<ImportItemsParams> {
        ImportItemsParams::items {
            constrain("Items must contain between 1 and ${ImportService.MAX_BATCH_ITEMS} items") {
                it.size in 1..ImportService.MAX_BATCH_ITEMS
            }
        }
    }

val validateSingleImportItemsParams =
    Validation<ImportItemsParams> {
        ImportItemsParams::items {
            constrain("Expected exactly one item") {
                it.size == 1
            }
        }
    }

fun List<JsonObject?>.validateOrThrow(): List<JsonObject> {
    val jsonObjects = filterNotNull()
    if (jsonObjects.size != size) {
        throw ValidationException(FieldError("items", "Each item must be a JSON object"))
    }
    return jsonObjects
}
