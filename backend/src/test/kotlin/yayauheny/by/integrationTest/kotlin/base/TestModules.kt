package integration.base

import org.jooq.DSLContext
import org.koin.core.module.Module
import org.koin.dsl.module
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

    return listOf(testDatabaseModule, serviceModule, importModule, controllerModule)
}
