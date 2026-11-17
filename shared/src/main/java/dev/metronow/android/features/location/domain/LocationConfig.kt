package dev.metronow.android.features.location.domain

/**
 * Location update settings used by `LocationDataSource`.
 *
 * @property intervalMillis desired (and minimum) interval between updates
 * @property minDistanceMeters minimum movement before a new update is delivered
 * @property highAccuracy use GPS-level accuracy (gms only; vanilla ignores it)
 */
data class LocationConfig(
    val intervalMillis: Long = 2_000,
    val minDistanceMeters: Float = 30f,
    val highAccuracy: Boolean = true,
)
