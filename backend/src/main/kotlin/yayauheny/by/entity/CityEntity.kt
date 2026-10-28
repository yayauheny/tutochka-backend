package yayauheny.by.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.locationtech.jts.geom.Point

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

    @Column(name = "slug", nullable = false)
    var slug: String = ""

    @Column(name = "region")
    var region: String? = null

    @Column(name = "coordinates", nullable = false, columnDefinition = "geometry(Point,4326)")
    var coordinates: Point? = null
}
