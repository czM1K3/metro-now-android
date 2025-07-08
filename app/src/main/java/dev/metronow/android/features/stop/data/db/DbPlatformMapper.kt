package dev.metronow.android.features.stop.data.db

import dev.metronow.android.features.stop.domain.Platform


fun DbPlatform.toDomain(): Platform {
    return Platform(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        code =  code,
        isMetro = isMetro
    )
}

fun Platform.toDb(stopId: String): DbPlatform {
    return DbPlatform(
        id = id,
        name = name,
        latitude = latitude,
        longitude = longitude,
        isMetro = isMetro,
        code = code,
        stopId = stopId,
    )
}
