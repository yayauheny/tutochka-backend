package yayauheny.by.bot.service

import org.springframework.stereotype.Service
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.util.DistanceFormat
import yayauheny.by.bot.util.EmojiConstants
import yayauheny.by.bot.util.Text
import yayauheny.by.bot.util.TextUtil
import yayauheny.by.bot.util.WorkTimeFormatter
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.subway.SubwayStationResponseDto
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

@Service
class FormatterService {

    private fun selectDisplayNameForDetails(toilet: RestroomResponseDto): String {
        val name = TextUtil.normalizeNullableText(toilet.name)
        if (name.isNullOrBlank() || "Туалет".equals(name, ignoreCase = true)) {
            return toilet.building?.displayName()?.takeIf { it.isNotBlank() } ?: "Туалет"
        }
        return name
    }

    private fun selectAddress(toilet: RestroomResponseDto): String? {
        var address = TextUtil.normalizeNullableText(toilet.address)
        if (address.isNullOrBlank() && toilet.building != null) {
            address = TextUtil.normalizeNullableText(toilet.building!!.address)
        }
        return if (address.isNullOrBlank()) null else address
    }

    private fun selectWorkTime(toilet: RestroomResponseDto): String {
        if (toilet.inheritBuildingSchedule == true && toilet.building != null) {
            val buildingWorkTime = toilet.building!!.workTime
            if (buildingWorkTime != null && buildingWorkTime.isNotEmpty()) {
                return WorkTimeFormatter.formatWorkTime(buildingWorkTime)
            }
        }

        val workTime = toilet.workTime
        if (workTime != null && workTime.isNotEmpty()) {
            return WorkTimeFormatter.formatWorkTime(workTime)
        }

        return "Время работы не указано"
    }

    fun toiletListItem(toilet: NearestRestroomSlimDto): String {
        val normalized = TextUtil.normalizeNullableText(toilet.displayName)
        val name = if (!normalized.isNullOrBlank()) normalized else "Туалет"
        val distance = DistanceFormat.meters(toilet.distanceMeters)
        val feeType = toilet.feeType
        val feeIcon = getFeeIcon(feeType)
        val feeSegment = if (feeIcon.isEmpty()) "" else "  $feeIcon"
        return "🚶 $distance$feeSegment  $name"
    }

    private fun formatSubwayInfo(station: SubwayStationResponseDto?): String {
        if (station == null) return ""

        val emoji = EmojiConstants.getEmojiForColor(station.lineColor)
        val stationName = station.displayName("ru")

        if (stationName.isNullOrBlank()) return ""

        return "$emoji $stationName"
    }

    private fun getFeeIcon(feeType: FeeType?): String {
        if (feeType == null || feeType == FeeType.UNKNOWN) {
            return ""
        }
        return if (feeType == FeeType.FREE) {
            EmojiConstants.FREE
        } else {
            EmojiConstants.PAID
        }
    }

    fun toiletsFound(count: Int): String {
        return Text.replace(Messages.TOILETS_FOUND, "count", count.toString())
    }

    fun colorToEmoji(hexColor: String?): String {
        return EmojiConstants.getEmojiForColor(hexColor)
    }

    fun toiletDetail(toilet: RestroomResponseDto, distanceMeters: Double?): String {
        val name = Text.escapeHtml(selectDisplayNameForDetails(toilet)) ?: "Туалет"
        val address = selectAddress(toilet)
        val tags = formatTags(toilet)
        val placeType = toilet.placeType?.getLocalizedName("ru") ?: "Не указано"

        val hasAddress = !address.isNullOrBlank()
        val distancePart = if (distanceMeters != null) "🚶 " + DistanceFormat.meters(distanceMeters) else ""
        val addressLine = if (hasAddress) {
            if (distancePart.isEmpty()) "📍 ${Text.escapeHtml(address)}"
            else "$distancePart   •   📍 ${Text.escapeHtml(address)}"
        } else {
            distancePart
        }

        val howToFindLine = formatHowToFindLine(toilet.directionGuide)
        val landmarkLine = formatLandmarkLine(toilet.building, toilet.subwayStation)

        val params = mapOf(
            "name" to name,
            "addressLine" to addressLine,
            "tags" to tags,
            "placeType" to Text.escapeHtml(placeType)!!,
            "howToFindLine" to howToFindLine,
            "landmarkLine" to landmarkLine
        )

        return Text.substitute(Messages.TOILET_DETAILS_COMPACT, params).trim()
    }

    private fun formatTags(toilet: RestroomResponseDto): String {
        val tags = mutableListOf<String>()

        val statusTag = formatStatusTag(toilet)
        if (statusTag != null) tags.add(statusTag)

        val feeTag = formatFeeTag(toilet.feeType)
        if (feeTag != null) tags.add(feeTag)

        val accessibilityTag = formatAccessibilityTag(toilet.accessibilityType)
        if (accessibilityTag != null) tags.add(accessibilityTag)

        return if (tags.isEmpty()) "—" else tags.joinToString(" • ")
    }

    private fun formatStatusTag(toilet: RestroomResponseDto): String? {
        val workTime = toilet.workTime
        if (is24x7(workTime)) return "Круглосуточно"

        if (toilet.inheritBuildingSchedule == true && toilet.building != null) {
            val buildingWorkTime = toilet.building!!.workTime
            if (is24x7(buildingWorkTime)) return "Круглосуточно"
        }

        return null
    }

    private fun formatFeeTag(feeType: FeeType?): String? {
        return when (feeType) {
            FeeType.FREE -> "Бесплатно"
            FeeType.PAID -> "Платно"
            else -> null
        }
    }

    private fun formatAccessibilityTag(accessibilityType: AccessibilityType?): String? {
        return when (accessibilityType) {
            AccessibilityType.WHEELCHAIR -> "МГН"
            AccessibilityType.INACCESSIBLE -> "Ограниченная"
            else -> null
        }
    }

    private fun formatHowToFindLine(directionGuide: String?): String {
        if (directionGuide.isNullOrBlank()) return ""

        val trimmed = directionGuide.trim()
        if (trimmed.length > 100) return ""

        return "<b>Как найти:</b> ${Text.escapeHtml(trimmed)}\n"
    }

    private fun formatLandmarkLine(building: BuildingResponseDto?, subway: SubwayStationResponseDto?): String {
        val result = StringBuilder()

        if (building != null) {
            val buildingName = building.displayName()
            if (!buildingName.isNullOrBlank()) {
                result.append(Text.escapeHtml(buildingName))
            }
        }

        if (subway != null) {
            val stationName = subway.displayName("ru")
            if (!stationName.isNullOrBlank()) {
                if (result.isNotEmpty()) {
                    result.append(", метро ").append(Text.escapeHtml(stationName))
                } else {
                    result.append("Метро ").append(Text.escapeHtml(stationName))
                }
            }
        }

        if (result.isEmpty()) return ""

        return "<b>Ориентир:</b> $result\n"
    }

    private fun is24x7(workTime: JsonObject?): Boolean {
        if (workTime == null) return false
        val value = workTime["is_24x7"]
        if (value !is JsonPrimitive) return false
        return value.booleanOrNull == true
    }
}
