package dev.metronow.android.features.infotext.data

import dev.metronow.android.features.infotext.data.api.InfoTextRemoteDataSource
import dev.metronow.android.features.infotext.data.db.InfoTextLocalDataSource
import dev.metronow.android.features.infotext.domain.InfoText

class InfoTextRepository(
    private val remoteDataSource: InfoTextRemoteDataSource,
    private val localDataSource: InfoTextLocalDataSource,
) {
    suspend fun getInfoTexts(): List<InfoText> {
        val infoTexts = remoteDataSource.getInfoTexts()
        localDataSource.deleteAll()
        localDataSource.insert(infoTexts)
        return infoTexts
    }

    suspend fun getInfoText(id: String): InfoText? {
        return localDataSource.getInfoText(id)
    }
}
