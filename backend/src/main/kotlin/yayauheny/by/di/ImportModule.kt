package yayauheny.by.di

import org.koin.dsl.module
import yayauheny.by.service.import.ImportService
import yayauheny.by.service.import.ImportStrategy
import yayauheny.by.service.import.twogis.TwoGisImportStrategy

/**
 * Модуль DI для сервисов импорта.
 * Здесь регистрируются все стратегии импорта и сервис ImportService.
 */
val importModule =
    module {
        // Стратегии импорта
        single<ImportStrategy> {
            TwoGisImportStrategy(
                buildingRepository = get(),
                restroomRepository = get(),
                subwayRepository = get()
            )
        }

        // Собираем все стратегии импорта
        single<List<ImportStrategy>> {
            listOf(get<ImportStrategy>())
        }

        // Сервис импорта
        single<ImportService> {
            ImportService(
                strategies = get(),
                restroomImportRepository = get()
            )
        }
    }
