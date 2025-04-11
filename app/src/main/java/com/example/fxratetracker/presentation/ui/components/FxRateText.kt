package com.example.fxratetracker.presentation.ui.components

import android.icu.text.DecimalFormat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.fxratetracker.domain.model.FxRate
import com.example.fxratetracker.presentation.ui.preview.FxRatePreviewParameterProvider

@Composable
fun FxRateText(
    fxRate: FxRate,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    numberColor: Color = MaterialTheme.colorScheme.onSurface,
    currencyColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildAnnotatedString {
            // TODO: Formatter
            val decimalFormat = DecimalFormat().apply {
                minimumFractionDigits = 5
                maximumFractionDigits = 4
                // TODO: fxRate.inverted.precision()
            }
            append(decimalFormat.format(fxRate.inverted))
            append(" ")
            withStyle(SpanStyle(color = currencyColor)) {
                append(fxRate.baseAsset.code)
            }
        },
        style = style,
        color = numberColor,
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun FxRateTextPreview(
    @PreviewParameter(FxRatePreviewParameterProvider::class, limit = 1)
    fxRate: FxRate,
) {
    FxRateText(fxRate)
}