@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fxratetracker.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TopAppBarSearch(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SearchBar(
        state = rememberSearchBarState(),
        inputField = {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.titleSmall
            ) {
                val focusManager = LocalFocusManager.current

                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = onQueryChange,
                    onSearch = onQueryChange,
                    expanded = false,
                    onExpandedChange = {},
                    enabled = enabled,
                    placeholder = { Text("Search assets") },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    focusManager.clearFocus()
                                    onClearQuery()
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = "Clear query",
                                )
                            }
                        }
                    },
                )
            }
        },
        modifier = modifier,
    )
}

@Preview
@Composable
private fun TopAppBarSearchPreviewWithQuery() {
    TopAppBarSearch(
        query = "query",
        onQueryChange = {},
        onClearQuery = {},
    )
}