package yayauheny.by.unit.importing.provider.yandex

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedLocation
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedPlace
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedWorkingHour
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedWorkingInterval
import yayauheny.by.importing.provider.yandex.YandexNormalizationProfile
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.RestroomStatus
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.toJacksonNode

@DisplayName("YandexNormalizationProfile Tests")
class YandexNormalizationProfileTest {
    @Test
    fun short_title_wins_over_title() {
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Long Name",
                    shortTitle = "Short Name",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1"
                )
            )

        assertThat(resolved.name).isEqualTo("Short Name")
    }

    @Test
    fun title_fallback_is_preserved_when_short_title_is_missing() {
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Main Name",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1"
                )
            )

        assertThat(resolved.name).isEqualTo("Main Name")
    }

    @Test
    fun missing_names_fall_back_to_default_toilet_name() {
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = null,
                    shortTitle = null,
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1"
                )
            )

        assertThat(resolved.name).isEqualTo("Туалет")
    }

    @Test
    fun address_cleanup_and_location_heuristic_are_preserved() {
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Туалет",
                    address = " Торговый центр Green ",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1"
                )
            )

        assertThat(resolved.address).isEqualTo("Торговый центр Green")
        assertThat(resolved.locationType).isEqualTo(LocationType.INSIDE_BUILDING)
    }

    @Test
    fun fee_status_amenities_and_raw_schedule_are_preserved() {
        val workingHours =
            listOf(
                YandexMapsScrapedWorkingHour(
                    day = "mon",
                    from = "10:00",
                    to = "18:00"
                )
            )
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Бесплатный туалет",
                    address = "Mall",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1",
                    statusText = "закрыт",
                    features = listOf("доступно", "туалет для людей с инвалидностью"),
                    workingHours = workingHours
                )
            )

        assertThat(resolved.feeType).isEqualTo(FeeType.FREE)
        assertThat(resolved.status).isEqualTo(RestroomStatus.INACTIVE)
        assertThat(resolved.amenities["accessible_entrance"]).isNotNull()
        assertThat(resolved.amenities["accessible_toilet"]).isNotNull()
        assertThat(resolved.rawSchedule).isNotNull()
    }

    @Test
    fun building_context_is_preserved_for_inside_building_locations() {
        val rawSchedule =
            buildJsonObject {
                put(
                    "mon",
                    buildJsonObject {
                        put("from", "10:00")
                        put("to", "18:00")
                    }
                )
            }.toJacksonNode()
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Туалет",
                    address = "Торговый центр Green",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1",
                    workingHours = listOf(YandexMapsScrapedWorkingHour(day = "mon", from = "10:00", to = "18:00"))
                )
            )

        assertThat(resolved.buildingContext).isNotNull()
        assertThat(resolved.buildingContext!!.address).isEqualTo("Торговый центр Green")
        assertThat(resolved.rawSchedule?.toString()).isEqualTo(rawSchedule.toString())
    }

    @Test
    fun raw_schedule_preserves_structured_intervals() {
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Туалет",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1",
                    workingHours =
                        listOf(
                            YandexMapsScrapedWorkingHour(
                                day = "mon",
                                from = "09:00",
                                to = "12:00",
                                intervals =
                                    listOf(
                                        YandexMapsScrapedWorkingInterval(
                                            from = "09:00",
                                            to = "12:00"
                                        ),
                                        YandexMapsScrapedWorkingInterval(
                                            from = "12:30",
                                            to = "21:00"
                                        )
                                    )
                            )
                        )
                )
            )

        assertThat(resolved.rawSchedule?.toString()).isEqualTo("""{"mon":{"from":"09:00","to":"12:00","intervals":[{"from":"09:00","to":"12:00"},{"from":"12:30","to":"21:00"}]}}""")
    }

    @Test
    fun blank_address_and_empty_working_hours_are_normalized_consistently() {
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Туалет",
                    address = " null ",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1",
                    workingHours = emptyList()
                )
            )

        assertThat(resolved.address).isNull()
        assertThat(resolved.rawSchedule).isNull()
    }
}
