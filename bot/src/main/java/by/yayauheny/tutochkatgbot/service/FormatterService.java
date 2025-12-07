package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.shared.dto.RestroomResponseDto;
import by.yayauheny.shared.enums.AccessibilityType;
import by.yayauheny.shared.enums.FeeType;
import by.yayauheny.shared.enums.PlaceType;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.util.SubwayEmoji;
import by.yayauheny.tutochkatgbot.util.DistanceFormat;
import by.yayauheny.tutochkatgbot.util.Links;
import by.yayauheny.tutochkatgbot.util.Text;
import by.yayauheny.tutochkatgbot.util.WorkTimeFormatter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class FormatterService {

    public String toiletDetails(RestroomResponseDto toilet) {
        String name = Optional.ofNullable(toilet.getName()).orElse("Туалет");
        String address = Optional.ofNullable(toilet.getAddress()).orElse("Адрес не указан");

        String accessNote =
            Optional.ofNullable(toilet.getAccessNote())
                .filter(note -> !note.trim().isEmpty())
                .orElse("Дополнительная информация не указана");

        String directionGuide =
            Optional.ofNullable(toilet.getDirectionGuide())
                .filter(note -> !note.trim().isEmpty())
                .orElse("Маршрут не указан");

        String placeType =
            Optional.ofNullable(toilet.getPlaceType())
                .map(pt -> pt.getLocalizedName("ru"))
                .orElse("Не указано");

        String fee =
            toilet.getFeeType() == FeeType.FREE
                ? "Бесплатный"
                : "Платный";

        String accessibility = formatAccessibility(toilet.getAccessibilityType());

        String externalMap = Links.getDefaultMapsLink(toilet.getCoordinates().getLat(), toilet.getCoordinates().getLon());

        return Text.substitute(Messages.TOILET_DETAILS, Map.of(
            "name", name,
            "address", address,
            "description", accessNote,
            "direction", directionGuide,
            "placeType", placeType,
            "fee", fee,
            "accessibility", accessibility,
            "mapsLink", externalMap
        ));
    }

    public String toiletListItem(NearestRestroomResponseDto toilet) {
        String name = Optional.ofNullable(toilet.getName()).orElse("Туалет");
        String distance = DistanceFormat.meters(toilet.getDistanceMeters());
        String feeType =
            toilet.getFeeType() == FeeType.FREE
                ? "Бесплатный"
                : "Платный";

        String place =
            Optional.ofNullable(toilet.getPlaceType())
                .map(pt -> pt.getLocalizedName("ru"))
                .orElse("Тип не указан");

        String building =
            Optional.ofNullable(toilet.getBuilding())
                .map(b -> b.displayName())
                .filter(str -> str != null && !str.isBlank())
                .orElse(null);

        String subway =
            Optional.ofNullable(toilet.getSubwayStation())
                .map(st -> {
                    String emoji = Optional.ofNullable(st.lineColor())
                        .map(SubwayEmoji::getEmojiForColor)
                        .orElse("🚇");
                    String label = Optional.ofNullable(st.displayName()).orElse("");
                    return (emoji + " " + label).trim();
                })
                .orElse("");

        StringBuilder sb = new StringBuilder();
        sb.append("🐥 ").append(name);
        sb.append(" • ").append(feeType);
        sb.append(" • ").append(place);
        sb.append(" — ").append(distance);
        if (building != null && !building.isBlank()) {
            sb.append("\n🏢 ").append(building);
        }
        if (!subway.isBlank()) {
            sb.append("\n🚇 ").append(subway);
        }

        return sb.toString();
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
        return SubwayEmoji.getEmojiForColor(hexColor);
    }
}