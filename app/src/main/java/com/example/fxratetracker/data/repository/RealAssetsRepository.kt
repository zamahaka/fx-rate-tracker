package com.example.fxratetracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.fxratetracker.data.remote.service.ExchangeRateService
import com.example.fxratetracker.domain.model.Asset
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

    override fun observeAssets(): Flow<List<Asset>> = store.data.map { preferences ->
        preferences[ASSETS_KEY].takeUnless { it.isNullOrBlank() }
            ?.let { json.decodeFromString(it) }
            ?: emptyList()
    }

    override suspend fun getAssets(): List<Asset> =
        observeAssets().first().ifEmpty { refreshAssets() }

    private suspend fun refreshAssets(): List<Asset> {
        val response = service.getAssets()
        val assets = response.currencies.map { (code, name) ->
            Asset(code = code, name = name)
        }

        store.edit { preferences ->
            preferences[ASSETS_KEY] = json.encodeToString(assets)
        }

        return assets
    }

    private companion object {
        val ASSETS_KEY = stringPreferencesKey("com.example.fxratetracker.data.assets")
    }
}