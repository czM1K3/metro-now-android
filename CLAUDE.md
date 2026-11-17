# CLAUDE.md

metro now is an Android app (Kotlin, Jetpack Compose, Material 3) with a standalone Wear OS companion that shows live PID public transport departures for the metro station and the stop closest to the user in Prague. Full docs are in [docs/](docs/README.md).

## Commands

```bash
./gradlew :app:assembleGmsDebug        # main dev build
./gradlew :app:assembleVanillaDebug    # build without Google Play Services
./gradlew :wear:assembleDebug          # Wear OS app (gms only)
./gradlew :shared:testGmsDebugUnitTest # unit tests (StopsViewModelTest)
./gradlew :wear:testDebugUnitTest      # watch unit tests (DepartureListItemsTest)
./gradlew build                        # what CI runs: all variants + lint + unit tests
./gradlew :shared:downloadApolloSchema --endpoint='https://api.metro.madsoft.cz/graphql' --schema=shared/src/main/graphql/dev/metronow/android/schema.graphqls
```

Verify changes with at least `./gradlew :app:assembleGmsDebug :app:assembleVanillaDebug :wear:assembleDebug`. Both phone flavors **and** `:wear` must compile.

## Architecture in brief

- Modules: `:shared` (Android library: GraphQL, Room, DataStore, all data/domain code, `StopsViewModel`, Koin `sharedModules`, `locationBackend` flavors, shared strings and color mappings), `:app` (phone UI, depends on `:shared`) and `:wear` (standalone Wear OS app, departures screen only, depends on `:shared`, package `dev.metronow.android.wear`). `:shared` and `:app` use package `dev.metronow.android`, organized by feature: `features/{stop,departure,location,infotext}`, each with `data/{api,db,ds}`, `domain`, `presentation` and `di`. Cross-feature code is in `core/`. When moving code between modules, keep its package.
- **Koin** DI: each feature has a module in `di/`. New shared modules go in `sharedModules` (`shared/.../core/di/SharedModules.kt`); app-specific ones go in that app's module list (`appModule` for the phone, `App.kt` starts `sharedModules + appModule`; `wearModule` for the watch, started in `WearApp.kt`). `RefreshConfig` and `LocationConfig` are provided by each app, never by `sharedModules`.
- **Apollo Kotlin 4** GraphQL: operations are in `shared/src/main/graphql/dev/metronow/android/*.graphql` and generate `dev.metronow.android.<Name>Query`. The endpoint is hardcoded in `core/di/CoreModule.kt`.
- **Room** `MetroDatabase` (`metro.db`, version 1, destructive migration) caches stops, platforms and info texts. **DataStore** (`fit_datastore`) stores the last stop-fetch date. The stop list is refreshed when it is more than 30 days old or empty.
- **Navigation Compose** with type-safe `@Serializable` routes in the `Screens` sealed interface (`core/presentation/ui/Navigation.kt`).
- `StopsViewModel` is the core: location → closest stops (Room, Manhattan distance) → departures refetched every 10 s (`UpdateFlow.reFetch`) → countdowns recalculated every 1 s (`UpdateFlow.update`). Intervals come from `RefreshConfig`, location settings from `LocationConfig`. All work runs in one upstream shared with `stateIn(WhileSubscribed(5_000))`, so it stops when the UI stops.

## Rules and gotchas

- **Product flavors** (`locationBackend`): `gms` (FusedLocationProvider) and `vanilla` (LocationManager, version suffix `-nogms`). Flavor-specific code is in `shared/src/{gms,vanilla}/java/` (`:app` declares the same flavors). Today that is only `LocationDataSource`, which must have the same FQN and API in both. Never use GMS classes from `src/main`. `:wear` is **gms only** (`missingDimensionStrategy("locationBackend", "gms")`, no flavors of its own) and must not reference vanilla code.
- Generated Apollo types and Room `Db*` entities stay in the `data` layer. Convert them with `toDomain()` / `toDb()` extension functions in `*Mapper.kt`.
- `when` over generated Apollo enums must handle `UNKNOWN__`. Custom scalars (date-times) arrive as `Any` and are cast `as String`, then parsed with `ISO_DATE_TIME`.
- API times are Prague local time. Countdown code converts them with `ZoneId.of("Europe/Prague")`, so keep that conversion.
- If you add or change a Room entity, bump the `MetroDatabase` version (the destructive fallback rebuilds the database).
- User-visible strings go in **both** `res/values/strings.xml` and `res/values-cs/strings.xml`. Strings used by more than one app live in `:shared` res and are referenced via `dev.metronow.android.shared.R` (R classes are non-transitive; alias it, e.g. `SharedR`, when a file also needs the app's `R`).
- `:wear` uses the same `applicationId` as the phone (`dev.metronow.android`) and `versionCode = 1_000_000 + appVersionCode`. App versions live in `gradle/libs.versions.toml` (`appVersionCode`, `appVersionName`). Wear UI uses Wear Compose Material 3 (`androidx.wear.compose.material3`), never phone `androidx.compose.material3`.
- Screens: a public composable takes the ViewModel via `koinViewModel()` and calls a private stateless composable. The ViewModel exposes `screenStateStream: StateFlow`.
- Dependency versions belong in `gradle/libs.versions.toml`. Toolchain: AGP 9 with built-in Kotlin (don't apply `kotlin.android` in modules), Gradle 9.6, `jvmToolchain(17)`, compileSdk 37, core library desugaring (minSdk 24 on the phone).
- Don't rename `InfoTextDetailVIewModel` (typo) or `fit_datastore` casually. See `docs/development.md#known-quirks`.

## Docs

`docs/architecture.md`, `docs/features.md`, `docs/data.md`, `docs/building.md`, `docs/development.md`. Update them when you change behavior they describe.
