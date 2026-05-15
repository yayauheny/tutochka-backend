package yayauheny.by.importing.repository

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import org.jooq.DSLContext
import yayauheny.by.importing.model.InboxMetadata
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider

interface ImportInboxRepository {
    suspend fun createPending(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        metadata: InboxMetadata?,
        rawPayload: JsonObject
    ): UUID

    suspend fun markSuccess(
        id: UUID,
        buildingId: UUID?,
        restroomId: UUID
    )

    suspend fun markFailed(
        id: UUID,
        errorMessage: String
    )

    fun upsertPendingInTx(
        txCtx: DSLContext,
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        metadata: InboxMetadata?,
        rawPayload: JsonObject
    ): UUID

    fun markSuccessInTx(
        txCtx: DSLContext,
        id: UUID,
        buildingId: UUID?,
        restroomId: UUID
    )

    fun markFailedInTx(
        txCtx: DSLContext,
        id: UUID,
        errorMessage: String
    )
}
