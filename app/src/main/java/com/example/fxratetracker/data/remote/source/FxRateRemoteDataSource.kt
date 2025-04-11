package com.example.fxratetracker.data.remote.source

import com.example.fxratetracker.data.remote.model.FxRatesResponse
import com.example.fxratetracker.data.remote.service.ExchangeRateService
import com.example.fxratetracker.domain.model.AssetCode
import com.example.fxratetracker.domain.model.FxRateEntity
import java.math.BigDecimal

class FxRateRemoteDataSource(
    private val service: ExchangeRateService,
) {

    suspend fun getFxRates(assets: Set<AssetCode>): List<FxRateEntity> {
        // Api will respond with list of all assets, which is undesirable
        assets.ifEmpty { return emptyList() }

        val response = service.getFxRates(currencies = assets.joinToString(separator = ","))
        val fxRates = mapResponseToEntity(response)
        val adjustedRates = if (shouldAddSourceRate(assets, response.source))
            addSourceRate(response.source, fxRates) else fxRates

        return adjustedRates
    }

    private fun mapResponseToEntity(response: FxRatesResponse): List<FxRateEntity> {
        return response.quotes.map { (pair, rate) ->
            val referenceCode = pair.removePrefix(response.source)

            FxRateEntity(
                baseAsset = response.source,
                referenceAsset = referenceCode,
                rate = rate,
            )
        }
    }

    // Api does not respond with source rate if it is present in requested assets.
    // We should add it manually
    private fun shouldAddSourceRate(
        requestedAssets: Set<AssetCode>,
        responseSource: AssetCode,
    ) = responseSource in requestedAssets

    private fun addSourceRate(
        responseSource: AssetCode,
        rates: List<FxRateEntity>,
    ): List<FxRateEntity> {
        return rates + listOf(
            FxRateEntity(
                baseAsset = responseSource,
                referenceAsset = responseSource,
                rate = BigDecimal.ONE,
            )
        )
    }

}