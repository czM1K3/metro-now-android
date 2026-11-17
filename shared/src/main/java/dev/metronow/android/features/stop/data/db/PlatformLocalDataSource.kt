package dev.metronow.android.features.stop.data.db

import dev.metronow.android.features.stop.domain.Platform


class PlatformLocalDataSource (private val platformDao: PlatformDao) {
    suspend fun insert(platforms: List<Pair<String, Platform>>) {
        val dbPlatforms = platforms.map { (stopId, platform) -> platform.toDb(stopId) }
        platformDao.insert(dbPlatforms)
    }

    suspend fun deleteAll() {
        platformDao.deleteAll()
    }
}
