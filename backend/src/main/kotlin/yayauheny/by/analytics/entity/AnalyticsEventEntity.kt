package yayauheny.by.analytics.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

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
