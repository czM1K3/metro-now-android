package dev.metronow.android.features.stop.di

import dev.metronow.android.core.data.db.MetroDatabase
import dev.metronow.android.features.stop.data.StopRepository
import dev.metronow.android.features.stop.data.api.StopRemoteDataSource
import dev.metronow.android.features.stop.data.db.PlatformLocalDataSource
import dev.metronow.android.features.stop.data.db.StopLocalDataSource
import dev.metronow.android.features.stop.data.ds.StopDataSource
import dev.metronow.android.features.stop.presentation.StopsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val stopModule = module {
    factoryOf(::StopRemoteDataSource)

    single { get<MetroDatabase>().stopDao() }
    single { get<MetroDatabase>().platformDao() }
    factoryOf(::StopLocalDataSource)
    factoryOf(::PlatformLocalDataSource)

    single { StopDataSource(get()) }

    singleOf(::StopRepository)

    viewModelOf(::StopsViewModel)
}
