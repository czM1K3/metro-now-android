# Building and releasing

## Toolchain

| Item | Value | Where |
| --- | --- | --- |
| Gradle | 9.6.0 wrapper (`./gradlew`); AGP 9.5 needs at least 9.6.0 | `gradle/wrapper/gradle-wrapper.properties` |
| AGP | 9.5.1, with AGP's built-in Kotlin support (modules don't apply `org.jetbrains.kotlin.android`; the root build declares it `apply false` only to pin the Kotlin version) | `gradle/libs.versions.toml` |
| Kotlin | 2.3.21 (with the Compose and serialization plugins) | `gradle/libs.versions.toml` |
| KSP | 2.3.14 (Room compiler) | `gradle/libs.versions.toml` |
| JDK for Gradle | 21 (as used in CI) | `.github/workflows/build.yml` |
| Bytecode target | Java 17 (`jvmToolchain(17)`). Current Compose libraries are compiled for JVM 11+, so Java 8 bytecode can't inline them | `app/build.gradle.kts`, `shared/build.gradle.kts` |
| `compileSdk` / `targetSdk` / `minSdk` | 37 / 36 / 24 | `app/build.gradle.kts` (`:shared`: 37 / – / 24) |

The Compose BOM (2026.09.00) and Wear Compose 1.7 require `compileSdk` 37 and AGP 9.1+. AGP downloads the API 37 platform automatically if it is missing. Material 3 no longer depends on `material-icons-core`, so `:app` declares it explicitly.

`java.time` is used throughout the code, and minSdk 24 doesn't ship it, so core library desugaring is enabled (`desugar_jdk_libs`).

All dependency versions live in the version catalog `gradle/libs.versions.toml`. Add new dependencies there and reference them as `libs.…`.

## Modules

| Module | Plugin | Notes |
| --- | --- | --- |
| `:shared` | `com.android.library` | Data, domain, `StopsViewModel`, GraphQL (Apollo), Room (KSP), DataStore, location data sources. Has the `locationBackend` flavors. |
| `:app` | `com.android.application` | Phone UI. Depends on `:shared`. |

## Variants

There is one flavor dimension, `locationBackend`, declared in both `:shared` and `:app`:

| Flavor | Location provider | Extra dependency | Version name |
| --- | --- | --- | --- |
| `gms` | Google Play Services fused location | `gmsImplementation(play-services-location)` | `<versionName>` (e.g. `1.1`) |
| `vanilla` | Android `LocationManager` (no Google services) | none | `<versionName>-nogms` (e.g. `1.1-nogms`) |

| Build type | Notes |
| --- | --- |
| `debug` | `applicationIdSuffix = ".debug"`, so it installs next to the release build |
| `release` | `isMinifyEnabled = false`; `proguard-rules.pro` contains no rules |

That gives four variants: `gmsDebug`, `gmsRelease`, `vanillaDebug` and `vanillaRelease`. The Play Store build is `gmsRelease`. The vanilla flavor exists for devices without Google Play Services (for example de-Googled ROMs and F-Droid-style distribution).

Flavor-specific code goes in `shared/src/<flavor>/java/...`. Today the only such file is `features/location/data/LocationDataSource.kt`. When you add flavor-specific code, give the class the **same fully qualified name and public API in both flavors**.

## Common commands

```bash
./gradlew :app:assembleGmsDebug          # build the debug APK (GMS)
./gradlew :app:assembleVanillaDebug      # build the debug APK (no GMS)
./gradlew :app:installGmsDebug           # install on a connected device
./gradlew :shared:testGmsDebugUnitTest   # JVM unit tests (StopsViewModelTest)
./gradlew :app:connectedGmsDebugAndroidTest  # instrumented tests (device needed)
./gradlew :app:lintGmsDebug              # Android lint
./gradlew build                          # all variants + lint + unit tests (what CI runs)
./gradlew :app:bundleGmsRelease          # Play Store bundle (needs signing config)
```

## CI

`.github/workflows/build.yml` runs on pushes and pull requests to `master`. Pushes that change only `README.md` or `LICENSE` are skipped. The workflow sets up Temurin JDK 21 with a Gradle cache and runs `./gradlew build`.

## Releasing

There is no signing config in the repository. Release builds are signed locally through Android Studio, and the output folder `app/release/` is git-ignored. Before cutting a release, bump `versionCode` (must increase for Google Play) and `versionName` in `app/build.gradle.kts`.

## Updating the GraphQL schema

```bash
./gradlew :shared:downloadApolloSchema --endpoint='https://api.metro.madsoft.cz/graphql' --schema=shared/src/main/graphql/dev/metronow/android/schema.graphqls
```

Then rebuild. Apollo regenerates its classes during the build (`generateServiceApolloSources`).
