package yayauheny.by.service.import

import java.util.UUID
import org.slf4j.LoggerFactory
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.repository.SubwayRepository

/**
 * Сервис для автоматической привязки ближайшей станции метро к туалету.
 * Использует PostGIS оператор <-> для эффективного поиска ближайшей станции.
 */
class SubwayBindingService(
    private val restroomRepository: RestroomRepository,
    private val subwayRepository: SubwayRepository
) {
    private val logger = LoggerFactory.getLogger(SubwayBindingService::class.java)

    /**
     * Находит и привязывает ближайшую станцию метро к туалету.
     * Использует координаты туалета для поиска ближайшей станции в том же городе.
     *
     * @param restroomId ID туалета для привязки станции метро
     * @return true если станция была найдена и привязана, false если станция не найдена или уже привязана
     */
    suspend fun bindNearestSubwayStation(restroomId: UUID): Boolean {
        return try {
            // Получаем туалет с координатами
            val restroom =
                restroomRepository.findById(restroomId)
                    ?: run {
                        logger.warn("Restroom not found: $restroomId")
                        return false
                    }

            // Проверяем, что у туалета есть координаты
            val coordinates =
                restroom.coordinates
                    ?: run {
                        logger.warn("Restroom $restroomId has no coordinates, skipping subway binding")
                        return false
                    }

            // Используем существующий метод из SubwayRepository для обновления
            val updated =
                subwayRepository.setNearestStationForRestroom(
                    restroomId = restroomId,
                    lat = coordinates.lat,
                    lon = coordinates.lon
                )

            if (updated) {
                logger.debug("Successfully bound nearest subway station for restroom: $restroomId")
            } else {
                logger.debug("No subway station bound for restroom: $restroomId (may already be set or no station found)")
            }

            updated
        } catch (e: Exception) {
            logger.error("Error binding nearest subway station for restroom: $restroomId", e)
            false
        }
    }
}
