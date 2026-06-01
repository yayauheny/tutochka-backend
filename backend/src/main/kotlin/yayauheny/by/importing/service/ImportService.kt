package yayauheny.by.importing.service

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yayauheny.by.importing.exception.CityNotFound
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.provider.ImportCapabilities
import yayauheny.by.model.enums.ImportJobStatus
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.imports.ImportBatchResult
import yayauheny.by.model.imports.ImportExecutionResult
import yayauheny.by.model.imports.ImportStatus
import yayauheny.by.repository.CityJpaRepository
import java.util.UUID

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
        val result = importBatch(provider, payloadType, cityId, wrapSinglePayload(payload))
        val item = result.results.single()
        if (item.outcome == ImportStatus.FAILED) {
            throw InvalidImportPayload(item.errorMessage ?: "Import failed")
        }

        return ImportExecutionResult(
            importId = result.importId,
            restroomId = requireNotNull(item.restroomId),
            buildingId = item.buildingId,
            status = ImportJobStatus.SUCCESS
        )
    }

    fun importBatch(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        payload: JsonNode
    ): ImportBatchResult {
        val items = extractItems(payload)
        if (items.isEmpty()) {
            throw InvalidImportPayload("No items found in payload")
        }
        if (items.size > MAX_BATCH_ITEMS) {
            throw InvalidImportPayload("Batch size must not exceed $MAX_BATCH_ITEMS items")
        }

        ImportCapabilities.requireSupported(provider, payloadType)
        cityId?.let(::requireCityExists)

        val summary = importBatchProcessor.process(
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
            results = summary.results.mapIndexed { index, result ->
                yayauheny.by.model.imports.ImportItemResult(
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

    private fun extractItems(payload: JsonNode): List<JsonNode> {
        val items = payload.get("items")
        if (items == null || !items.isArray) {
            throw InvalidImportPayload("Payload must contain an 'items' array")
        }
        return items.toList().filter { it.isObject }
    }

    private fun wrapSinglePayload(payload: JsonNode): JsonNode =
        if (payload.has("items")) {
            payload
        } else {
            com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode().set<JsonNode>(
                "items",
                com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode().add(payload)
            )
        }
}
