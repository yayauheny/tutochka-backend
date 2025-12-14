package yayauheny.by.repository.impl

import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import yayauheny.by.util.toJSONBOrEmpty
import yayauheny.by.model.import.ImportJobStatus
import yayauheny.by.model.import.ImportPayloadType
import yayauheny.by.model.import.ImportProvider
import yayauheny.by.repository.RestroomImportRepository
import yayauheny.by.util.transactionSuspend

class RestroomImportRepositoryImpl(
    private val ctx: DSLContext
) : RestroomImportRepository {
    private val restroomImports = DSL.table("restroom_imports")
    private val idField = DSL.field("id", SQLDataType.UUID)
    private val providerField = DSL.field("provider", SQLDataType.VARCHAR(50))
    private val payloadTypeField = DSL.field("payload_type", SQLDataType.VARCHAR(50))
    private val cityIdField = DSL.field("city_id", SQLDataType.UUID)
    private val rawPayloadField = DSL.field("raw_payload", SQLDataType.JSONB)
    private val buildingIdField = DSL.field("building_id", SQLDataType.UUID)
    private val restroomIdField = DSL.field("restroom_id", SQLDataType.UUID)
    private val statusField = DSL.field("status", SQLDataType.VARCHAR(20))
    private val errorMessageField = DSL.field("error_message", SQLDataType.CLOB)
    private val processedAtField = DSL.field("processed_at", SQLDataType.LOCALDATETIME)

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
                    .insertInto(restroomImports)
                    .set(idField, id)
                    .set(providerField, provider.name.lowercase())
                    .set(payloadTypeField, payloadType.name)
                    .set(cityIdField, cityId)
                    .set(rawPayloadField, rawPayload.toJSONBOrEmpty())
                    .set(statusField, ImportJobStatus.PENDING.name.lowercase())
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
                    .update(restroomImports)
                    .set(statusField, ImportJobStatus.SUCCESS.name.lowercase())
                    .set(buildingIdField, buildingId)
                    .set(restroomIdField, restroomId)
                    .set(processedAtField, LocalDateTime.now())
                    .where(idField.eq(id))
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
                    .update(restroomImports)
                    .set(statusField, ImportJobStatus.FAILED.name.lowercase())
                    .set(errorMessageField, errorMessage.take(2000))
                    .set(processedAtField, LocalDateTime.now())
                    .where(idField.eq(id))
                    .execute()
            }
        }
}
