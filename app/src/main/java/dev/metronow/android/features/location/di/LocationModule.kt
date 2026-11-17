package dev.metronow.android.features.location.di

import dev.metronow.android.features.location.data.LocationDataSource
import dev.metronow.android.features.location.data.LocationRepository
import dev.metronow.android.features.location.data.LocationRepositoryImpl
import dev.metronow.android.features.location.domain.LocationConfig
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val locationModule = module {
    singleOf(::LocationRepositoryImpl) { bind<LocationRepository>() }
    factoryOf(::LocationDataSource)
    single { LocationConfig() }
}
