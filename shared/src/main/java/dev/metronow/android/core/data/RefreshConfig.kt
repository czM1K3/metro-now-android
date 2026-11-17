package dev.metronow.android.core.data

/**
 * Timing of the departures screen.
 *
 * @property tickMillis how often countdowns and "updated X ago" are recalculated
 * @property reFetchMillis how often departures are fetched from the API
 */
data class RefreshConfig(
    val tickMillis: Long = 1_000,
    val reFetchMillis: Long = 10_000,
)
