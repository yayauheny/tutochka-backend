package yayauheny.by.importing.provider.twogis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class TwoGisScrapedPlace(
    val id: String,
    val title: String? = null,
    val category: String? = null,
    val address: String? = null,
    val street: String? = null,
    val houseNumber: String? = null,
    val location: TwoGisScrapedLocation,
    @SerialName("working_hours") val workingHours: JsonObject? = null,
    @SerialName("attributeGroups") val attributeGroups: List<String> = emptyList(),
    val rubrics: List<String> = emptyList()
)

@Serializable
data class TwoGisScrapedLocation(
    val lat: Double,
    val lng: Double
)
