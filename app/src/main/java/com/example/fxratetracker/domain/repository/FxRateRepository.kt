package com.example.fxratetracker.domain.repository

import com.example.fxratetracker.domain.model.AssetCode
import com.example.fxratetracker.domain.model.FxRateEntity
import kotlinx.coroutines.flow.Flow

interface FxRateRepository {

    fun observeFxRates(assets: Set<AssetCode>): Flow<List<FxRateEntity>>

    suspend fun getFxRates(assets: Set<AssetCode>): List<FxRateEntity>

    suspend fun refreshFxRates(assets: Set<AssetCode>): List<FxRateEntity>

}