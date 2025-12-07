package yayauheny.by.repository

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.import.ImportPayloadType
import yayauheny.by.model.import.ImportProvider

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
}
