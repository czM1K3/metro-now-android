package dev.metronow.android

import android.app.Application
import dev.metronow.android.core.di.coreModule
import dev.metronow.android.features.departure.di.departureModule
import dev.metronow.android.features.infotext.di.infoTextModule
import dev.metronow.android.features.location.di.locationModule
import dev.metronow.android.features.stop.di.stopModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(coreModule, stopModule, locationModule, departureModule, infoTextModule)
        }
    }
}