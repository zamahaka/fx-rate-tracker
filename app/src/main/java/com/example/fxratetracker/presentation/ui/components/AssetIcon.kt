package com.example.fxratetracker.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fxratetracker.domain.model.Asset

@Composable
fun AssetIcon(
    asset: Asset,
    modifier: Modifier = Modifier,
) {
    val minSize = 36.dp

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = minSize, minHeight = minSize)
            .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
    )
}