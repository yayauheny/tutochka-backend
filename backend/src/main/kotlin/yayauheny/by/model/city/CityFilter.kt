package yayauheny.by.model.city

import jakarta.validation.constraints.Size
import java.util.UUID

data class CityFilter(
    var countryId: UUID? = null,
    @field:Size(max = 100)
    var name: String? = null,
    @field:Size(max = 100)
    var region: String? = null
)
