package yayauheny.by.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.cache.nearest.NearestRestroomCacheRow
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
            WHERE (
                (:provider = '2gis' AND two_gis_id = :externalId)
                OR (:provider = 'yandex' AND yandex_id = :externalId)
                OR (:provider = 'google' AND google_id = :externalId)
                OR (:provider = 'apple' AND apple_id = :externalId)
            )
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
              AND ST_DWithin(coordinates, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326), :distanceMeters, true)
            ORDER BY coordinates <-> ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)
            LIMIT :limit
        """,
        nativeQuery = true
    )
    fun findNearest(lon: Double, lat: Double, distanceMeters: Int, limit: Int): List<RestroomEntity>

    @Query(
        value = """
            SELECT
                r.id as id,
                ST_Y(r.coordinates) as lat,
                ST_X(r.coordinates) as lon,
                COALESCE(NULLIF(BTRIM(r.name), ''), NULLIF(BTRIM(r.address), ''), 'Unknown') as displayName,
                r.fee_type as feeType
            FROM restrooms r
            WHERE r.status = 'ACTIVE'
              AND r.is_hidden = false
            ORDER BY r.id
        """,
        countQuery = """
            SELECT COUNT(*)
            FROM restrooms r
            WHERE r.status = 'ACTIVE'
              AND r.is_hidden = false
        """,
        nativeQuery = true
    )
    fun findActiveNearestCacheRows(pageable: Pageable): Page<NearestRestroomCacheRow>
}
