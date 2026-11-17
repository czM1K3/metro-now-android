package dev.metronow.android.core.di

import dev.metronow.android.features.departure.di.departureModule
import dev.metronow.android.features.infotext.di.infoTextModule
import dev.metronow.android.features.location.di.locationModule
import dev.metronow.android.features.stop.di.stopModule
import org.koin.core.module.Module

/**
 * Koin modules shared by the phone and watch apps.
 *
 * They don't provide `RefreshConfig` or `LocationConfig`: each app supplies its own in its app
 * module, tuned for the device.
 */
val sharedModules: List<Module> = listOf(
    coreModule,
    stopModule,
    locationModule,
    departureModule,
    infoTextModule,
)
