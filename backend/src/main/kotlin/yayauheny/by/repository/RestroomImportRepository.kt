package yayauheny.by.repository

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import org.jooq.DSLContext
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider

/**
 * Репозиторий для работы с таблицей restroom_imports.
 * Отслеживает историю импортов туалетов из внешних источников.
 */
interface RestroomImportRepository {
    /**
     * Создает новую запись импорта со статусом PENDING.
     */
    suspend fun createPending(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        rawPayload: JsonObject
    ): UUID

    /**
     * Обновляет запись импорта на статус SUCCESS с привязкой к созданным сущностям.
     */
    suspend fun markSuccess(
        id: UUID,
        buildingId: UUID?,
        restroomId: UUID
    )

    /**
     * Обновляет запись импорта на статус FAILED с сообщением об ошибке.
     */
    suspend fun markFailed(
        id: UUID,
        errorMessage: String
    )

    suspend fun createPendingInTx(
        txCtx: DSLContext,
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        rawPayload: JsonObject
    ): UUID

    suspend fun markSuccessInTx(
        txCtx: DSLContext,
        id: UUID,
        buildingId: UUID?,
        restroomId: UUID
    )

    suspend fun markFailedInTx(
        txCtx: DSLContext,
        id: UUID,
        errorMessage: String
    )
}
