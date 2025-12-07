package yayauheny.by.model.import

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Структура для хранения внешних ID карт в поле external_maps.
 */
@Serializable
data class ExternalMapIds(
    @SerialName("2gis") val twoGis: String? = null,
    val yandex: String? = null,
    val google: String? = null,
    val mapsme: String? = null
)
