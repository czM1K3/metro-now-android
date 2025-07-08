package dev.metronow.android.features.infotext.di

import dev.metronow.android.core.data.db.MetroDatabase
import dev.metronow.android.features.infotext.data.InfoTextRepository
import dev.metronow.android.features.infotext.data.api.InfoTextRemoteDataSource
import dev.metronow.android.features.infotext.data.db.InfoTextLocalDataSource
import dev.metronow.android.features.infotext.presentation.detail.InfoTextDetailVIewModel
import dev.metronow.android.features.infotext.presentation.list.InfoTextsListViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val infoTextModule = module {
    factoryOf(::InfoTextRemoteDataSource)

    single { get<MetroDatabase>().infoTextDao() }
    factoryOf(::InfoTextLocalDataSource)

    singleOf(::InfoTextRepository)

    viewModelOf(::InfoTextsListViewModel)
    viewModelOf(::InfoTextDetailVIewModel)
}
