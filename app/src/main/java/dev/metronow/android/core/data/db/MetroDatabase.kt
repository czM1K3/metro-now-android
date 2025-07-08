package dev.metronow.android.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.metronow.android.features.infotext.data.db.DbInfoText
import dev.metronow.android.features.infotext.data.db.InfoTextDao
import dev.metronow.android.features.stop.data.db.DbPlatform
import dev.metronow.android.features.stop.data.db.DbStop
import dev.metronow.android.features.stop.data.db.PlatformDao
import dev.metronow.android.features.stop.data.db.StopDao

@Database(version = 1, entities = [DbStop::class, DbPlatform::class, DbInfoText::class])
abstract class MetroDatabase : RoomDatabase() {
    abstract fun stopDao(): StopDao
    abstract fun platformDao(): PlatformDao
    abstract fun infoTextDao(): InfoTextDao

    companion object {
        fun newInstance(context: Context): MetroDatabase {
            return Room
                .databaseBuilder(context, MetroDatabase::class.java, "metro.db")
                .fallbackToDestructiveMigration(true)
                .build()
        }
    }
}
