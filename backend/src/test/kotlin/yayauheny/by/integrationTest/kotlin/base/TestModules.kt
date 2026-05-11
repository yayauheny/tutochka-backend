package integration.base

import org.jooq.DSLContext
import org.koin.core.module.Module
import org.koin.dsl.module
import yayauheny.by.config.metricsModule
import yayauheny.by.analytics.repository.AnalyticsRepository
import yayauheny.by.analytics.repository.AnalyticsRepositoryImpl
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.di.controllerModule
import yayauheny.by.di.importModule
import yayauheny.by.di.serviceModule
import yayauheny.by.repository.BuildingRepository
import yayauheny.by.repository.CityRepository
import yayauheny.by.repository.CountryRepository
import yayauheny.by.repository.RestroomImportRepository
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.repository.SubwayRepository
import yayauheny.by.repository.impl.BuildingRepositoryImpl
import yayauheny.by.repository.impl.CityRepositoryImpl
import yayauheny.by.repository.impl.CountryRepositoryImpl
import yayauheny.by.repository.impl.RestroomImportRepositoryImpl
import yayauheny.by.repository.impl.RestroomRepositoryImpl
import yayauheny.by.repository.impl.SubwayRepositoryImpl
import yayauheny.by.testsupport.TEST_ENCRYPTION_KEYSET_JSON
import yayauheny.by.util.EncryptionService

fun buildTestModules(testDslContext: DSLContext): List<Module> {
    val testDatabaseModule =
        module {
            single<DSLContext> { testDslContext }
            single<CountryRepository> { CountryRepositoryImpl(get()) }
            single<CityRepository> { CityRepositoryImpl(get()) }
            single<RestroomRepository> { RestroomRepositoryImpl(get()) }
            single<BuildingRepository> { BuildingRepositoryImpl(get()) }
            single<SubwayRepository> { SubwayRepositoryImpl(get()) }
            single<RestroomImportRepository> { RestroomImportRepositoryImpl(get()) }
        }

    val analyticsTestModule =
        module {
            single { EncryptionService(TEST_ENCRYPTION_KEYSET_JSON) }
            single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }
            single { AnalyticsService(get(), get()) }
        }

    return listOf(testDatabaseModule, serviceModule, analyticsTestModule, importModule, controllerModule, metricsModule)
}
