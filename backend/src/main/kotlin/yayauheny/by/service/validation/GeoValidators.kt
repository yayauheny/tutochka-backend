package yayauheny.by.service.validation

import io.konform.validation.Validation
import io.konform.validation.constraints.maximum
import io.konform.validation.constraints.minimum
import yayauheny.by.model.dto.Coordinates

/**
 * Параметры для поиска ближайших туалетов
 */
data class NearestRestroomsParams(
    val coordinates: Coordinates,
    val limit: Int?,
    val distanceMeters: Int
)

/**
 * Валидатор для координат LatLon.
 * Проверяет, что широта находится в диапазоне [-90, 90],
 * а долгота в диапазоне [-180, 180].
 * Также проверяет на NaN и Infinity.
 */
val validateCoordinates =
    Validation<Coordinates> {
        Coordinates::lat {
            constrain("Широта должна быть конечным числом") { it.isFinite() }
            minimum(-90.0) hint "Широта должна быть не менее -90 градусов"
            maximum(90.0) hint "Широта должна быть не более 90 градусов"
        }
        Coordinates::lon {
            constrain("Долгота должна быть конечным числом") { it.isFinite() }
            minimum(-180.0) hint "Долгота должна быть не менее -180 градусов"
            maximum(180.0) hint "Долгота должна быть не более 180 градусов"
        }
    }

/**
 * Валидатор для параметров поиска ближайших туалетов
 */
val validateNearestRestroomsParams =
    Validation<NearestRestroomsParams> {
        NearestRestroomsParams::coordinates {
            run(validateCoordinates)
        }
        NearestRestroomsParams::limit ifPresent {
            minimum(1) hint "Лимит должен быть не менее 1"
            maximum(100) hint "Лимит должен быть не более 100"
        }
        NearestRestroomsParams::distanceMeters {
            minimum(1) hint "Радиус поиска должен быть не менее 1 метра"
            maximum(50000) hint "Радиус поиска должен быть не более 50000 метров (50 км)"
        }
    }
