package dev.metronow.android.wear.presentation.departures

import dev.metronow.android.features.departure.domain.DepartureWithCountdown
import dev.metronow.android.features.departure.domain.RouteDetail
import dev.metronow.android.features.departure.domain.VehicleType
import dev.metronow.android.features.stop.domain.ClosestStops
import dev.metronow.android.features.stop.domain.Platform
import dev.metronow.android.features.stop.domain.Stop
import dev.metronow.android.features.stop.presentation.StopsScreenState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class DepartureListItemsTest {
    private val now = LocalDateTime.of(2026, 1, 15, 12, 0)

    private val metroPlatform = Platform("M1", "Dejvická", 0.0, 0.0, isMetro = true, code = "M1")
    private val metroPlatform2 = Platform("M2", "Dejvická", 0.0, 0.0, isMetro = true, code = "M2")
    private val tramPlatform = Platform("T", "Vítězné náměstí", 0.0, 0.0, isMetro = false, code = "A")
    private val trainPlatform = Platform("R", "Vítězné náměstí", 0.0, 0.0, isMetro = false, code = null)
    private val ignoredMetroAtOtherStop = Platform("X", "Vítězné náměstí", 0.0, 0.0, isMetro = true, code = "M9")

    private val metroStop = Stop("U1", "Dejvická", 0.0, 0.0, hasMetro = true, platforms = listOf(metroPlatform, metroPlatform2))
    private val otherStop = Stop(
        "U2", "Vítězné náměstí", 0.0, 0.0, hasMetro = false,
        platforms = listOf(tramPlatform, trainPlatform, ignoredMetroAtOtherStop),
    )

    private val metroA = RouteDetail("A", isSubstitute = false, vehicleType = VehicleType.SUBWAY, isNight = false)
    private val metroB = RouteDetail("B", isSubstitute = false, vehicleType = VehicleType.SUBWAY, isNight = false)
    private val tram8 = RouteDetail("8", isSubstitute = false, vehicleType = VehicleType.TRAM, isNight = false)
    private val tram20 = RouteDetail("20", isSubstitute = false, vehicleType = VehicleType.TRAM, isNight = false)
    private val train = RouteDetail("R", isSubstitute = false, vehicleType = VehicleType.TRAIN, isNight = false)

    private fun dep(platform: Platform, route: RouteDetail, heading: String, countdown: Double) =
        DepartureWithCountdown(platform.id, route, heading, now, countdown)

    private fun state(
        departures: Map<String, Map<RouteDetail?, List<DepartureWithCountdown>>>,
        loadingMetro: Boolean = false,
        loadingOther: Boolean = false,
    ) = StopsScreenState(
        closestStops = ClosestStops(closestMetroStop = metroStop, closestStop = otherStop),
        loadingMetro = loadingMetro,
        loadingOther = loadingOther,
        departures = departures,
    )

    @Test
    fun noClosestStopsGivesNoItems() {
        assertEquals(emptyList<DepartureListItem>(), StopsScreenState().toDepartureListItems())
    }

    @Test
    fun selectsLikeThePhoneScreen() {
        val items = state(
            mapOf(
                // Metro: only the first route group per platform
                metroPlatform.id to mapOf(
                    metroA to listOf(dep(metroPlatform, metroA, "Depo Hostivař", 60.0), dep(metroPlatform, metroA, "Depo Hostivař", 240.0)),
                    metroB to listOf(dep(metroPlatform, metroB, "Zličín", 30.0)),
                ),
                // Tram platform with a code: one row per route group, first departure
                tramPlatform.id to mapOf(
                    tram8 to listOf(dep(tramPlatform, tram8, "Starý Hloubětín", 10.0), dep(tramPlatform, tram8, "Starý Hloubětín", 400.0)),
                    tram20 to listOf(dep(tramPlatform, tram20, "Sídliště Barrandov", 20.0)),
                ),
                // Platform without a code: one row per departure, no header
                trainPlatform.id to mapOf(
                    train to listOf(dep(trainPlatform, train, "Kladno", 100.0), dep(trainPlatform, train, "Praha hl.n.", 200.0)),
                ),
            )
        ).toDepartureListItems()

        val summary = items.map {
            when (it) {
                is DepartureListItem.Header -> "H:${it.title}:${it.isMetro}"
                is DepartureListItem.Loading -> "L"
                is DepartureListItem.Empty -> "E"
                is DepartureListItem.Row -> "R:${it.routeDetail?.name}:${it.departure.heading}:${it.departure.countdown}"
            }
        }
        assertEquals(
            listOf(
                "H:Dejvická:true",
                "R:A:Depo Hostivař:60.0",
                // metroPlatform2 has no departures: skipped like on the phone
                "H:Vítězné náměstí A:false",
                "R:8:Starý Hloubětín:10.0",
                "R:20:Sídliště Barrandov:20.0",
                "R:R:Kladno:100.0",
                "R:R:Praha hl.n.:200.0",
                // Metro platforms of the closest (non-metro) stop are not shown
            ),
            summary,
        )
        assertEquals("keys must be unique", items.size, items.map { it.key }.toSet().size)
    }

    @Test
    fun loadingAndEmptySections() {
        val loading = state(emptyMap(), loadingMetro = true, loadingOther = true).toDepartureListItems()
        assertEquals(
            listOf("metro-header", "metro-loading", "platform-T-header", "platform-T-loading", "platform-R-loading"),
            loading.map { it.key },
        )

        val empty = state(emptyMap()).toDepartureListItems()
        assertEquals(
            listOf("metro-header", "metro-empty", "platform-T-header", "platform-T-empty", "platform-R-empty"),
            empty.map { it.key },
        )
        assertTrue(empty.filterIsInstance<DepartureListItem.Empty>().size == 3)
    }

    @Test
    fun routeGroupsWithoutDeparturesAreSkipped() {
        val items = state(mapOf(tramPlatform.id to mapOf(tram8 to emptyList()))).toDepartureListItems()
        assertTrue(items.any { it.key == "platform-T-empty" })
        assertTrue(items.none { it is DepartureListItem.Row })
    }
}
