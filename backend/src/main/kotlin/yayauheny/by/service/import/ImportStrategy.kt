package yayauheny.by.service.import

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import org.jooq.DSLContext
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider

/**
 * Результат импорта одного объекта (туалет + при необходимости здание).
 */
data class ImportObjectResult(
    val restroomId: UUID,
    val buildingId: UUID?
)

/**
 * Интерфейс стратегии импорта для различных провайдеров данных.
 * Каждый провайдер (2ГИС, Яндекс.Карты, Google Maps, OSM) реализует свою стратегию.
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

    /**
     * Импортирует batch объектов из JSON-структуры провайдера.
     * По умолчанию обрабатывает только первый элемент для обратной совместимости.
     * Стратегии могут переопределить этот метод для эффективной batch обработки.
     *
     * @param cityId ID города, в котором происходит импорт
     * @param payloadType тип формата payload
     * @param payload JsonObject - payload с массивом items или одиночным объектом
     * @param tx контекст транзакции; при tx != null стратегия использует его и не открывает свою транзакцию
     * @return список результатов импорта для каждого элемента
     */
    suspend fun importBatch(
        cityId: UUID,
        payloadType: ImportPayloadType,
        payload: JsonObject,
        tx: DSLContext? = null
    ): List<ImportObjectResult> {
        return listOf(importObject(cityId, payloadType, payload))
    }
}
