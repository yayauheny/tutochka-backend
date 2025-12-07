package yayauheny.by.service.import

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.import.ImportPayloadType
import yayauheny.by.model.import.ImportProvider

/**
 * Результат импорта одного объекта (туалет + при необходимости здание).
 */
data class ImportObjectResult(
    val restroomId: UUID,
    val buildingId: UUID?
)

/**
 * Интерфейс стратегии импорта для различных провайдеров данных.
 * Каждый провайдер (2ГИС, Яндекс.Карты, Google Maps) реализует свою стратегию.
 */
interface ImportStrategy {
    /**
     * Возвращает провайдера, для которого предназначена эта стратегия.
     */
    fun provider(): ImportProvider

    /**
     * Импортирует один "логический объект" (туалет + при необходимости здание)
     * из JSON-структуры провайдера.
     *
     * @param cityId ID города, в котором происходит импорт
     * @param payloadType тип формата payload
     * @param payload JsonObject - либо весь ответ провайдера, либо единичный item,
     *                нужно поддержать оба варианта
     * @return результат с id туалета и связанного здания
     */
    suspend fun importObject(
        cityId: UUID,
        payloadType: ImportPayloadType,
        payload: JsonObject
    ): ImportObjectResult
}
