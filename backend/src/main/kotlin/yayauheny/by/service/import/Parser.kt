package yayauheny.by.service.import

import kotlinx.serialization.json.JsonObject

/**
 * Интерфейс для парсинга JSON объектов от провайдеров в DTO модели.
 * Каждый провайдер реализует свой парсер для своего формата данных.
 */
interface Parser<T> {
    /**
     * Парсит JsonObject в DTO модель провайдера.
     * @param jsonObject JSON объект от провайдера
     * @return DTO модель провайдера
     * @throws IllegalArgumentException если JSON невалидный или отсутствуют обязательные поля
     */
    fun parse(jsonObject: JsonObject): T
}
