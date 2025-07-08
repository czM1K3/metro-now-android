package dev.metronow.android.features.stop.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.metronow.android.core.data.UpdateFlow
import dev.metronow.android.features.departure.data.DepartureRepository
import dev.metronow.android.features.departure.data.addCountDown
import dev.metronow.android.features.departure.domain.DeparturesGrouped
import dev.metronow.android.features.departure.domain.DeparturesWithCountdownGrouped
import dev.metronow.android.features.location.data.LocationRepository
import dev.metronow.android.features.location.domain.Coordinates
import dev.metronow.android.features.stop.data.StopRepository
import dev.metronow.android.features.stop.domain.ClosestStops
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class StopsViewModel(
    private val stopRepository: StopRepository,
    private val locationRepository: LocationRepository,
    private val departureRepository: DepartureRepository,
    private val updateFlow: UpdateFlow = UpdateFlow(),
) : ViewModel() {
    // Internal state
    private val _currentLocation = MutableStateFlow<Coordinates?>(null)
    private val _closestStops = MutableStateFlow<ClosestStops?>(null)
    private val _departures = MutableStateFlow<StopsViewModelDepartures>(StopsViewModelDepartures())

    // Public state
    private val _screenStateStream = MutableStateFlow(StopsScreenState())
    val screenStateStream get() = _screenStateStream.asStateFlow()

    init {
        // Updating location
        viewModelScope.launch {
            locationRepository.locationFlow().collectLatest { location ->
                _currentLocation.value = location
            }
        }

        // When location changes, get closest stops
        viewModelScope.launch {
            _currentLocation
                .filterNotNull()
                .flatMapLatest { coordinates ->
                    flowOf(stopRepository.getClosestStops(coordinates))
                }
                .collectLatest { stopPair ->
                    val metroChanged = _closestStops.value?.closestMetroStop?.id != stopPair?.closestMetroStop?.id
                    val otherChanged = _closestStops.value?.closestStop?.id != stopPair?.closestStop?.id
                    _closestStops.value = stopPair
                    _screenStateStream.update { state ->
                        state.copy(
                            closestStops = stopPair,
                            loadingMetro = metroChanged,
                            loadingOther = otherChanged,
                        )
                    }
                }
        }

        // When closest stops change or refetch timer clicks, refetch latest departures for those stops
        viewModelScope.launch {
            _closestStops
                .filterNotNull()
                .combine(updateFlow.reFetch) { stops, _ ->
                    departureRepository.getDepartures(stops.getPlatformIds())
                }.collectLatest { departures ->
                    _departures.value = StopsViewModelDepartures(
                        departures,
                        LocalDateTime.now()
                    )
                    _screenStateStream.update { state ->
                        state.copy(
                            loadingOther = false,
                            loadingMetro = false,
                        )
                    }
                }
        }

        // Every second update time since last fetch
        viewModelScope.launch {
            updateFlow.update.collectLatest {
                if (_departures.value.updatedAt != null) {
                    val currentTime = LocalDateTime.now()
                    _screenStateStream.update { state ->
                        state.copy(
                            secondsFromLastFetch = ChronoUnit.SECONDS.between(_departures.value.updatedAt, currentTime).toDouble()
                        )
                    }
                }
            }
        }

        // Every second or when departures changes, recalculates seconds to departure
        viewModelScope.launch {
            _departures
                .combine(updateFlow.update) { departures, _ -> departures }.collectLatest { newState ->
                    val currentTime = LocalDateTime.now()
                    _screenStateStream.update { state ->
                        state.copy(
                            departures = newState.departures.addCountDown(currentTime),
                        )
                    }
                }
        }
    }
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
