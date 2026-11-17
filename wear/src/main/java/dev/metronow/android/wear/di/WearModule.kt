package dev.metronow.android.wear.di

import dev.metronow.android.core.data.RefreshConfig
import dev.metronow.android.features.location.domain.LocationConfig
import org.koin.dsl.module

/** Watch-only bindings: settings tuned for battery life. */
val wearModule = module {
    single { RefreshConfig(tickMillis = 1_000, reFetchMillis = 30_000) }
    single { LocationConfig(intervalMillis = 10_000, minDistanceMeters = 50f, highAccuracy = false) }
}
