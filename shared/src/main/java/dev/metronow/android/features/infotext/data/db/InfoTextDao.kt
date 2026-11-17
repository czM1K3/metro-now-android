package dev.metronow.android.features.infotext.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface InfoTextDao {
    @Query("SELECT * FROM infoText")
    suspend fun getInfoTexts(): List<DbInfoText>

    @Query("SELECT * FROM infoText WHERE id = :id")
    suspend fun getInfoText(id: String): DbInfoText?

    @Insert
    suspend fun insert(infoTexts: List<DbInfoText>)

    @Query("DELETE FROM infoText")
    suspend fun deleteAll()
}
