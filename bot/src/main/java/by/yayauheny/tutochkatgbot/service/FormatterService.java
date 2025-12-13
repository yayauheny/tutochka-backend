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
        String feeIcon = getFeeIcon(feeType);
        String feeText = feeText(feeType);
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
        params.put("address", address);
        params.put("workTime", workTime);
        params.put("placeType", placeType);
        params.put("feeIcon", feeIcon);
        params.put("feeText", feeText);
        params.put("paymentMethods", paymentMethods);
        params.put("accessibility", accessibility);
        params.put("accessNote", accessNote != null ? accessNote : "—");
        params.put("directionGuide", directionGuide != null ? directionGuide : "—");
        params.put("subwayInfo", subwayInfo);
        params.put("buildingInfo", buildingInfo != null ? buildingInfo : "—");
        params.put("mapsLink", externalMap);
        
        String result = Text.substitute(Messages.TOILET_DETAILS, params);
        
        if (buildingInfo == null) {
            result = result.replaceAll("(?m)^.*Здание:.*$", "");
        }
        
        return result.trim();
    }
    
    private String selectDisplayNameForDetails(RestroomResponseDto toilet) {
        String name = Optional.ofNullable(toilet.name())
            .map(String::trim)
            .orElse("");
        
        if (name.isBlank() || name.equalsIgnoreCase("Туалет")) {
            return Optional.ofNullable(toilet.building())
                .map(b -> b.displayName())
                .filter(str -> str != null && !str.isBlank())
                .orElse("Туалет");
        }
        
        return name;
    }
    
    private String selectAddress(RestroomResponseDto toilet) {
        String address = Optional.ofNullable(toilet.address())
            .map(String::trim)
            .orElse("");
        
        if (address.isBlank() && toilet.building() != null) {
            address = Optional.ofNullable(toilet.building().address())
                .map(String::trim)
                .orElse("");
        }
        
        return address.isBlank() ? "Адрес не указан" : address;
    }
    
    private String selectWorkTime(RestroomResponseDto toilet) {
        // If inheritBuildingSchedule is true and building exists, use building's workTime
        if (Boolean.TRUE.equals(toilet.inheritBuildingSchedule()) && toilet.building() != null) {
            java.util.Map<String, Object> buildingWorkTime = toilet.building().workTime();
            if (buildingWorkTime != null && !buildingWorkTime.isEmpty()) {
                return WorkTimeFormatter.formatWorkTime(buildingWorkTime);
            }
        }
        
        // Otherwise, use restroom's own workTime
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
                    // Ignore
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors
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

    /**
     * Format toilet list item for inline button.
     * Returns plain text without HTML tags (mobile-friendly).
     */
    public String toiletListItem(NearestRestroomSlimDto toilet) {
        String name = toilet.displayName();
        String distance = DistanceFormat.meters(toilet.distanceMeters());
        FeeType feeType = toilet.feeType();
        String feeIcon = getFeeIcon(feeType);
        String feeText = feeText(feeType);
        String subwayInfo = formatSubwayInfoSlim(toilet.subwayStation());
        
        StringBuilder details = new StringBuilder();
        details.append(EmojiConstants.PIN).append(" ").append(distance);
        details.append(" • ").append(feeIcon).append(" ").append(feeText);
        
        if (!subwayInfo.isBlank()) {
            details.append(" • ").append(subwayInfo);
        }
        
        // Plain text without HTML - mobile-friendly
        return name + "\n" + details.toString();
    }
    
    /**
     * Format subway info from slim DTO for list display.
     */
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
    
    /**
     * Format subway info from full DTO for details display.
     */
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
        if (feeType == null || feeType == FeeType.FREE) {
            return EmojiConstants.FREE;
        }
        return EmojiConstants.PAID;
    }
    
    private String feeText(FeeType feeType) {
        if (feeType == null || feeType == FeeType.FREE) {
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
        if (type == null) return "Не указано";
        return switch (type) {
            case DISABLED -> "Для МГН";
            case NONE -> "Не указано";
            default -> "Доступность: " + type.name();
        };
    }

    public String colorToEmoji(String hexColor) {
        return EmojiConstants.getEmojiForColor(hexColor);
    }

    /**
     * Format compact toilet details card (default view)
     */
    public String toiletDetailsCompact(RestroomResponseDto toilet, Double distanceMeters) {
        String name = selectDisplayNameForDetails(toilet);
        String address = selectAddress(toilet);
        String tags = formatTags(toilet, distanceMeters);
        String placeType = Optional.ofNullable(toilet.placeType())
            .map(pt -> pt.getLocalizedName("ru"))
            .orElse("Не указано");
        
        String distancePart = distanceMeters != null 
            ? "🚶 " + DistanceFormat.meters(distanceMeters) + "   •   " 
            : "";
        
        String howToFindLine = formatHowToFindLine(toilet.directionGuide());
        String landmarkLine = formatLandmarkLine(toilet.building(), toilet.subwayStation());
        
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("distancePart", distancePart);
        params.put("address", address);
        params.put("tags", tags);
        params.put("placeType", placeType);
        params.put("howToFindLine", howToFindLine);
        params.put("landmarkLine", landmarkLine);
        
        return Text.substitute(Messages.TOILET_DETAILS_COMPACT, params).trim();
    }

    /**
     * Format full toilet details card (expanded view)
     */
    public String toiletDetailsFull(RestroomResponseDto toilet, Double distanceMeters) {
        String name = selectDisplayNameForDetails(toilet);
        String address = selectAddress(toilet);
        String tags = formatTags(toilet, distanceMeters);
        String placeType = Optional.ofNullable(toilet.placeType())
            .map(pt -> pt.getLocalizedName("ru"))
            .orElse("Не указано");
        
        String distancePart = distanceMeters != null 
            ? "🚶 " + DistanceFormat.meters(distanceMeters) + "   •   " 
            : "";
        
        String buildingLine = formatBuildingLine(toilet.building());
        String subwayLine = formatSubwayLine(toilet.subwayStation());
        String scheduleBlock = formatScheduleBlock(toilet);
        String noteBlock = formatNoteBlock(toilet.accessNote());
        String routeBlock = formatRouteBlock(toilet.directionGuide());
        
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("distancePart", distancePart);
        params.put("address", address);
        params.put("tags", tags);
        params.put("placeType", placeType);
        params.put("buildingLine", buildingLine);
        params.put("subwayLine", subwayLine);
        params.put("scheduleBlock", scheduleBlock);
        params.put("noteBlock", noteBlock);
        params.put("routeBlock", routeBlock);
        
        return Text.substitute(Messages.TOILET_DETAILS_FULL, params).trim();
    }

    /**
     * Format tags: Открыто/Закрыто/Круглосуточно • Платно/Бесплатно • МГН
     */
    private String formatTags(RestroomResponseDto toilet, Double distanceMeters) {
        java.util.List<String> tags = new java.util.ArrayList<>();
        
        // Status tag (Открыто/Закрыто/Круглосуточно)
        String statusTag = formatStatusTag(toilet);
        if (statusTag != null) {
            tags.add(statusTag);
        }
        
        // Fee tag (Платно/Бесплатно)
        String feeTag = formatFeeTag(toilet.feeType());
        if (feeTag != null) {
            tags.add(feeTag);
        }
        
        // Accessibility tag (МГН)
        String accessibilityTag = formatAccessibilityTag(toilet.accessibilityType());
        if (accessibilityTag != null) {
            tags.add(accessibilityTag);
        }
        
        return tags.isEmpty() ? "—" : String.join(" • ", tags);
    }

    /**
     * Format status tag: Открыто/Закрыто/Круглосуточно
     */
    private String formatStatusTag(RestroomResponseDto toilet) {
        // Check if 24/7
        java.util.Map<String, Object> workTime = toilet.workTime();
        if (workTime != null) {
            Object is24x7 = workTime.get("is_24x7");
            if (is24x7 instanceof Boolean && (Boolean) is24x7) {
                return "Круглосуточно";
            }
        }
        
        // Check building workTime if inheritBuildingSchedule
        if (Boolean.TRUE.equals(toilet.inheritBuildingSchedule()) && toilet.building() != null) {
            java.util.Map<String, Object> buildingWorkTime = toilet.building().workTime();
            if (buildingWorkTime != null) {
                Object is24x7 = buildingWorkTime.get("is_24x7");
                if (is24x7 instanceof Boolean && (Boolean) is24x7) {
                    return "Круглосуточно";
                }
            }
        }
        
        // TODO: Check isOpen from backend if available
        // For now, return null if we can't determine
        return null;
    }

    /**
     * Format fee tag: Платно/Бесплатно
     */
    private String formatFeeTag(FeeType feeType) {
        if (feeType == null) {
            return null;
        }
        return switch (feeType) {
            case FREE -> "Бесплатно";
            case PAID -> "Платно";
        };
    }

    /**
     * Format accessibility tag: МГН
     */
    private String formatAccessibilityTag(AccessibilityType accessibilityType) {
        if (accessibilityType == null) {
            return null;
        }
        return switch (accessibilityType) {
            case DISABLED -> "МГН";
            case NONE, MEN, WOMEN, UNISEX, FAMILY -> null;
        };
    }

    /**
     * Format "How to find" line (only if directionGuide is short)
     */
    private String formatHowToFindLine(String directionGuide) {
        if (directionGuide == null || directionGuide.trim().isEmpty()) {
            return "";
        }
        
        String trimmed = directionGuide.trim();
        // Only show if short (≤ 100 chars)
        if (trimmed.length() > 100) {
            return "";
        }
        
        return "<b>Как найти:</b> " + trimmed + "\n";
    }

    /**
     * Format landmark line: building + subway in one line
     */
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

    /**
     * Format building line for full card
     */
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

    /**
     * Format subway line for full card
     */
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

    /**
     * Format schedule block for full card
     */
    private String formatScheduleBlock(RestroomResponseDto toilet) {
        String workTime = selectWorkTime(toilet);
        if (workTime == null || workTime.isBlank() || workTime.equals("Время работы не указано")) {
            return "";
        }
        
        return "<b>Расписание:</b>\n" + workTime + "\n\n";
    }

    /**
     * Format note block for full card
     */
    private String formatNoteBlock(String accessNote) {
        if (accessNote == null || accessNote.trim().isEmpty()) {
            return "";
        }
        
        return "<b>Заметка:</b>\n" + accessNote.trim() + "\n\n";
    }

    /**
     * Format route block for full card
     */
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
        
        // Map payment method keys to RU-friendly strings
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