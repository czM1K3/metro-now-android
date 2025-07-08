package dev.metronow.android.features.stop.data

import dev.metronow.android.features.location.domain.Coordinates
import dev.metronow.android.features.stop.data.api.StopRemoteDataSource
import dev.metronow.android.features.stop.data.db.PlatformLocalDataSource
import dev.metronow.android.features.stop.data.db.StopLocalDataSource
import dev.metronow.android.features.stop.data.ds.StopDataSource
import dev.metronow.android.features.stop.domain.ClosestStops
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class StopRepository(
    private val remoteDataSource: StopRemoteDataSource,
    private val localStopDataSource: StopLocalDataSource,
    private val localPlatformDataSource: PlatformLocalDataSource,
    private val stopDataSource: StopDataSource
) {
    suspend fun getClosestStops(coordinates: Coordinates): ClosestStops? {
        stopsLocalCacheUpdate()
        val closestMetroStop = localStopDataSource.getClosestMetroStop(coordinates)
        val closestStop = localStopDataSource.getClosestStop(coordinates)
        return if (closestStop == null || closestMetroStop == null) {
            null
        } else {
            ClosestStops(closestMetroStop = closestMetroStop, closestStop = closestStop)
        }
    }

    private suspend fun stopsLocalCacheUpdate() {
        val last = stopDataSource.getFetchDate()
        val now = LocalDateTime.now()
        val difference = ChronoUnit.DAYS.between(last, now)
        val countExistingStops = localStopDataSource.count()
        if (difference > 30 || countExistingStops == 0) {
            val stops = remoteDataSource.getStops()
            if (stops.isEmpty()) {
                return
            }
            localPlatformDataSource.deleteAll()
            localStopDataSource.deleteAll()
            localStopDataSource.insert(stops)
            val platformsWithStopIds = stops.flatMap { stop ->
                stop.platforms.map { platform ->
                    Pair(stop.id, platform)
                }
            }
            localPlatformDataSource.insert(platformsWithStopIds)
            stopDataSource.saveFetchDate(LocalDateTime.now())
        }
    }
}
