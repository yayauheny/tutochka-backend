package yayauheny.by.service.import

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.import.ImportExecutionResult
import yayauheny.by.model.import.ImportJobStatus
import yayauheny.by.model.import.ImportPayloadType
import yayauheny.by.model.import.ImportProvider
import yayauheny.by.repository.RestroomImportRepository

/**
 * Сервис для координации импорта данных из внешних источников.
 * Использует стратегии (ImportStrategy) для различных провайдеров.
 */
class ImportService(
    strategies: List<ImportStrategy>,
    private val restroomImportRepository: RestroomImportRepository
) {
    private val strategiesByProvider = strategies.associateBy { it.provider() }

    /**
     * Импортирует объект из внешнего источника данных.
     *
     * @param provider провайдер данных (2ГИС, Яндекс.Карты и т.д.)
     * @param payloadType тип формата payload
     * @param cityId ID города для импорта
     * @param payload JSON payload от провайдера
     * @return результат импорта с ID созданных сущностей
     * @throws IllegalArgumentException если провайдер не поддерживается
     * @throws Exception если произошла ошибка при импорте (запись будет помечена как FAILED)
     */
    suspend fun import(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID,
        payload: JsonObject
    ): ImportExecutionResult {
        val strategy =
            strategiesByProvider[provider]
                ?: error("Unsupported import provider: $provider")

        // 1. Создаем запись импорта (PENDING)
        val importId =
            restroomImportRepository.createPending(
                provider = provider,
                payloadType = payloadType,
                cityId = cityId,
                rawPayload = payload
            )

        return try {
            // 2. Вызываем стратегию
            val result = strategy.importObject(cityId, payloadType, payload)

            // 3. Обновляем запись импорта (SUCCESS)
            restroomImportRepository.markSuccess(
                id = importId,
                buildingId = result.buildingId,
                restroomId = result.restroomId
            )

            ImportExecutionResult(
                importId = importId,
                restroomId = result.restroomId,
                buildingId = result.buildingId,
                status = ImportJobStatus.SUCCESS
            )
        } catch (t: Throwable) {
            // 4. Обновляем запись импорта (FAILED)
            restroomImportRepository.markFailed(
                id = importId,
                errorMessage = t.message ?: "Unknown error"
            )
            throw t
        }
    }
}
