package dev.metronow.android.features.stop.presentation

import dev.metronow.android.core.data.RefreshConfig
import dev.metronow.android.core.data.UpdateFlow
import dev.metronow.android.features.departure.data.DepartureRepository
import dev.metronow.android.features.departure.domain.Departure
import dev.metronow.android.features.departure.domain.DeparturesGrouped
import dev.metronow.android.features.departure.domain.RouteDetail
import dev.metronow.android.features.departure.domain.VehicleType
import dev.metronow.android.features.location.data.LocationRepository
import dev.metronow.android.features.location.domain.Coordinates
import dev.metronow.android.features.stop.data.StopRepository
import dev.metronow.android.features.stop.domain.ClosestStops
import dev.metronow.android.features.stop.domain.Platform
import dev.metronow.android.features.stop.domain.Stop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class StopsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var originalTimeZone: TimeZone

    private val base = LocalDateTime.of(2026, 1, 15, 12, 0, 0)
    private val route = RouteDetail(name = "A", isSubstitute = false, vehicleType = VehicleType.SUBWAY, isNight = false)
    private val platform = Platform(
        id = "U321Z101P",
        name = "Dejvická",
        latitude = 50.1004639,
        longitude = 14.3938885,
        isMetro = true,
        code = "M1",
    )
    private val stop = Stop(
        id = "U321",
        name = "Dejvická",
        latitude = 50.1009254,
        longitude = 14.39318,
        hasMetro = true,
        platforms = listOf(platform),
    )

    private var fetchCount = 0
    private var locationSubscribed = false

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        // Countdowns convert API times from Prague time to the device zone; pin the device zone.
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Prague"))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        TimeZone.setDefault(originalTimeZone)
    }

    private fun TestScope.createViewModel(): StopsViewModel {
        val locationRepository = object : LocationRepository {
            override fun locationFlow() = flow {
                locationSubscribed = true
                emit(Coordinates(50.1, 14.39))
                awaitCancellation()
            }.onCompletion { locationSubscribed = false }
        }
        val stopRepository = object : StopRepository {
            override suspend fun getClosestStops(coordinates: Coordinates) =
                ClosestStops(closestMetroStop = stop, closestStop = stop)
        }
        val departureRepository = object : DepartureRepository {
            override suspend fun getDepartures(platformIds: List<String>): DeparturesGrouped {
                fetchCount++
                return mapOf(
                    platform.id to mapOf(
                        route to listOf(Departure(platform.id, route, "Depo Hostivař", base.plusSeconds(120)))
                    )
                )
            }
        }
        return StopsViewModel(
            stopRepository = stopRepository,
            locationRepository = locationRepository,
            departureRepository = departureRepository,
            updateFlow = UpdateFlow(RefreshConfig(tickMillis = 1_000, reFetchMillis = 10_000)),
            now = { base.plusNanos(testScheduler.currentTime * 1_000_000) },
        )
    }

    private fun StopsScreenState.firstCountdown() = departures[platform.id]!![route]!!.first().countdown

    private fun TestScope.subscribe(viewModel: StopsViewModel) =
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.screenStateStream.collect {}
        }

    @Test
    fun countdownAndSecondsFromLastFetchTickEverySecond() = runTest(dispatcher) {
        val viewModel = createViewModel()
        subscribe(viewModel)
        runCurrent()

        viewModel.screenStateStream.value.let { state ->
            assertNotNull(state.closestStops)
            assertFalse(state.loadingMetro)
            assertFalse(state.loadingOther)
            assertEquals(120.0, state.firstCountdown(), 0.0)
            assertEquals(0.0, state.secondsFromLastFetch, 0.0)
        }

        advanceTimeBy(3_000)
        runCurrent()

        viewModel.screenStateStream.value.let { state ->
            assertEquals(117.0, state.firstCountdown(), 0.0)
            assertEquals(3.0, state.secondsFromLastFetch, 0.0)
        }
        assertEquals(1, fetchCount)
    }

    @Test
    fun refetchResetsSecondsFromLastFetch() = runTest(dispatcher) {
        val viewModel = createViewModel()
        subscribe(viewModel)
        runCurrent()

        advanceTimeBy(9_000)
        runCurrent()
        assertEquals(9.0, viewModel.screenStateStream.value.secondsFromLastFetch, 0.0)

        advanceTimeBy(1_000)
        runCurrent()
        assertEquals(2, fetchCount)
        assertEquals(0.0, viewModel.screenStateStream.value.secondsFromLastFetch, 0.0)
        // Countdown is still relative to the clock, not to the fetch
        assertEquals(110.0, viewModel.screenStateStream.value.firstCountdown(), 0.0)

        advanceTimeBy(4_000)
        runCurrent()
        assertEquals(4.0, viewModel.screenStateStream.value.secondsFromLastFetch, 0.0)
    }

    @Test
    fun workStopsWithoutSubscribersAndResumesWithImmediateRefetch() = runTest(dispatcher) {
        val viewModel = createViewModel()
        val subscription = subscribe(viewModel)
        runCurrent()
        assertTrue(locationSubscribed)
        assertEquals(1, fetchCount)

        subscription.cancel()
        // Past the 5 s grace period and several refetch intervals
        advanceTimeBy(60_000)
        runCurrent()
        assertFalse(locationSubscribed)
        assertEquals(1, fetchCount)

        // Previous state is kept while the upstream restarts
        assertNotNull(viewModel.screenStateStream.value.closestStops)

        subscribe(viewModel)
        runCurrent()
        assertTrue(locationSubscribed)
        assertEquals(2, fetchCount)
        assertEquals(0.0, viewModel.screenStateStream.value.secondsFromLastFetch, 0.0)
        assertEquals(60.0, viewModel.screenStateStream.value.firstCountdown(), 0.0)
    }
}
