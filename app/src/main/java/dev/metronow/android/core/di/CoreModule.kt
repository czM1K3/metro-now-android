package dev.metronow.android.core.di

import com.apollographql.apollo.ApolloClient
import dev.metronow.android.core.data.UpdateFlow
import dev.metronow.android.core.data.db.MetroDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single { MetroDatabase.newInstance(androidContext()) }
    single { ApolloClient.Builder().serverUrl("https://api.metro.madsoft.cz/graphql").build() }
    single { UpdateFlow() }
}
