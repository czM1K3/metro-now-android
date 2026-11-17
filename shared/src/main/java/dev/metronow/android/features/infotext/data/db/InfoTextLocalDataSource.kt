package dev.metronow.android.features.infotext.data.db

import dev.metronow.android.features.infotext.domain.InfoText


class InfoTextLocalDataSource(private val infoTextDao: InfoTextDao) {
    suspend fun getInfoTexts(): List<InfoText> {
        return infoTextDao.getInfoTexts().map { it.toDomain() }
    }

    suspend fun getInfoText(id: String): InfoText? {
        val temp = infoTextDao.getInfoText(id)
        return temp?.toDomain()
    }

    suspend fun insert(infoTexts: List<InfoText>) {
        infoTextDao.insert(infoTexts.map { it.toDb() })
    }

    suspend fun deleteAll() {
        infoTextDao.deleteAll()
    }
}
