package dev.metronow.android.features.location.domain

import android.location.Location

data class Coordinates(val latitude: Double, val longitude: Double)

fun Location.toDomain() = Coordinates(latitude = this.latitude, longitude = this.longitude)
