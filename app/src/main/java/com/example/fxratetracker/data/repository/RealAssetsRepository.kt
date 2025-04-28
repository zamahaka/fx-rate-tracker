package com.example.fxratetracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import com.example.fxratetracker.data.remote.service.ExchangeRateService
import com.example.fxratetracker.domain.model.Asset
import com.example.fxratetracker.domain.model.Failure
import com.example.fxratetracker.domain.model.LocalFailure
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

    override fun observeAssets(): Flow<List<Asset>> =
        observeDataStoreAssets().map { it.getOrElse { emptyList() } }

    override suspend fun getAssets(): Either<Failure, List<Asset>> = either {
        observeDataStoreAssets().first().bind().ifEmpty { refreshAssets().bind() }
    }

    private fun observeDataStoreAssets(): Flow<Either<LocalFailure, List<Asset>>> =
        store.data.map { preferences ->
            Either.catchUnexpected {
                preferences[ASSETS_KEY].takeUnless { it.isNullOrBlank() }
                    ?.let { json.decodeFromString(it) } ?: emptyList()
            }
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