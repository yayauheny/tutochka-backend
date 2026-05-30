package yayauheny.by.model.country

import jakarta.validation.constraints.Size

data class CountryFilter(
    @field:Size(max = 2)
    var code: String? = null,
    @field:Size(max = 100)
    var nameRu: String? = null,
    @field:Size(max = 100)
    var nameEn: String? = null
)
