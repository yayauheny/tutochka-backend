package yayauheny.by.model.restroom

import jakarta.validation.constraints.Size
import java.util.UUID
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus

data class RestroomFilter(
    var cityId: UUID? = null,
    var buildingId: UUID? = null,
    var subwayStationId: UUID? = null,
    var status: RestroomStatus? = null,
    var feeType: FeeType? = null,
    var genderType: GenderType? = null,
    var accessibilityType: AccessibilityType? = null,
    var placeType: PlaceType? = null,
    var locationType: LocationType? = null,
    var originProvider: ImportProvider? = null,
    var isHidden: Boolean? = null,
    @field:Size(max = 100)
    var name: String? = null,
    @field:Size(max = 255)
    var address: String? = null
)
