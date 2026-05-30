package yayauheny.by.importing.service

import com.fasterxml.jackson.databind.JsonNode
import java.util.UUID
import org.springframework.stereotype.Service
import yayauheny.by.common.errors.NotImplementedException
import yayauheny.by.importing.exception.CityNotFound
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.provider.ImportCapabilities
import yayauheny.by.model.enums.ImportJobStatus
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.imports.ImportBatchResult
import yayauheny.by.model.imports.ImportExecutionResult
import yayauheny.by.model.imports.ImportItemResult
import yayauheny.by.model.imports.ImportStatus
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
        throw NotImplementedException("Import is not available in this stage")
    }

    fun importBatch(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        payload: JsonNode
    ): ImportBatchResult {
        throw NotImplementedException("Batch import is not available in this stage")
    }
}
