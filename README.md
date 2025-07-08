# metro now - Android
This repository contains code of app for tracking departure times of public transport in Prague and surroundings. For more info check [website](https://metronow.dev/) or [repository](https://github.com/krystxf/metro-now/)

## Download
You can download latest version on [Google Play](https://play.google.com/store/apps/details?id=dev.metronow.android)

## Useful commands
### Download schema from API
```bash
./gradlew :app:downloadApolloSchema --endpoint='https://api.metronow.dev/graphql' --schema=app/src/main/graphql/dev/metronow/android/schema.graphqls
```
