package yayauheny.by.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.Polygon

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
