package dev.metronow.android.features.location.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dev.metronow.android.features.location.domain.Coordinates
import dev.metronow.android.features.location.domain.LocationConfig
import dev.metronow.android.features.location.domain.toDomain
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationDataSource(
    private val context: Context,
    private val config: LocationConfig,
) {
    @SuppressLint("MissingPermission")
    fun locationFlow(): Flow<Coordinates> = callbackFlow {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        val priority = if (config.highAccuracy) {
            Priority.PRIORITY_HIGH_ACCURACY
        } else {
            Priority.PRIORITY_BALANCED_POWER_ACCURACY
        }
        val locationRequest = LocationRequest.Builder(config.intervalMillis)
            .setPriority(priority)
            .setWaitForAccurateLocation(config.highAccuracy)
            .setMinUpdateIntervalMillis(config.intervalMillis)
            .setMinUpdateDistanceMeters(config.minDistanceMeters)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    trySend(location.toDomain())
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }
}
