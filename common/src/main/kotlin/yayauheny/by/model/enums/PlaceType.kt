package yayauheny.by.model.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import kotlin.jvm.JvmStatic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class PlaceType(
    @get:JsonValue val code: String,
    val ruName: String,
    val enName: String
) {
    @SerialName("public_toilet")
    PUBLIC("public_toilet", "Общественный туалет", "Public Toilet"),

    @SerialName("mall")
    MALL("mall", "Торговый центр", "Shopping Mall"),

    @SerialName("gas_station")
    GAS_STATION("gas_station", "Заправка", "Gas Station"),

    @SerialName("fast_food")
    FAST_FOOD("fast_food", "Фастфуд", "Fast Food"),

    @SerialName("park")
    PARK("park", "Парк/Сквер", "Park"),

    @SerialName("restaurant")
    RESTAURANT("restaurant", "Кафе/Ресторан", "Cafe/Restaurant"),

    @SerialName("market")
    MARKET("market", "Рынок", "Market"),

    @SerialName("culture")
    CULTURE("culture", "Культура", "Cultural Place"),

    @SerialName("transport")
    TRANSPORT("transport", "Вокзал/Аэропорт", "Transport Station"),

    @SerialName("medical")
    MEDICAL("medical", "Медицина", "Medical Facility"),

    @SerialName("administrative")
    ADMINISTRATIVE("administrative", "Госучреждение", "Government Building"),

    @SerialName("education")
    EDUCATION("education", "Учеба", "Education"),

    @SerialName("office")
    OFFICE("office", "Офис/Бизнес-центр", "Office Center"),

    @SerialName("residential")
    RESIDENTIAL("residential", "Жилой дом", "Residential Building"),

    @SerialName("other")
    OTHER("other", "Прочее", "Other");

    companion object {
        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun fromCode(value: String?): PlaceType = entries.find { it.code == value } ?: OTHER
    }

    fun getLocalizedName(locale: String): String = if (locale.equals("ru", ignoreCase = true)) ruName else enName
}
