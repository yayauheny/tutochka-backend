package yayauheny.by.di

import com.zaxxer.hikari.HikariDataSource
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.impl.DefaultDSLContext
import org.koin.dsl.module
import yayauheny.by.config.DatabaseConfig
import yayauheny.by.repository.impl.CityRepositoryImpl
import yayauheny.by.repository.impl.CountryRepositoryImpl
import yayauheny.by.repository.impl.RestroomRepositoryImpl

val databaseConfigModule =
    module {
        single<DatabaseConfig> { DatabaseConfig() }
        single<HikariDataSource> { get<DatabaseConfig>().createDataSource() }
        single<Configuration> { get<DatabaseConfig>().createJooqConfiguration(get<HikariDataSource>()) }
        single<DSLContext> { DefaultDSLContext(get<Configuration>()) }

        single<CityRepositoryImpl> { CityRepositoryImpl(get()) }
        single<CountryRepositoryImpl> { CountryRepositoryImpl(get()) }
        single<RestroomRepositoryImpl> { RestroomRepositoryImpl(get()) }
    }
