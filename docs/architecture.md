# Architecture

It is a Kotlin, Jetpack Compose and Material 3 Android app split into Gradle modules. The code is grouped **by feature**, and each feature has a `data` / `domain` / `presentation` / `di` split.

## Modules

```
settings.gradle.kts: include(":app", ":shared")

:shared  com.android.library      namespace dev.metronow.android.shared
         Apollo + GraphQL, Room, DataStore, all data/domain code, UpdateFlow,
         LanguageHelper, SecondsHelper, StopsViewModel, Koin sharedModules,
         locationBackend flavors (gms/vanilla), shared strings, color mappings
:app     com.android.application  namespace dev.metronow.android
         phone UI, flavors gms/vanilla, depends on :shared
```

Moving code between modules keeps its Kotlin package (`dev.metronow.android.core…`, `dev.metronow.android.features…`), so only the module changes. `:shared` exposes Koin, `lifecycle-viewmodel` and Compose `ui-graphics` as `api` dependencies; Apollo, Room, DataStore and Play Services location are internal to it.

## Package layout

Both modules use the package `dev.metronow.android`. Code lives in `shared/src/main/java/dev/metronow/android/` and `app/src/main/java/dev/metronow/android/`.

```
dev.metronow.android
├── App.kt                      [app] Application: starts Koin with sharedModules + appModule
├── MainActivity.kt             [app] Single activity: edge-to-edge, AppTheme, Navigation()
├── di/AppModule.kt             [app] RefreshConfig, LocationConfig, info text ViewModels
├── core/
│   ├── data/                   [shared]
│   │   ├── db/MetroDatabase.kt Room database (stops, platforms, info texts)
│   │   ├── LanguageHelper.kt   AppLanguage enum (EN/CS) + date format per language
│   │   ├── RefreshConfig.kt    Tick and refetch intervals (phone: 1 s / 10 s)
│   │   └── UpdateFlow.kt       Ticker flows built from RefreshConfig
│   ├── di/                     [shared]
│   │   ├── CoreModule.kt       Database, ApolloClient, UpdateFlow singletons
│   │   └── SharedModules.kt    sharedModules: the list every app passes to Koin
│   └── presentation/
│       ├── SecondsHelper.kt    [shared] Double.secondsToString(): "1h 5m", "3m 20s", "-12s"
│       ├── CustomCard.kt       [app] Card with Empty / Loading / Showing states
│       └── ui/                 [app]
│           ├── Navigation.kt   Type-safe NavHost + Screens sealed interface
│           └── theme/          Material 3 colors, theme (dynamic color on 12+), typography
└── features/
    ├── departure/              data/domain/di + RouteDetailColor [shared]; DepartureItem [app]
    ├── infotext/               data/domain/di + PriorityColor [shared]; screens + ViewModels [app]
    ├── location/               data/domain/di [shared]; permission UI [app]
    └── stop/                   data/domain/di + StopsViewModel [shared]; StopsScreen [app]
```

Flavor-specific sources:

```
shared/src/gms/java/.../features/location/data/LocationDataSource.kt      FusedLocationProviderClient
shared/src/vanilla/java/.../features/location/data/LocationDataSource.kt  android.location.LocationManager
```

`:app` declares the same `locationBackend` flavors, so Gradle matches `gms` → `gms` and `vanilla` → `vanilla`.

GraphQL operations and the schema live in `shared/src/main/graphql/dev/metronow/android/`.

**Resources.** Strings used by more than one app (`app_name`, `metro`, `also_in`, `empty`, `updated`, `rationale`, `no_location`, `go_to_settings`, `ok`) are in `shared/src/main/res/values{,-cs}/strings.xml`. R classes are non-transitive, so code that uses them imports `dev.metronow.android.shared.R` (with an alias such as `SharedR` when the file also needs the app's `R`).

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

`App.onCreate` starts Koin with the shared modules plus the phone's own module:

```kotlin
modules(sharedModules + appModule)
```

| Module | Where | Provides |
| --- | --- | --- |
| `coreModule` | `:shared` | `MetroDatabase` (single), `ApolloClient` (single, endpoint hardcoded), `UpdateFlow` (single, built from `RefreshConfig`) |
| `stopModule` | `:shared` | `StopRemoteDataSource`, DAOs, `StopLocalDataSource`, `PlatformLocalDataSource`, `StopDataSource`, `StopRepository`, `StopsViewModel` |
| `locationModule` | `:shared` | `LocationDataSource` (flavor-specific, needs `LocationConfig`), `LocationRepository` |
| `departureModule` | `:shared` | `DepartureRemoteDataSource`, `DepartureRepository` |
| `infoTextModule` | `:shared` | `InfoTextRemoteDataSource`, `InfoTextDao`, `InfoTextLocalDataSource`, `InfoTextRepository` |
| `appModule` | `:app` | `RefreshConfig()` and `LocationConfig()` with phone defaults, both info text ViewModels |

`sharedModules` (in `core/di/SharedModules.kt`) lists the five shared modules. It deliberately does **not** provide `RefreshConfig` or `LocationConfig`: every app supplies its own, tuned for the device.

The conventions are `factoryOf` for data sources, `singleOf` for repositories, `single { get<MetroDatabase>().xDao() }` for DAOs, and `viewModelOf` for ViewModels (`StopsViewModel` uses `viewModel { StopsViewModel(get(), get(), get(), get()) }` because its last parameter, the clock, has a default value). **New shared modules go in `sharedModules`; app-specific ones go in that app's module list.**

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
