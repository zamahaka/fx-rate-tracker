package com.example.fxratetracker.domain.model

import com.example.fxratetracker.data.remote.BigDecimalJson
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.math.RoundingMode

data class FxRate(
    val baseAsset: Asset,
    val referenceAsset: Asset,
    val rate: BigDecimal,
) {

    /**
     * Inverted exchange rate
     *
     * Scale and rounding from [ExchangeRate.operations()](https://github.com/JodaOrg/joda-money/blob/1f0d133c96868c2d93f58c72e882e508a8974e84/src/main/java/org/joda/money/ExchangeRate.java#L231)
     *
     * Division from [DefaultExchangeRateOperations.invert()](https://github.com/JodaOrg/joda-money/blob/1f0d133c96868c2d93f58c72e882e508a8974e84/src/main/java/org/joda/money/DefaultExchangeRateOperations.java#L63)
     * */
    val inverted: BigDecimal
        get() = BigDecimal.ONE.divide(rate, 16, RoundingMode.HALF_EVEN)

}

@Serializable
data class FxRateEntity(
    val baseAsset: AssetCode,
    val referenceAsset: AssetCode,
    val rate: BigDecimalJson,
)
