package dev.metronow.android.di

import dev.metronow.android.core.data.RefreshConfig
import dev.metronow.android.features.infotext.presentation.detail.InfoTextDetailVIewModel
import dev.metronow.android.features.infotext.presentation.list.InfoTextsListViewModel
import dev.metronow.android.features.location.domain.LocationConfig
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** Phone-only bindings: device-tuned settings and the info text ViewModels. */
val appModule = module {
    single { RefreshConfig() }
    single { LocationConfig() }

    viewModelOf(::InfoTextsListViewModel)
    viewModelOf(::InfoTextDetailVIewModel)
}
