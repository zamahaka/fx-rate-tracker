package com.example.fxratetracker.domain.repository

import com.example.fxratetracker.domain.model.Asset
import kotlinx.coroutines.flow.Flow

interface AssetsRepository {

    fun observeAssets(): Flow<List<Asset>>

    suspend fun getAssets(): List<Asset>

}