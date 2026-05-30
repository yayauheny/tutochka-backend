package yayauheny.by.importing.service

import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID
import org.springframework.stereotype.Service
import yayauheny.by.importing.exception.CityNotFound
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.provider.ImportCapabilities
import yayauheny.by.model.enums.ImportJobStatus
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.import.ImportBatchResult
import yayauheny.by.model.import.ImportExecutionResult
import yayauheny.by.model.import.ImportItemResult
import yayauheny.by.model.import.ImportStatus
import yayauheny.by.repository.CityJpaRepository

import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ImportService(
    private val cityRepository: CityJpaRepository,
    private val importBatchProcessor: ImportBatchProcessor
) {
    companion object {
        const val MAX_BATCH_ITEMS = 5000
        const val CHUNK_SIZE = 150
    }

    fun import(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        payload: JsonNode
    ): ImportExecutionResult {
        ImportCapabilities.requireSupported(provider, payloadType)
        cityId?.let { requireCityExists(it) }

        val summary =
            importBatchProcessor.process(
                provider = provider,
                payloadType = payloadType,
                cityId = cityId,
                items = listOf(payload),
                chunkSize = 1
            )

        val result = summary.results.single()
        if (result.outcome == ImportStatus.FAILED) {
            if (result.errorCode == "INVALID_PAYLOAD" || result.errorCode == "CITY_RESOLUTION_FAILED") {
                throw InvalidImportPayload(result.errorMessage ?: "Import failed")
            }
            throw IllegalStateException(result.errorMessage ?: "Import failed")
        }

        return ImportExecutionResult(
            importId = summary.importId,
            restroomId = requireNotNull(result.restroomId),
            buildingId = result.buildingId,
            status = ImportJobStatus.SUCCESS
        )
    }

    fun importBatch(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        payload: JsonNode
    ): ImportBatchResult {
        val itemsNode = payload.get("items")
        val items = if (itemsNode != null && itemsNode.isArray) {
            itemsNode.elements().asSequence().toList()
        } else emptyList()

        if (items.isEmpty()) {
            throw InvalidImportPayload("No items found in payload")
        }
        if (items.size > MAX_BATCH_ITEMS) {
            throw InvalidImportPayload("Batch size must not exceed $MAX_BATCH_ITEMS items")
        }
        ImportCapabilities.requireSupported(provider, payloadType)
        cityId?.let { requireCityExists(it) }

        val summary =
            importBatchProcessor.process(
                provider = provider,
                payloadType = payloadType,
                cityId = cityId,
                items = items,
                chunkSize = CHUNK_SIZE
            )

        return ImportBatchResult(
            importId = summary.importId,
            totalProcessed = summary.totalProcessed,
            successful = summary.successful,
            failed = summary.failed,
            results =
                summary.results.mapIndexed { index, result ->
                    ImportItemResult(
                        index = index,
                        outcome = result.outcome,
                        providerExternalId = result.providerExternalId,
                        restroomId = result.restroomId,
                        buildingId = result.buildingId,
                        duplicateOfRestroomId = result.duplicateOfRestroomId,
                        duplicateReason = result.duplicateReason,
                        errorCode = result.errorCode,
                        errorMessage = result.errorMessage
                    )
                }
        )
    }

    private fun requireCityExists(cityId: UUID) {
        if (!cityRepository.existsById(cityId)) {
            throw CityNotFound(cityId)
        }
    }
}
