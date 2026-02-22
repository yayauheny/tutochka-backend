package yayauheny.by.service.import

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.import.ImportBatchResult
import yayauheny.by.model.import.ImportExecutionResult
import yayauheny.by.model.import.ImportItemResult
import yayauheny.by.model.enums.ImportJobStatus
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.repository.CityRepository
import yayauheny.by.repository.RestroomImportRepository

/**
 * Сервис для координации импорта данных из внешних источников.
 * Использует реестр стратегий (ImportStrategyRegistry) и проверяет допустимые пары provider/payloadType (ImportCapabilities).
 */
class ImportService(
    private val registry: ImportStrategyRegistry,
    private val cityRepository: CityRepository,
    private val restroomImportRepository: RestroomImportRepository
) {
    suspend fun import(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID,
        payload: JsonObject
    ): ImportExecutionResult {
        ImportCapabilities.requireSupported(provider, payloadType)
        requireCityExists(cityId)
        val strategy = registry.get(provider)

        val importId =
            restroomImportRepository.createPending(
                provider = provider,
                payloadType = payloadType,
                cityId = cityId,
                rawPayload = payload
            )

        return try {
            val result = strategy.importObject(cityId, payloadType, payload)

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
            restroomImportRepository.markFailed(
                id = importId,
                errorMessage = t.message ?: "Unknown error"
            )
            throw t
        }
    }

    suspend fun importBatch(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID,
        payload: JsonObject
    ): ImportBatchResult {
        ImportCapabilities.requireSupported(provider, payloadType)
        requireCityExists(cityId)
        val strategy = registry.get(provider)

        val importId =
            restroomImportRepository.createPending(
                provider = provider,
                payloadType = payloadType,
                cityId = cityId,
                rawPayload = payload
            )

        val results = mutableListOf<ImportItemResult>()
        var successful = 0
        var failed = 0

        try {
            val batchResults = strategy.importBatch(cityId, payloadType, payload)

            batchResults.forEachIndexed { index, result ->
                results.add(
                    ImportItemResult(
                        index = index,
                        restroomId = result.restroomId,
                        buildingId = result.buildingId,
                        success = true
                    )
                )
                successful++

                // Помечаем первый успешный импорт в репозитории
                if (index == 0) {
                    restroomImportRepository.markSuccess(
                        id = importId,
                        buildingId = result.buildingId,
                        restroomId = result.restroomId
                    )
                }
            }

            return ImportBatchResult(
                importId = importId,
                totalProcessed = batchResults.size,
                successful = successful,
                failed = failed,
                results = results
            )
        } catch (t: Throwable) {
            // Если batch полностью провалился, помечаем как failed
            // Но если часть элементов обработалась успешно, это уже не сработает
            // В реальной реализации нужно обрабатывать ошибки на уровне стратегии
            restroomImportRepository.markFailed(
                id = importId,
                errorMessage = t.message ?: "Unknown error"
            )
            throw t
        }
    }

    private suspend fun requireCityExists(cityId: UUID) {
        if (cityRepository.findById(cityId) == null) throw CityNotFound(cityId)
    }
}
