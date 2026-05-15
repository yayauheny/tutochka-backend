package yayauheny.by.di

import java.time.Clock
import org.koin.dsl.module
import yayauheny.by.analytics.repository.AnalyticsRepository
import yayauheny.by.analytics.repository.AnalyticsRepositoryImpl
import yayauheny.by.analytics.repository.PublicAnalyticsRepository
import yayauheny.by.analytics.repository.PublicAnalyticsRepositoryImpl
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.analytics.service.PublicAnalyticsService
import yayauheny.by.util.EncryptionService

val analyticsModule =
    module {
        single<Clock> { Clock.systemUTC() }
        single { EncryptionService() }
        single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }
        single<PublicAnalyticsRepository> { PublicAnalyticsRepositoryImpl(get()) }
        single { AnalyticsService(get(), get()) }
        single { PublicAnalyticsService(get(), get()) }
    }
