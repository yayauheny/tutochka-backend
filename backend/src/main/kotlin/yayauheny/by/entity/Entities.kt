package yayauheny.by.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon
import yayauheny.by.model.enums.*
import java.time.Instant
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null
}

@Entity
@Table(name = "countries")
class CountryEntity : BaseEntity() {
    @Column(nullable = false, unique = true, length = 10)
    var code: String = ""

    @Column(name = "name_ru", nullable = false)
    var nameRu: String = ""

    @Column(name = "name_en", nullable = false)
    var nameEn: String = ""
}

@Entity
@Table(name = "cities")
class CityEntity : BaseEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    var country: CountryEntity? = null

    @Column(name = "name_ru", nullable = false)
    var nameRu: String = ""

    @Column(name = "name_en", nullable = false)
    var nameEn: String = ""

    @Column(name = "region")
    var region: String? = null

    @Column(name = "city_bounds", columnDefinition = "geometry(Polygon,4326)")
    var cityBounds: Polygon? = null

    @Column(name = "coordinates", nullable = false, columnDefinition = "geometry(Point,4326)")
    var coordinates: Point? = null
}

@Entity
@Table(name = "subway_lines")
class SubwayLineEntity : BaseEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    var city: CityEntity? = null

    @Column(name = "name_ru", nullable = false, length = 100)
    var nameRu: String = ""

    @Column(name = "name_en", nullable = false, length = 100)
    var nameEn: String = ""

    @Column(name = "short_code", length = 20)
    var shortCode: String? = null

    @Column(name = "hex_color", nullable = false, length = 7)
    var hexColor: String = ""
}

@Entity
@Table(name = "subway_stations")
class SubwayStationEntity : BaseEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subway_line_id", nullable = false)
    var subwayLine: SubwayLineEntity? = null

    @Column(name = "name_ru", nullable = false)
    var nameRu: String = ""

    @Column(name = "name_en", nullable = false)
    var nameEn: String = ""

    @Column(name = "is_transfer", nullable = false)
    var isTransfer: Boolean = false

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_ids", columnDefinition = "jsonb")
    var externalIds: Map<String, String>? = null

    @Column(name = "coordinates", nullable = false, columnDefinition = "geometry(Point,4326)")
    var coordinates: Point? = null
}

@Entity
@Table(name = "buildings")
class BuildingEntity : BaseEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    var city: CityEntity? = null

    @Column(name = "name")
    var name: String? = null

    @Column(name = "address", nullable = false)
    var address: String = ""

    @Column(name = "building_type", length = 50)
    var buildingType: String? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "work_time", columnDefinition = "jsonb")
    var workTime: Map<String, Any>? = null

    @Column(name = "coordinates", nullable = false, columnDefinition = "geometry(Point,4326)")
    var coordinates: Point? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_ids", columnDefinition = "jsonb")
    var externalIds: Map<String, String>? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "import_status", nullable = false, length = 20)
    var importStatus: BuildingImportStatus = BuildingImportStatus.COMPLETED

    @Column(name = "building_match_key", length = 64)
    var buildingMatchKey: String? = null
}

@Entity
@Table(name = "restrooms")
class RestroomEntity : BaseEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    var city: CityEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    var building: BuildingEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subway_station_id")
    var subwayStation: SubwayStationEntity? = null

    @Column(name = "name")
    var name: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "place_type", nullable = false, length = 50)
    var placeType: PlaceType = PlaceType.OTHER

    @Column(name = "address")
    var address: String? = null

    @Column(name = "direction_guide", columnDefinition = "text")
    var directionGuide: String? = null

    @Column(name = "access_note", columnDefinition = "text")
    var accessNote: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 20)
    var feeType: FeeType = FeeType.UNKNOWN

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_type", nullable = false, length = 20)
    var genderType: GenderType = GenderType.UNKNOWN

    @Enumerated(EnumType.STRING)
    @Column(name = "accessibility_type", nullable = false, length = 20)
    var accessibilityType: AccessibilityType = AccessibilityType.UNKNOWN

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: RestroomStatus = RestroomStatus.ACTIVE

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "phones", columnDefinition = "jsonb")
    var phones: List<String>? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "work_time", columnDefinition = "jsonb")
    var workTime: Map<String, Any>? = null

    @Column(name = "inherit_building_schedule")
    var inheritBuildingSchedule: Boolean = false

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "amenities", columnDefinition = "jsonb")
    var amenities: Map<String, Boolean>? = null

    @Column(name = "has_photos")
    var hasPhotos: Boolean = false

    @Column(name = "coordinates", nullable = false, columnDefinition = "geometry(Point,4326)")
    var coordinates: Point? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_maps", columnDefinition = "jsonb")
    var externalMaps: Map<String, String>? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", nullable = false, length = 50)
    var dataSource: DataSourceType = DataSourceType.MANUAL

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 20)
    var locationType: LocationType = LocationType.UNKNOWN

    @Column(name = "is_hidden", nullable = false)
    var isHidden: Boolean = false

    @Column(name = "hidden_reason", columnDefinition = "text")
    var hiddenReason: String? = null

    @Column(name = "hidden_at")
    var hiddenAt: Instant? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_provider", nullable = false, length = 50)
    var originProvider: ImportProvider = ImportProvider.MANUAL

    @Column(name = "origin_id", length = 100)
    var originId: String? = null

    @Column(name = "restroom_match_key", length = 64)
    var restroomMatchKey: String? = null
}

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
