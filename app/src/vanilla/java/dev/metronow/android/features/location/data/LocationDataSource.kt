package dev.metronow.android.features.location.data

import dev.metronow.android.features.location.domain.Coordinates
import dev.metronow.android.features.location.domain.toDomain
import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat

class LocationDataSource(
    private val context: Context
) {
    @SuppressLint("MissingPermission")
    fun locationFlow(): Flow<Coordinates> = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            close(IllegalStateException("Location permissions not granted"))
            return@callbackFlow
        }

        val locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trySend(location.toDomain())
            }

            @Deprecated("Deprecated in API 29")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }

            override fun onProviderEnabled(provider: String) {
            }

            override fun onProviderDisabled(provider: String) {
            }
        }

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000L,
            30f,
            locationListener
        )

        // Fallback to Network Provider if GPS is not available or doesn't provide updates
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                2000L,
                30f,
                locationListener
            )
        }

        awaitClose {
            locationManager.removeUpdates(locationListener)
        }
    }
}