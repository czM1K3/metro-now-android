package dev.metronow.android.features.stop.data.ds

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime


class StopDataSource(private val context: Context) {
    private val Context.dataStore by preferencesDataStore("fit_datastore")
    private val fetchDateKey = stringPreferencesKey("fetch_date")

    suspend fun saveFetchDate(date: LocalDateTime) {
        context.dataStore.edit { preferences ->
            preferences[fetchDateKey] = date.toString()
        }
    }

    suspend fun getFetchDate(): LocalDateTime {
        val raw = context.dataStore.data.map { preferences ->
            preferences[fetchDateKey]
        }.first()

        return if (raw.isNullOrEmpty()) {
            LocalDateTime.MIN
        } else {
            LocalDateTime.parse(raw)
        }
    }
}
