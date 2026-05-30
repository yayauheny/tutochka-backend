package yayauheny.by.analytics.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "tg_user_id", nullable = false, unique = true)
    var tgUserId: String = ""

    @Column(name = "tg_chat_id", nullable = false)
    var tgChatId: String = ""

    @Column(name = "source", nullable = false)
    var source: String = "telegram_bot"

    @Column(name = "username")
    var username: String? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null

    @Column(name = "is_blocked", nullable = false)
    var isBlocked: Boolean = false

    @Column(name = "blocked_at")
    var blockedAt: Instant? = null
}

@Entity
@Table(name = "user_analytics")
class UserAnalyticsEntity {
    @Id
    @Column(name = "user_id", nullable = false)
    var userId: UUID? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    var user: UserEntity? = null

    @Column(name = "searches_count", nullable = false)
    var searchesCount: Int = 0

    @Column(name = "successful_searches_count", nullable = false)
    var successfulSearchesCount: Int = 0

    @Column(name = "empty_searches_count", nullable = false)
    var emptySearchesCount: Int = 0

    @Column(name = "details_opened_count", nullable = false)
    var detailsOpenedCount: Int = 0

    @Column(name = "routes_clicked_count", nullable = false)
    var routesClickedCount: Int = 0

    @Column(name = "last_event")
    var lastEvent: String? = null

    @Column(name = "last_event_at")
    var lastEventAt: Instant? = null
}

@Entity
@Table(name = "analytics_events")
class AnalyticsEventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "event", nullable = false)
    var event: String = ""

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: UserEntity? = null

    @Column(name = "source", nullable = false)
    var source: String = "telegram_bot"

    @Column(name = "lat", precision = 5, scale = 2)
    var lat: BigDecimal? = null

    @Column(name = "lon", precision = 5, scale = 2)
    var lon: BigDecimal? = null

    @Column(name = "results_count")
    var resultsCount: Int? = null

    @Column(name = "duration_ms")
    var durationMs: Int? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", nullable = false, columnDefinition = "jsonb")
    var metadata: Map<String, Any> = mutableMapOf()
}
