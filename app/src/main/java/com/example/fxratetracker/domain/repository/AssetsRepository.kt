package com.example.fxratetracker.domain.repository

import arrow.core.Either
import com.example.fxratetracker.domain.model.Asset
import com.example.fxratetracker.domain.model.Failure
import kotlinx.coroutines.flow.Flow

interface AssetsRepository {

    fun observeAssets(): Flow<List<Asset>>

    suspend fun getAssets(): Either<Failure, List<Asset>>

}