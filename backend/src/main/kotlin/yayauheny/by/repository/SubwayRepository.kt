package yayauheny.by.repository

import java.util.UUID
import yayauheny.by.model.subway.SubwayLineCreateDto
import yayauheny.by.model.subway.SubwayLineResponseDto
import yayauheny.by.model.subway.SubwayStationCreateDto
import yayauheny.by.model.subway.SubwayStationResponseDto

interface SubwayRepository {
    suspend fun createLine(createDto: SubwayLineCreateDto): SubwayLineResponseDto

    suspend fun findLineById(id: UUID): SubwayLineResponseDto?

    suspend fun findAllLinesByCityId(cityId: UUID): List<SubwayLineResponseDto>

    suspend fun createStation(createDto: SubwayStationCreateDto): SubwayStationResponseDto

    suspend fun findStationById(id: UUID): SubwayStationResponseDto?

    suspend fun findStationsByLineId(lineId: UUID): List<SubwayStationResponseDto>

    suspend fun findNearestStation(
        lat: Double,
        lon: Double
    ): SubwayStationResponseDto?

    suspend fun setNearestStationForRestroom(
        restroomId: UUID,
        lat: Double,
        lon: Double
    ): Boolean

    suspend fun batchUpdateStationsForCity(
        cityId: UUID,
        forceUpdate: Boolean
    ): Int
}
