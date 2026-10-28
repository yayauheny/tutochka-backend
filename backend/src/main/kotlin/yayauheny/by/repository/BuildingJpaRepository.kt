package yayauheny.by.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import yayauheny.by.entity.BuildingEntity
import java.util.UUID

@Repository
interface BuildingJpaRepository : JpaRepository<BuildingEntity, UUID> {
    fun findByBuildingMatchKey(buildingMatchKey: String): BuildingEntity?

    @Query(
        value = """
            SELECT *
            FROM buildings
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
    fun findByExternalId(
        @Param("provider") provider: String,
        @Param("externalId") externalId: String
    ): BuildingEntity?
}
