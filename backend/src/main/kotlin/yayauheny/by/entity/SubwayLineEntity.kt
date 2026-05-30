package yayauheny.by.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

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
