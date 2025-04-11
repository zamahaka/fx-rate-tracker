package com.example.fxratetracker.presentation.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.fxratetracker.domain.model.SelectableAsset

class SelectableAssetPreviewParameterProvider() : PreviewParameterProvider<SelectableAsset> {
    override val values: Sequence<SelectableAsset> =
        AssetPreviewParameterProvider().values.mapIndexed { i, a ->
            SelectableAsset(
                asset = a,
                isSelected = i % 2 == 0,
            )
        }
}