package dev.metronow.android.features.stop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.metronow.android.core.data.UpdateFlow
import dev.metronow.android.features.departure.data.DepartureRepository
import dev.metronow.android.features.departure.data.addCountDown
import dev.metronow.android.features.departure.domain.DeparturesGrouped
import dev.metronow.android.features.departure.domain.DeparturesWithCountdownGrouped
import dev.metronow.android.features.location.data.LocationRepository
import dev.metronow.android.features.stop.data.StopRepository
import dev.metronow.android.features.stop.domain.ClosestStops
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Location → closest stops → departures → countdowns.
 *
 * All work (location updates, refetching, ticking) runs only while [screenStateStream] has a
 * subscriber, plus 5 s grace for configuration changes. The last state is kept between
 * subscriptions, so returning to the screen shows the previous data until the immediate refetch
 * finishes.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StopsViewModel(
    private val stopRepository: StopRepository,
    private val locationRepository: LocationRepository,
    private val departureRepository: DepartureRepository,
    private val updateFlow: UpdateFlow,
    private val now: () -> LocalDateTime = LocalDateTime::now,
) : ViewModel() {
    // Internal state, kept across subscriptions
    private val closestStops = MutableStateFlow<ClosestStops?>(null)
    private val departures = MutableStateFlow(StopsViewModelDepartures())
    private val state = MutableStateFlow(StopsScreenState())

    val screenStateStream: StateFlow<StopsScreenState> = channelFlow {
        // When location changes, get closest stops
        launch {
            locationRepository.locationFlow()
                .mapLatest { coordinates -> stopRepository.getClosestStops(coordinates) }
                .collect { stopPair ->
                    val metroChanged = closestStops.value?.closestMetroStop?.id != stopPair?.closestMetroStop?.id
                    val otherChanged = closestStops.value?.closestStop?.id != stopPair?.closestStop?.id
                    closestStops.value = stopPair
                    state.update {
                        it.copy(
                            closestStops = stopPair,
                            loadingMetro = metroChanged,
                            loadingOther = otherChanged,
                        )
                    }
                }
        }

        // When closest stops change or refetch timer clicks, refetch latest departures for those stops
        launch {
            closestStops
                .filterNotNull()
                .combine(updateFlow.reFetch) { stops, _ ->
                    departureRepository.getDepartures(stops.getPlatformIds())
                }
                .collect { fetched ->
                    departures.value = StopsViewModelDepartures(fetched, now())
                    state.update { it.copy(loadingMetro = false, loadingOther = false) }
                }
        }

        // Every tick or when departures change, recalculate countdowns and time since last fetch
        launch {
            departures
                .combine(updateFlow.update) { current, _ -> current }
                .collect { current ->
                    val currentTime = now()
                    state.update {
                        it.copy(
                            departures = current.departures.addCountDown(currentTime),
                            secondsFromLastFetch = current.updatedAt
                                ?.let { updatedAt -> ChronoUnit.SECONDS.between(updatedAt, currentTime).toDouble() }
                                ?: it.secondsFromLastFetch,
                        )
                    }
                }
        }

        state.collect { send(it) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), state.value)
}

data class StopsViewModelDepartures(
    val departures: DeparturesGrouped = emptyMap(),
    val updatedAt: LocalDateTime? = null,
)

data class StopsScreenState(
    val closestStops: ClosestStops? = null,
    val loadingMetro: Boolean = true,
    val loadingOther: Boolean = true,
    val departures: DeparturesWithCountdownGrouped = mutableMapOf(),
    val secondsFromLastFetch: Double = 0.0,
)
