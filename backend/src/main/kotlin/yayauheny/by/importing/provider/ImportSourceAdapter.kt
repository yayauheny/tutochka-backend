package yayauheny.by.importing.provider

import com.fasterxml.jackson.databind.JsonNode
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.model.InboxMetadata
import yayauheny.by.importing.model.NormalizedImportCommand
import yayauheny.by.importing.model.SourceLocationHint
import yayauheny.by.model.enums.ImportProvider

data class ProviderImportEnvelope(
    val inboxMetadata: InboxMetadata,
    val command: NormalizedImportCommand,
    val rawPayload: JsonNode
)

class ImportEnvelopeParsingException(
    message: String,
    val inboxMetadata: InboxMetadata? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

interface ImportSourceAdapter {
    val provider: ImportProvider

    fun extractInboxMetadata(rawPayload: JsonNode): InboxMetadata

    fun extractSourceLocation(rawPayload: JsonNode): SourceLocationHint

    fun parseEnvelope(
        rawPayload: JsonNode,
        context: ImportAdapterContext
    ): ProviderImportEnvelope
}
