package dev.metronow.android.features.stop.domain

data class Stop (
    val id: String, // U321
    val name: String, // Dejvická
    val latitude: Double, // 50.1009254
    val longitude: Double, // 14.39318
    val hasMetro: Boolean, // true
    val platforms: List<Platform> // [...]
)
