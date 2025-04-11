package com.example.fxratetracker.presentation.screens.selectassets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.example.fxratetracker.domain.model.AssetCode
import com.example.fxratetracker.domain.model.SelectableAsset
import com.example.fxratetracker.domain.repository.AssetsRepository
import com.example.fxratetracker.domain.repository.SelectedAssetsRepository
import com.example.fxratetracker.domain.usecase.ObserveSearchableAssets
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreen.Event.AssetSelectionChanged
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreen.Event.ClearQuery
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreen.Event.GoBack
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreen.Event.QueryChanged
import com.example.fxratetracker.presentation.ui.circuit.wrapEventSink
import com.example.fxratetracker.presentation.ui.components.SelectableAssetItem
import com.example.fxratetracker.presentation.ui.components.TopAppBarSearch
import com.example.fxratetracker.presentation.ui.preview.SelectableAssetListPreviewParameterProvider
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@Parcelize
data object SelectAssetsScreen : Screen {
    data class State(
        val assets: List<SelectableAsset>,
        val query: String,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class AssetSelectionChanged(val id: AssetCode, val isSelected: Boolean) : Event
        data class QueryChanged(val value: String) : Event

        data object ClearQuery : Event
        data object GoBack : Event
    }
}

class SelectAssetsPresenter(
    private val navigator: Navigator,
    private val assetsRepository: AssetsRepository,
    private val selectedAssetsRepository: SelectedAssetsRepository,
    private val observeSearchableAssets: ObserveSearchableAssets,
) : Presenter<SelectAssetsScreen.State> {
    @Composable
    override fun present(): SelectAssetsScreen.State {
        // TODO: Usecase?
        var query by remember { mutableStateOf("") }
        val assets by remember(query) { observeSearchableAssets(query) }
            .collectAsState(emptyList())
        val selectedCodes by selectedAssetsRepository.observeSelectedAssets()
            .collectAsState(emptySet())

        LaunchedEffect(Unit) {
            assetsRepository.getAssets()
        }

        val selectableAssets = assets.map {
            SelectableAsset(
                asset = it,
                isSelected = it.code in selectedCodes,
            )
        }

        val eventSink = wrapEventSink { event: SelectAssetsScreen.Event ->
            when (event) {
                is AssetSelectionChanged -> launch {
                    selectedAssetsRepository.saveAssetSelected(
                        id = event.id,
                        isSelected = event.isSelected,
                    )
                }

                is QueryChanged -> query = event.value
                ClearQuery -> query = ""

                GoBack -> navigator.pop()
            }
        }

        return SelectAssetsScreen.State(
            assets = selectableAssets,
            query = query,
            eventSink = eventSink,
        )
    }

    class Factory(
        private val assetsRepository: AssetsRepository,
        private val selectedAssetsRepository: SelectedAssetsRepository,
        private val observeSearchableAssets: ObserveSearchableAssets,
    ) : Presenter.Factory {
        override fun create(
            screen: Screen,
            navigator: Navigator,
            context: CircuitContext,
        ): Presenter<*>? {
            return when (screen) {
                SelectAssetsScreen -> SelectAssetsPresenter(
                    navigator, assetsRepository, selectedAssetsRepository,
                    observeSearchableAssets,
                )

                else -> null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectAssetsScreenUi(
    state: SelectAssetsScreen.State,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TopAppBarSearch(
                        query = state.query,
                        onQueryChange = { query -> state.eventSink(QueryChanged(query)) },
                        onClearQuery = { state.eventSink(ClearQuery) },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(GoBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.only(
            WindowInsetsSides.Start + WindowInsetsSides.End + WindowInsetsSides.Top
        ),
        modifier = modifier,
    ) { innerPadding ->
        val navigationBarHeight = ScaffoldDefaults.contentWindowInsets
            .only(WindowInsetsSides.Bottom)
            .asPaddingValues()
            .calculateBottomPadding()
        val listOffset = 8.dp

        val listState = rememberLazyListState()
        LaunchedEffect(state.query) {
            listState.animateScrollToItem(0)
        }

        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(
                start = listOffset,
                end = listOffset,
                top = listOffset,
                bottom = listOffset + navigationBarHeight,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(innerPadding),
        ) {
            items(
                state.assets,
                key = { it.asset.code },
            ) { asset ->
                SelectableAssetItem(
                    asset = asset,
                    onSelectedChanged = { isSelected ->
                        state.eventSink(AssetSelectionChanged(asset.asset.code, isSelected))
                    },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewFxRateListScreenUi(
    @PreviewParameter(SelectableAssetListPreviewParameterProvider::class, limit = 1)
    assets: List<SelectableAsset>,
) {
    SelectAssetsScreenUi(
        state = SelectAssetsScreen.State(
            assets = assets,
            query = "",
            eventSink = {},
        )
    )
}