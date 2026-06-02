package yayauheny.by.analytics.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import yayauheny.by.analytics.entity.AnalyticsEventEntity
import java.util.UUID

@Repository
interface AnalyticsEventJpaRepository : JpaRepository<AnalyticsEventEntity, UUID>
