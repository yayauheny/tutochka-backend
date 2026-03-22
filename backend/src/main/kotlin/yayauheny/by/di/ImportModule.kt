package yayauheny.by.di

import org.jooq.DSLContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.service.import.ImportService
import yayauheny.by.service.import.ImportStrategy
import yayauheny.by.service.import.ImportStrategyRegistry
import yayauheny.by.service.import.schedule.ScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter
import yayauheny.by.service.import.twogis.TwoGisScrapedImportStrategy
import yayauheny.by.service.import.yandex.YandexMapsScrapedImportStrategy

val importModule =
    module {
        single<ImportStrategy>(named("twoGisScrapedImportStrategy")) {
            TwoGisScrapedImportStrategy(
                dsl = get<DSLContext>(),
                restroomRepository = get<RestroomRepository>()
            )
        }

        single<ImportStrategy>(named("yandexMapsScrapedImportStrategy")) {
            YandexMapsScrapedImportStrategy(
                dsl = get<DSLContext>(),
                restroomRepository = get<RestroomRepository>()
            )
        }

        single<List<ImportStrategy>> {
            getAll<ImportStrategy>()
        }

        single<ImportStrategyRegistry> {
            ImportStrategyRegistry(strategies = get())
        }

        single<ImportService> {
            ImportService(
                ctx = get<DSLContext>(),
                registry = get(),
                cityRepository = get(),
                restroomImportRepository = get()
            )
        }

        single<ScheduleAdapter> { TwoGisScheduleAdapter() }

        single<ScheduleMappingService> {
            ScheduleMappingService(adapters = getAll<ScheduleAdapter>())
        }
    }
