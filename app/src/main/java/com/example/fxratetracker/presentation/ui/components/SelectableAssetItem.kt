package com.example.fxratetracker.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.example.fxratetracker.domain.model.SelectableAsset
import com.example.fxratetracker.presentation.ui.preview.SelectableAssetPreviewParameterProvider

@Composable
fun SelectableAssetItem(
    asset: SelectableAsset,
    onSelectedChanged: (isSelected: Boolean) -> Unit,
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
                AssetIcon(asset.asset)

                Column {
                    Text(
                        text = asset.asset.code,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = asset.asset.name,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Checkbox(
                checked = asset.isSelected,
                onCheckedChange = onSelectedChanged,
            )
        }
    }
}

@Preview
@Composable
private fun SelectableAssetItemPreview(
    @PreviewParameter(
        SelectableAssetPreviewParameterProvider::class,
        limit = 2,
    ) asset: SelectableAsset,
) {
    var isSelected by remember { mutableStateOf(asset.isSelected) }
    val asset = asset.copy(isSelected = isSelected)

    SelectableAssetItem(
        asset = asset,
        onSelectedChanged = { isSelected = it }
    )
}