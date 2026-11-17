package dev.metronow.android.wear

import android.app.Application
import dev.metronow.android.core.di.sharedModules
import dev.metronow.android.wear.di.wearModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WearApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@WearApp)
            modules(sharedModules + wearModule)
        }
    }
}
