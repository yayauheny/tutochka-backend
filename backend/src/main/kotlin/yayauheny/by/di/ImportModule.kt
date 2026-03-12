package yayauheny.by.di

import org.jooq.DSLContext
import org.koin.dsl.module
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.service.import.ImportService
import yayauheny.by.service.import.ImportStrategy
import yayauheny.by.service.import.ImportStrategyRegistry
import yayauheny.by.service.import.schedule.ScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter
import yayauheny.by.service.import.twogis.TwoGisScrapedImportStrategy

val importModule =
    module {
        single<ImportStrategy> {
            TwoGisScrapedImportStrategy(
                dsl = get<DSLContext>(),
                restroomRepository = get<RestroomRepository>()
            )
        }

        single<List<ImportStrategy>> {
            listOf(get<ImportStrategy>())
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
