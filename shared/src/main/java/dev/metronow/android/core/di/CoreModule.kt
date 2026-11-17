package dev.metronow.android.core.di

import com.apollographql.apollo.ApolloClient
import dev.metronow.android.core.data.RefreshConfig
import dev.metronow.android.core.data.UpdateFlow
import dev.metronow.android.core.data.db.MetroDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Expects the app to provide a `RefreshConfig`. */
val coreModule = module {
    single { MetroDatabase.newInstance(androidContext()) }
    single { ApolloClient.Builder().serverUrl("https://api.metro.madsoft.cz/graphql").build() }
    single { UpdateFlow(get<RefreshConfig>()) }
}
