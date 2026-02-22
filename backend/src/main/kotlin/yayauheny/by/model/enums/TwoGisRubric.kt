package yayauheny.by.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class TwoGisRubric(
    val value: String,
    val placeType: PlaceType,
    val locationType: LocationType,
    val enName: String
) {
    AVTOVOKZALY("Автовокзалы", PlaceType.TRANSPORT, LocationType.INSIDE_BUILDING, "Bus stations"),
    ZHELEZNODOROZHNYE_VOKZALY("Железнодорожные вокзалы", PlaceType.TRANSPORT, LocationType.INSIDE_BUILDING, "Railway stations"),
    ZAPRAVOCHNYE_STANTSII("Заправочные станции", PlaceType.GAS_STATION, LocationType.INSIDE_BUILDING, "Gas stations"),

    BIZNES_TSENTRY("Бизнес-центры", PlaceType.OFFICE, LocationType.INSIDE_BUILDING, "Business centers"),
    KONFERENTS_ZALY("Конференц-залы", PlaceType.OFFICE, LocationType.INSIDE_BUILDING, "Conference halls"),
    KOPIROVALNYE_USLUGI("Копировальные услуги", PlaceType.OFFICE, LocationType.INSIDE_BUILDING, "Copying services"),

    GIPERMARKETY("Гипермаркеты", PlaceType.MARKET, LocationType.INSIDE_BUILDING, "Hypermarkets"),
    SUPERMARKETY("Супермаркеты", PlaceType.MARKET, LocationType.INSIDE_BUILDING, "Supermarkets"),
    RYNKI("Рынки", PlaceType.MARKET, LocationType.INSIDE_BUILDING, "Markets"),
    STROITELNYE_GIPERMARKETY("Строительные гипермаркеты", PlaceType.MARKET, LocationType.INSIDE_BUILDING, "Construction hypermarkets"),
    TORGOVYE_TSENTRY("Торговые центры", PlaceType.MALL, LocationType.INSIDE_BUILDING, "Shopping centers"),
    TORGOVO_RAZVLECHATELNYE(
        "Торгово-развлекательные центры",
        PlaceType.MALL,
        LocationType.INSIDE_BUILDING,
        "Shopping and entertainment centers"
    ),
    TS_TOVAROV_DLYA_INTERERA(
        "ТЦ товаров для интерьера и ремонта",
        PlaceType.MARKET,
        LocationType.INSIDE_BUILDING,
        "Interior and renovation mall"
    ),

    KAFE("Кафе", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Cafes"),
    KAFE_KONDITERSKIE("Кафе-кондитерские", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Confectionery cafes"),
    KOFEYNI("Кофейни", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Coffee shops"),
    RESTORANY("Рестораны", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Restaurants"),
    BARY("Бары", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Bars"),
    RYUMOCHNYE("Рюмочные", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Shot bars"),
    BANKETNYE_ZALY("Банкетные залы", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Banquet halls"),
    KEYTERING("Кейтеринг", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Catering"),
    KULINARII("Кулинарии", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Deli"),
    PEKARNI("Пекарни", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Bakeries"),
    PITSTSERII("Пиццерии", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Pizzerias"),
    SUSHI_BARY("Суши-бары", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Sushi bars"),
    FUDMOLLY("Фудмоллы", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Food courts"),
    PRODAZHA_KOFE("Продажа кофе", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Coffee sales"),
    PRODAZHA_CHAYA("Продажа чая", PlaceType.RESTAURANT, LocationType.INSIDE_BUILDING, "Tea sales"),

    BYSTROE_PITANIE("Быстрое питание", PlaceType.FAST_FOOD, LocationType.INSIDE_BUILDING, "Fast food"),
    STOLOVYE("Столовые", PlaceType.FAST_FOOD, LocationType.INSIDE_BUILDING, "Canteens"),

    KARAOKE_ZALY("Караоке-залы", PlaceType.CULTURE, LocationType.INSIDE_BUILDING, "Karaoke halls"),
    NOCHNYE_KLUBY("Ночные клубы", PlaceType.CULTURE, LocationType.INSIDE_BUILDING, "Nightclubs"),
    ARENDA_KONTSERTNYKH("Аренда концертных площадок", PlaceType.CULTURE, LocationType.INSIDE_BUILDING, "Concert venue rental"),

    GOSTINITSY("Гостиницы", PlaceType.OTHER, LocationType.INSIDE_BUILDING, "Hotels"),
    BANI_I_SAUNY("Бани и сауны", PlaceType.OTHER, LocationType.INSIDE_BUILDING, "Baths and saunas"),
    INTERESNYE_ZDANIYA("Интересные здания", PlaceType.OTHER, LocationType.INSIDE_BUILDING, "Notable buildings"),

    TUALETY("Туалеты", PlaceType.PUBLIC, LocationType.STANDALONE, "Toilets");

    companion object {
        fun fromValue(value: String?): TwoGisRubric? = value?.trim()?.let { v -> entries.find { it.value == v } }

        fun resolveLocationTypeFromRubrics(rubrics: List<String>): LocationType? =
            rubrics.firstNotNullOfOrNull { fromValue(it)?.locationType }
    }
}
