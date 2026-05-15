package yayauheny.by.importing.provider.yandex

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.provider.Parser
import yayauheny.by.model.import.NormalizedRestroomCandidate

class YandexMapsScrapedParser : Parser<YandexMapsScrapedPlace> {
    override fun parse(jsonObject: JsonObject): YandexMapsScrapedPlace {
        val title = jsonObject.stringOrNull("title")
        val shortTitle = jsonObject.stringOrNull("shortTitle")
        val address =
            when (val addr = jsonObject["address"]) {
                null, is JsonNull -> null
                is JsonPrimitive ->
                    addr.content
                        .takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
                else -> null
            }

        val locationObj =
            jsonObject["location"]?.jsonObject
                ?: throw InvalidImportPayload("Missing 'location' object for Yandex place")
        val lat =
            locationObj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: throw InvalidImportPayload("Missing or invalid 'location.lat'")
        val lng =
            locationObj["lng"]?.jsonPrimitive?.content?.toDoubleOrNull()
                ?: throw InvalidImportPayload("Missing or invalid 'location.lng'")

        val placeId =
            jsonObject.stringOrNull("placeId")
                ?: throw InvalidImportPayload("placeId is required for Yandex place")

        return YandexMapsScrapedPlace(
            title = title,
            shortTitle = shortTitle,
            address = address,
            location = YandexMapsScrapedLocation(lat = lat, lng = lng),
            statusText = jsonObject.stringOrNull("statusText"),
            isOpenNow = jsonObject.booleanOrNull("isOpenNow"),
            placeId = placeId,
            workingHours = jsonObject.workingHoursOrNull(),
            features = jsonObject.stringList("features"),
        )
    }

    override fun toCommonModel(
        providerDto: YandexMapsScrapedPlace,
        context: ImportAdapterContext
    ): NormalizedRestroomCandidate {
        val resolved = YandexNormalizationProfile.resolve(providerDto)
        return NormalizedRestroomCandidate(
            provider =
                yayauheny.by.model.enums.ImportProvider
                    .fromPayloadType(context.payloadType),
            providerObjectId = providerDto.placeId,
            cityId = context.cityId,
            name = resolved.name,
            address = resolved.address,
            lat = providerDto.location.lat,
            lng = providerDto.location.lng,
            placeType = resolved.placeType,
            locationType = resolved.locationType,
            feeType = resolved.feeType,
            accessibilityType = resolved.accessibilityType,
            status = resolved.status,
            amenities = resolved.amenities,
            rawSchedule = resolved.rawSchedule,
            buildingContext = resolved.buildingContext,
            genderType = null
        )
    }

    private fun JsonObject.stringOrNull(key: String): String? =
        (this[key] as? JsonPrimitive)
            ?.content
            ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }

    private fun JsonObject.booleanOrNull(key: String): Boolean? =
        (this[key] as? JsonPrimitive)
            ?.content
            ?.toBooleanStrictOrNull()

    private fun JsonObject.stringList(key: String): List<String> =
        (this[key] as? JsonArray)
            ?.mapNotNull { element ->
                (element as? JsonPrimitive)
                    ?.content
                    ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
            }.orEmpty()

    private fun JsonObject.workingHoursOrNull(): List<YandexMapsScrapedWorkingHour>? =
        (this["workingHours"] as? JsonArray)
            ?.mapNotNull { element -> element.toWorkingHourOrNull() }
            ?.takeIf { it.isNotEmpty() }

    private fun JsonElement.toWorkingHourOrNull(): YandexMapsScrapedWorkingHour? {
        val workingHourObject = this as? JsonObject ?: return null
        val day = workingHourObject.stringOrNull("day") ?: return null
        val from = workingHourObject.stringOrNull("from") ?: return null
        val to = workingHourObject.stringOrNull("to") ?: return null

        return YandexMapsScrapedWorkingHour(
            day = day,
            from = from,
            to = to,
            intervals = workingHourObject.workingIntervals("intervals")
        )
    }

    private fun JsonObject.workingIntervals(key: String): List<YandexMapsScrapedWorkingInterval> =
        (this[key] as? JsonArray)
            ?.mapNotNull { element ->
                val intervalObject = element as? JsonObject ?: return@mapNotNull null
                val from = intervalObject.stringOrNull("from") ?: return@mapNotNull null
                val to = intervalObject.stringOrNull("to") ?: return@mapNotNull null
                YandexMapsScrapedWorkingInterval(from = from, to = to)
            }.orEmpty()
}
