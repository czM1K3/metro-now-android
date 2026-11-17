# Data

## GraphQL API (Apollo Kotlin 4)

- **Endpoint:** `https://api.metro.madsoft.cz/graphql`, hardcoded in `core/di/CoreModule.kt`.
- **Apollo service:** `service`, package `dev.metronow.android` (`shared/build.gradle.kts`). Generated classes are `dev.metronow.android.<Name>Query`, and enums are in `dev.metronow.android.type`.
- **Operations** live in `shared/src/main/graphql/dev/metronow/android/`:

| File | Query | Used by |
| --- | --- | --- |
| `Stops.graphql` | `stops { id name avgLatitude avgLongitude platforms { id name latitude longitude isMetro code } }` | `StopRemoteDataSource` (fetches the whole stop list) |
| `Departures.graphql` | `departures(platformIds: [ID!]!) { headsign departureTime { predicted } platform { id } route { name isSubstitute vehicleType isNight } }` | `DepartureRemoteDataSource` |
| `InfoTexts.graphql` | `infotexts { id text textEn validFrom validTo priority }` | `InfoTextRemoteDataSource` |

- **Schema:** `schema.graphqls` is a checked-in copy of the server schema. To update it:

  ```bash
  ./gradlew :shared:downloadApolloSchema --endpoint='https://api.metro.madsoft.cz/graphql' --schema=shared/src/main/graphql/dev/metronow/android/schema.graphqls
  ```

  After updating it, rebuild and fix the mappers if enums gained values. Every `when` over a generated enum handles `UNKNOWN__`.

- **Custom scalars** are not mapped. They arrive as `Any` and are cast with `as String`, then parsed with `DateTimeFormatter.ISO_DATE_TIME`. Fields affected: `departureTime.predicted`, `validFrom` and `validTo`.
- **Errors:** remote data sources return `response.data?.… ?: emptyList()`, so a network or GraphQL error looks like an empty result. `StopRepositoryImpl` skips the cache replace when the stop list comes back empty.

## Room database

`core/data/db/MetroDatabase.kt` creates the file `metro.db` at **version 1** with `fallbackToDestructiveMigration(true)`. Bumping the version wipes the cache instead of migrating it. That is acceptable because everything in it can be fetched again.

| Table | Entity | Columns | Notes |
| --- | --- | --- | --- |
| `stop` | `DbStop` | `id` (PK), `name`, `latitude`, `longitude`, `hasMetro` | `hasMetro` = any platform `isMetro` |
| `platform` | `DbPlatform` | `id` (PK), `stopId`, `name`, `latitude`, `longitude`, `isMetro`, `code?` | Linked to `stop` through `DbStopWithPlatform` (`@Relation`), with no foreign key |
| `infoText` | `DbInfoText` | `id` (PK), `textCz`, `textEn?`, `validFrom?`, `validTo?`, `priority` | Dates are stored as `LocalDateTime.toString()` and priority as `"LOW"`/`"NORMAL"`/`"HIGH"` |

Every write is a full replace (`deleteAll()` and then `insert(list)`). There are no upserts.

## DataStore

`features/stop/data/ds/StopDataSource.kt` uses a Preferences DataStore named **`fit_datastore`** with a single key, `fetch_date` (`LocalDateTime.toString()`). It records when the stop cache was last refreshed. A missing value reads as `LocalDateTime.MIN`.

Renaming the store or the key isn't harmful, but it forces one refetch for every user.

## Caching rules

### Stop cache refresh

`StopRepositoryImpl.stopsLocalCacheUpdate()` runs before every closest-stop lookup, which happens on every location update. It refetches the full stop list when either of these is true:

- more than **30 days** have passed since `fetch_date`, or
- the `stop` table is empty.

When the fetch returns stops, the repository clears `platform` and `stop`, inserts the new stops and platforms, and saves `fetch_date = now`.

### Departures

Departures are never cached. They are fetched again every 10 s and whenever the closest stops change.

### Info texts

Info texts are fetched each time the list screen opens and cached so the detail screen can read them.
