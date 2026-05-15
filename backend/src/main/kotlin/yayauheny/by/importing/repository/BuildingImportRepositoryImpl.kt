package yayauheny.by.importing.repository

import java.util.UUID
import org.jooq.DSLContext
import yayauheny.by.model.building.BuildingCreateDto
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.repository.impl.BuildingRepositoryImpl

class BuildingImportRepositoryImpl(
    private val ctx: DSLContext
) : BuildingImportRepository {
    override fun findByExternalIdsInTx(
        txCtx: DSLContext,
        provider: String,
        externalIds: Collection<String>
    ): List<BuildingResponseDto> = BuildingRepositoryImpl(ctx).findByExternalIdsInTx(txCtx, provider, externalIds)

    override fun findByMatchKeysInTx(
        txCtx: DSLContext,
        matchKeys: Collection<String>
    ): List<BuildingResponseDto> = BuildingRepositoryImpl(ctx).findByMatchKeysInTx(txCtx, matchKeys)

    override fun upsertImportedBuildingInTx(
        txCtx: DSLContext,
        provider: String,
        externalId: String,
        createDto: BuildingCreateDto,
        matchKey: String?
    ): ImportedBuildingUpsertResult {
        val result = BuildingRepositoryImpl(ctx).upsertImportedBuildingInTx(txCtx, provider, externalId, createDto, matchKey)
        return ImportedBuildingUpsertResult(
            building = result.building,
            created = result.created
        )
    }

    override fun linkExternalIdInTx(
        txCtx: DSLContext,
        buildingId: UUID,
        provider: String,
        externalId: String
    ): BuildingResponseDto = BuildingRepositoryImpl(ctx).linkExternalIdInTx(txCtx, buildingId, provider, externalId)
}
