package yayauheny.by.di

import org.jooq.DSLContext
import org.koin.dsl.module
import yayauheny.by.service.import.ImportService
import yayauheny.by.service.import.ImportStrategy
import yayauheny.by.service.import.schedule.ScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.service.import.schedule.TwoGisScheduleAdapter
import yayauheny.by.service.import.twogis.TwoGisImportStrategy

val importModule =
    module {
        single<ImportStrategy> {
            TwoGisImportStrategy(
                dsl = get<DSLContext>()
            )
        }

        single<List<ImportStrategy>> {
            listOf(get<ImportStrategy>())
        }

        single<ImportService> {
            ImportService(
                strategies = get(),
                restroomImportRepository = get()
            )
        }

        // Schedule adapters
        single<ScheduleAdapter> { TwoGisScheduleAdapter() }

        single<List<ScheduleAdapter>> {
            listOf(get<ScheduleAdapter>())
        }

        single<ScheduleMappingService> {
            ScheduleMappingService(adapters = get())
        }
    }
