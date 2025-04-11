package com.example.fxratetracker.domain.usecase

import com.example.fxratetracker.domain.model.Asset
import com.example.fxratetracker.domain.model.AssetCode
import com.example.fxratetracker.domain.model.FxRate
import com.example.fxratetracker.domain.repository.AssetsRepository
import com.example.fxratetracker.domain.repository.FxRateRepository
import com.example.fxratetracker.domain.repository.SelectedAssetsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveSelectedFxRates(
    private val dispatcher: CoroutineDispatcher,
    private val selectedAssetsRepository: SelectedAssetsRepository,
    private val assetsRepository: AssetsRepository,
    private val fxRateRepository: FxRateRepository,
) {

    operator fun invoke(): Flow<List<FxRate>> {
        return selectedAssetsRepository.observeSelectedAssets().flatMapLatest { selectedCodes ->
            combine(
                fxRateRepository.observeFxRates(selectedCodes),
                assetsRepository.observeAssets(),
            ) { rates, assets ->
                rates.mapNotNull { rate ->
                    FxRate(
                        baseAsset = findAsset(rate.baseAsset, assets)
                            ?: return@mapNotNull null,
                        referenceAsset = findAsset(rate.referenceAsset, assets)
                            ?: return@mapNotNull null,
                        rate = rate.rate
                    )
                }.let(::sortRates)
            }
        }.flowOn(dispatcher)
    }

    private fun findAsset(code: AssetCode, assets: List<Asset>): Asset? {
        return assets.find { it.code == code }
    }

    private fun sortRates(rates: List<FxRate>): List<FxRate> {
        return rates.sortedBy { it.referenceAsset.code }
    }

}