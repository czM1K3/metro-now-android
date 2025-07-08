package dev.metronow.android.features.location.di

import dev.metronow.android.features.location.data.LocationDataSource
import dev.metronow.android.features.location.data.LocationRepository
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val locationModule = module {
    singleOf(::LocationRepository)
    factoryOf(::LocationDataSource)
}
