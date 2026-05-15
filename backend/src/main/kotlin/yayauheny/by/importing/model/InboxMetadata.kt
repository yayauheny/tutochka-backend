package yayauheny.by.importing.model

import java.time.Instant
import yayauheny.by.model.enums.ImportProvider

data class InboxMetadata(
    val provider: ImportProvider,
    val entityType: ImportEntityType,
    val externalId: String?,
    val sourceUrl: String?,
    val scrapedAt: Instant?,
    val payloadHash: String
)
