package dev.metronow.android.features.stop.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stop")
data class DbStop (
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val hasMetro: Boolean,
)
