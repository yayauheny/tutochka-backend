package yayauheny.by.importing.repository

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.jooq.DSLContext
import yayauheny.by.importing.model.ImportEntityType
import yayauheny.by.importing.model.InboxMetadata
import yayauheny.by.model.enums.ImportJobStatus
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.tables.references.RESTROOM_IMPORTS
import yayauheny.by.util.toJSONB
import yayauheny.by.util.transactionSuspend

class ImportInboxRepositoryImpl(
    private val ctx: DSLContext
) : ImportInboxRepository {
    override suspend fun createPending(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        metadata: InboxMetadata?,
        rawPayload: JsonObject
    ): UUID =
        withContext(Dispatchers.IO) {
            ctx.transactionSuspend { txCtx ->
                upsertPendingInTx(txCtx, provider, payloadType, cityId, metadata, rawPayload)
            }
        }

    override suspend fun markSuccess(
        id: UUID,
        buildingId: UUID?,
        restroomId: UUID
    ) {
        withContext(Dispatchers.IO) {
            ctx.transactionSuspend { txCtx ->
                markSuccessInTx(txCtx, id, buildingId, restroomId)
            }
        }
    }

    override suspend fun markFailed(
        id: UUID,
        errorMessage: String
    ) {
        withContext(Dispatchers.IO) {
            ctx.transactionSuspend { txCtx ->
                markFailedInTx(txCtx, id, errorMessage)
            }
        }
    }

    override fun upsertPendingInTx(
        txCtx: DSLContext,
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        metadata: InboxMetadata?,
        rawPayload: JsonObject
    ): UUID {
        val now = Instant.now()
        val id = UUID.randomUUID()
        val entityType = (metadata?.entityType ?: ImportEntityType.PLACE).name.lowercase()
        val externalId = metadata?.externalId
        val payloadHash = metadata?.payloadHash
        val sourceUrl = metadata?.sourceUrl
        val scrapedAt = metadata?.scrapedAt

        if (externalId == null) {
            txCtx
                .insertInto(RESTROOM_IMPORTS)
                .set(RESTROOM_IMPORTS.ID, id)
                .set(RESTROOM_IMPORTS.PROVIDER, provider.name)
                .set(RESTROOM_IMPORTS.PAYLOAD_TYPE, payloadType.name)
                .set(RESTROOM_IMPORTS.CITY_ID, cityId)
                .set(RESTROOM_IMPORTS.ENTITY_TYPE, entityType)
                .set(RESTROOM_IMPORTS.EXTERNAL_ID, null as String?)
                .set(RESTROOM_IMPORTS.PAYLOAD_HASH, payloadHash)
                .set(RESTROOM_IMPORTS.RAW_PAYLOAD, rawPayload.toJSONB()!!)
                .set(RESTROOM_IMPORTS.SOURCE_URL, sourceUrl)
                .set(RESTROOM_IMPORTS.SCRAPED_AT, scrapedAt)
                .set(RESTROOM_IMPORTS.STATUS, ImportJobStatus.PENDING.name)
                .set(RESTROOM_IMPORTS.ERROR_MESSAGE, null as String?)
                .set(RESTROOM_IMPORTS.BUILDING_ID, null as UUID?)
                .set(RESTROOM_IMPORTS.RESTROOM_ID, null as UUID?)
                .set(RESTROOM_IMPORTS.PROCESSED_AT, null as Instant?)
                .set(RESTROOM_IMPORTS.ATTEMPTS, 1)
                .set(RESTROOM_IMPORTS.LAST_ATTEMPT_AT, now)
                .set(RESTROOM_IMPORTS.NEXT_RETRY_AT, null as Instant?)
                .execute()
            return id
        }

        return txCtx
            .insertInto(RESTROOM_IMPORTS)
            .set(RESTROOM_IMPORTS.ID, id)
            .set(RESTROOM_IMPORTS.PROVIDER, provider.name)
            .set(RESTROOM_IMPORTS.PAYLOAD_TYPE, payloadType.name)
            .set(RESTROOM_IMPORTS.CITY_ID, cityId)
            .set(RESTROOM_IMPORTS.ENTITY_TYPE, entityType)
            .set(RESTROOM_IMPORTS.EXTERNAL_ID, externalId)
            .set(RESTROOM_IMPORTS.PAYLOAD_HASH, payloadHash)
            .set(RESTROOM_IMPORTS.RAW_PAYLOAD, rawPayload.toJSONB()!!)
            .set(RESTROOM_IMPORTS.SOURCE_URL, sourceUrl)
            .set(RESTROOM_IMPORTS.SCRAPED_AT, scrapedAt)
            .set(RESTROOM_IMPORTS.STATUS, ImportJobStatus.PENDING.name)
            .set(RESTROOM_IMPORTS.ERROR_MESSAGE, null as String?)
            .set(RESTROOM_IMPORTS.BUILDING_ID, null as UUID?)
            .set(RESTROOM_IMPORTS.RESTROOM_ID, null as UUID?)
            .set(RESTROOM_IMPORTS.PROCESSED_AT, null as Instant?)
            .set(RESTROOM_IMPORTS.ATTEMPTS, 1)
            .set(RESTROOM_IMPORTS.LAST_ATTEMPT_AT, now)
            .set(RESTROOM_IMPORTS.NEXT_RETRY_AT, null as Instant?)
            .onConflict(RESTROOM_IMPORTS.PROVIDER, RESTROOM_IMPORTS.ENTITY_TYPE, RESTROOM_IMPORTS.EXTERNAL_ID)
            .where(RESTROOM_IMPORTS.EXTERNAL_ID.isNotNull)
            .doUpdate()
            .set(RESTROOM_IMPORTS.PAYLOAD_TYPE, payloadType.name)
            .set(RESTROOM_IMPORTS.CITY_ID, cityId)
            .set(RESTROOM_IMPORTS.PAYLOAD_HASH, payloadHash)
            .set(RESTROOM_IMPORTS.RAW_PAYLOAD, rawPayload.toJSONB()!!)
            .set(RESTROOM_IMPORTS.SOURCE_URL, sourceUrl)
            .set(RESTROOM_IMPORTS.SCRAPED_AT, scrapedAt)
            .set(RESTROOM_IMPORTS.STATUS, ImportJobStatus.PENDING.name)
            .set(RESTROOM_IMPORTS.ERROR_MESSAGE, null as String?)
            .set(RESTROOM_IMPORTS.BUILDING_ID, null as UUID?)
            .set(RESTROOM_IMPORTS.RESTROOM_ID, null as UUID?)
            .set(RESTROOM_IMPORTS.PROCESSED_AT, null as Instant?)
            .set(RESTROOM_IMPORTS.ATTEMPTS, RESTROOM_IMPORTS.ATTEMPTS.plus(1))
            .set(RESTROOM_IMPORTS.LAST_ATTEMPT_AT, now)
            .set(RESTROOM_IMPORTS.NEXT_RETRY_AT, null as Instant?)
            .returning(RESTROOM_IMPORTS.ID)
            .fetchOne()
            ?.get(RESTROOM_IMPORTS.ID)
            ?: id
    }

    override fun markSuccessInTx(
        txCtx: DSLContext,
        id: UUID,
        buildingId: UUID?,
        restroomId: UUID
    ) {
        txCtx
            .update(RESTROOM_IMPORTS)
            .set(RESTROOM_IMPORTS.STATUS, ImportJobStatus.SUCCESS.name)
            .set(RESTROOM_IMPORTS.BUILDING_ID, buildingId)
            .set(RESTROOM_IMPORTS.RESTROOM_ID, restroomId)
            .set(RESTROOM_IMPORTS.PROCESSED_AT, Instant.now())
            .where(RESTROOM_IMPORTS.ID.eq(id))
            .execute()
    }

    override fun markFailedInTx(
        txCtx: DSLContext,
        id: UUID,
        errorMessage: String
    ) {
        txCtx
            .update(RESTROOM_IMPORTS)
            .set(RESTROOM_IMPORTS.STATUS, ImportJobStatus.FAILED.name)
            .set(RESTROOM_IMPORTS.ERROR_MESSAGE, errorMessage)
            .set(RESTROOM_IMPORTS.PROCESSED_AT, Instant.now())
            .where(RESTROOM_IMPORTS.ID.eq(id))
            .execute()
    }
}
