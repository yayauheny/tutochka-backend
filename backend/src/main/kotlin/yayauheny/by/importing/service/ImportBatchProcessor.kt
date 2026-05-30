package yayauheny.by.importing.service

import java.util.UUID
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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
        // TODO: Re-implement batch processing with JPA 
        return ImportBatchSummary(
            importId = UUID.randomUUID(),
            totalProcessed = items.size,
            successful = items.size,
            failed = 0,
            created = items.size,
            updated = 0,
            linkedDuplicates = 0,
            skippedDuplicates = 0,
            warnings = 0,
            results = emptyList()
        )
    }
}
