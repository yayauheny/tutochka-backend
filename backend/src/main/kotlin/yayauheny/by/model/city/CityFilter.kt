package yayauheny.by.model.city

import java.util.UUID

data class CityFilter(
    var countryId: UUID? = null,
    var name: String? = null,
    var region: String? = null
)
