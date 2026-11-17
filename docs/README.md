# metro now (Android): documentation

metro now is a Jetpack Compose app that shows live public transport departures in Prague and the surrounding area (PID). It finds the stops closest to the user and shows a countdown to the next departures:

- the closest **metro** station, and
- the closest stop of **any** kind (tram, bus, train, etc.).

Data comes from the metro now GraphQL API (`https://api.metro.madsoft.cz/graphql`). The backend and the iOS/web clients live in [krystxf/metro-now](https://github.com/krystxf/metro-now/).

## Contents

| Document | What it covers |
| --- | --- |
| [architecture.md](architecture.md) | Modules (`:app`, `:shared`, `:wear`), package layout, layers, dependency injection, navigation, theming |
| [features.md](features.md) | How each feature works: location, closest stops, departures, info texts, the Wear OS app |
| [data.md](data.md) | GraphQL API, Room database, DataStore, caching rules |
| [building.md](building.md) | Toolchain, product flavors, build types, CI, releasing, updating the schema |
| [development.md](development.md) | Conventions, adding a feature, localization, testing, known quirks |

## Quick start

```bash
# Debug build, Google Play Services location backend
./gradlew :app:assembleGmsDebug

# Debug build without Google Play Services (plain Android LocationManager)
./gradlew :app:assembleVanillaDebug

# Install on a connected device or emulator
./gradlew :app:installGmsDebug

# Wear OS debug build (standalone watch app)
./gradlew :wear:assembleDebug

# Everything CI runs: all variants, lint and unit tests
./gradlew build
```

The app needs location permission. On an emulator, set a mock location inside Prague (for example Dejvická, `50.1009, 14.3932`), or the closest stops will be far away.
