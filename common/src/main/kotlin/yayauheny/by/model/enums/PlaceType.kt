package yayauheny.by.model.enums

import com.fasterxml.jackson.annotation.JsonProperty

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import kotlin.jvm.JvmStatic




enum class PlaceType(
    @get:JsonValue val code: String,
    val ruName: String,
    val enName: String
) {
    @JsonProperty("public_toilet")
    PUBLIC("public_toilet", "Общественный туалет", "Public Toilet"),

    @JsonProperty("mall")
    MALL("mall", "Торговый центр", "Shopping Mall"),

    @JsonProperty("gas_station")
    GAS_STATION("gas_station", "Заправка", "Gas Station"),

    @JsonProperty("fast_food")
    FAST_FOOD("fast_food", "Фастфуд", "Fast Food"),

    @JsonProperty("park")
    PARK("park", "Парк/Сквер", "Park"),

    @JsonProperty("restaurant")
    RESTAURANT("restaurant", "Кафе/Ресторан", "Cafe/Restaurant"),

    @JsonProperty("market")
    MARKET("market", "Рынок", "Market"),

    @JsonProperty("culture")
    CULTURE("culture", "Культура", "Cultural Place"),

    @JsonProperty("transport")
    TRANSPORT("transport", "Вокзал/Аэропорт", "Transport Station"),

    @JsonProperty("medical")
    MEDICAL("medical", "Медицина", "Medical Facility"),

    @JsonProperty("administrative")
    ADMINISTRATIVE("administrative", "Госучреждение", "Government Building"),

    @JsonProperty("education")
    EDUCATION("education", "Учеба", "Education"),

    @JsonProperty("office")
    OFFICE("office", "Офис/Бизнес-центр", "Office Center"),

    @JsonProperty("residential")
    RESIDENTIAL("residential", "Жилой дом", "Residential Building"),

    @JsonProperty("other")
    OTHER("other", "Прочее", "Other");

    companion object {
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun fromCode(value: String?): PlaceType = entries.find { it.code == value } ?: OTHER
    }

    fun getLocalizedName(locale: String): String = if (locale.equals("ru", ignoreCase = true)) ruName else enName
}
