package dev.metronow.android.features.departure.data

import dev.metronow.android.features.departure.domain.DepartureWithCountdown
import dev.metronow.android.features.departure.domain.DeparturesGrouped
import dev.metronow.android.features.departure.domain.DeparturesWithCountdownGrouped
import dev.metronow.android.features.departure.domain.DeparturesWithCountdownGroupedMutable
import dev.metronow.android.features.departure.domain.RouteDetail
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.collections.iterator
import kotlin.collections.set

fun DeparturesGrouped.addCountDown(current: LocalDateTime): DeparturesWithCountdownGrouped {
    val prague = ZoneId.of("Europe/Prague")
    val local = ZoneId.systemDefault()
    val departuresWithCountdownGrouped: DeparturesWithCountdownGroupedMutable =
        mutableMapOf()
    for ((key1, innerMap) in this) {
        val innerCountdownMap: MutableMap<RouteDetail?, MutableList<DepartureWithCountdown>> =
            mutableMapOf()
        for ((key2, departures) in innerMap) {
            val departuresWithCountdown: MutableList<DepartureWithCountdown> =
                mutableListOf()
            for (departure in departures) {
                val countdown = ChronoUnit.SECONDS.between(
                    current.atZone(local),
                    departure.departureTimePredicted.atZone(prague),
                ).toDouble()

                val departureWithCountdown = DepartureWithCountdown(
                    platformId = departure.platformId,
                    routeDetail = departure.routeDetail,
                    heading = departure.heading,
                    departureTimePredicted = departure.departureTimePredicted,
                    countdown = countdown
                )
                // Remove those, that should be 20 seconds gone
                if (departureWithCountdown.countdown > -20) {
                    departuresWithCountdown.add(departureWithCountdown)
                    // We care just about two latest departures for route. This is not necessary, but should speed up when we have a lot of departures for one route.
                    if (departuresWithCountdown.size >= 2) {
                        break
                    }
                }
            }
            innerCountdownMap[key2] = departuresWithCountdown
        }
        departuresWithCountdownGrouped[key1] = innerCountdownMap
    }
    return departuresWithCountdownGrouped
}

