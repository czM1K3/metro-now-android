package dev.metronow.android.features.departure.domain

import java.time.LocalDateTime

enum class VehicleType {
    BUS,
    TRAM,
    FERRY,
    TRAIN,
    FUNICULAR,
    SUBWAY,
    TROLLEYBUS
}

data class RouteDetail(
    val name: String?,
    val isSubstitute: Boolean,
    val vehicleType: VehicleType,
    val isNight: Boolean,
)

interface IDeparture {
    val platformId: String
    val routeDetail: RouteDetail?
    val heading: String?
    val departureTimePredicted: LocalDateTime
}

data class Departure(
    override val platformId: String,
    override val routeDetail: RouteDetail?,
    override val heading: String?,
    override val departureTimePredicted: LocalDateTime,
) : IDeparture

data class DepartureWithCountdown(
    override val platformId: String,
    override val routeDetail: RouteDetail?,
    override val heading: String?,
    override val departureTimePredicted: LocalDateTime,
    val countdown: Double,
) : IDeparture

// First map key is identifier is ID of platforms
// Seconds map key is object of each line
// Value of second map is list of departure, that are only on each platform for each line
typealias DeparturesGrouped = Map<String, Map<RouteDetail?, List<Departure>>>
// Same as previous, but with countdown times
typealias DeparturesWithCountdownGrouped = Map<String, Map<RouteDetail?, List<DepartureWithCountdown>>>
// Same as previous, but mutable
typealias DeparturesWithCountdownGroupedMutable = MutableMap<String, MutableMap<RouteDetail?, MutableList<DepartureWithCountdown>>>
