package com.example.fxratetracker.presentation.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.fxratetracker.domain.model.FxRate

class FxRateListPreviewParameterProvider() : PreviewParameterProvider<List<FxRate>> {
    override val values = sequenceOf(FxRatePreviewParameterProvider().values.toList())
}