package yayauheny.by.di

import org.koin.dsl.module
import yayauheny.by.analytics.repository.AnalyticsRepository
import yayauheny.by.analytics.repository.AnalyticsRepositoryImpl
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.util.EncryptionService

val analyticsModule =
    module {
        single { EncryptionService() }
        single<AnalyticsRepository> { AnalyticsRepositoryImpl(get()) }
        single { AnalyticsService(get(), get()) }
    }
