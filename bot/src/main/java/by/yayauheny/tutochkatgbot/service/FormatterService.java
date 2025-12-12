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
        if (toilet.inheritBuildingSchedule() && toilet.building() != null) {
            java.util.Map<String, Object> buildingWorkTime = extractWorkTimeMapViaReflection(toilet.building());
            if (buildingWorkTime != null && !buildingWorkTime.isEmpty()) {
                return WorkTimeFormatter.formatWorkTime(buildingWorkTime);
            }
        }
        
        java.util.Map<String, Object> workTime = extractWorkTimeMapViaReflection(toilet);
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