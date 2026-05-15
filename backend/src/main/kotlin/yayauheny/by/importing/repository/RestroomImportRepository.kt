package yayauheny.by.importing.repository

import java.util.UUID
import org.jooq.DSLContext
import yayauheny.by.importing.model.ImportOriginKey
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto

data class ImportedRestroomUpsertResult(
    val restroom: RestroomResponseDto,
    val created: Boolean
)

interface RestroomImportRepository {
    fun findByOriginsInTx(
        txCtx: DSLContext,
        origins: Collection<ImportOriginKey>
    ): List<RestroomResponseDto>

    fun findByExternalMapsInTx(
        txCtx: DSLContext,
        provider: String,
        externalIds: Collection<String>
    ): List<RestroomResponseDto>

    fun findByMatchKeysInTx(
        txCtx: DSLContext,
        matchKeys: Collection<String>
    ): List<RestroomResponseDto>

    fun upsertImportedRestroomInTx(
        txCtx: DSLContext,
        createDto: RestroomCreateDto,
        matchKey: String?
    ): ImportedRestroomUpsertResult

    fun linkExternalMapInTx(
        txCtx: DSLContext,
        restroomId: UUID,
        provider: String,
        externalId: String
    ): RestroomResponseDto

    fun findByIdInTx(
        txCtx: DSLContext,
        restroomId: UUID
    ): RestroomResponseDto?
}
