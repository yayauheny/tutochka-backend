package yayauheny.by.repository.impl

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.jooq.DSLContext
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.ImportJobStatus
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.repository.RestroomImportRepository
import yayauheny.by.tables.references.RESTROOM_IMPORTS
import yayauheny.by.util.toJSONB
import yayauheny.by.util.transactionSuspend

class RestroomImportRepositoryImpl(
    private val ctx: DSLContext
) : RestroomImportRepository {
    override suspend fun createPending(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        rawPayload: JsonObject
    ): UUID =
        withContext(Dispatchers.IO) {
            ctx.transactionSuspend { txCtx ->
                val id = UUID.randomUUID()

                txCtx
                    .insertInto(RESTROOM_IMPORTS)
                    .set(RESTROOM_IMPORTS.ID, id)
                    .set(RESTROOM_IMPORTS.PROVIDER, provider.name)
                    .set(RESTROOM_IMPORTS.PAYLOAD_TYPE, payloadType.name)
                    .set(RESTROOM_IMPORTS.CITY_ID, cityId)
                    .set(RESTROOM_IMPORTS.RAW_PAYLOAD, rawPayload.toJSONB()!!)
                    .set(RESTROOM_IMPORTS.STATUS, ImportJobStatus.PENDING.name)
                    .execute()

                id
            }
        }

    override suspend fun markSuccess(
        id: UUID,
        buildingId: UUID?,
        restroomId: UUID
    ): Unit =
        withContext(Dispatchers.IO) {
            ctx.transactionSuspend { txCtx ->
                txCtx
                    .update(RESTROOM_IMPORTS)
                    .set(RESTROOM_IMPORTS.STATUS, ImportJobStatus.SUCCESS.name)
                    .set(RESTROOM_IMPORTS.BUILDING_ID, buildingId)
                    .set(RESTROOM_IMPORTS.RESTROOM_ID, restroomId)
                    .set(RESTROOM_IMPORTS.PROCESSED_AT, Instant.now())
                    .where(RESTROOM_IMPORTS.ID.eq(id))
                    .execute()
            }
        }

    override suspend fun markFailed(
        id: UUID,
        errorMessage: String
    ): Unit =
        withContext(Dispatchers.IO) {
            ctx.transactionSuspend { txCtx ->
                txCtx
                    .update(RESTROOM_IMPORTS)
                    .set(RESTROOM_IMPORTS.STATUS, ImportJobStatus.FAILED.name)
                    .set(RESTROOM_IMPORTS.ERROR_MESSAGE, errorMessage)
                    .set(RESTROOM_IMPORTS.PROCESSED_AT, Instant.now())
                    .where(RESTROOM_IMPORTS.ID.eq(id))
                    .execute()
            }
        }

    override suspend fun createPendingInTx(
        txCtx: DSLContext,
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        rawPayload: JsonObject
    ): UUID {
        val id = UUID.randomUUID()
        txCtx
            .insertInto(RESTROOM_IMPORTS)
            .set(RESTROOM_IMPORTS.ID, id)
            .set(RESTROOM_IMPORTS.PROVIDER, provider.name)
            .set(RESTROOM_IMPORTS.PAYLOAD_TYPE, payloadType.name)
            .set(RESTROOM_IMPORTS.CITY_ID, cityId)
            .set(RESTROOM_IMPORTS.RAW_PAYLOAD, rawPayload.toJSONB()!!)
            .set(RESTROOM_IMPORTS.STATUS, ImportJobStatus.PENDING.name)
            .execute()
        return id
    }

    override suspend fun markSuccessInTx(
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

    override suspend fun markFailedInTx(
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
