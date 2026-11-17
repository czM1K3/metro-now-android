package dev.metronow.android.features.stop.data.db

import dev.metronow.android.features.location.domain.Coordinates
import dev.metronow.android.features.stop.domain.Stop

class StopLocalDataSource (private val stopDao: StopDao) {

    suspend fun getClosestStop(coordinates: Coordinates) = stopDao.getClosestStop(coordinates.latitude, coordinates.longitude)?.toDomain()

    suspend fun getClosestMetroStop(coordinates: Coordinates) = stopDao.getClosestMetroStop(coordinates.latitude, coordinates.longitude)?.toDomain()
    
    suspend fun insert(stops: List<Stop>) {
        val dbStops = stops.map { it.toDb() }
        stopDao.insert(dbStops)
    }

    suspend fun deleteAll() {
        stopDao.deleteAll()
    }

    suspend fun count() = stopDao.count()

}
