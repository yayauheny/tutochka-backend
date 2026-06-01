package yayauheny.by.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.model.enums.ImportProvider
import java.util.UUID

@Repository
interface RestroomJpaRepository : JpaRepository<RestroomEntity, UUID>, RestroomFilterRepository {
    fun findByOriginProviderAndOriginId(originProvider: ImportProvider, originId: String): RestroomEntity?

    fun findByRestroomMatchKey(restroomMatchKey: String): RestroomEntity?

    @Query(
        value = """
            SELECT *
            FROM restrooms
            WHERE is_deleted = false
              AND external_maps ->> :provider = :externalId
            LIMIT 1
        """,
        nativeQuery = true
    )
    fun findByExternalMap(
        @Param("provider") provider: String,
        @Param("externalId") externalId: String
    ): RestroomEntity?

    @Query(
        value = """
            SELECT *
            FROM restrooms
            WHERE status = 'ACTIVE'
              AND is_hidden = false
              AND is_deleted = false
              AND ST_DWithin(coordinates, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :distanceMeters, true)
            ORDER BY coordinates <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)
            LIMIT :limit
        """,
        nativeQuery = true
    )
    fun findNearest(lon: Double, lat: Double, distanceMeters: Int, limit: Int): List<RestroomEntity>
}
