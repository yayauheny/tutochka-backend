package yayauheny.by.model.enums

import kotlinx.serialization.Serializable

@Serializable
enum class TwoGisAttributeKind {
    PAYMENT,
    TOILET_FEE,
    TOILET_ACCESSIBLE,
    ACCESSIBLE_ENTRANCE,
    ACCESS_LIMITED
}

@Serializable
enum class TwoGisAttributeGroup(
    val value: String,
    val kind: TwoGisAttributeKind,
    val paymentCode: String? = null
) {
    OPLATA_KARTOY("Оплата картой", TwoGisAttributeKind.PAYMENT, "card"),
    NALICHNYY_RASCHYOT("Наличный расчёт", TwoGisAttributeKind.PAYMENT, "cash"),
    OPLATA_PO_QR("Оплата по QR-коду", TwoGisAttributeKind.PAYMENT, "qr"),
    OPLATA_CHEREZ_BANK("Оплата через банк", TwoGisAttributeKind.PAYMENT, "bank"),
    OPLATA_CHEREZ_APPLICATION("Оплата через приложение", TwoGisAttributeKind.PAYMENT, "app"),
    PEREVOD_S_KARTY("Перевод с карты", TwoGisAttributeKind.PAYMENT, "card_transfer"),

    BESPLATNYY_TUALET("Бесплатный туалет", TwoGisAttributeKind.TOILET_FEE, null),
    PLATNYY_TUALET("Платный туалет", TwoGisAttributeKind.TOILET_FEE, null),

    TUALET("Туалет", TwoGisAttributeKind.TOILET_ACCESSIBLE, null),
    TUALET_DLYA_MALOMOBILNYKH("Туалет для маломобильных людей", TwoGisAttributeKind.TOILET_ACCESSIBLE, null),

    DOSTUPNYY_VKHOD("Доступный вход для людей с инвалидностью", TwoGisAttributeKind.ACCESSIBLE_ENTRANCE, null),
    PANDUS("Пандус", TwoGisAttributeKind.ACCESSIBLE_ENTRANCE, null),
    PODYOMNIK("Подъёмник", TwoGisAttributeKind.ACCESSIBLE_ENTRANCE, null),
    SHIROKIY_LIFT("Широкий лифт", TwoGisAttributeKind.ACCESSIBLE_ENTRANCE, null),
    NET_DVERI("Нет двери / есть автоматическая дверь", TwoGisAttributeKind.ACCESSIBLE_ENTRANCE, null),
    DOSTUPNO("Доступно", TwoGisAttributeKind.ACCESSIBLE_ENTRANCE, null),

    DOSTUP_OGRANICHEN("Доступ ограничен", TwoGisAttributeKind.ACCESS_LIMITED, null);

    companion object {
        fun fromValue(value: String?): TwoGisAttributeGroup? =
            value?.trim()?.let { v -> entries.find { it.value.equals(v, ignoreCase = true) } }

        fun findPaymentCode(attr: String): String? = fromValue(attr)?.paymentCode

        fun isToiletFeeFree(attr: String): Boolean = fromValue(attr) == BESPLATNYY_TUALET

        fun isToiletFeePaid(attr: String): Boolean = fromValue(attr) == PLATNYY_TUALET

        fun isAccessibleEntrance(attr: String): Boolean = fromValue(attr)?.kind == TwoGisAttributeKind.ACCESSIBLE_ENTRANCE

        fun isAccessibleToilet(attr: String): Boolean = fromValue(attr) == TUALET_DLYA_MALOMOBILNYKH

        fun isAccessLimited(attr: String): Boolean = fromValue(attr)?.kind == TwoGisAttributeKind.ACCESS_LIMITED
    }
}
