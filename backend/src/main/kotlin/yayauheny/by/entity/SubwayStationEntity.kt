package yayauheny.by.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.locationtech.jts.geom.Point

@Entity
@Table(name = "subway_stations")
class SubwayStationEntity : BaseEntity() {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subway_line_id", nullable = false)
    var subwayLine: SubwayLineEntity? = null

    @Column(name = "line_order", nullable = false)
    var lineOrder: Int = 0

    @Column(name = "slug", nullable = false)
    var slug: String = ""

    @Column(name = "name_ru", nullable = false)
    var nameRu: String = ""

    @Column(name = "name_en", nullable = false)
    var nameEn: String = ""

    @Column(name = "is_transfer", nullable = false)
    var isTransfer: Boolean = false

    @Column(name = "coordinates", nullable = false, columnDefinition = "geometry(Point,4326)")
    var coordinates: Point? = null
}
