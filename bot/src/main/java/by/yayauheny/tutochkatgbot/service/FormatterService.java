package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.shared.dto.BuildingResponseDto;
import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.shared.dto.RestroomResponseDto;
import by.yayauheny.shared.dto.SubwayStationResponseDto;
import by.yayauheny.shared.enums.AccessibilityType;
import by.yayauheny.shared.enums.FeeType;
import by.yayauheny.shared.enums.PlaceType;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.util.EmojiConstants;
import by.yayauheny.tutochkatgbot.util.DistanceFormat;
import by.yayauheny.tutochkatgbot.util.Links;
import by.yayauheny.tutochkatgbot.util.Text;
import by.yayauheny.tutochkatgbot.util.WorkTimeFormatter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FormatterService {

    public String toiletDetails(RestroomResponseDto toilet) {
        // Выбираем название с учетом здания
        String name = selectDisplayNameForDetails(toilet);
        
        // Выбираем адрес с учетом наследования от здания
        String address = selectAddress(toilet);
        
        // Выбираем расписание работы с учетом наследования
        String workTime = selectWorkTime(toilet);
        
        // Форматируем тип места
        String placeType =
            Optional.ofNullable(toilet.getPlaceType())
                .map(pt -> pt.getLocalizedName("ru"))
                .orElse("Не указано");
        
        // Иконка и текст оплаты
        String feeIcon = getFeeIcon(toilet.getFeeType());
        String feeText = toilet.getFeeType() == FeeType.FREE ? "Бесплатно" : "Платно";
        
        // Форматируем доступность
        String accessibility = formatAccessibility(toilet.getAccessibilityType());
        
        // Дополнительная информация
        String accessNote =
            Optional.ofNullable(toilet.getAccessNote())
                .filter(note -> !note.trim().isEmpty())
                .orElse(null);
        
        String directionGuide =
            Optional.ofNullable(toilet.getDirectionGuide())
                .filter(note -> !note.trim().isEmpty())
                .orElse(null);
        
        // Информация о метро
        String subwayInfo = formatSubwayInfoForDetails(toilet.getSubwayStation());
        
        // Информация о здании
        String buildingInfo = formatBuildingInfo(toilet.getBuilding());
        
        // Ссылка на карту
        String externalMap = Links.getDefaultMapsLink(toilet.getCoordinates().getLat(), toilet.getCoordinates().getLon());

        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("address", address);
        params.put("workTime", workTime);
        params.put("placeType", placeType);
        params.put("feeIcon", feeIcon);
        params.put("feeText", feeText);
        params.put("accessibility", accessibility);
        params.put("accessNote", accessNote != null ? accessNote : "—");
        params.put("directionGuide", directionGuide != null ? directionGuide : "—");
        params.put("subwayInfo", subwayInfo);
        params.put("buildingInfo", buildingInfo);
        params.put("mapsLink", externalMap);
        
        return Text.substitute(Messages.TOILET_DETAILS, params);
    }
    
    /**
     * Выбирает отображаемое название для детального вида.
     */
    private String selectDisplayNameForDetails(RestroomResponseDto toilet) {
        String name = Optional.ofNullable(toilet.getName())
            .map(String::trim)
            .orElse("");
        
        if (name.isBlank() || name.equalsIgnoreCase("Туалет")) {
            return Optional.ofNullable(toilet.getBuilding())
                .map(b -> b.displayName())
                .filter(str -> str != null && !str.isBlank())
                .orElse("Туалет");
        }
        
        return name;
    }
    
    /**
     * Выбирает адрес с учетом наследования от здания.
     */
    private String selectAddress(RestroomResponseDto toilet) {
        String address = Optional.ofNullable(toilet.getAddress())
            .map(String::trim)
            .orElse("");
        
        // Если адрес туалета пустой и есть здание, используем адрес здания
        if (address.isBlank() && toilet.getBuilding() != null) {
            address = Optional.ofNullable(toilet.getBuilding().getAddress())
                .map(String::trim)
                .orElse("");
        }
        
        return address.isBlank() ? "Адрес не указан" : address;
    }
    
    /**
     * Выбирает расписание работы с учетом наследования от здания.
     */
    @SuppressWarnings("unchecked")
    private String selectWorkTime(RestroomResponseDto toilet) {
        // Если включено наследование расписания и есть здание, используем расписание здания
        if (toilet.getInheritBuildingSchedule() && toilet.getBuilding() != null) {
            java.util.Map<String, Object> buildingWorkTime = extractWorkTimeMapViaReflection(toilet.getBuilding());
            if (buildingWorkTime != null && !buildingWorkTime.isEmpty()) {
                return WorkTimeFormatter.formatWorkTime(buildingWorkTime);
            }
        }
        
        // Иначе используем расписание туалета
        java.util.Map<String, Object> workTime = extractWorkTimeMapViaReflection(toilet);
        if (workTime != null && !workTime.isEmpty()) {
            return WorkTimeFormatter.formatWorkTime(workTime);
        }
        
        return "Время работы не указано";
    }
    
    /**
     * Извлекает Map из workTime через рефлексию, чтобы избежать зависимости от JsonObject.
     */
    @SuppressWarnings("unchecked")
    private java.util.Map<String, Object> extractWorkTimeMapViaReflection(Object dto) {
        try {
            // Вызываем getWorkTime() через рефлексию
            java.lang.reflect.Method getWorkTimeMethod = dto.getClass().getMethod("getWorkTime");
            Object workTimeObj = getWorkTimeMethod.invoke(dto);
            
            if (workTimeObj == null) {
                return null;
            }
            
            // Если уже Map (после десериализации kotlinx.serialization)
            if (workTimeObj instanceof java.util.Map) {
                return (java.util.Map<String, Object>) workTimeObj;
            }
            
            // Если это JsonObject из kotlinx.serialization, пытаемся получить содержимое
            // JsonObject имеет метод getContent() или content, который возвращает Map
            try {
                java.lang.reflect.Method contentMethod = workTimeObj.getClass().getMethod("getContent");
                Object content = contentMethod.invoke(workTimeObj);
                if (content instanceof java.util.Map) {
                    return (java.util.Map<String, Object>) content;
                }
            } catch (NoSuchMethodException e) {
                // Пробуем другой метод
                try {
                    java.lang.reflect.Method contentMethod = workTimeObj.getClass().getMethod("content");
                    Object content = contentMethod.invoke(workTimeObj);
                    if (content instanceof java.util.Map) {
                        return (java.util.Map<String, Object>) content;
                    }
                } catch (NoSuchMethodException e2) {
                    // Игнорируем
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки рефлексии
        }
        
        return null;
    }
    
    /**
     * Форматирует информацию о метро для детального вида.
     */
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
    
    /**
     * Форматирует информацию о здании для детального вида.
     */
    private String formatBuildingInfo(BuildingResponseDto building) {
        if (building == null) {
            return "—";
        }
        
        String buildingName = building.displayName();
        if (buildingName == null || buildingName.isBlank()) {
            return "—";
        }
        
        return buildingName;
    }

    public String toiletListItem(NearestRestroomResponseDto toilet) {
        // Выбираем название: если название общее ("Туалет"), используем название здания
        String name = selectDisplayName(toilet);
        
        // Форматируем расстояние
        String distance = DistanceFormat.meters(toilet.getDistanceMeters());
        
        // Иконка и текст оплаты
        String feeIcon = getFeeIcon(toilet.getFeeType());
        String feeText = toilet.getFeeType() == FeeType.FREE ? "Бесплатно" : "Платно";
        
        // Форматируем метро с цветным индикатором
        String subwayInfo = formatSubwayInfo(toilet.getSubwayStation());
        
        // Собираем детали в одну строку через разделитель •
        StringBuilder details = new StringBuilder();
        details.append(EmojiConstants.PIN).append(" ").append(distance);
        details.append(" • ").append(feeIcon).append(" ").append(feeText);
        
        if (!subwayInfo.isBlank()) {
            details.append(" • ").append(subwayInfo);
        }
        
        // Формируем итоговое сообщение: название жирным, детали обычным текстом
        return "<b>" + name + "</b>\n" + details.toString();
    }
    
    /**
     * Выбирает отображаемое название туалета.
     * Если название общее ("Туалет") или пустое, использует название здания.
     */
    private String selectDisplayName(NearestRestroomResponseDto toilet) {
        String name = Optional.ofNullable(toilet.getName())
            .map(String::trim)
            .orElse("");
        
        // Если название пустое или слишком общее, используем название здания
        if (name.isBlank() || name.equalsIgnoreCase("Туалет") || name.equalsIgnoreCase("Туалет")) {
            return Optional.ofNullable(toilet.getBuilding())
                .map(b -> b.displayName())
                .filter(str -> str != null && !str.isBlank())
                .orElse("Туалет");
        }
        
        return name;
    }
    
    /**
     * Форматирует информацию о метро с цветным индикатором.
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
    
    /**
     * Возвращает иконку для типа оплаты.
     */
    private String getFeeIcon(FeeType feeType) {
        return feeType == FeeType.FREE ? EmojiConstants.FREE : EmojiConstants.PAID;
    }

    public String toiletsFound(int count) {
        return Text.replace(Messages.TOILETS_FOUND, "count", String.valueOf(count));
    }

    public String generateMapsLink(RestroomResponseDto toilet) {
        return Links.getDefaultMapsLink(toilet.getCoordinates().getLat(), toilet.getCoordinates().getLon());
    }

    public String generateMapsLink(NearestRestroomResponseDto toilet) {
        return Links.getDefaultMapsLink(toilet.getCoordinates().getLat(), toilet.getCoordinates().getLon());
    }

    private String formatAccessibility(AccessibilityType type) {
        if (type == null) return "Не указано";
        return switch (type) {
            case DISABLED -> "♿ Для МГН";
            case NONE -> "Без доступности";
            default -> "Доступность: " + type.name();
        };
    }

    /**
     * Преобразование цвета линии метро (если появится в DTO) в эмодзи.
     * Пока используется как вспомогательный хелпер для будущих ответов с метро.
     */
    public String colorToEmoji(String hexColor) {
        return EmojiConstants.getEmojiForColor(hexColor);
    }
}