package yayauheny.by.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class TwoGisCategory(
    val value: String,
    val placeType: PlaceType,
    val locationType: LocationType,
    val ruName: String,
    val enName: String
) {
    RAILWAY_STATION(
        "railway_station",
        PlaceType.TRANSPORT,
        LocationType.INSIDE_BUILDING,
        "Вокзал",
        "Railway Station"
    ),
    MALL(
        "mall",
        PlaceType.MALL,
        LocationType.INSIDE_BUILDING,
        "Торговый центр",
        "Shopping Mall"
    ),
    KARAOKE(
        "karaoke",
        PlaceType.CULTURE,
        LocationType.INSIDE_BUILDING,
        "Караоке",
        "Karaoke"
    ),
    CONSTRUCTION_HYPERMARKET(
        "construction_hypermarket",
        PlaceType.MARKET,
        LocationType.INSIDE_BUILDING,
        "Строймаркет",
        "Construction Hypermarket"
    ),
    MART(
        "mart",
        PlaceType.MARKET,
        LocationType.INSIDE_BUILDING,
        "Магазин",
        "Mart"
    ),
    BUSINESS_CENTER(
        "business_center",
        PlaceType.OFFICE,
        LocationType.INSIDE_BUILDING,
        "Бизнес-центр",
        "Business Center"
    ),
    COFFEE_SHOP(
        "coffee_shop",
        PlaceType.RESTAURANT,
        LocationType.INSIDE_BUILDING,
        "Кофейня",
        "Coffee Shop"
    ),
    BAR(
        "bar",
        PlaceType.RESTAURANT,
        LocationType.INSIDE_BUILDING,
        "Бар",
        "Bar"
    ),
    BUS_STATION(
        "bus_station",
        PlaceType.TRANSPORT,
        LocationType.INSIDE_BUILDING,
        "Автовокзал",
        "Bus Station"
    ),
    FOOD_RESTAURANT(
        "food_restaurant",
        PlaceType.RESTAURANT,
        LocationType.INSIDE_BUILDING,
        "Ресторан",
        "Restaurant"
    ),
    TOILET(
        "toilet",
        PlaceType.PUBLIC,
        LocationType.STANDALONE,
        "Туалет",
        "Toilet"
    ),
    FOOD_SERVICE(
        "food_service",
        PlaceType.FAST_FOOD,
        LocationType.INSIDE_BUILDING,
        "Фастфуд",
        "Fast Food"
    ),
    HOTEL(
        "hotel",
        PlaceType.OTHER,
        LocationType.INSIDE_BUILDING,
        "Отель",
        "Hotel"
    ),
    GAS_STATION(
        "gas_station",
        PlaceType.GAS_STATION,
        LocationType.INSIDE_BUILDING,
        "АЗС",
        "Gas Station"
    ),
    MARKET(
        "market",
        PlaceType.MARKET,
        LocationType.INSIDE_BUILDING,
        "Рынок",
        "Market"
    );

    companion object {
        fun fromValue(value: String?): TwoGisCategory? = value?.trim()?.lowercase()?.let { v -> entries.find { it.value == v } }
    }
}
