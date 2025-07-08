package dev.metronow.android.features.stop.data.api

import dev.metronow.android.StopsQuery
import dev.metronow.android.features.stop.domain.Stop

fun StopsQuery.Stop.toDomain(): Stop {
    return Stop(
        id =  id,
        name = name,
        latitude = avgLatitude,
        longitude = avgLongitude,
        hasMetro = platforms.any { it.isMetro },
        platforms = platforms.map { it.toDomain() }
    )
}
