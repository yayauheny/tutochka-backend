package yayauheny.by.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table

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
