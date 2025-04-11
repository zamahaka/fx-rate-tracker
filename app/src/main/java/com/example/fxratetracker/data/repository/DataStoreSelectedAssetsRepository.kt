package com.example.fxratetracker.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.example.fxratetracker.domain.model.AssetCode
import com.example.fxratetracker.domain.repository.SelectedAssetsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DataStoreSelectedAssetsRepository(
    private val store: DataStore<Preferences>,
) : SelectedAssetsRepository {

    override fun observeSelectedAssets(): Flow<Set<AssetCode>> {
        return store.data.map { preferences ->
            preferences[SELECTED_ASSETS_KEY] ?: emptySet()
        }
    }

    override suspend fun getSelectedAssets(): Set<AssetCode> {
        return store.data.map { preferences ->
            preferences[SELECTED_ASSETS_KEY] ?: emptySet()
        }.first()
    }

    override suspend fun saveSelectedAssets(data: Set<AssetCode>) {
        store.edit { preferences ->
            preferences[SELECTED_ASSETS_KEY] = data
        }
    }

    override suspend fun saveAssetSelected(
        id: AssetCode,
        isSelected: Boolean,
    ) {
        store.edit { preferences ->
            val selectedCodes = preferences[SELECTED_ASSETS_KEY] ?: emptySet()
            val updatedCodes = if (isSelected) selectedCodes + id else selectedCodes - id
            preferences[SELECTED_ASSETS_KEY] = updatedCodes
        }
    }


    private companion object {
        val SELECTED_ASSETS_KEY =
            stringSetPreferencesKey("com.example.fxratetracker.data.SELECTED_ASSETS_KEY")
    }

}