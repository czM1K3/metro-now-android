package dev.metronow.android.features.stop.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "platform")
data class DbPlatform (
    @PrimaryKey val id: String,
    val stopId: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isMetro: Boolean,
    val code: String?,
)
