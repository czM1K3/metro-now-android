package dev.metronow.android.features.stop.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PlatformDao {
    @Insert
    suspend fun insert(breeds: List<DbPlatform>)


    @Query("DELETE FROM platform")
    suspend fun deleteAll()
}
