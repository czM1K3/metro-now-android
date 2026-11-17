# Architecture

It is a single-module (`:app`) Android app built with Kotlin, Jetpack Compose and Material 3. The code is grouped **by feature**, and each feature has a `data` / `domain` / `presentation` / `di` split.

## Package layout

All code lives under `app/src/main/java/dev/metronow/android/`.

```
dev.metronow.android
├── App.kt                      Application: starts Koin with all modules
├── MainActivity.kt             Single activity: edge-to-edge, AppTheme, Navigation()
├── core/
│   ├── data/
│   │   ├── db/MetroDatabase.kt Room database (stops, platforms, info texts)
│   │   ├── LanguageHelper.kt   AppLanguage enum (EN/CS) + date format per language
│   │   ├── RefreshConfig.kt    Tick and refetch intervals (phone: 1 s / 10 s)
│   │   └── UpdateFlow.kt       Ticker flows built from RefreshConfig
│   ├── di/CoreModule.kt        Database, ApolloClient, RefreshConfig, UpdateFlow singletons
│   └── presentation/
│       ├── CustomCard.kt       Card with Empty / Loading / Showing states
│       ├── SecondsHelper.kt    Double.secondsToString(): "1h 5m", "3m 20s", "-12s"
│       └── ui/
│           ├── Navigation.kt   Type-safe NavHost + Screens sealed interface
│           └── theme/          Material 3 colors, theme (dynamic color on 12+), typography
└── features/
    ├── departure/              Fetching and grouping departures, DepartureItem UI
    ├── infotext/               Service disruption notices (list + detail screens)
    ├── location/               Location flow + permission UI
    └── stop/                   Stop/platform cache, closest-stop lookup, main screen
```

Flavor-specific sources:

```
app/src/gms/java/.../features/location/data/LocationDataSource.kt      FusedLocationProviderClient
app/src/vanilla/java/.../features/location/data/LocationDataSource.kt  android.location.LocationManager
```

GraphQL operations and the schema live in `app/src/main/graphql/dev/metronow/android/`.

## Layers

Each feature follows the same pattern:

| Layer | Contents | Naming |
| --- | --- | --- |
| `data/api` | Apollo calls + mappers from generated query types to domain | `XRemoteDataSource`, `XMapper.kt` with `toDomain()` |
| `data/db` | Room entities, DAOs, local data sources, mappers | `DbX`, `XDao`, `XLocalDataSource`, `DbXMapper.kt` with `toDomain()` / `toDb()` |
| `data/ds` | DataStore preferences | `XDataSource` |
| `data` | Repository: combines remote and local sources. Repositories that `StopsViewModel` uses are interfaces with an `Impl` class, so tests can fake them | `XRepository` (+ `XRepositoryImpl`) |
| `domain` | Plain Kotlin data classes and enums used by UI and repositories. No Compose imports: UI mappings such as `RouteDetail.toColor()` live in `presentation` | `X` |
| `presentation` | Composable screens + ViewModels exposing `StateFlow` | `XScreen`, `XViewModel`, `XScreenState` |
| `di` | Koin module for the feature | `xModule` |

The rules:

- Generated Apollo types (`dev.metronow.android.*Query`, `dev.metronow.android.type.*`) and Room entities (`Db*`) never leave the `data` layer. Mappers convert them to domain types.
- Screens are split into a public composable that takes the ViewModel (`koinViewModel()`) and collects state with `collectAsStateWithLifecycle()`, and a private composable that only takes plain state. Keep this split, because it makes previews and tests possible.
- ViewModels expose a single `screenStateStream: StateFlow<...>` backed by a private `MutableStateFlow`. `StopsViewModel` builds it with `stateIn(WhileSubscribed(5_000))` so its timers and location updates stop when the UI does.

## Dependency injection (Koin)

`App.onCreate` starts Koin with these modules:

```kotlin
modules(coreModule, stopModule, locationModule, departureModule, infoTextModule)
```

| Module | Provides |
| --- | --- |
| `coreModule` | `MetroDatabase` (single), `ApolloClient` (single, endpoint hardcoded), `RefreshConfig` (single), `UpdateFlow` (single) |
| `stopModule` | `StopRemoteDataSource`, DAOs, `StopLocalDataSource`, `PlatformLocalDataSource`, `StopDataSource`, `StopRepository`, `StopsViewModel` |
| `locationModule` | `LocationDataSource` (flavor-specific), `LocationConfig`, `LocationRepository` |
| `departureModule` | `DepartureRemoteDataSource`, `DepartureRepository` |
| `infoTextModule` | `InfoTextRemoteDataSource`, `InfoTextDao`, `InfoTextLocalDataSource`, `InfoTextRepository`, both info text ViewModels |

The conventions are `factoryOf` for data sources, `singleOf` for repositories, `single { get<MetroDatabase>().xDao() }` for DAOs, and `viewModelOf` for ViewModels (`StopsViewModel` uses `viewModel { StopsViewModel(get(), get(), get(), get()) }` because its last parameter, the clock, has a default value). **A new module must be added to the list in `App.kt`.**

## Navigation

`core/presentation/ui/Navigation.kt` uses Navigation Compose with type-safe `@Serializable` routes:

```kotlin
sealed interface Screens {
    data object ClosestStop : Screens          // start destination
    data object InfoTexts : Screens
    data class InfoText(val id: String) : Screens
}
```

- `ClosestStop` is wrapped in `LocationPermission { ... }`. It shows `StopsScreen` once permission is granted, and `LocationNotAvailable` (a button that opens the app settings) when it isn't.
- `InfoText` reads its `id` in the ViewModel with `savedStateHandle.toRoute<Screens.InfoText>()`.
- Transitions: a slide-in from the left on forward navigation and a slide-out to the right on pop. There is no exit or pop-enter animation.

## Theming

`AppTheme` in `core/presentation/ui/theme/Theme.kt` uses dynamic color on Android 12+ and otherwise falls back to the generated Material 3 light/dark schemes in `Color.kt`. `Color.kt` also defines medium- and high-contrast schemes, but nothing uses them yet.

Transit-specific colors are not theme colors. They are extension functions in the features' `presentation` packages, so the domain stays free of Compose:

- `RouteDetail.toColor()` (`features/departure/presentation/RouteDetailColor.kt`) colors route badges. Night routes are black, substitute routes are red, metro A/B/C are green/yellow/red, and every other vehicle type has its own color.
- `InfoText.Priority.toColor()` (`features/infotext/presentation/PriorityColor.kt`) gives red, blue or green for HIGH, NORMAL or LOW priority.
