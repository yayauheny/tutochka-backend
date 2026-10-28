package yayauheny.by.entity

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Point
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.util.StringListToTextConverter

@Entity
@Table(name = "restrooms")
class RestroomEntity : BaseEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    var city: CityEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    var building: BuildingEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subway_station_id")
    var subwayStation: SubwayStationEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    var createdByUser: yayauheny.by.analytics.entity.UserEntity? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id")
    var updatedByUser: yayauheny.by.analytics.entity.UserEntity? = null

    @Column(name = "name")
    var name: String? = null

    @Convert(converter = PlaceTypeJpaConverter::class)
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

    @Convert(converter = AccessibilityTypeJpaConverter::class)
    @Column(name = "accessibility_type", nullable = false, length = 20)
    var accessibilityType: AccessibilityType = AccessibilityType.UNKNOWN

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    var status: RestroomStatus = RestroomStatus.ACTIVE

    @Column(name = "moderation_status", nullable = false, length = 20)
    var moderationStatus: String = "PENDING"

    @Convert(converter = StringListToTextConverter::class)
    @Column(name = "phones", columnDefinition = "text")
    var phones: List<String>? = null

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "work_time", columnDefinition = "jsonb")
    var workTime: Map<String, Any>? = null

    @Column(name = "inherit_building_schedule")
    var inheritBuildingSchedule: Boolean = false

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "amenities", columnDefinition = "jsonb")
    var amenities: Map<String, Any>? = null

    @Column(name = "has_photos")
    var hasPhotos: Boolean = false

    @Column(name = "coordinates", nullable = false, columnDefinition = "geometry(Point,4326)")
    var coordinates: Point? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "data_source", nullable = false, length = 50)
    var dataSource: DataSourceType = DataSourceType.MANUAL

    @Enumerated(EnumType.STRING)
    @Column(name = "location_type", nullable = false, length = 20)
    var locationType: LocationType = LocationType.UNKNOWN

    @Column(name = "is_hidden", nullable = false)
    var isHidden: Boolean = false

    @Enumerated(EnumType.STRING)
    @Column(name = "origin_provider", nullable = false, length = 50)
    var originProvider: ImportProvider = ImportProvider.MANUAL

    @Column(name = "origin_id", length = 100)
    var originId: String? = null

    @Column(name = "two_gis_id")
    var twoGisId: String? = null

    @Column(name = "yandex_id")
    var yandexId: String? = null

    @Column(name = "google_id")
    var googleId: String? = null

    @Column(name = "apple_id")
    var appleId: String? = null

    @Column(name = "restroom_match_key", length = 64)
    var restroomMatchKey: String? = null
}
