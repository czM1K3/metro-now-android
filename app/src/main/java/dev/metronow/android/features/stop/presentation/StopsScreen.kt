package dev.metronow.android.features.stop.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.metronow.android.R
import dev.metronow.android.core.presentation.CustomCard
import dev.metronow.android.core.presentation.CustomCardState
import dev.metronow.android.core.presentation.secondsToString
import dev.metronow.android.features.departure.presentation.DepartureItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun StopsScreen(
    viewModel: StopsViewModel = koinViewModel(),
    navigateToInfoTextsList: () -> Unit,
) {
    val screenState by viewModel.screenStateStream.collectAsStateWithLifecycle()
    StopsScreen(screenState, navigateToInfoTextsList)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StopsScreen(
    screenState: StopsScreenState,
    navigateToInfoTextsList: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (!screenState.loadingMetro) {
                        Text(
                            text = stringResource(
                                R.string.updated,
                                screenState.secondsFromLastFetch.secondsToString()
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = navigateToInfoTextsList) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Info texts",
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            if (screenState.closestStops == null) {
                LoadingState()
            } else {
                LoadedState(screenState)
            }
        }

    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoadedState(screenState: StopsScreenState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        val metroState = if (screenState.loadingMetro) {
            CustomCardState.Loading
        } else {
            CustomCardState.Showing
        }

        // Metro on top
        CustomCard(
            title = screenState.closestStops!!.closestMetroStop.name,
            isMetro = true,
            state = metroState,
        ) {
            screenState.closestStops.closestMetroStop.platforms.filter { it.isMetro }
                .forEach { platform ->
                    screenState.departures[platform.id]?.entries?.firstOrNull()
                        ?.let { departuresMap ->
                            DepartureItem(
                                routeDetail = departuresMap.key,
                                departures = departuresMap.value,
                            )
                        }
                }
        }

        // Others under the metro
        // Going over each non-metro platform on closest stop
        screenState.closestStops.closestStop.platforms.filter { !it.isMetro }.forEach { platform ->
            val currentPlatform = screenState.departures[platform.id]?.toList()
            val otherState = if (screenState.loadingOther) {
                CustomCardState.Loading
            } else if (currentPlatform == null) {
                // We can have platforms, where currently nothing is going to departure
                CustomCardState.Empty
            } else {
                CustomCardState.Showing
            }
            CustomCard(
                // Title is nullable because long distance busses and trains don't have platform
                // from PID
                title = if (platform.code.isNullOrEmpty()) null else "${screenState.closestStops.closestStop.name} ${platform.code}",
                state = otherState,
            ) {
                currentPlatform?.forEachIndexed { i, (route, departures) ->
                    // Add divider
                    if (i != 0) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            modifier = Modifier.alpha(0.3f),
                        )
                    }
                    // If it's long distance bus or train, it doesn't have platform code and thus
                    // everything would be grouped to same departure. So because of this we want
                    // to render them separately.
                    if (platform.code.isNullOrEmpty()) {
                        departures.forEach { departure ->
                            DepartureItem(
                                routeDetail = route,
                                departures = listOf(departure),
                            )
                        }
                    } else {
                        DepartureItem(
                            routeDetail = route,
                            departures = departures,
                        )
                    }
                }
            }
        }
    }
}
