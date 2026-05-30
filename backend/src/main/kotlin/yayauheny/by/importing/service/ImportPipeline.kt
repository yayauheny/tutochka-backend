package yayauheny.by.importing.service

import java.util.UUID
import org.springframework.stereotype.Service
import yayauheny.by.importing.model.ImportPipelineResult
import yayauheny.by.importing.provider.ProviderImportEnvelope
import yayauheny.by.model.imports.ImportStatus

@Service
class ImportPipeline {

    fun importEnvelope(
        envelope: ProviderImportEnvelope
    ): ImportPipelineResult {
        // TODO: Re-implement using JPA instead of JOOQ
        return ImportPipelineResult(
            outcome = ImportStatus.CREATED,
            providerExternalId = envelope.command.providerObjectId,
            restroomId = UUID.randomUUID(),
            buildingId = null
        )
    }
}
