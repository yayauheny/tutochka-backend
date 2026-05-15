package yayauheny.by.importing.provider

import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.import.BuildingContext

internal interface ProviderNormalizationProfile<T> {
    fun resolve(providerDto: T): ProviderResolvedFields
}

internal data class ProviderResolvedFields(
    val name: String?,
    val address: String?,
    val locationType: LocationType,
    val placeType: PlaceType,
    val feeType: FeeType,
    val accessibilityType: AccessibilityType,
    val status: RestroomStatus,
    val amenities: JsonObject,
    val rawSchedule: JsonObject?,
    val buildingContext: BuildingContext?,
    val genderType: GenderType?
)
