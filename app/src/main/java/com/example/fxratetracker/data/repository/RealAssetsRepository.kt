package com.example.fxratetracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import arrow.core.Either
import arrow.core.raise.either
import com.example.fxratetracker.data.remote.service.ExchangeRateService
import com.example.fxratetracker.domain.model.Asset
import com.example.fxratetracker.domain.model.Failure
import com.example.fxratetracker.domain.model.catchUnexpected
import com.example.fxratetracker.domain.repository.AssetsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class RealAssetsRepository(
    private val store: DataStore<Preferences>,
    private val service: ExchangeRateService,
    private val json: Json,
) : AssetsRepository {

    // TODO: Catch errors
    override fun observeAssets(): Flow<List<Asset>> = store.data.map { preferences ->
        preferences[ASSETS_KEY].takeUnless { it.isNullOrBlank() }
            ?.let { json.decodeFromString(it) }
            ?: emptyList()
    }

    override suspend fun getAssets(): Either<Failure, List<Asset>> = either {
        observeAssets().first().ifEmpty { refreshAssets().bind() }
    }

    private suspend fun refreshAssets(): Either<Failure, List<Asset>> = either {
        val response = Either.catchUnexpected { service.getAssets() }.bind()
        val assets = response.currencies.map { (code, name) ->
            Asset(code = code, name = name)
        }

        Either.catchUnexpected {
            store.edit { preferences ->
                preferences[ASSETS_KEY] = json.encodeToString(assets)
            }
        }.bind()

        assets
    }

    private companion object {
        val ASSETS_KEY = stringPreferencesKey("com.example.fxratetracker.data.assets")
    }
}