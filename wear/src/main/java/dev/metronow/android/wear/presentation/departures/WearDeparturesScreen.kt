package dev.metronow.android.wear.presentation.departures

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.timeTextCurvedText
import androidx.wear.compose.material3.timeTextSeparator
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import dev.metronow.android.core.presentation.secondsToString
import dev.metronow.android.features.departure.domain.DepartureWithCountdown
import dev.metronow.android.features.departure.domain.RouteDetail
import dev.metronow.android.features.departure.domain.VehicleType
import dev.metronow.android.features.stop.domain.ClosestStops
import dev.metronow.android.features.stop.domain.Platform
import dev.metronow.android.features.stop.domain.Stop
import dev.metronow.android.features.stop.presentation.StopsScreenState
import dev.metronow.android.features.stop.presentation.StopsViewModel
import dev.metronow.android.shared.R
import dev.metronow.android.wear.presentation.theme.WearAppTheme
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime

@Composable
fun WearDeparturesScreen(
    viewModel: StopsViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenStateStream.collectAsStateWithLifecycle()
    WearDeparturesContent(screenState)
}

@Composable
private fun WearDeparturesContent(screenState: StopsScreenState) {
    val listState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val updatedText = if (screenState.closestStops != null && !screenState.loadingMetro) {
        stringResource(R.string.updated, screenState.secondsFromLastFetch.secondsToString())
    } else {
        null
    }

    // Passing the list state lets ScreenScaffold drive the scroll indicator and TimeText;
    // TransformingLazyColumn handles rotary (crown/bezel) input itself.
    ScreenScaffold(
        scrollState = listState,
        timeText = {
            TimeText { time ->
                timeTextCurvedText(time)
                if (updatedText != null) {
                    timeTextSeparator()
                    timeTextCurvedText(updatedText)
                }
            }
        },
    ) { contentPadding ->
        if (screenState.closestStops == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val items = remember(screenState) { screenState.toDepartureListItems() }
            TransformingLazyColumn(
                state = listState,
                contentPadding = contentPadding,
            ) {
                items(items, key = { it.key }) { item ->
                    val itemModifier = Modifier.fillMaxWidth().transformedHeight(this, transformationSpec)
                    when (item) {
                        is DepartureListItem.Header -> SectionHeader(
                            item = item,
                            modifier = itemModifier,
                            transformation = SurfaceTransformation(transformationSpec),
                        )

                        is DepartureListItem.Loading -> Box(
                            modifier = itemModifier,
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        }

                        is DepartureListItem.Empty -> Text(
                            text = stringResource(R.string.empty),
                            modifier = itemModifier,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        is DepartureListItem.Row -> WearDepartureRow(
                            routeDetail = item.routeDetail,
                            departure = item.departure,
                            modifier = itemModifier,
                            transformation = SurfaceTransformation(transformationSpec),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    item: DepartureListItem.Header,
    modifier: Modifier = Modifier,
    transformation: SurfaceTransformation? = null,
) {
    ListHeader(modifier = modifier, transformation = transformation) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = item.title,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.isMetro) {
                Text(
                    text = stringResource(R.string.metro),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

// Previews

private fun previewState(): StopsScreenState {
    val now = LocalDateTime.now()
    val metroPlatform1 = Platform("U321Z101P", "Dejvická", 50.1004, 14.3938, isMetro = true, code = "M1")
    val metroPlatform2 = Platform("U321Z102P", "Dejvická", 50.1004, 14.3938, isMetro = true, code = "M2")
    val tramPlatform = Platform("U321Z1P", "Dejvická", 50.1007, 14.3927, isMetro = false, code = "A")
    val busPlatform = Platform("U321Z3P", "Dejvická", 50.1010, 14.3922, isMetro = false, code = "C")
    val stop = Stop(
        id = "U321",
        name = "Dejvická",
        latitude = 50.1009,
        longitude = 14.3932,
        hasMetro = true,
        platforms = listOf(metroPlatform1, metroPlatform2, tramPlatform, busPlatform),
    )
    val metroA = RouteDetail("A", isSubstitute = false, vehicleType = VehicleType.SUBWAY, isNight = false)
    val tram20 = RouteDetail("20", isSubstitute = false, vehicleType = VehicleType.TRAM, isNight = false)
    val tram26 = RouteDetail("26", isSubstitute = false, vehicleType = VehicleType.TRAM, isNight = false)

    fun departure(platform: Platform, route: RouteDetail, heading: String, seconds: Double) =
        DepartureWithCountdown(platform.id, route, heading, now.plusSeconds(seconds.toLong()), seconds)

    return StopsScreenState(
        closestStops = ClosestStops(closestMetroStop = stop, closestStop = stop),
        loadingMetro = false,
        loadingOther = false,
        departures = mapOf(
            metroPlatform1.id to mapOf(metroA to listOf(departure(metroPlatform1, metroA, "Depo Hostivař", 95.0))),
            metroPlatform2.id to mapOf(metroA to listOf(departure(metroPlatform2, metroA, "Nemocnice Motol", 210.0))),
            tramPlatform.id to mapOf(
                tram20 to listOf(departure(tramPlatform, tram20, "Sídliště Barrandov", 42.0)),
                tram26 to listOf(departure(tramPlatform, tram26, "Nádraží Hostivař", 380.0)),
            ),
        ),
        secondsFromLastFetch = 4.0,
    )
}

@WearPreviewDevices
@WearPreviewFontScales
@Composable
private fun WearDeparturesContentPreview() {
    WearAppTheme {
        WearDeparturesContent(previewState())
    }
}

@WearPreviewDevices
@Composable
private fun WearDeparturesLoadingPreview() {
    WearAppTheme {
        WearDeparturesContent(StopsScreenState())
    }
}
