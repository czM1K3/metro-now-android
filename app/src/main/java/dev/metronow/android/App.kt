package dev.metronow.android

import android.app.Application
import dev.metronow.android.core.di.sharedModules
import dev.metronow.android.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(sharedModules + appModule)
        }
    }
}
