package dev.metronow.android.features.location.data

import dev.metronow.android.features.location.domain.Coordinates
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    /** Cold flow of location updates. Collecting it starts updates, cancelling it stops them. */
    fun locationFlow(): Flow<Coordinates>
}

class LocationRepositoryImpl(
    private val locationDataSource: LocationDataSource
) : LocationRepository {
    override fun locationFlow() = locationDataSource.locationFlow()
}
