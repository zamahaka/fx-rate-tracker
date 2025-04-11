package com.example.fxratetracker.domain.repository

import com.example.fxratetracker.domain.model.AssetCode
import kotlinx.coroutines.flow.Flow

interface SelectedAssetsRepository {

    fun observeSelectedAssets(): Flow<Set<AssetCode>>

    suspend fun getSelectedAssets(): Set<AssetCode>

    suspend fun saveSelectedAssets(data: Set<AssetCode>)

    suspend fun saveAssetSelected(id: AssetCode, isSelected: Boolean)

}