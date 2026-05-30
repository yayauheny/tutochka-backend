package yayauheny.by.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yayauheny.by.entity.RestroomEntity
import java.util.UUID

@Repository
interface RestroomJpaRepository : JpaRepository<RestroomEntity, UUID>, RestroomFilterRepository {
    @Query(value = "SELECT * FROM restrooms WHERE ST_DWithin(coordinates, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :distanceMeters, true) ORDER BY coordinates <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326) LIMIT :limit", nativeQuery = true)
    fun findNearest(lon: Double, lat: Double, distanceMeters: Int, limit: Int): List<RestroomEntity>
}
