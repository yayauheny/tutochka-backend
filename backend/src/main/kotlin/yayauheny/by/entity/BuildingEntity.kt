package yayauheny.by.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Point
import yayauheny.by.model.enums.BuildingImportStatus

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

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "import_status", nullable = false, length = 20)
    var importStatus: BuildingImportStatus = BuildingImportStatus.COMPLETED

    @Column(name = "building_match_key", length = 64)
    var buildingMatchKey: String? = null
}
