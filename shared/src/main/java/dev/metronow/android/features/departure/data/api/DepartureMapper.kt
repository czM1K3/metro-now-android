package dev.metronow.android.features.departure.data.api

import dev.metronow.android.DeparturesQuery
import dev.metronow.android.features.departure.domain.Departure
import dev.metronow.android.features.departure.domain.RouteDetail
import dev.metronow.android.features.departure.domain.VehicleType
import dev.metronow.android.type.VehicleType.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun DeparturesQuery.Departure.toDomain(): Departure {
    return Departure(
        platformId = platform.id,
        routeDetail = route?.let {
            RouteDetail(
                name = it.name,
                isSubstitute = it.isSubstitute,
                vehicleType = when (it.vehicleType) {
                    BUS -> VehicleType.BUS
                    TRAM -> VehicleType.TRAM
                    FERRY -> VehicleType.FERRY
                    TRAIN -> VehicleType.TRAIN
                    FUNICULAR -> VehicleType.FUNICULAR
                    SUBWAY -> VehicleType.SUBWAY
                    TROLLEYBUS -> VehicleType.TROLLEYBUS
                    UNKNOWN__ -> VehicleType.BUS
                },
                isNight = it.isNight,
            )
        },
        heading = headsign,
        departureTimePredicted = LocalDateTime.parse(departureTime.predicted as String,
            DateTimeFormatter.ISO_DATE_TIME),
    )
}
