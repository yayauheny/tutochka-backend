package yayauheny.by.di

import org.jooq.DSLContext
import org.koin.dsl.module
import yayauheny.by.service.import.ImportService
import yayauheny.by.service.import.ImportStrategy
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
    }
