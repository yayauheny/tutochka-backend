package by.yayauheny.tutochkatgbot.dto.backend;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Place type enum
 */
public enum PlaceType {
    PUBLIC_TOILET("public_toilet", "Общественный туалет", "Public Toilet"),
    MALL("mall", "Торговый центр", "Shopping Mall"),
    GAS_STATION("gas_station", "Заправка", "Gas Station"),
    FAST_FOOD("fast_food", "Фастфуд", "Fast Food"),
    PARK("park", "Парк/Сквер", "Park"),
    RESTAURANT("restaurant", "Кафе/Ресторан", "Cafe/Restaurant"),
    MARKET("market", "Рынок", "Market"),
    CULTURE("culture", "Культура", "Cultural Place"),
    TRANSPORT("transport", "Вокзал/Аэропорт", "Transport Station"),
    MEDICAL("medical", "Медицина", "Medical Facility"),
    ADMINISTRATIVE("administrative", "Госучреждение", "Government Building"),
    EDUCATION("education", "Учеба", "Education"),
    OFFICE("office", "Офис/Бизнес-центр", "Office Center"),
    RESIDENTIAL("residential", "Жилой дом", "Residential Building"),
    OTHER("other", "Прочее", "Other");

    private final String code;
    private final String ruName;
    private final String enName;

    PlaceType(String code, String ruName, String enName) {
        this.code = code;
        this.ruName = ruName;
        this.enName = enName;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    public String getRuName() {
        return ruName;
    }

    public String getEnName() {
        return enName;
    }

    @JsonCreator
    public static PlaceType fromString(String value) {
        if (value == null) {
            return OTHER;
        }
        for (PlaceType type : values()) {
            if (type.code.equals(value)) {
                return type;
            }
        }
        return OTHER;
    }

    public String getLocalizedName(String locale) {
        if (locale != null && locale.equalsIgnoreCase("ru")) {
            return ruName;
        }
        return enName;
    }
}

