package yayauheny.by.analytics.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import yayauheny.by.analytics.entity.UserAnalyticsEntity
import java.time.Instant
import java.util.UUID

@Repository
interface UserAnalyticsJpaRepository : JpaRepository<UserAnalyticsEntity, UUID> {
    @Modifying
    @Query(
        value = """
            INSERT INTO user_analytics (user_id)
            VALUES (:userId)
            ON CONFLICT (user_id) DO NOTHING
        """,
        nativeQuery = true
    )
    fun ensureUserAnalyticsRow(userId: UUID)

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
