package yayauheny.by.importing.service

import java.util.UUID
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yayauheny.by.common.errors.NotImplementedException
import yayauheny.by.importing.model.ImportBatchSummary
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider

@Service
class ImportBatchProcessor(
    private val importPipeline: ImportPipeline
) {
    @Transactional
    fun process(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        items: List<JsonNode>,
        chunkSize: Int
    ): ImportBatchSummary {
        throw NotImplementedException("Batch import is not available in this stage")
    }
}
