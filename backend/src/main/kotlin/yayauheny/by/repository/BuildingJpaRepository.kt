package yayauheny.by.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import yayauheny.by.entity.BuildingEntity
import java.util.UUID

@Repository
interface BuildingJpaRepository : JpaRepository<BuildingEntity, UUID>
