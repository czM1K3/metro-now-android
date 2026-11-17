package dev.metronow.android.features.stop.domain

data class ClosestStops(
    val closestMetroStop: Stop,
    val closestStop: Stop
) {
    fun getPlatformIds(): List<String> {
        val ids1 = closestMetroStop.platforms.filter { it.isMetro }.map { it.id }
        val ids2 = closestStop.platforms.filter { !it.isMetro }.map { it.id }
        return ids1 + ids2
    }
}
