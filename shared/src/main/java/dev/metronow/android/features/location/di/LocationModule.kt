package dev.metronow.android.features.location.di

import dev.metronow.android.features.location.data.LocationDataSource
import dev.metronow.android.features.location.data.LocationRepository
import dev.metronow.android.features.location.data.LocationRepositoryImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

/** Expects the app to provide a `LocationConfig`. */
val locationModule = module {
    singleOf(::LocationRepositoryImpl) { bind<LocationRepository>() }
    factoryOf(::LocationDataSource)
}
