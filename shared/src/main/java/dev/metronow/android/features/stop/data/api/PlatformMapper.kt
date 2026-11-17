package dev.metronow.android.features.stop.data.api

import dev.metronow.android.StopsQuery
import dev.metronow.android.features.stop.domain.Platform

fun StopsQuery.Platform.toDomain(): Platform {
    return Platform(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        isMetro = isMetro,
        code = code,
    )
}
