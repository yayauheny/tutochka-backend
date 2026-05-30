package yayauheny.by.importing.provider.yandex

import com.fasterxml.jackson.databind.JsonNode
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.provider.Parser
import yayauheny.by.model.imports.NormalizedRestroomCandidate

class YandexMapsScrapedParser : Parser<YandexMapsScrapedPlace> {
    override fun parse(jsonObject: JsonNode): YandexMapsScrapedPlace {
        val title = jsonObject.stringOrNull("title")
        val shortTitle = jsonObject.stringOrNull("shortTitle")
        val address = jsonObject.stringOrNull("address")

        val locationObj =
            jsonObject.get("location")?.takeIf { it.isObject }
                ?: throw InvalidImportPayload("Missing 'location' object for Yandex place")
        val lat =
            locationObj.get("lat")?.takeIf { it.isNumber || it.isTextual }?.asDouble()
                ?: throw InvalidImportPayload("Missing or invalid 'location.lat'")
        val lng =
            locationObj.get("lng")?.takeIf { it.isNumber || it.isTextual }?.asDouble()
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

    private fun JsonNode.stringOrNull(key: String): String? =
        this.get(key)
            ?.takeIf { it.isTextual }
            ?.asText()
            ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }

    private fun JsonNode.booleanOrNull(key: String): Boolean? =
        this.get(key)?.takeIf { it.isBoolean }?.asBoolean()
            ?: this.get(key)?.takeIf { it.isTextual }?.asText()?.toBooleanStrictOrNull()

    private fun JsonNode.stringList(key: String): List<String> =
        this.get(key)
            ?.takeIf { it.isArray }
            ?.mapNotNull { element ->
                element.takeIf { it.isTextual }
                    ?.asText()
                    ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }
            }.orEmpty()

    private fun JsonNode.workingHoursOrNull(): List<YandexMapsScrapedWorkingHour>? =
        this.get("workingHours")
            ?.takeIf { it.isArray }
            ?.mapNotNull { element -> element.toWorkingHourOrNull() }
            ?.takeIf { it.isNotEmpty() }

    private fun JsonNode.toWorkingHourOrNull(): YandexMapsScrapedWorkingHour? {
        if (!this.isObject) return null
        val day = this.stringOrNull("day") ?: return null
        val from = this.stringOrNull("from") ?: return null
        val to = this.stringOrNull("to") ?: return null

        return YandexMapsScrapedWorkingHour(
            day = day,
            from = from,
            to = to,
            intervals = this.workingIntervals("intervals")
        )
    }

    private fun JsonNode.workingIntervals(key: String): List<YandexMapsScrapedWorkingInterval> =
        this.get(key)
            ?.takeIf { it.isArray }
            ?.mapNotNull { element ->
                if (!element.isObject) return@mapNotNull null
                val from = element.stringOrNull("from") ?: return@mapNotNull null
                val to = element.stringOrNull("to") ?: return@mapNotNull null
                YandexMapsScrapedWorkingInterval(from = from, to = to)
            }.orEmpty()
}
