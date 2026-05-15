package yayauheny.by.di

import org.jooq.DSLContext
import org.koin.dsl.module
import yayauheny.by.importing.api.ImportController
import yayauheny.by.importing.provider.twogis.TwoGisImportAdapter
import yayauheny.by.importing.provider.yandex.YandexImportAdapter
import yayauheny.by.importing.repository.BuildingImportRepository
import yayauheny.by.importing.repository.BuildingImportRepositoryImpl
import yayauheny.by.importing.repository.DuplicateSuspicionRepository
import yayauheny.by.importing.repository.DuplicateSuspicionRepositoryImpl
import yayauheny.by.importing.repository.ImportInboxRepository
import yayauheny.by.importing.repository.ImportInboxRepositoryImpl
import yayauheny.by.importing.repository.RestroomImportRepository
import yayauheny.by.importing.repository.RestroomImportRepositoryImpl
import yayauheny.by.importing.service.ImportAdapterRegistry
import yayauheny.by.importing.service.ImportBatchProcessor
import yayauheny.by.importing.service.ImportCityResolver
import yayauheny.by.importing.service.ImportPipeline
import yayauheny.by.importing.service.ImportService

val importingModule =
    module {
        single { TwoGisImportAdapter() }
        single { YandexImportAdapter() }
        single {
            ImportAdapterRegistry(
                listOf(
                    get<TwoGisImportAdapter>(),
                    get<YandexImportAdapter>()
                )
            )
        }

        single<ImportInboxRepository> { ImportInboxRepositoryImpl(get()) }
        single<BuildingImportRepository> { BuildingImportRepositoryImpl(get()) }
        single<RestroomImportRepository> { RestroomImportRepositoryImpl(get()) }
        single<DuplicateSuspicionRepository> { DuplicateSuspicionRepositoryImpl() }
        single { ImportCityResolver(get()) }

        single {
            ImportPipeline(
                buildingImportRepository = get(),
                restroomImportRepository = get(),
                duplicateSuspicionRepository = get()
            )
        }
        single {
            ImportBatchProcessor(
                ctx = get<DSLContext>(),
                registry = get(),
                importCityResolver = get(),
                importInboxRepository = get(),
                importPipeline = get()
            )
        }
        single {
            ImportService(
                cityRepository = get(),
                importBatchProcessor = get()
            )
        }
        single { ImportController(get()) }
    }
