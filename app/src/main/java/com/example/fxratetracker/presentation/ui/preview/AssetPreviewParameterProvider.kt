package com.example.fxratetracker.presentation.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.fxratetracker.domain.model.Asset

class AssetPreviewParameterProvider() : PreviewParameterProvider<Asset> {

    override val values: Sequence<Asset> = sequenceOf(
        Asset(
            code = "USD",
            name = "US Dollar",
        ),
        Asset(
            code = "EUR",
            name = "Euro",
        ),
        Asset(
            code = "UAH",
            name = "Ukrainian Hryvnia",
        ),
        Asset(
            code = "BTC",
            name = "Bitcoin",
        ),
    )

}