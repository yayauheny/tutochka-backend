package yayauheny.by.model.import.twogis

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Модели для парсинга ответа 2ГИС API.
 */

@Serializable
data class TwoGisResponse(
    val result: TwoGisResult
)

@Serializable
data class TwoGisResult(
    val items: List<TwoGisPlace>,
    val total: Int? = null
)

@Serializable
data class TwoGisPlace(
    val id: String,
    val name: String,
    val point: TwoGisPoint,
    @SerialName("full_address_name") val fullAddressName: String? = null,
    @SerialName("address_name") val addressName: String,
    @SerialName("address_comment") val addressComment: String? = null,
    val address: TwoGisAddress,
    @SerialName("attribute_groups") val attributeGroups: List<TwoGisAttributeGroup> = emptyList(),
    val flags: TwoGisFlags? = null,
    val schedule: JsonObject? = null,
    val type: String? = null
)

@Serializable
data class TwoGisPoint(
    val lat: Double,
    val lon: Double
)

@Serializable
data class TwoGisAddress(
    @SerialName("building_id") val buildingId: String? = null,
    val postcode: String? = null
)

@Serializable
data class TwoGisAttributeGroup(
    val name: String,
    val attributes: List<TwoGisAttribute> = emptyList()
)

@Serializable
data class TwoGisAttribute(
    val id: String,
    val name: String,
    val tag: String
)

@Serializable
data class TwoGisFlags(
    val photos: Boolean? = null
)
