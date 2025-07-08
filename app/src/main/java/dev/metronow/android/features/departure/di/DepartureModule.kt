package dev.metronow.android.features.departure.di

import dev.metronow.android.features.departure.data.DepartureRepository
import dev.metronow.android.features.departure.data.api.DepartureRemoteDataSource
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val departureModule = module {
    factoryOf(::DepartureRemoteDataSource)
    singleOf(::DepartureRepository)
}
