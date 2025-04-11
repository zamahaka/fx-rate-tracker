package com.example.fxratetracker.presentation.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.fxratetracker.domain.model.Asset
import com.example.fxratetracker.domain.model.FxRate

class FxRatePreviewParameterProvider() : PreviewParameterProvider<FxRate> {

    private val usdAsset = Asset(
        code = "USD",
        name = "US Dollar",
    )

    override val values: Sequence<FxRate> = sequenceOf(
        FxRate(
            baseAsset = usdAsset,
            referenceAsset = Asset(
                code = "EUR",
                name = "Euro",
            ),
            rate = "0.902645".toBigDecimal(),
        ),
        FxRate(
            baseAsset = usdAsset,
            referenceAsset = Asset(
                code = "UAH",
                name = "Ukrainian Hryvnia",
            ),
            rate = "41.262408".toBigDecimal(),
        ),
        FxRate(
            baseAsset = usdAsset,
            referenceAsset = Asset(
                code = "BTC",
                name = "Bitcoin",
            ),
            rate = "1.3039391e-5".toBigDecimal(),
        ),
    )
}