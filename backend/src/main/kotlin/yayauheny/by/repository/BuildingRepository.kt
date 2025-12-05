package yayauheny.by.repository

import java.util.UUID
import yayauheny.by.model.building.BuildingCreateDto
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.building.BuildingUpdateDto

interface BuildingRepository : BaseRepository<BuildingResponseDto, BuildingCreateDto, BuildingUpdateDto, UUID> {
    suspend fun findByExternalId(provider: String, externalId: String): BuildingResponseDto?
}
