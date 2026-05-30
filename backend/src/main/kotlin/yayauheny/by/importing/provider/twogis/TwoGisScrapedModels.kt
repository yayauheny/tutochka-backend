package yayauheny.by.importing.provider.twogis

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode

data class TwoGisScrapedPlace(
    val id: String,
    val title: String? = null,
    val category: String? = null,
    val address: String? = null,
    val street: String? = null,
    val houseNumber: String? = null,
    val location: TwoGisScrapedLocation,
    @JsonProperty("working_hours") val workingHours: JsonNode? = null,
    @JsonProperty("attributeGroups") val attributeGroups: List<String> = emptyList(),
    val rubrics: List<String> = emptyList()
)

data class TwoGisScrapedLocation(
    val lat: Double,
    val lng: Double
)
