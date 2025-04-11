# fxratetracker
Application with simple currency exchange rates tracking.

Features 2 screens:
- Exchange rate list
  - List of selected exchange rates
  - Automatic refresh
  - Indication of last successful refresh or failure
- Settings screen with all available assets.
  - List of all available assets
  - Assets can be checked to add them to exchange rate list
  - Searchable with user query

## API
Solution uses [exchangerate.host](https://exchangerate.host/documentation) as source of data

## Persistence
Data is cached to local storage, using [data store](https://developer.android.com/jetpack/androidx/releases/datastore) built on top of shared preferences)

## Configuration
Use root [local.properties](./local.properties) to provide required configuration.
- `api.key to provide` exchangerate.host access_key
- `fxrate.refresh.period` to provide exchange rate refresh frequency in seconds

## Assumptions
- exchangerate.host updates own data on hourly basis.
  Application will still fetch exchange rates with period of provided `fxrate.refresh.period`.
  Beware, exchange rates will not visually change, unless api responds with different data.