package yayauheny.by.importing.repository

import java.util.UUID
import org.jooq.DSLContext
import yayauheny.by.model.building.BuildingCreateDto
import yayauheny.by.model.building.BuildingResponseDto

data class ImportedBuildingUpsertResult(
    val building: BuildingResponseDto,
    val created: Boolean
)

interface BuildingImportRepository {
    fun findByExternalIdsInTx(
        txCtx: DSLContext,
        provider: String,
        externalIds: Collection<String>
    ): List<BuildingResponseDto>

    fun findByMatchKeysInTx(
        txCtx: DSLContext,
        matchKeys: Collection<String>
    ): List<BuildingResponseDto>

    fun upsertImportedBuildingInTx(
        txCtx: DSLContext,
        provider: String,
        externalId: String,
        createDto: BuildingCreateDto,
        matchKey: String?
    ): ImportedBuildingUpsertResult

    fun linkExternalIdInTx(
        txCtx: DSLContext,
        buildingId: UUID,
        provider: String,
        externalId: String
    ): BuildingResponseDto
}
