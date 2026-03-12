package yayauheny.by.service.import

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import org.jooq.DSLContext
import yayauheny.by.model.import.ImportBatchResult
import yayauheny.by.model.import.ImportExecutionResult
import yayauheny.by.model.import.ImportItemResult
import yayauheny.by.model.enums.ImportJobStatus
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.repository.CityRepository
import yayauheny.by.repository.RestroomImportRepository
import yayauheny.by.util.transactionSuspend

/**
 * Сервис для координации импорта данных из внешних источников.
 * Использует реестр стратегий (ImportStrategyRegistry) и проверяет допустимые пары provider/payloadType (ImportCapabilities).
 */
class ImportService(
    private val ctx: DSLContext,
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

        val items = payload["items"]?.jsonArray?.mapNotNull { it as? JsonObject } ?: emptyList()
        if (items.isEmpty()) {
            throw InvalidImportPayload("No items found in payload")
        }

        return ctx.transactionSuspend { tx ->
            val importIds =
                items.map { item ->
                    restroomImportRepository.createPendingInTx(
                        txCtx = tx,
                        provider = provider,
                        payloadType = payloadType,
                        cityId = cityId,
                        rawPayload = item
                    )
                }

            val batchResults = strategy.importBatch(cityId, payloadType, payload, tx)

            batchResults.forEachIndexed { i, result ->
                restroomImportRepository.markSuccessInTx(
                    txCtx = tx,
                    id = importIds[i],
                    buildingId = result.buildingId,
                    restroomId = result.restroomId
                )
            }

            val results =
                batchResults.mapIndexed { index, r ->
                    ImportItemResult(
                        index = index,
                        restroomId = r.restroomId,
                        buildingId = r.buildingId,
                        success = true
                    )
                }

            ImportBatchResult(
                importId = importIds.first(),
                totalProcessed = batchResults.size,
                successful = batchResults.size,
                failed = 0,
                results = results
            )
        }
    }

    private suspend fun requireCityExists(cityId: UUID) {
        if (cityRepository.findById(cityId) == null) throw CityNotFound(cityId)
    }
}
