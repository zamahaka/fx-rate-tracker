package com.example.fxratetracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.fxratetracker.data.remote.source.FxRateRemoteDataSource
import com.example.fxratetracker.domain.model.AssetCode
import com.example.fxratetracker.domain.model.FxRateEntity
import com.example.fxratetracker.domain.repository.FxRateRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class RealFxRateRepository(
    private val store: DataStore<Preferences>,
    private val remote: FxRateRemoteDataSource,
    private val json: Json,
) : FxRateRepository {

    override fun observeFxRates(
        assets: Set<AssetCode>,
    ): Flow<List<FxRateEntity>> = store.data.map { preferences ->
        preferences[FX_RATES_KEY].takeUnless { it.isNullOrBlank() }
            ?.let { json.decodeFromString<List<FxRateEntity>>(it) }
            ?.let { filter(it, assets) }
            ?: emptyList()
    }

    override suspend fun getFxRates(assets: Set<AssetCode>): List<FxRateEntity> {
        val cache = observeFxRates(assets).first()

        if (cache.isNotEmpty() && cache.map { it.referenceAsset }.containsAll(assets)) {
            return filter(cache, assets)
        }

        return refreshFxRates(assets)
    }

    override suspend fun refreshFxRates(assets: Set<AssetCode>): List<FxRateEntity> {
        val remote = remote.getFxRates(assets)
        store.edit { preferences ->
            preferences[FX_RATES_KEY] = json.encodeToString(remote)
        }
        return remote
    }

    private fun filter(rates: List<FxRateEntity>, byAssets: Set<AssetCode>): List<FxRateEntity> {
        return rates.filter { it.referenceAsset in byAssets }
    }


    private companion object {
        val FX_RATES_KEY = stringPreferencesKey("com.example.fxratetracker.data.fx-rates")
    }
}