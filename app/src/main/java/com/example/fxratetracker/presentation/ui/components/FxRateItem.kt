package com.example.fxratetracker.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.example.fxratetracker.domain.model.FxRate
import com.example.fxratetracker.presentation.ui.preview.FxRatePreviewParameterProvider

@Composable
fun FxRateItem(
    fxRate: FxRate,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .heightIn(min = 64.dp)
                .padding(8.dp)
                .fillMaxWidth(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AssetIcon(fxRate.referenceAsset)

                Column {
                    Text(
                        text = fxRate.referenceAsset.code,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = fxRate.referenceAsset.name,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            FxRateText(fxRate)
        }
    }
}

@Preview
@Composable
private fun FxRateItemPreview(
    @PreviewParameter(FxRatePreviewParameterProvider::class, limit = 3) fxRate: FxRate,
) {
    FxRateItem(
        fxRate = fxRate,
    )
}