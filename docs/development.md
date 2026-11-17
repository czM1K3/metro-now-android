# Development guide

## Conventions

- **Kotlin style:** `kotlin.code.style=official` (from `gradle.properties`). Use 4-space indentation and trailing commas in multi-line argument lists.
- **Package by feature** under `dev.metronow.android.features.<feature>`, with `data/api`, `data/db`, `data/ds`, `domain`, `presentation` and `di` subpackages as needed. See [architecture.md](architecture.md).
- **Mappers** are top-level extension functions named `toDomain()` / `toDb()` in a `*Mapper.kt` file next to the type they convert from.
- **State:** each ViewModel has one private `MutableStateFlow<XScreenState>` exposed as `screenStateStream`. Screens collect it with `collectAsStateWithLifecycle()`.
- **Strings:** every user-visible string goes in `res/values/strings.xml` **and** `res/values-cs/strings.xml`. Mark strings that must not be translated with `translatable="false"`.
- **Dates:** use `java.time` (desugared). Show dates to the user with `AppLanguage.getCurrent().getFormat()`.

## Adding a feature

1. Create `features/<name>/` with the layers you need.
2. If it needs API data, add a `.graphql` operation under `app/src/main/graphql/dev/metronow/android/` and build, which generates `<Name>Query`. Then write a `RemoteDataSource` and a mapper to a domain class.
3. If it needs persistence:
   - add a `Db*` `@Entity` and a `@Dao`
   - register the entity in `MetroDatabase`'s `entities` array
   - add an abstract DAO getter
   - **bump `version`**: the destructive fallback will wipe and recreate the database
4. Create a Koin module in `features/<name>/di/` and **add it to `startKoin { modules(...) }` in `App.kt`**.
5. For a new screen, add a `@Serializable` entry to `Screens` and a `composable<Screens.X> { ... }` block in `Navigation.kt`.
6. Add strings in both languages.

## Localization

The app supports English (default `values/`) and Czech (`values-cs/`). Data from the API comes in Czech, and info texts also have an optional English version (`textEn`). Code that picks a language at runtime goes through `AppLanguage` in `core/data/LanguageHelper.kt`.

## Testing

`StopsViewModelTest` (JVM, `app/src/test`) covers countdowns, "seconds since last fetch", refetching, and stopping work without subscribers. It uses fake repositories (the three repositories the ViewModel needs are interfaces), an `UpdateFlow` built from `RefreshConfig`, a clock tied to the test scheduler, and `kotlinx-coroutines-test` virtual time. Avoid inline-mocking libraries such as mockk: unit tests run on the Java 8 toolchain, where agent attachment can fail.

JUnit 4, AndroidX test, Espresso and Compose UI test are also on the classpath. These pure functions are easy next targets:

- `DeparturesGrouped.addCountDown()`: the 20 s cutoff, the two-per-route limit, and the time zone handling
- `Double.secondsToString()`
- `ClosestStops.getPlatformIds()`
- the API and DB mappers

## Known quirks

These are worth knowing before you change related code:

- **Closest stop is Manhattan distance in degrees** (`StopDao`), not geodesic distance.
- **If the stop cache has no metro stops, the main screen spins forever**, because `getClosestStops` needs both a metro stop and an "any" stop to be non-null.
- **API errors are swallowed** and show up as empty lists. Nothing is shown to the user.
- **The stop cache check runs on every location update**: one DataStore read and one `COUNT` per update. That is cheap, but keep it in mind.
- **The info text detail reads only from Room.** Opening it before the list has been fetched shows nothing.
- **The PID logo** on the info text detail screen is loaded from an external Google image URL (`InfoTextDetailScreen.kt`).
- **Class name typo:** `InfoTextDetailVIewModel` (capital `I`). It is referenced from Koin and the screen, so rename all usages together if you fix it.
- **DataStore name** is `fit_datastore`, left over from an earlier project. See [data.md](data.md#datastore).
- **The departure sort** uses `routeDetail.name.toIntOrNull()`, so non-numeric route names (metro `A`/`B`/`C`, `S`/`R` trains, `X` substitutes) sort before numeric ones in an unspecified relative order.
- **Medium- and high-contrast color schemes** are defined in `Theme.kt` but not used.
