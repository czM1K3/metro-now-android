# Plan: Wear OS support

These are instructions for Claude. Work through the steps in order. Each **milestone** ends in a state that builds, passes CI and can be committed, so don't move to the next milestone until its verification passes. Read `CLAUDE.md` and `docs/architecture.md` first. Their rules still apply unless this plan overrides them.

## Decisions (already made by the owner; don't reopen them)

| Topic | Decision |
| --- | --- |
| App type | **Standalone** Wear OS app. The watch calls the GraphQL API directly and gets its own location. No Data Layer / phone↔watch sync. |
| Location backend on the watch | **`gms` only.** The watch module doesn't build a vanilla variant. |
| v1 scope | **Departures screen only** (closest metro stop and closest stop). No info texts, tiles, complications or ambient mode on the watch in v1. |
| Distribution | **Same Play listing** as the phone app, so the same `applicationId` (`dev.metronow.android`) on the Wear OS form factor track. |

## Target module layout

```
settings.gradle.kts: include(":app", ":shared", ":wear")

:shared  com.android.library   namespace dev.metronow.android.shared
         Apollo + GraphQL, Room, DataStore, all data/domain code,
         UpdateFlow, LanguageHelper, SecondsHelper, StopsViewModel,
         Koin modules (sharedModules), locationBackend flavors (gms/vanilla),
         strings used by both apps, route color mapping
:app     phone (unchanged behavior)  depends on :shared, flavors gms/vanilla
:wear    com.android.application     namespace dev.metronow.android.wear
         applicationId dev.metronow.android, gms only, depends on :shared
```

**Keep the Kotlin package names** of everything you move (`dev.metronow.android.core…`, `dev.metronow.android.features…`). Only the module changes, so the diffs stay mostly file moves. Use `git mv` so history is kept.

---

## Milestone 1: Prep refactors inside `:app` (no visible behavior change)

### 1.1 Remove Compose `Color` from the domain layer

`features/departure/domain/Departure.kt` (`RouteDetail.getColor()`) and `features/infotext/domain/InfoText.kt` (`InfoText.Priority.toColor()`) import `androidx.compose.ui.graphics.Color`.

- Move `getColor()` to an extension `fun RouteDetail.toColor(): Color` in `features/departure/presentation/RouteDetailColor.kt`, and move `Priority.toColor()` to `features/infotext/presentation/PriorityColor.kt`. Keep the exact same color values.
- Update the call sites (`DepartureItem.kt` and the info text screens).
- After this, nothing under `features/*/domain` or `features/*/data` imports Compose.

### 1.2 Make timing and location settings configurable

- Add `core/data/RefreshConfig.kt`:
  ```kotlin
  data class RefreshConfig(
      val tickMillis: Long = 1_000,
      val reFetchMillis: Long = 10_000,
  )
  ```
  `UpdateFlow` takes a `RefreshConfig` instead of hardcoded delays. Read `UpdateFlow.kt` first and keep its current semantics, including whether it emits immediately.
- Add `features/location/domain/LocationConfig.kt`:
  ```kotlin
  data class LocationConfig(
      val intervalMillis: Long = 2_000,
      val minDistanceMeters: Float = 30f,
      val highAccuracy: Boolean = true,
  )
  ```
  Both flavor `LocationDataSource`s take it as a constructor parameter. They must keep the same FQN and API.
  - gms: `highAccuracy` → `Priority.PRIORITY_HIGH_ACCURACY`, otherwise `PRIORITY_BALANCED_POWER_ACCURACY`. Keep `setWaitForAccurateLocation(highAccuracy)`.
  - vanilla: use the interval and distance; `highAccuracy` can be ignored.
- Provide both with `single { RefreshConfig() }` / `single { LocationConfig() }` in `coreModule` / `locationModule` for now. In Milestone 2 they move to the per-app modules.

### 1.3 Make `StopsViewModel` lifecycle-aware

Today `StopsViewModel.init` launches every collector in `viewModelScope`, so location updates, the 10 s refetch and the 1 s tick keep running while the app is in the background. On a watch that drains the battery.

- Rewrite it so that all work hangs off one upstream flow that is shared with `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StopsScreenState())`.
- Keep the public API: `val screenStateStream: StateFlow<StopsScreenState>` and the same `StopsScreenState` fields.
- Keep the behavior:
  - closest stops are recomputed on location change
  - `loadingMetro`/`loadingOther` become true only when the respective stop ID changed and false after a fetch
  - departures are refetched on stop change and every refetch tick
  - countdowns and `secondsFromLastFetch` are recomputed every tick
- The screens already use `collectAsStateWithLifecycle()`, so collection stops when the UI is stopped.
- Add JVM unit tests in `app/src/test` (they move with the code in Milestone 2) for the countdown and "seconds since last fetch" behavior. Use fake repositories and a `RefreshConfig` with short intervals, or inject an `UpdateFlow` with finite flows. Add `kotlinx-coroutines-test` to the version catalog if needed.

### Verification

```bash
./gradlew :app:assembleGmsDebug :app:assembleVanillaDebug :app:testGmsDebugUnitTest
./gradlew build
```

If a device or emulator is available, install `gmsDebug` and confirm that departures load, the countdowns tick and the "Updated Xs ago" text still works.

---

## Milestone 2: Extract the `:shared` library module

### 2.1 Gradle setup

- `settings.gradle.kts`: `include(":app", ":shared")`.
- Root `build.gradle.kts`: add `alias(libs.plugins.android.library) apply false`, plus the apollo, ksp and serialization plugins with `apply false` if Gradle needs them there.
- Version catalog: add `android-library = { id = "com.android.library", version.ref = "agp" }`.
- `shared/build.gradle.kts`:
  - plugins: android.library, kotlin.android, kotlin.compose (only if the color mapping needs it; see 2.2), kotlin.serialization, apollo.client, ksp
  - `apollo { service("service") { packageName.set("dev.metronow.android") } }`, so generated classes keep their current FQN
  - `namespace = "dev.metronow.android.shared"`, `compileSdk = 36`, `minSdk = 24`
  - the same `compileOptions`, desugaring and `jvmToolchain(8)` as `:app`
  - `flavorDimensions += "locationBackend"` with `gms` and `vanilla`, and `"gmsImplementation"(libs.gms.play.services.location)`
  - dependencies: apollo runtime, room + ksp compiler, datastore, koin-android, koin-androidx-compose (only if needed), lifecycle viewmodel, serialization json, compose ui-graphics (for the color mapping), desugaring
  - expose what consumers need through `api(...)`: Koin, lifecycle-viewmodel, and the domain types' dependencies
- `:app` keeps its `locationBackend` flavors, so Gradle matches `gms`→`gms` and `vanilla`→`vanilla`. Move the Apollo, Room/KSP, DataStore and play-services-location dependencies out of `:app` when they are no longer used there directly.

### 2.2 What moves into `shared/src/main/java/dev/metronow/android/…`

| Moves to `:shared` | Stays in `:app` |
| --- | --- |
| `core/data/**` (MetroDatabase, LanguageHelper, UpdateFlow, RefreshConfig) | `App.kt`, `MainActivity.kt` |
| `core/di/CoreModule.kt` | `core/presentation/CustomCard.kt` |
| `core/presentation/SecondsHelper.kt` | `core/presentation/ui/**` (Navigation, theme) |
| `features/*/data/**`, `features/*/domain/**` | `features/stop/presentation/StopsScreen.kt` |
| `features/stop/presentation/StopsViewModel.kt` (+ `StopsScreenState`) | `features/departure/presentation/DepartureItem.kt` |
| `features/departure/presentation/RouteDetailColor.kt` | `features/infotext/presentation/**` (screens **and** both info text ViewModels) |
| `features/infotext/presentation/PriorityColor.kt` (simplest to keep it next to the other mapper) | `features/location/presentation/**` |
| the `di/` modules for stop, location, departure and info text (data parts only) | |
| `app/src/main/graphql/**` → `shared/src/main/graphql/**` | |
| `app/src/{gms,vanilla}/java/**` → `shared/src/{gms,vanilla}/java/**` | |
| unit tests for the moved code → `shared/src/test` | |

DI:
- `infoTextModule` currently registers the two info text ViewModels with `viewModelOf`. Split it: the data bindings stay in `:shared`, and the ViewModel bindings move to a new `appModule` in `:app` (`app/src/main/java/dev/metronow/android/di/AppModule.kt`).
- `StopsViewModel` stays registered in `stopModule` in `:shared`.
- In `:shared`, add `val sharedModules = listOf(coreModule, stopModule, locationModule, departureModule, infoTextModule)`, but **without** `RefreshConfig`/`LocationConfig`. Each app provides those itself.
- `appModule` provides `RefreshConfig()` and `LocationConfig()` with the phone defaults. `App.kt` becomes `modules(sharedModules + appModule)`.

### 2.3 Resources

- Strings that both apps will use go to `shared/src/main/res/values/strings.xml` **and** `values-cs/strings.xml`: `app_name`, `metro`, `also_in`, `empty`, `updated`, `rationale`, `no_location`, `go_to_settings`, `ok`. Keep `translatable="false"` where it is set today. Everything else stays in `:app`.
- AGP 8 uses non-transitive R classes, so call sites that use a moved string must use `dev.metronow.android.shared.R`. Use import aliases where a file needs both R classes.
- Delete moved strings from `:app` so there are no duplicates.

### 2.4 Things that must not change

- The database name `metro.db`, the `MetroDatabase` version, and the DataStore name `fit_datastore` stay the same.
- The Apollo-generated FQNs (`dev.metronow.android.*Query`) stay the same.
- The `InfoTextDetailVIewModel` class name stays the same.

### Verification

```bash
./gradlew :app:assembleGmsDebug :app:assembleVanillaDebug
./gradlew :shared:testGmsDebugUnitTest
./gradlew build
```

The phone app must behave exactly as before. Check this on a device or emulator if one is available.

### Docs to update in this milestone

- `CLAUDE.md`:
  - replace "Single `:app` module" with the module layout
  - change the schema command to `./gradlew :shared:downloadApolloSchema --endpoint='https://api.metro.madsoft.cz/graphql' --schema=shared/src/main/graphql/dev/metronow/android/schema.graphqls`
  - change the Koin rule to "new shared modules go in `sharedModules`; app-specific ones go in that app's module list"
  - flavor-specific code now lives in `shared/src/{gms,vanilla}`
  - shared strings are in `:shared` res
- `docs/architecture.md` (package and module layout, DI table), `docs/building.md` (modules, variants, schema command), `docs/development.md` ("Adding a feature" steps, test locations).

---

## Milestone 3: The `:wear` app (departures only)

### 3.1 Gradle and manifest

- Version catalog: add Wear Compose. Use the **latest stable** versions; check Google Maven or the release notes rather than guessing.
  - `androidx.wear.compose:compose-material3`
  - `androidx.wear.compose:compose-foundation`
  - `androidx.wear.compose:compose-navigation`
  - `androidx.wear.compose:compose-ui-tooling` (for `@WearPreviewDevices` etc.)
  - `androidx.wear:wear` only if something needs it

  If Wear Compose needs a newer Compose BOM, AGP, Kotlin or KSP, bump them in the catalog and re-verify `:app` too. Note that KSP is currently `2.1.20-2.0.0` while Kotlin is `2.2.0`.
- `settings.gradle.kts`: add `":wear"`.
- `wear/build.gradle.kts`:
  - plugins: android.application, kotlin.android, kotlin.compose, kotlin.serialization (only if routes need it)
  - `namespace = "dev.metronow.android.wear"`, `applicationId = "dev.metronow.android"`, debug `applicationIdSuffix = ".debug"`
  - `minSdk = 30`, `compileSdk = targetSdk = 36`
  - Java 8 and desugaring, same as `:app`
  - `missingDimensionStrategy("locationBackend", "gms")` in `defaultConfig`. Don't declare any flavors in `:wear`.
  - Version code: `versionCode` must never collide with the phone's. Use a fixed offset, e.g. `versionCode = 1_000_000 + <phone versionCode>`. Write it down in `docs/building.md`. `versionName` matches the phone's.
- `wear/src/main/AndroidManifest.xml`:
  - `<uses-feature android:name="android.hardware.type.watch" />`
  - INTERNET, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION
  - `<meta-data android:name="com.google.android.wearable.standalone" android:value="true" />` inside `<application>`
  - `android:name=".WearApp"`, one launcher activity `.MainActivity`, theme `@android:style/Theme.DeviceDefault`, or a minimal splash theme
- Launcher icons: copy the phone's mipmaps into `wear/src/main/res/` (round icons matter on watches).

### 3.2 Code (`wear/src/main/java/dev/metronow/android/wear/…`)

- `WearApp.kt`: `startKoin { androidContext(this); modules(sharedModules + wearModule) }`.
- `di/WearModule.kt` provides the watch-tuned settings:
  - `RefreshConfig(tickMillis = 1_000, reFetchMillis = 30_000)`
  - `LocationConfig(intervalMillis = 10_000, minDistanceMeters = 50f, highAccuracy = false)`
- `MainActivity.kt`: `setContent { WearAppTheme { WearNavigation() } }`.
- `presentation/theme/WearAppTheme.kt`: Wear Compose Material 3 `MaterialTheme`. Use dynamic color where the Wear M3 API supports it, otherwise a dark scheme based on the phone's brand colors.
- `presentation/WearNavigation.kt`: `AppScaffold { SwipeDismissableNavHost(...) }`. In v1 the only destination is the departures screen, wrapped in the permission gate. Use type-safe routes if the chosen wear-compose-navigation version supports them; otherwise use a single string route.
- `presentation/permission/WearLocationPermission.kt`:
  - use an activity-result `RequestMultiplePermissions` launcher (or Accompanist, which is already in the catalog)
  - if permission is denied, show a Wear-styled screen with the `rationale` text and a button
  - if a rationale should be shown, the button asks again; otherwise it opens the app's settings page with `Settings.ACTION_APPLICATION_DETAILS_SETTINGS`
  - same decision logic as the phone's `LocationPermission`, but no Material 3 `AlertDialog`
- `presentation/departures/WearDeparturesScreen.kt`:
  - the public composable takes `koinViewModel<StopsViewModel>()`, collects `screenStateStream` with `collectAsStateWithLifecycle()`, and calls a private stateless `WearDeparturesContent(state)`
  - `ScreenScaffold` + `TransformingLazyColumn` (or `ScalingLazyColumn` if that is what the chosen version recommends). Rotary-crown scrolling must work, so pass the scroll state to `ScreenScaffold`.
  - `TimeText` at the top, using the shared `updated` string with `secondsFromLastFetch.secondsToString()`
  - while `closestStops == null`, show a centered `CircularProgressIndicator`
  - a section header with the closest metro stop name and "Metro", then one row per metro platform's first route group, matching the phone's `StopsScreen` logic. Read `StopsScreen.kt` and copy the selection logic, not the layout.
  - for each non-metro platform of the closest stop, a section header `"<stop name> <platform code>"` (no header when `code` is null), then one row per route group. For platforms without a code, show one row per departure, as the phone does.
  - a per-section loading state while `loadingMetro`/`loadingOther` is true, and the shared `empty` string when a platform has no departures
- `presentation/departures/WearDepartureRow.kt`: a compact row with the route badge (`RouteDetail.toColor()` background, white text), the headsign on **one line with ellipsis**, and the first countdown right-aligned. Skip the phone's "Also in" line and the second headsign. It must fit the smallest round screen without clipping, so use the list's edge padding.
- Previews: `@WearPreviewDevices` and `@WearPreviewFontScales` on the stateless content with fake `StopsScreenState` data.

### 3.3 Wear-specific strings

Any new user-visible strings go in `wear/src/main/res/values/strings.xml` **and** `values-cs/strings.xml`. Prefer reusing the shared ones.

### Verification

```bash
./gradlew :wear:assembleDebug :wear:lintDebug
./gradlew :app:assembleGmsDebug :app:assembleVanillaDebug
./gradlew build
```

If the run skill or an emulator is available, run the watch app on a **small round** and a **large round** Wear OS emulator. Grant location and set a Prague location (for example 50.0755, 14.4378), then check that:

- the permission flow works, including the denied path to the settings page
- rows don't clip at the screen edges
- the crown/rotary scroll works
- countdowns tick and the departures refresh

Report anything you couldn't verify.

### Docs to update in this milestone

- `CLAUDE.md`: add `./gradlew :wear:assembleDebug` to the commands; the rule that `:wear` is gms-only and must not reference vanilla code; the rule that both `:app` flavors **and** `:wear` must compile.
- `docs/architecture.md`: the `:wear` module, its DI module, its navigation.
- `docs/building.md`: wear variants, `minSdk 30`, version code offset, same `applicationId`, how to install on a watch or emulator, Play Console Wear OS track (same listing; watch screenshots needed; Wear OS quality guidelines).
- `docs/features.md`: a "Wear OS" section describing the departures screen and the power-tuned refresh and location settings.

---

## Out of scope for v1 (don't implement; listed so nobody adds them by accident)

- Info texts on the watch
- Tiles, complications, ambient/always-on mode, ongoing activities
- Data Layer / phone↔watch communication, "open on phone", prompting watch install from the phone
- A vanilla (non-GMS) watch build

If one of these later gets approved, the preferred direction is a tile or complication that shows departures for the **last stop the app displayed** (cached in DataStore), so it doesn't need `ACCESS_BACKGROUND_LOCATION`, with countdowns done through ProtoLayout dynamic time or `TimeDifferenceComplicationText`.

## Risks to watch while executing

- **First-launch stop cache download** over a Bluetooth proxy may be slow. If it is, note it in the report; don't redesign the API usage in v1.
- **API errors are swallowed** (they show up as empty lists). Keep that behavior in v1, but don't make it worse.
- **Dependency bumps** forced by Wear Compose can affect `:app`. Always re-run both phone flavors.

## Commits

Make one commit per milestone, and more if a milestone gets large, each with a descriptive message. Only commit or push when the user asks. Branch off `master` first.
