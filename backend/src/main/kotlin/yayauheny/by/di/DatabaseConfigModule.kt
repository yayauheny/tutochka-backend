package yayauheny.by.di

import com.zaxxer.hikari.HikariDataSource
import org.jooq.Configuration
import org.jooq.DSLContext
import org.jooq.impl.DefaultDSLContext
import org.koin.dsl.module
import yayauheny.by.config.DatabaseConfig
import yayauheny.by.repository.BuildingRepository
import yayauheny.by.repository.CityRepository
import yayauheny.by.repository.CountryRepository
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.repository.SubwayRepository
import yayauheny.by.repository.impl.BuildingRepositoryImpl
import yayauheny.by.repository.impl.CityRepositoryImpl
import yayauheny.by.repository.impl.CountryRepositoryImpl
import yayauheny.by.repository.impl.RestroomRepositoryImpl
import yayauheny.by.repository.impl.SubwayRepositoryImpl
import yayauheny.by.service.import.schedule.ScheduleMappingService

val databaseConfigModule =
    module {
        single<DatabaseConfig> { DatabaseConfig() }
        single<HikariDataSource> { get<DatabaseConfig>().createDataSource() }
        single<Configuration> { get<DatabaseConfig>().createJooqConfiguration(get<HikariDataSource>()) }
        single<DSLContext> { DefaultDSLContext(get<Configuration>()) }

        single<CityRepository> { CityRepositoryImpl(get()) }
        single<CountryRepository> { CountryRepositoryImpl(get()) }
        single<RestroomRepository> {
            RestroomRepositoryImpl(
                ctx = get(),
                scheduleMappingService = get<ScheduleMappingService>()
            )
        }
        single<BuildingRepository> { BuildingRepositoryImpl(get()) }
        single<SubwayRepository> { SubwayRepositoryImpl(get()) }
    }
