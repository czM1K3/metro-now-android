package dev.metronow.android.features.infotext.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "infoText")
data class DbInfoText(
    @PrimaryKey val id: String,
    val textCz: String,
    val textEn: String?,
    val validFrom: String?,
    val validTo: String?,
    val priority: String,
)
