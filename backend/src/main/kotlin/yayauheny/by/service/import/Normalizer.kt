package yayauheny.by.service.import

import java.util.UUID
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.import.NormalizedRestroomCandidate

/**
 * Интерфейс для нормализации данных от провайдеров в каноническую модель.
 * Каждый провайдер реализует свой нормализатор для преобразования своих DTO в NormalizedRestroomCandidate.
 */
interface Normalizer<T> {
    /**
     * Нормализует DTO провайдера в каноническую модель NormalizedRestroomCandidate.
     * @param cityId ID города для импорта
     * @param providerDto DTO модель провайдера
     * @param payloadType тип payload (определяет originProvider через ImportProvider.fromPayloadType)
     * @return нормализованная каноническая модель
     */
    fun normalize(
        cityId: UUID,
        providerDto: T,
        payloadType: ImportPayloadType
    ): NormalizedRestroomCandidate
}
