package yayauheny.by.entity

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
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "restroom_duplicate_suspicions")
class DuplicateSuspicionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "existing_restroom_id", nullable = false)
    var existingRestroom: RestroomEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_restroom_id", nullable = false)
    var candidateRestroom: RestroomEntity? = null

    @Column(name = "distance_m", nullable = false)
    var distanceM: Double = 0.0

    @Column(name = "reason", nullable = false, length = 100)
    var reason: String = ""

    @Column(name = "status", nullable = false, length = 20)
    var status: String = "PENDING"

    @Column(name = "provider", nullable = false, length = 50)
    var provider: String = ""

    @Column(name = "external_id", length = 128)
    var externalId: String? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null
}
