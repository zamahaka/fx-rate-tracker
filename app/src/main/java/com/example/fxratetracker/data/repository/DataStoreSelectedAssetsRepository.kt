package com.example.fxratetracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import arrow.core.Either
import arrow.core.getOrElse
import com.example.fxratetracker.domain.model.AssetCode
import com.example.fxratetracker.domain.model.Failure
import com.example.fxratetracker.domain.model.catchUnexpected
import com.example.fxratetracker.domain.repository.SelectedAssetsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreSelectedAssetsRepository(
    private val store: DataStore<Preferences>,
) : SelectedAssetsRepository {

    override fun observeSelectedAssets(): Flow<Set<AssetCode>> =
        observeDataStoreSelectedAssets().map { it.getOrElse { emptySet() } }

    override suspend fun getSelectedAssets(): Either<Failure, Set<AssetCode>> =
        observeDataStoreSelectedAssets().first()

    override suspend fun saveSelectedAssets(data: Set<AssetCode>): Either<Failure, Unit> {
        return Either.catchUnexpected {
            store.edit { preferences ->
                preferences[SELECTED_ASSETS_KEY] = data
            }
        }
    }

    override suspend fun saveAssetSelected(
        id: AssetCode,
        isSelected: Boolean,
    ): Either<Failure, Unit> = Either.catchUnexpected {
        store.edit { preferences ->
            val selectedCodes = preferences[SELECTED_ASSETS_KEY] ?: emptySet()
            val updatedCodes = if (isSelected) selectedCodes + id else selectedCodes - id
            preferences[SELECTED_ASSETS_KEY] = updatedCodes
        }
    }


    private fun observeDataStoreSelectedAssets(): Flow<Either<Failure, Set<AssetCode>>> {
        return store.data.map { preferences ->
            Either.catchUnexpected { preferences[SELECTED_ASSETS_KEY] ?: emptySet() }
        }
    }

    private companion object {
        val SELECTED_ASSETS_KEY =
            stringSetPreferencesKey("com.example.fxratetracker.data.SELECTED_ASSETS_KEY")
    }

}