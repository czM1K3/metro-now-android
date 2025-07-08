package dev.metronow.android.features.departure.data

import dev.metronow.android.features.departure.data.api.DepartureRemoteDataSource
import dev.metronow.android.features.departure.domain.DeparturesGrouped


class DepartureRepository(
    private val departureRemoteDataSource: DepartureRemoteDataSource
) {
    suspend fun getDepartures(platformIds: List<String>): DeparturesGrouped {
        val result = departureRemoteDataSource.getDepartures(platformIds)
        val grouped = result
            .sortedBy{ it.routeDetail?.name?.toIntOrNull() }
            .groupBy { it.platformId }
            .mapValues { (_, platformDepartures) ->
                platformDepartures.groupBy { it.routeDetail }
            }
        return grouped
    }
}
