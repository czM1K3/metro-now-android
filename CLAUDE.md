# CLAUDE.md

metro now is an Android app (Kotlin, Jetpack Compose, Material 3) that shows live PID public transport departures for the metro station and the stop closest to the user in Prague. Full docs are in [docs/](docs/README.md).

## Commands

```bash
./gradlew :app:assembleGmsDebug        # main dev build
./gradlew :app:assembleVanillaDebug    # build without Google Play Services
./gradlew :app:testGmsDebugUnitTest    # unit tests (StopsViewModelTest + templates)
./gradlew build                        # what CI runs: all variants + lint + unit tests
./gradlew :app:downloadApolloSchema --endpoint='https://api.metro.madsoft.cz/graphql' --schema=app/src/main/graphql/dev/metronow/android/schema.graphqls
```

Verify changes with at least `./gradlew :app:assembleGmsDebug :app:assembleVanillaDebug`. Both flavors must compile.

## Architecture in brief

- Single `:app` module, package `dev.metronow.android`, organized by feature: `features/{stop,departure,location,infotext}`, each with `data/{api,db,ds}`, `domain`, `presentation` and `di`. Shared code is in `core/`.
- **Koin** DI: each feature has a module in `di/`. New modules **must** be added to `startKoin { modules(...) }` in `App.kt`.
- **Apollo Kotlin 4** GraphQL: operations are in `app/src/main/graphql/dev/metronow/android/*.graphql` and generate `dev.metronow.android.<Name>Query`. The endpoint is hardcoded in `core/di/CoreModule.kt`.
- **Room** `MetroDatabase` (`metro.db`, version 1, destructive migration) caches stops, platforms and info texts. **DataStore** (`fit_datastore`) stores the last stop-fetch date. The stop list is refreshed when it is more than 30 days old or empty.
- **Navigation Compose** with type-safe `@Serializable` routes in the `Screens` sealed interface (`core/presentation/ui/Navigation.kt`).
- `StopsViewModel` is the core: location → closest stops (Room, Manhattan distance) → departures refetched every 10 s (`UpdateFlow.reFetch`) → countdowns recalculated every 1 s (`UpdateFlow.update`). Intervals come from `RefreshConfig`, location settings from `LocationConfig`. All work runs in one upstream shared with `stateIn(WhileSubscribed(5_000))`, so it stops when the UI stops.

## Rules and gotchas

- **Product flavors** (`locationBackend`): `gms` (FusedLocationProvider) and `vanilla` (LocationManager, version suffix `-nogms`). Flavor-specific code is in `app/src/{gms,vanilla}/java/`. Today that is only `LocationDataSource`, which must have the same FQN and API in both. Never use GMS classes from `src/main`.
- Generated Apollo types and Room `Db*` entities stay in the `data` layer. Convert them with `toDomain()` / `toDb()` extension functions in `*Mapper.kt`.
- `when` over generated Apollo enums must handle `UNKNOWN__`. Custom scalars (date-times) arrive as `Any` and are cast `as String`, then parsed with `ISO_DATE_TIME`.
- API times are Prague local time. Countdown code converts them with `ZoneId.of("Europe/Prague")`, so keep that conversion.
- If you add or change a Room entity, bump the `MetroDatabase` version (the destructive fallback rebuilds the database).
- User-visible strings go in **both** `res/values/strings.xml` and `res/values-cs/strings.xml`.
- Screens: a public composable takes the ViewModel via `koinViewModel()` and calls a private stateless composable. The ViewModel exposes `screenStateStream: StateFlow`.
- Dependency versions belong in `gradle/libs.versions.toml`. Bytecode targets Java 8 with core library desugaring (minSdk 24).
- Don't rename `InfoTextDetailVIewModel` (typo) or `fit_datastore` casually. See `docs/development.md#known-quirks`.

## Docs

`docs/architecture.md`, `docs/features.md`, `docs/data.md`, `docs/building.md`, `docs/development.md`. Update them when you change behavior they describe.
