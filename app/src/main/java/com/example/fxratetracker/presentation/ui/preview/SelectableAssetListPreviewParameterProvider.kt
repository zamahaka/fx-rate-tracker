package com.example.fxratetracker.presentation.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.fxratetracker.domain.model.SelectableAsset

class SelectableAssetListPreviewParameterProvider() :
    PreviewParameterProvider<List<SelectableAsset>> {
    override val values: Sequence<List<SelectableAsset>> =
        sequenceOf(SelectableAssetPreviewParameterProvider().values.toList())
}