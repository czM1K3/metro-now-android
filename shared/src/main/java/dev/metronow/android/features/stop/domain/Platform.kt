package dev.metronow.android.features.stop.domain

data class Platform (
    val id: String, // U321Z101P
    val name: String, // Dejvická
    val latitude: Double, // 50.1004639
    val longitude: Double, // 14.3938885
    val isMetro: Boolean, // true
    // Nullable, because long distance busses and trains don't have that in PID
    val code: String?, // M1
)
