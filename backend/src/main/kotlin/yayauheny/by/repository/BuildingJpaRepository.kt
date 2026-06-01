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
            WHERE is_deleted = false
              AND external_ids ->> :provider = :externalId
            LIMIT 1
        """,
        nativeQuery = true
    )
    fun findByExternalId(
        @Param("provider") provider: String,
        @Param("externalId") externalId: String
    ): BuildingEntity?
}
