package yayauheny.by.analytics.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yayauheny.by.analytics.entity.AnalyticsEventEntity
import java.time.Instant
import java.util.UUID

@Repository
interface AnalyticsEventJpaRepository : JpaRepository<AnalyticsEventEntity, UUID> {
    @Query("SELECT COUNT(u) FROM UserEntity u")
    fun countTotalUsers(): Long

    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.createdAt >= :since")
    fun countActiveUsersSince(since: Instant): Long

    @Query("SELECT COUNT(e) FROM AnalyticsEventEntity e WHERE e.event = 'SEARCH_COMPLETED'")
    fun countTotalSearches(): Long

    @Query("SELECT COUNT(e) FROM AnalyticsEventEntity e WHERE e.event = 'SEARCH_COMPLETED' AND e.createdAt >= :since")
    fun countSearchesSince(since: Instant): Long
}
