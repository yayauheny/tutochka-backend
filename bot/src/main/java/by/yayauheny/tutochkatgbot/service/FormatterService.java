package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.dto.backend.AccessibilityType;
import by.yayauheny.tutochkatgbot.dto.backend.BuildingResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.FeeType;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.SubwayStationResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.SubwayStationSlimDto;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.util.DistanceFormat;
import by.yayauheny.tutochkatgbot.util.EmojiConstants;
import by.yayauheny.tutochkatgbot.util.Links;
import by.yayauheny.tutochkatgbot.util.Text;
import by.yayauheny.tutochkatgbot.util.TextUtil;
import by.yayauheny.tutochkatgbot.util.WorkTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FormatterService {

    public String toiletDetails(RestroomResponseDto toilet) {
        String name = selectDisplayNameForDetails(toilet);
        String address = selectAddress(toilet);
        String workTime = selectWorkTime(toilet);
        
        String placeType =
            Optional.ofNullable(toilet.placeType())
                .map(pt -> pt.getLocalizedName("ru"))
                .orElse("Не указано");
        
        FeeType feeType = toilet.feeType();
        String feeIcon = feeType != null ? getFeeIcon(feeType) : "";
        String feeText = feeType != null ? feeText(feeType) : "";
        String accessibility = formatAccessibility(toilet.accessibilityType());
        
        String accessNote =
            Optional.ofNullable(toilet.accessNote())
                .filter(note -> !note.trim().isEmpty())
                .orElse(null);
        
        String directionGuide =
            Optional.ofNullable(toilet.directionGuide())
                .filter(note -> !note.trim().isEmpty())
                .orElse(null);
        
        String subwayInfo = formatSubwayInfoForDetails(toilet.subwayStation());
        String buildingInfo = formatBuildingInfo(toilet.building());
        String paymentMethods = formatPaymentMethods(toilet.amenities());
        String externalMap = Links.getDefaultMapsLink(toilet.coordinates().lat(), toilet.coordinates().lon());

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("address", address != null ? address : "");
        params.put("workTime", workTime);
        params.put("placeType", placeType);
        params.put("feeIcon", feeIcon);
        params.put("feeText", feeText);
        params.put("paymentMethods", paymentMethods);
        params.put("accessibility", accessibility != null ? accessibility : "—");
        params.put("accessNote", accessNote != null ? accessNote : "—");
        params.put("directionGuide", directionGuide != null ? directionGuide : "—");
        params.put("subwayInfo", subwayInfo);
        params.put("buildingInfo", buildingInfo != null ? buildingInfo : "—");
        params.put("mapsLink", externalMap);
        
        String result = Text.substitute(Messages.TOILET_DETAILS, params);
        
        if (address == null || address.isBlank()) {
            result = result.replaceAll("(?m)^.*📍.*$\\n?", "");
        }
        if (buildingInfo == null) {
            result = result.replaceAll("(?m)^.*Здание:.*$", "");
        }
        if (accessibility == null) {
            result = result.replaceAll("(?m)^.*Доступность:.*$\\n?", "");
        }
        if (feeType == null || feeType == FeeType.UNKNOWN) {
            result = result.replaceAll("(?m)^.*Оплата:.*$\\n?", "");
        }
        
        return result.trim();
    }
    
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
    
    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> extractWorkTimeMapViaReflection(Object dto) {
        try {
            java.lang.reflect.Method workTimeMethod = dto.getClass().getMethod("workTime");
            Object workTimeObj = workTimeMethod.invoke(dto);
            
            if (workTimeObj == null) {
                return null;
            }
            
            if (workTimeObj instanceof java.util.Map) {
                return (java.util.Map<String, Object>) workTimeObj;
            }
            
            try {
                java.lang.reflect.Method contentMethod = workTimeObj.getClass().getMethod("getContent");
                Object content = contentMethod.invoke(workTimeObj);
                if (content instanceof java.util.Map) {
                    return (java.util.Map<String, Object>) content;
                }
            } catch (NoSuchMethodException e) {
                try {
                    java.lang.reflect.Method contentMethod = workTimeObj.getClass().getMethod("content");
                    Object content = contentMethod.invoke(workTimeObj);
                    if (content instanceof java.util.Map) {
                        return (java.util.Map<String, Object>) content;
                    }
                } catch (NoSuchMethodException e2) {
                }
            }
        } catch (Exception e) {
        }
        
        return null;
    }
    
    private String formatSubwayInfoForDetails(SubwayStationResponseDto station) {
        if (station == null) {
            return "—";
        }
        
        String emoji = Optional.ofNullable(station.lineColor())
            .map(EmojiConstants::getEmojiForColor)
            .orElse(EmojiConstants.METRO);
        
        String stationName = Optional.ofNullable(station.displayName("ru"))
            .orElse("");
        
        if (stationName.isBlank()) {
            return "—";
        }
        
        return emoji + " " + stationName;
    }
    
    private String formatBuildingInfo(BuildingResponseDto building) {
        if (building == null) {
            return null;
        }

        String buildingName = building.displayName();
        if (buildingName == null || buildingName.isBlank()) {
            return null;
        }

        return buildingName;
    }

    public String toiletListItem(NearestRestroomSlimDto toilet) {
        String normalized = TextUtil.normalizeNullableText(toilet.displayName());
        String name = (normalized != null && !normalized.isBlank()) ? normalized : "Туалет";
        String distance = DistanceFormat.meters(toilet.distanceMeters());
        FeeType feeType = toilet.feeType();
        String feeIcon = getFeeIcon(feeType);
        String subwayInfo = formatSubwayInfoSlim(toilet.subwayStation());
        
        StringBuilder header = new StringBuilder();
        header.append(EmojiConstants.PIN).append(" ").append(distance);
        header.append(" • ").append(feeIcon);
        if (!subwayInfo.isBlank()) {
            header.append(" • ").append(subwayInfo);
        }
        return header.toString() + "\n" + name;
    }
    
    private String formatSubwayInfoSlim(SubwayStationSlimDto station) {
        if (station == null) {
            return "";
        }
        
        String emoji = Optional.ofNullable(station.lineColorHex())
            .map(EmojiConstants::getEmojiForColor)
            .orElse(EmojiConstants.METRO);
        
        String stationName = Optional.ofNullable(station.displayName())
            .filter(s -> !s.isBlank())
            .orElse("");
        
        if (stationName.isBlank()) {
            return "";
        }
        
        return emoji + " " + stationName;
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
    
    private String feeText(FeeType feeType) {
        if (feeType == null || feeType == FeeType.UNKNOWN) {
            return "";
        }
        if (feeType == FeeType.FREE) {
            return "Бесплатно";
        }
        return "Платно";
    }

    public String toiletsFound(int count) {
        return Text.replace(Messages.TOILETS_FOUND, "count", String.valueOf(count));
    }

    public String generateMapsLink(RestroomResponseDto toilet) {
        return Links.getDefaultMapsLink(toilet.coordinates().lat(), toilet.coordinates().lon());
    }

    private String formatAccessibility(AccessibilityType type) {
        if (type == null || type == AccessibilityType.UNKNOWN) return null;
        return switch (type) {
            case WHEELCHAIR -> "Для МГН";
            case INACCESSIBLE -> "Ограниченная доступность";
            case CHANGING_PLACES -> "Площадка для переодевания";
            case UNKNOWN -> null;
        };
    }

    public String colorToEmoji(String hexColor) {
        return EmojiConstants.getEmojiForColor(hexColor);
    }

    public String toiletDetailsCompact(RestroomResponseDto toilet, Double distanceMeters) {
        String name = selectDisplayNameForDetails(toilet);
        String address = selectAddress(toilet);
        String tags = formatTags(toilet, distanceMeters);
        String placeType = Optional.ofNullable(toilet.placeType())
            .map(pt -> pt.getLocalizedName("ru"))
            .orElse("Не указано");
        
        boolean hasAddress = address != null && !address.isBlank();
        String distancePart = distanceMeters != null ? "🚶 " + DistanceFormat.meters(distanceMeters) : "";
        String addressLine = hasAddress
            ? (distancePart.isEmpty() ? "📍 " + address : distancePart + "   •   📍 " + address)
            : distancePart;
        
        String howToFindLine = formatHowToFindLine(toilet.directionGuide());
        String landmarkLine = formatLandmarkLine(toilet.building(), toilet.subwayStation());
        
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("addressLine", addressLine);
        params.put("tags", tags);
        params.put("placeType", placeType);
        params.put("howToFindLine", howToFindLine);
        params.put("landmarkLine", landmarkLine);
        
        return Text.substitute(Messages.TOILET_DETAILS_COMPACT, params).trim();
    }

    public String toiletDetailsFull(RestroomResponseDto toilet, Double distanceMeters) {
        String name = selectDisplayNameForDetails(toilet);
        String address = selectAddress(toilet);
        String tags = formatTags(toilet, distanceMeters);
        String placeType = Optional.ofNullable(toilet.placeType())
            .map(pt -> pt.getLocalizedName("ru"))
            .orElse("Не указано");
        
        boolean hasAddress = address != null && !address.isBlank();
        String distancePart = distanceMeters != null ? "🚶 " + DistanceFormat.meters(distanceMeters) : "";
        String addressLine = hasAddress
            ? (distancePart.isEmpty() ? "📍 " + address : distancePart + "   •   📍 " + address)
            : distancePart;
        
        String buildingLine = formatBuildingLine(toilet.building());
        String subwayLine = formatSubwayLine(toilet.subwayStation());
        String scheduleBlock = formatScheduleBlock(toilet);
        String noteBlock = formatNoteBlock(toilet.accessNote());
        String routeBlock = formatRouteBlock(toilet.directionGuide());
        
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("addressLine", addressLine);
        params.put("tags", tags);
        params.put("placeType", placeType);
        params.put("buildingLine", buildingLine);
        params.put("subwayLine", subwayLine);
        params.put("scheduleBlock", scheduleBlock);
        params.put("noteBlock", noteBlock);
        params.put("routeBlock", routeBlock);
        
        return Text.substitute(Messages.TOILET_DETAILS_FULL, params).trim();
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
        
        return "<b>Как найти:</b> " + trimmed + "\n";
    }

    private String formatLandmarkLine(BuildingResponseDto building, SubwayStationResponseDto subway) {
        StringBuilder result = new StringBuilder();
        
        if (building != null) {
            String buildingName = building.displayName();
            if (buildingName != null && !buildingName.isBlank()) {
                result.append(buildingName);
            }
        }
        
        if (subway != null) {
            String stationName = subway.displayName("ru");
            if (stationName != null && !stationName.isBlank()) {
                if (result.length() > 0) {
                    result.append(", метро ").append(stationName);
                } else {
                    result.append("Метро ").append(stationName);
                }
            }
        }
        
        if (result.length() == 0) {
            return "";
        }
        
        return "<b>Ориентир:</b> " + result.toString() + "\n";
    }

    private String formatBuildingLine(BuildingResponseDto building) {
        if (building == null) {
            return "";
        }
        
        String buildingName = building.displayName();
        if (buildingName == null || buildingName.isBlank()) {
            return "";
        }
        
        return "<b>Здание:</b> " + buildingName + "\n";
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
        
        return "<b>Метро:</b> " + emoji + " " + stationName + "\n";
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
        
        return "<b>Заметка:</b>\n" + accessNote.trim() + "\n\n";
    }

    private String formatRouteBlock(String directionGuide) {
        if (directionGuide == null || directionGuide.trim().isEmpty()) {
            return "";
        }
        
        return "<b>Маршрут:</b>\n" + directionGuide.trim() + "\n";
    }

    /**
     * Format payment methods from amenities.
     * Returns formatted string like " (Картой, Наличными)" or empty string if no payment methods.
     */
    private String formatPaymentMethods(Map<String, Object> amenities) {
        if (amenities == null) {
            return "";
        }
        
        Object paymentMethodsObj = amenities.get("payment_methods");
        if (!(paymentMethodsObj instanceof java.util.List)) {
            return "";
        }
        
        @SuppressWarnings("unchecked")
        java.util.List<String> paymentMethods = (java.util.List<String>) paymentMethodsObj;
        if (paymentMethods == null || paymentMethods.isEmpty()) {
            return "";
        }
        
        java.util.List<String> localized = paymentMethods.stream()
            .map(method -> {
                return switch (method != null ? method.toLowerCase() : "") {
                    case "card" -> "Картой";
                    case "cash" -> "Наличными";
                    case "mobile" -> "Мобильным";
                    default -> method != null ? method : "";
                };
            })
            .filter(s -> !s.isBlank())
            .toList();
        
        if (localized.isEmpty()) {
            return "";
        }
        
        return " (" + String.join(", ", localized) + ")";
    }
}