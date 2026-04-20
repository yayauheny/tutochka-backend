package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.dto.backend.AccessibilityType;
import by.yayauheny.tutochkatgbot.dto.backend.BuildingResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.FeeType;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.SubwayStationResponseDto;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.util.DistanceFormat;
import by.yayauheny.tutochkatgbot.util.EmojiConstants;
import by.yayauheny.tutochkatgbot.util.Text;
import by.yayauheny.tutochkatgbot.util.TextUtil;
import by.yayauheny.tutochkatgbot.util.WorkTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FormatterService {

    private String selectDisplayNameForDetails(RestroomResponseDto toilet) {
        String name = TextUtil.normalizeNullableText(toilet.name());
        if (name == null || name.isBlank() || "Туалет".equalsIgnoreCase(name)) {
            return Optional.ofNullable(toilet.building())
                .map(b -> b.displayName())
                .filter(str -> str != null && !str.isBlank())
                .orElse("Туалет");
        }
        return name;
    }
    
    private String selectAddress(RestroomResponseDto toilet) {
        String address = TextUtil.normalizeNullableText(toilet.address());
        if ((address == null || address.isBlank()) && toilet.building() != null) {
            address = TextUtil.normalizeNullableText(toilet.building().address());
        }
        return (address == null || address.isBlank()) ? null : address;
    }
    
    private String selectWorkTime(RestroomResponseDto toilet) {
        if (Boolean.TRUE.equals(toilet.inheritBuildingSchedule()) && toilet.building() != null) {
            java.util.Map<String, Object> buildingWorkTime = toilet.building().workTime();
            if (buildingWorkTime != null && !buildingWorkTime.isEmpty()) {
                return WorkTimeFormatter.formatWorkTime(buildingWorkTime);
            }
        }
        
        java.util.Map<String, Object> workTime = toilet.workTime();
        if (workTime != null && !workTime.isEmpty()) {
            return WorkTimeFormatter.formatWorkTime(workTime);
        }
        
        return "Время работы не указано";
    }

    public String toiletListItem(NearestRestroomSlimDto toilet) {
        String normalized = TextUtil.normalizeNullableText(toilet.displayName());
        String name = (normalized != null && !normalized.isBlank()) ? normalized : "Туалет";
        String distance = DistanceFormat.meters(toilet.distanceMeters());
        FeeType feeType = toilet.feeType();
        String feeIcon = getFeeIcon(feeType);
        String feeSegment = feeIcon.isEmpty() ? "" : "  " + feeIcon;
        return "🚶 " + distance + feeSegment + "  " + name;
    }

    private String formatSubwayInfo(SubwayStationResponseDto station) {
        if (station == null) {
            return "";
        }
        
        String emoji = Optional.ofNullable(station.lineColor())
            .map(EmojiConstants::getEmojiForColor)
            .orElse(EmojiConstants.METRO);
        
        String stationName = Optional.ofNullable(station.displayName("ru"))
            .orElse("");
        
        if (stationName.isBlank()) {
            return "";
        }
        
        return emoji + " " + stationName;
    }
    
    private String getFeeIcon(FeeType feeType) {
        if (feeType == null || feeType == FeeType.UNKNOWN) {
            return "";
        }
        if (feeType == FeeType.FREE) {
            return EmojiConstants.FREE;
        }
        return EmojiConstants.PAID;
    }

    public String toiletsFound(int count) {
        return Text.replace(Messages.TOILETS_FOUND, "count", String.valueOf(count));
    }

    public String colorToEmoji(String hexColor) {
        return EmojiConstants.getEmojiForColor(hexColor);
    }

    public String toiletDetail(RestroomResponseDto toilet, Double distanceMeters) {
        String name = Text.escapeHtml(selectDisplayNameForDetails(toilet));
        String address = selectAddress(toilet);
        String tags = formatTags(toilet, distanceMeters);
        String placeType = Optional.ofNullable(toilet.placeType())
            .map(pt -> pt.getLocalizedName("ru"))
            .orElse("Не указано");
        
        boolean hasAddress = address != null && !address.isBlank();
        String distancePart = distanceMeters != null ? "🚶 " + DistanceFormat.meters(distanceMeters) : "";
        String addressLine = hasAddress
            ? (distancePart.isEmpty() ? "📍 " + Text.escapeHtml(address) : distancePart + "   •   📍 " + Text.escapeHtml(address))
            : distancePart;
        
        String howToFindLine = formatHowToFindLine(toilet.directionGuide());
        String landmarkLine = formatLandmarkLine(toilet.building(), toilet.subwayStation());
        
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("addressLine", addressLine);
        params.put("tags", tags);
        params.put("placeType", Text.escapeHtml(placeType));
        params.put("howToFindLine", howToFindLine);
        params.put("landmarkLine", landmarkLine);
        
        return Text.substitute(Messages.TOILET_DETAILS_COMPACT, params).trim();
    }

    private String formatTags(RestroomResponseDto toilet, Double distanceMeters) {
        java.util.List<String> tags = new java.util.ArrayList<>();
        
        String statusTag = formatStatusTag(toilet);
        if (statusTag != null) {
            tags.add(statusTag);
        }
        
        String feeTag = formatFeeTag(toilet.feeType());
        if (feeTag != null) {
            tags.add(feeTag);
        }
        
        String accessibilityTag = formatAccessibilityTag(toilet.accessibilityType());
        if (accessibilityTag != null) {
            tags.add(accessibilityTag);
        }
        
        return tags.isEmpty() ? "—" : String.join(" • ", tags);
    }

    private String formatStatusTag(RestroomResponseDto toilet) {
        java.util.Map<String, Object> workTime = toilet.workTime();
        if (workTime != null) {
            Object is24x7 = workTime.get("is_24x7");
            if (is24x7 instanceof Boolean && (Boolean) is24x7) {
                return "Круглосуточно";
            }
        }
        
        if (Boolean.TRUE.equals(toilet.inheritBuildingSchedule()) && toilet.building() != null) {
            java.util.Map<String, Object> buildingWorkTime = toilet.building().workTime();
            if (buildingWorkTime != null) {
                Object is24x7 = buildingWorkTime.get("is_24x7");
                if (is24x7 instanceof Boolean && (Boolean) is24x7) {
                    return "Круглосуточно";
                }
            }
        }
        
        return null;
    }

    private String formatFeeTag(FeeType feeType) {
        if (feeType == null || feeType == FeeType.UNKNOWN) {
            return null;
        }
        return switch (feeType) {
            case FREE -> "Бесплатно";
            case PAID -> "Платно";
            case UNKNOWN -> null;
        };
    }

    private String formatAccessibilityTag(AccessibilityType accessibilityType) {
        if (accessibilityType == null || accessibilityType == AccessibilityType.UNKNOWN) {
            return null;
        }
        return switch (accessibilityType) {
            case WHEELCHAIR -> "МГН";
            case INACCESSIBLE -> "Ограниченная";
            case CHANGING_PLACES, UNKNOWN -> null;
        };
    }

    private String formatHowToFindLine(String directionGuide) {
        if (directionGuide == null || directionGuide.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = directionGuide.trim();
        if (trimmed.length() > 100) {
            return "";
        }
        
        return "<b>Как найти:</b> " + Text.escapeHtml(trimmed) + "\n";
    }

    private String formatLandmarkLine(BuildingResponseDto building, SubwayStationResponseDto subway) {
        StringBuilder result = new StringBuilder();
        
        if (building != null) {
            String buildingName = building.displayName();
            if (buildingName != null && !buildingName.isBlank()) {
                result.append(Text.escapeHtml(buildingName));
            }
        }
        
        if (subway != null) {
            String stationName = subway.displayName("ru");
            if (stationName != null && !stationName.isBlank()) {
                if (result.length() > 0) {
                    result.append(", метро ").append(Text.escapeHtml(stationName));
                } else {
                    result.append("Метро ").append(Text.escapeHtml(stationName));
                }
            }
        }
        
        if (result.length() == 0) {
            return "";
        }
        
        return "<b>Ориентир:</b> " + result + "\n";
    }

    private String formatBuildingLine(BuildingResponseDto building) {
        if (building == null) {
            return "";
        }
        
        String buildingName = building.displayName();
        if (buildingName == null || buildingName.isBlank()) {
            return "";
        }
        
        return "<b>Здание:</b> " + Text.escapeHtml(buildingName) + "\n";
    }

    private String formatSubwayLine(SubwayStationResponseDto subway) {
        if (subway == null) {
            return "";
        }
        
        String stationName = subway.displayName("ru");
        if (stationName == null || stationName.isBlank()) {
            return "";
        }
        
        String emoji = Optional.ofNullable(subway.lineColor())
            .map(EmojiConstants::getEmojiForColor)
            .orElse(EmojiConstants.METRO);
        
        return "<b>Метро:</b> " + emoji + " " + Text.escapeHtml(stationName) + "\n";
    }

    private String formatScheduleBlock(RestroomResponseDto toilet) {
        String workTime = selectWorkTime(toilet);
        if (workTime == null || workTime.isBlank() || workTime.equals("Время работы не указано")) {
            return "";
        }
        
        return "<b>Расписание:</b>\n" + workTime + "\n\n";
    }

    private String formatNoteBlock(String accessNote) {
        if (accessNote == null || accessNote.trim().isEmpty()) {
            return "";
        }
        
        return "<b>Заметка:</b>\n" + Text.escapeHtml(accessNote.trim()) + "\n\n";
    }

    private String formatRouteBlock(String directionGuide) {
        if (directionGuide == null || directionGuide.trim().isEmpty()) {
            return "";
        }
        
        return "<b>Маршрут:</b>\n" + Text.escapeHtml(directionGuide.trim()) + "\n";
    }
}
