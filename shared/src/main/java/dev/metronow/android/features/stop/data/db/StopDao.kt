package dev.metronow.android.features.stop.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface StopDao {
    @Transaction
    @Query("SELECT * FROM stop WHERE hasMetro = 1 ORDER BY abs(latitude - :latitude) + abs(longitude - :longitude)")
    suspend fun getClosestMetroStop(latitude: Double, longitude: Double): DbStopWithPlatform?

    @Transaction
    @Query("SELECT * FROM stop ORDER BY abs(latitude - :latitude) + abs(longitude - :longitude)")
    suspend fun getClosestStop(latitude: Double, longitude: Double): DbStopWithPlatform?
    
    @Insert
    suspend fun insert(breeds: List<DbStop>)

    @Query("DELETE FROM stop")
    suspend fun deleteAll()

    @Query("SELECT COUNT(id) FROM stop")
    suspend fun count(): Int
}
