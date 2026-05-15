package yayauheny.by.importing.provider

import kotlinx.serialization.json.JsonObject
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.model.InboxMetadata
import yayauheny.by.importing.model.NormalizedImportCommand
import yayauheny.by.importing.model.SourceLocationHint
import yayauheny.by.model.enums.ImportProvider

data class ProviderImportEnvelope(
    val inboxMetadata: InboxMetadata,
    val command: NormalizedImportCommand,
    val rawPayload: JsonObject
)

class ImportEnvelopeParsingException(
    message: String,
    val inboxMetadata: InboxMetadata? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause)

interface ImportSourceAdapter {
    val provider: ImportProvider

    fun extractInboxMetadata(rawPayload: JsonObject): InboxMetadata

    fun extractSourceLocation(rawPayload: JsonObject): SourceLocationHint

    fun parseEnvelope(
        rawPayload: JsonObject,
        context: ImportAdapterContext
    ): ProviderImportEnvelope
}
