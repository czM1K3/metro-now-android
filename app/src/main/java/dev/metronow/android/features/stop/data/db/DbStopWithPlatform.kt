package dev.metronow.android.features.stop.data.db

import androidx.room.Embedded
import androidx.room.Relation

data class DbStopWithPlatform (
    @Embedded val stop: DbStop,
    @Relation(
        parentColumn = "id",
        entityColumn = "stopId"
    )
    val platforms: List<DbPlatform>
)
