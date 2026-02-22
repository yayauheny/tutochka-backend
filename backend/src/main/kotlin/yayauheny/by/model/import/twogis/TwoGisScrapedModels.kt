package yayauheny.by.model.import.twogis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Модели для парсинга scraped JSON формата 2ГИС.
 * Отличается от API формата: attributeGroups - это List<String>, location вместо point, title вместо name.
 */

@Serializable
data class TwoGisScrapedPlace(
    val id: String,
    val title: String,
    val category: String? = null,
    val address: String,
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
