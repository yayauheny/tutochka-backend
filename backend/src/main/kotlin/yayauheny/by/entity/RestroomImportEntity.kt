package yayauheny.by.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
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
import java.time.Instant
import java.util.UUID
import yayauheny.by.model.enums.ImportJobStatus
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider

@Entity
@Table(name = "restroom_imports")
class RestroomImportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    var provider: ImportProvider? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "payload_type", nullable = false, length = 50)
    var payloadType: ImportPayloadType? = null

    @Column(name = "entity_type", nullable = false, length = 30)
    var entityType: String = "place"

    @Column(name = "external_id", length = 128)
    var externalId: String? = null

    @Column(name = "source_url", columnDefinition = "text")
    var sourceUrl: String? = null

    @Column(name = "scraped_at")
    var scrapedAt: Instant? = null

    @Column(name = "payload_hash", length = 64)
    var payloadHash: String? = null

    @Column(name = "attempts", nullable = false)
    var attempts: Int = 0

    @Column(name = "last_attempt_at")
    var lastAttemptAt: Instant? = null

    @Column(name = "next_retry_at")
    var nextRetryAt: Instant? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    var city: CityEntity? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", nullable = false, columnDefinition = "jsonb")
    var rawPayload: Map<String, Any>? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    var building: BuildingEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restroom_id")
    var restroom: RestroomEntity? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: ImportJobStatus = ImportJobStatus.PENDING

    @Column(name = "error_message", columnDefinition = "text")
    var errorMessage: String? = null

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @Column(name = "processed_at")
    var processedAt: Instant? = null
}
