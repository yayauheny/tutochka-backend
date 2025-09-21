package yayauheny.by.di

import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module
import yayauheny.by.config.DatabaseConfig
import yayauheny.by.repository.CityRepository
import yayauheny.by.repository.CityRepositoryImpl
import yayauheny.by.repository.CountryRepository
import yayauheny.by.repository.CountryRepositoryImpl
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.repository.RestroomRepositoryImpl

val databaseConfigModule =
    module {
        single<DatabaseConfig> { DatabaseConfig() }
        single<Database> { get<DatabaseConfig>().createDatabase() }
        single<CountryRepository> { CountryRepositoryImpl() }
        single<CityRepository> { CityRepositoryImpl() }
        single<RestroomRepository> { RestroomRepositoryImpl() }
    }
