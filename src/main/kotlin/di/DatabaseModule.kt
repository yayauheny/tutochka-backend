package yayauheny.by.di

import org.koin.dsl.module

val databaseModule = module {
    includes(databaseConfigModule, serviceModule)
}
