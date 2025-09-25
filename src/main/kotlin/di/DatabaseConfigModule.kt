package yayauheny.by.di

import com.zaxxer.hikari.HikariDataSource
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
        single<HikariDataSource> { get<DatabaseConfig>().createDataSource() }
        single<Database>(createdAtStart = true) { Database.connect(get<HikariDataSource>()) }
        single<CountryRepository> { CountryRepositoryImpl() }
        single<CityRepository> { CityRepositoryImpl() }
        single<RestroomRepository> { RestroomRepositoryImpl() }
    }
