package yayauheny.by.analytics.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

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
