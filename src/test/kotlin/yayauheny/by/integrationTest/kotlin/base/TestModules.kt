package integration.base

import org.jetbrains.exposed.sql.Database
import org.koin.core.module.Module
import org.koin.dsl.module
import yayauheny.by.di.controllerModule
import yayauheny.by.di.serviceModule
import yayauheny.by.repository.impl.CityRepositoryImpl
import yayauheny.by.repository.impl.CountryRepositoryImpl
import yayauheny.by.repository.impl.RestroomRepositoryImpl

fun buildTestModules(testDatabase: Database): List<Module> {
    val testDatabaseModule =
        module {
            single<Database> { testDatabase }
            single<CountryRepository> { CountryRepositoryImpl() }
            single<CityRepository> { CityRepositoryImpl() }
            single<RestroomRepository> { RestroomRepositoryImpl() }
        }

    return listOf(testDatabaseModule, serviceModule, controllerModule)
}
