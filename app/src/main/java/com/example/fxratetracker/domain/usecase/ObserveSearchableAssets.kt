package com.example.fxratetracker.domain.usecase

import com.example.fxratetracker.domain.model.Asset
import com.example.fxratetracker.domain.repository.AssetsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveSearchableAssets(
    private val assetsRepository: AssetsRepository,
) {

    operator fun invoke(query: String): Flow<List<Asset>> {
        return assetsRepository.observeAssets()
            .map { filterAssets(query, it) }
            .map { sortAssets(it) }
    }

    private fun filterAssets(query: String, assets: List<Asset>): List<Asset> {
        return assets.filter {
            it.code.contains(query, ignoreCase = true)
                    || it.name.contains(query, ignoreCase = true)
        }
    }

    private fun sortAssets(assets: List<Asset>): List<Asset> {
        return assets.sortedBy { it.code }
    }

}