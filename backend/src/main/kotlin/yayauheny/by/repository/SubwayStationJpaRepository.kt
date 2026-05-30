package yayauheny.by.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yayauheny.by.entity.SubwayStationEntity
import java.util.UUID

@Repository
interface SubwayStationJpaRepository : JpaRepository<SubwayStationEntity, UUID> {
    fun findBySubwayLineId(lineId: UUID): List<SubwayStationEntity>

    @Query(value = "SELECT * FROM subway_stations ORDER BY coordinates <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) LIMIT 1", nativeQuery = true)
    fun findNearestStation(lon: Double, lat: Double): SubwayStationEntity?
}
