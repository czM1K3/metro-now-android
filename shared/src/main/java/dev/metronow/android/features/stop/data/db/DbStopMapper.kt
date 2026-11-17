package dev.metronow.android.features.stop.data.db

import dev.metronow.android.features.stop.domain.Stop

fun Stop.toDb(): DbStop {
   return DbStop(
       id = id,
       name = name,
       latitude = latitude,
       longitude = longitude,
       hasMetro = hasMetro
   )
}

fun DbStopWithPlatform.toDomain(): Stop {
    return Stop(
        id = stop.id,
        name = stop.name,
        latitude = stop.latitude,
        longitude = stop.longitude,
        hasMetro = platforms.any { it.isMetro },
        platforms = platforms.map { it.toDomain() }
    )
}
