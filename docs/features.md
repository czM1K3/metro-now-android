# Features

## Location (`features/location`)

`LocationRepository.locationFlow()` (implemented by `LocationRepositoryImpl`) returns a cold `Flow<Coordinates>` from the flavor-specific `LocationDataSource`. Both implementations have the same class name and signature (`LocationDataSource(context, LocationConfig)`), so `main` code compiles against either one.

`LocationConfig` (`intervalMillis`, `minDistanceMeters`, `highAccuracy`) is provided through Koin. The phone uses the defaults: 2 s, 30 m, high accuracy.

| Flavor | Implementation | Settings |
| --- | --- | --- |
| `gms` | `FusedLocationProviderClient` | `PRIORITY_HIGH_ACCURACY` when `highAccuracy`, otherwise `PRIORITY_BALANCED_POWER_ACCURACY`; interval and minimum distance from `LocationConfig`; waits for an accurate location when `highAccuracy` |
| `vanilla` | `LocationManager` | `GPS_PROVIDER` with the configured interval and distance (`highAccuracy` is ignored); also registers `NETWORK_PROVIDER` when GPS is disabled; closes the flow with an exception when permission is missing |

Both are `callbackFlow`s that unregister in `awaitClose`.

**Permissions.** `LocationPermission` (Accompanist) asks for `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`:

- When the user should see a rationale, it shows an `AlertDialog` with an OK button that asks again.
- Otherwise it launches the request once and shows `LocationNotAvailable`, which has a "Go to settings" button that opens the app's system settings page.

## Closest stops (`features/stop`)

The main screen is `StopsScreen` with `StopsViewModel`.

### How the closest stops are chosen

`StopRepository.getClosestStops(coordinates)` (implemented by `StopRepositoryImpl`):

1. Refreshes the local stop cache if needed (see [data.md](data.md#stop-cache-refresh)).
2. Runs two Room queries, each ordered by `abs(lat - userLat) + abs(lon - userLon)`. This is Manhattan distance in degrees. It is cheap and good enough within Prague, but it is not a real geographic distance.
   - `getClosestMetroStop`: the nearest stop where `hasMetro = 1`
   - `getClosestStop`: the nearest stop of any kind
3. Returns `ClosestStops(closestMetroStop, closestStop)`, or `null` if either lookup is empty.

`ClosestStops.getPlatformIds()` decides which platforms to request departures for:

- the **metro** platforms of the closest metro stop, and
- the **non-metro** platforms of the closest stop.

So when the user stands at a metro station, the screen shows its metro directions together with its tram and bus platforms.

### StopsViewModel data flow

`UpdateFlow` provides two infinite tickers built from `RefreshConfig`: `update` every `tickMillis` (phone: **1 s**) and `reFetch` every `reFetchMillis` (phone: **10 s**). Both emit immediately when collected.

All work hangs off one upstream `channelFlow` that is turned into `screenStateStream` with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ...)`. Location updates, refetching and ticking therefore run only while the screen collects the state (`collectAsStateWithLifecycle()`), plus a 5 s grace period for configuration changes. The ViewModel keeps the last closest stops, departures and screen state between subscriptions, so returning to the screen shows the previous data and refetches immediately.

```
locationFlow
     │ mapLatest
     ▼
stopRepository.getClosestStops ──► closestStops ──► screenState.closestStops
                                        │            (loadingMetro/loadingOther = true
                                        │             when the stop ID changed)
             combine(reFetch)           ▼
departureRepository.getDepartures(platformIds) ──► departures (+ updatedAt)
                                        │            loading flags = false
             combine(update)            ▼
departures.addCountDown(now) ──► screenState.departures
now - updatedAt              ──► screenState.secondsFromLastFetch ("Updated 5s ago")
```

`StopsScreenState` holds `closestStops`, `loadingMetro`, `loadingOther`, `departures` (with countdowns) and `secondsFromLastFetch`.

### Screen layout

- **Top bar:** "Updated X ago" plus an info button that opens the info texts list.
- **Spinner** until `closestStops` is non-null.
- **Metro card:** the closest metro stop's name and a "Metro" label. Each metro platform (one per direction) shows the first route group.
- **One card per non-metro platform** of the closest stop, titled `"<stop name> <platform code>"`. A card shows **Empty** when nothing is departing from that platform. Platforms without a `code` are long-distance buses and trains that PID doesn't number. They get no title, and each departure is drawn as its own row, because otherwise every route would merge into one row.

## Departures (`features/departure`)

### Fetching and grouping

`DepartureRepository.getDepartures(platformIds)` (implemented by `DepartureRepositoryImpl`) calls the `Departures` query, sorts the result by `routeDetail.name.toIntOrNull()`, and groups it into:

```kotlin
typealias DeparturesGrouped = Map<String /* platformId */, Map<RouteDetail?, List<Departure>>>
```

`RouteDetail` is a data class, so all departures of the same route with the same night/substitute flags share one group.

### Countdown

`DeparturesGrouped.addCountDown(now)` (`DepartureWithCountdownHelper.kt`) converts each group to `DepartureWithCountdown`:

- The API returns `departureTime.predicted` as an ISO date-time. It is parsed to `LocalDateTime` and treated as **Europe/Prague** time, while `now` is in the device's zone. This keeps countdowns correct on devices set to another time zone.
- Departures more than **20 s in the past** are dropped.
- Only the **first two** remaining departures per route are kept.

### `DepartureItem`

A row with a colored route badge (`RouteDetail.toColor()` in `presentation/RouteDetailColor.kt`), the headsign, and the countdown (`secondsToString()`). When there is a second departure, it shows "Also in X", and the second headsign appears in smaller text if it differs from the first.

## Info texts (`features/infotext`)

Info texts are PID service notices: disruptions, closures and similar.

- **List** (`InfoTextsListScreen` / `InfoTextsListViewModel`): calls `InfoTextRepository.getInfoTexts()` once on creation. That fetches from the API, **replaces** the Room table, and returns the list. Each card has a border in its priority color and shows the text on one line, cut off with an ellipsis.
- **Detail** (`InfoTextDetailScreen` / `InfoTextDetailVIewModel`): loads the text by ID **from Room only**, so it relies on the list having been fetched first. It shows the full text, the valid-from/valid-to dates formatted for the current language, the priority, and a PID logo loaded with Coil from a remote URL.
- **Language:** `InfoText.getCorrectText()` returns `textEn` (falling back to `textCz`) unless the device language is Czech. `AppLanguage.getCurrent()` reads the first locale in `LocaleListCompat.getDefault()`.
