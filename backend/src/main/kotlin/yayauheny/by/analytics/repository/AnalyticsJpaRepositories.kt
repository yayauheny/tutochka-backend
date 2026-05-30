package yayauheny.by.analytics.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yayauheny.by.analytics.entity.AnalyticsEventEntity
import yayauheny.by.analytics.entity.UserAnalyticsEntity
import yayauheny.by.analytics.entity.UserEntity
import java.time.Instant
import java.util.UUID

@Repository
interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    fun findByTgUserId(tgUserId: String): UserEntity?
}

@Repository
interface UserAnalyticsJpaRepository : JpaRepository<UserAnalyticsEntity, UUID> {

    @Modifying
    @Query(
        """
        UPDATE UserAnalyticsEntity ua 
        SET ua.searchesCount = ua.searchesCount + :searchesInc,
            ua.successfulSearchesCount = ua.successfulSearchesCount + :successfulSearchesInc,
            ua.emptySearchesCount = ua.emptySearchesCount + :emptySearchesInc,
            ua.detailsOpenedCount = ua.detailsOpenedCount + :detailsOpenedInc,
            ua.routesClickedCount = ua.routesClickedCount + :routesClickedInc,
            ua.lastEvent = :lastEvent,
            ua.lastEventAt = :lastEventAt
        WHERE ua.userId = :userId
    """
    )
    fun incrementMetrics(
        userId: UUID,
        searchesInc: Int,
        successfulSearchesInc: Int,
        emptySearchesInc: Int,
        detailsOpenedInc: Int,
        routesClickedInc: Int,
        lastEvent: String?,
        lastEventAt: Instant?
    )
}

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
