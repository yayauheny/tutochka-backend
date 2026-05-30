package yayauheny.by.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import yayauheny.by.entity.SubwayLineEntity
import java.util.UUID

@Repository
interface SubwayLineJpaRepository : JpaRepository<SubwayLineEntity, UUID> {
    fun findByCityId(cityId: UUID): List<SubwayLineEntity>
}
