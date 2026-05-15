package yayauheny.by.importing.repository

import java.util.UUID
import org.jooq.DSLContext
import yayauheny.by.importing.model.ImportOriginKey
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto

class RestroomImportRepositoryImpl(
    private val ctx: DSLContext
) : RestroomImportRepository {
    override fun findByOriginsInTx(
        txCtx: DSLContext,
        origins: Collection<ImportOriginKey>
    ): List<RestroomResponseDto> =
        yayauheny.by.repository.impl.RestroomRepositoryImpl(ctx).findByOriginsInTx(
            txCtx = txCtx,
            origins = origins
        )

    override fun findByExternalMapsInTx(
        txCtx: DSLContext,
        provider: String,
        externalIds: Collection<String>
    ): List<RestroomResponseDto> =
        yayauheny.by.repository.impl
            .RestroomRepositoryImpl(ctx)
            .findByExternalMapsInTx(txCtx, provider, externalIds)

    override fun findByMatchKeysInTx(
        txCtx: DSLContext,
        matchKeys: Collection<String>
    ): List<RestroomResponseDto> =
        yayauheny.by.repository.impl
            .RestroomRepositoryImpl(ctx)
            .findByMatchKeysInTx(txCtx, matchKeys)

    override fun upsertImportedRestroomInTx(
        txCtx: DSLContext,
        createDto: RestroomCreateDto,
        matchKey: String?
    ): ImportedRestroomUpsertResult {
        val result =
            yayauheny.by.repository.impl
                .RestroomRepositoryImpl(ctx)
                .upsertImportedRestroomInTx(txCtx, createDto, matchKey)
        return ImportedRestroomUpsertResult(
            restroom = result.restroom,
            created = result.created
        )
    }

    override fun linkExternalMapInTx(
        txCtx: DSLContext,
        restroomId: UUID,
        provider: String,
        externalId: String
    ): RestroomResponseDto =
        yayauheny.by.repository.impl
            .RestroomRepositoryImpl(ctx)
            .linkExternalMapInTx(txCtx, restroomId, provider, externalId)

    override fun findByIdInTx(
        txCtx: DSLContext,
        restroomId: UUID
    ): RestroomResponseDto? =
        yayauheny.by.repository.impl
            .RestroomRepositoryImpl(ctx)
            .findByIdInTx(txCtx, restroomId)
}
