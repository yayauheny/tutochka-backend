package yayauheny.by.service.import

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

/**
 * Извлекает список объектов из payload.
 * Поддерживает различные форматы:
 * - {"items": [...]} - массив в поле items
 * - [...] - прямой массив
 * - {...} - одиночный объект
 */
interface PayloadExtractor {
    fun extractItems(payload: JsonObject): List<JsonObject>
}

class ArrayOrSingleExtractor : PayloadExtractor {
    override fun extractItems(payload: JsonObject): List<JsonObject> {
        // Проверяем наличие поля "items" с массивом
        val itemsField = payload["items"]
        if (itemsField != null && itemsField is JsonArray) {
            return itemsField.mapNotNull { element ->
                if (element is JsonObject) element else null
            }
        }

        // Если payload сам является объектом (не массивом), возвращаем его как единственный элемент
        return listOf(payload)
    }
}
