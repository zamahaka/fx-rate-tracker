package com.example.fxratetracker.domain.repository

import arrow.core.Either
import com.example.fxratetracker.domain.model.AssetCode
import com.example.fxratetracker.domain.model.Failure
import kotlinx.coroutines.flow.Flow

interface SelectedAssetsRepository {

    fun observeSelectedAssets(): Flow<Set<AssetCode>>

    suspend fun getSelectedAssets(): Either<Failure, Set<AssetCode>>

    suspend fun saveSelectedAssets(data: Set<AssetCode>): Either<Failure, Unit>

    suspend fun saveAssetSelected(id: AssetCode, isSelected: Boolean): Either<Failure, Unit>

}