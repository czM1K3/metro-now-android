package dev.metronow.android.wear.presentation.departures

import dev.metronow.android.features.departure.domain.DepartureWithCountdown
import dev.metronow.android.features.departure.domain.RouteDetail
import dev.metronow.android.features.stop.presentation.StopsScreenState

/** One entry of the watch departures list. [key] is unique and stable for the lazy list. */
internal sealed interface DepartureListItem {
    val key: String

    /** Section header. Metro sections also show the shared "Metro" label. */
    data class Header(override val key: String, val title: String, val isMetro: Boolean) : DepartureListItem

    data class Loading(override val key: String) : DepartureListItem

    data class Empty(override val key: String) : DepartureListItem

    data class Row(
        override val key: String,
        val routeDetail: RouteDetail?,
        val departure: DepartureWithCountdown,
    ) : DepartureListItem
}

/**
 * Flattens the screen state into list items. The selection logic matches the phone's
 * `StopsScreen`:
 * - metro section: the closest metro stop, then the first route group of each metro platform;
 * - one section per non-metro platform of the closest stop, titled "<stop name> <platform code>"
 *   (no header when the platform has no code), with one row per route group, or one row per
 *   departure for platforms without a code (long-distance buses and trains).
 *
 * Differences from the phone, because a watch row shows only one departure: route groups with no
 * departures left are skipped, and a section without any row shows [DepartureListItem.Empty].
 */
internal fun StopsScreenState.toDepartureListItems(): List<DepartureListItem> {
    val stops = closestStops ?: return emptyList()
    val items = mutableListOf<DepartureListItem>()

    // Metro on top
    items += DepartureListItem.Header(key = "metro-header", title = stops.closestMetroStop.name, isMetro = true)
    if (loadingMetro) {
        items += DepartureListItem.Loading(key = "metro-loading")
    } else {
        val rows = stops.closestMetroStop.platforms
            .filter { it.isMetro }
            .mapNotNull { platform ->
                val (route, platformDepartures) = departures[platform.id]?.entries?.firstOrNull()
                    ?: return@mapNotNull null
                platformDepartures.firstOrNull()?.let { first ->
                    DepartureListItem.Row(key = "metro-${platform.id}", routeDetail = route, departure = first)
                }
            }
        items += rows.ifEmpty { listOf(DepartureListItem.Empty(key = "metro-empty")) }
    }

    // Each non-metro platform of the closest stop
    stops.closestStop.platforms.filter { !it.isMetro }.forEach { platform ->
        val prefix = "platform-${platform.id}"
        val hasCode = !platform.code.isNullOrEmpty()
        if (hasCode) {
            items += DepartureListItem.Header(
                key = "$prefix-header",
                title = "${stops.closestStop.name} ${platform.code}",
                isMetro = false,
            )
        }
        val routeGroups = departures[platform.id]
        when {
            loadingOther -> items += DepartureListItem.Loading(key = "$prefix-loading")
            routeGroups == null -> items += DepartureListItem.Empty(key = "$prefix-empty")
            else -> {
                val rows = routeGroups.entries.flatMapIndexed { groupIndex, (route, groupDepartures) ->
                    if (hasCode) {
                        listOfNotNull(
                            groupDepartures.firstOrNull()?.let { first ->
                                DepartureListItem.Row(key = "$prefix-$groupIndex", routeDetail = route, departure = first)
                            }
                        )
                    } else {
                        // Without a platform code every route would merge into one row, so show
                        // each departure separately.
                        groupDepartures.mapIndexed { departureIndex, departure ->
                            DepartureListItem.Row(
                                key = "$prefix-$groupIndex-$departureIndex",
                                routeDetail = route,
                                departure = departure,
                            )
                        }
                    }
                }
                items += rows.ifEmpty { listOf(DepartureListItem.Empty(key = "$prefix-empty")) }
            }
        }
    }
    return items
}
