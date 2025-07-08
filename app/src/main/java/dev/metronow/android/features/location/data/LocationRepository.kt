package dev.metronow.android.features.location.data

class LocationRepository(
    private val locationDataSource: LocationDataSource
) {
    fun locationFlow() = locationDataSource.locationFlow()
}
