package com.example.fxratetracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.either
import com.example.fxratetracker.data.remote.source.FxRateRemoteDataSource
import com.example.fxratetracker.domain.model.AssetCode
import com.example.fxratetracker.domain.model.Failure
import com.example.fxratetracker.domain.model.FxRateEntity
import com.example.fxratetracker.domain.model.catchUnexpected
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
    ): Flow<List<FxRateEntity>> =
        observeDataStoreFxRates(assets).map { it.getOrElse { emptyList() } }

    override suspend fun getFxRates(
        assets: Set<AssetCode>,
    ): Either<Failure, List<FxRateEntity>> = either {
        val cache = observeDataStoreFxRates(assets).first().bind()

        if (cache.isNotEmpty() && cache.map { it.referenceAsset }.containsAll(assets)) {
            return@either filter(cache, assets)
        }

        refreshFxRates(assets).bind()
    }

    override suspend fun refreshFxRates(
        assets: Set<AssetCode>,
    ): Either<Failure, List<FxRateEntity>> = Either.catchUnexpected {
        val remote = remote.getFxRates(assets)
        store.edit { preferences ->
            preferences[FX_RATES_KEY] = json.encodeToString(remote)
        }

        remote
    }


    private fun observeDataStoreFxRates(
        assets: Set<AssetCode>,
    ): Flow<Either<Failure, List<FxRateEntity>>> = store.data.map { preferences ->
        Either.catchUnexpected {
            preferences[FX_RATES_KEY].takeUnless { it.isNullOrBlank() }
                ?.let { json.decodeFromString<List<FxRateEntity>>(it) }
                ?.let { filter(it, assets) }
                ?: emptyList()
        }
    }

    private fun filter(rates: List<FxRateEntity>, byAssets: Set<AssetCode>): List<FxRateEntity> {
        return rates.filter { it.referenceAsset in byAssets }
    }


    private companion object {
        val FX_RATES_KEY = stringPreferencesKey("com.example.fxratetracker.data.fx-rates")
    }
}