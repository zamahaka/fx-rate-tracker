@file:OptIn(ExperimentalAnimationApi::class)

package com.example.fxratetracker.presentation.screens.selectassets

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.example.fxratetracker.domain.model.AssetCode
import com.example.fxratetracker.domain.model.Failure
import com.example.fxratetracker.domain.model.SelectableAsset
import com.example.fxratetracker.domain.repository.AssetsRepository
import com.example.fxratetracker.domain.repository.SelectedAssetsRepository
import com.example.fxratetracker.domain.usecase.ObserveSearchableAssets
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreen.Event.AssetSelectionChanged
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreen.Event.ClearQuery
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreen.Event.GoBack
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreen.Event.QueryChanged
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreen.Event.Retry
import com.example.fxratetracker.presentation.ui.circuit.wrapEventSink
import com.example.fxratetracker.presentation.ui.components.SelectableAssetItem
import com.example.fxratetracker.presentation.ui.components.TopAppBarSearch
import com.example.fxratetracker.presentation.ui.preview.SelectableAssetListPreviewParameterProvider
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.retained.produceRetainedState
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

    sealed interface State : CircuitUiState {
        val eventSink: (GoBack) -> Unit

        data class Loading(
            override val eventSink: (Event.LoadingEvent) -> Unit,
        ) : State

        data class Failed(
            override val eventSink: (Event.FailedEvent) -> Unit,
        ) : State

        data class Loaded(
            val assets: List<SelectableAsset>,
            val query: String,
            override val eventSink: (Event.LoadedEvent) -> Unit,
        ) : State
    }

    sealed interface Event : CircuitUiEvent {
        sealed interface LoadingEvent : Event
        sealed interface LoadedEvent : Event
        sealed interface FailedEvent : Event

        data class AssetSelectionChanged(val id: AssetCode, val isSelected: Boolean) : LoadedEvent
        data class QueryChanged(val value: String) : LoadedEvent
        data object Retry : FailedEvent

        data object ClearQuery : LoadedEvent
        data object GoBack : LoadedEvent, FailedEvent, LoadingEvent
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
        var loadGeneration by remember { mutableIntStateOf(1) }
        val loadState by produceRetainedState<LoadState>(LoadState.Loading, loadGeneration) {
            value = LoadState.Loading
            val result = assetsRepository.getAssets()
            value = result.fold(
                { LoadState.Failed(it) },
                { LoadState.Loaded },
            )
        }

        var query by remember { mutableStateOf("") }
        val assets by remember(query) { observeSearchableAssets(query) }
            .collectAsRetainedState(emptyList())
        val selectedCodes by selectedAssetsRepository.observeSelectedAssets()
            .collectAsRetainedState(emptySet())

        val selectableAssets = assets.map {
            SelectableAsset(
                asset = it,
                isSelected = it.code in selectedCodes,
            )
        }

        val eventSink = wrapEventSink { event: SelectAssetsScreen.Event ->
            when (event) {
                is AssetSelectionChanged -> launch {
                    // TODO: Post message to the user
                    selectedAssetsRepository.saveAssetSelected(
                        id = event.id,
                        isSelected = event.isSelected,
                    )
                }

                is QueryChanged -> query = event.value
                ClearQuery -> query = ""
                Retry -> loadGeneration++
                GoBack -> navigator.pop()
            }
        }

        return when (loadState) {
            LoadState.Loading -> SelectAssetsScreen.State.Loading(eventSink)
            is LoadState.Failed -> SelectAssetsScreen.State.Failed(eventSink)
            LoadState.Loaded -> SelectAssetsScreen.State.Loaded(
                assets = selectableAssets,
                query = query,
                eventSink = eventSink,
            )
        }
    }

    sealed interface LoadState {
        data object Loading : LoadState
        data object Loaded : LoadState
        data class Failed(val failure: Failure) : LoadState
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
                    // TODO: Think of a better approach. Maybe placeholder TopAppBarSearch?
                    val query = when (state) {
                        is SelectAssetsScreen.State.Loaded -> state.query
                        is SelectAssetsScreen.State.Loading,
                        is SelectAssetsScreen.State.Failed,
                            -> ""
                    }
                    val enabled = when (state) {
                        is SelectAssetsScreen.State.Loaded -> true
                        is SelectAssetsScreen.State.Loading,
                        is SelectAssetsScreen.State.Failed,
                            -> false
                    }

                    val eventSink = { event: SelectAssetsScreen.Event.LoadedEvent ->
                        if (state is SelectAssetsScreen.State.Loaded) state.eventSink(event)
                    }

                    TopAppBarSearch(
                        enabled = enabled,
                        query = query,
                        onQueryChange = { q -> eventSink(QueryChanged(q)) },
                        onClearQuery = { eventSink(ClearQuery) },
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
        val transition = updateTransition(state)
        transition.Crossfade(
            contentKey = { state ->
                when (state) {
                    is SelectAssetsScreen.State.Loading -> "Loading"
                    is SelectAssetsScreen.State.Failed -> "Failed"
                    is SelectAssetsScreen.State.Loaded -> "Loaded"
                }
            },
        ) { state ->
            val innerModifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
            when (state) {
                is SelectAssetsScreen.State.Loading -> RenderLoadingState(state, innerModifier)
                is SelectAssetsScreen.State.Failed -> RenderFailedState(state, innerModifier)
                is SelectAssetsScreen.State.Loaded -> RenderLoadedState(state, innerModifier)
            }
        }
    }
}

@Composable
private fun RenderLoadingState(
    state: SelectAssetsScreen.State.Loading,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun RenderFailedState(
    state: SelectAssetsScreen.State.Failed,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            18.dp, alignment = Alignment.CenterVertically,
        ),
        modifier = modifier,
    ) {
        Text("Loading failed")
        FilledTonalButton(
            onClick = { state.eventSink(Retry) }
        ) { Text("Retry") }
    }
}

@Composable
private fun RenderLoadedState(
    state: SelectAssetsScreen.State.Loaded,
    modifier: Modifier = Modifier,
) {
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
        modifier = modifier,
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

@Preview(showSystemUi = true)
@Composable
private fun PreviewSelectAssetsScreenLoadedUi(
    @PreviewParameter(SelectableAssetListPreviewParameterProvider::class, limit = 1)
    assets: List<SelectableAsset>,
) {
    SelectAssetsScreenUi(
        state = SelectAssetsScreen.State.Loaded(
            assets = assets,
            query = "",
            eventSink = {},
        )
    )
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewSelectAssetsScreenLoadingUi() {
    SelectAssetsScreenUi(
        state = SelectAssetsScreen.State.Loading(
            eventSink = {},
        )
    )
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewSelectAssetsScreenFailedUi() {
    SelectAssetsScreenUi(
        state = SelectAssetsScreen.State.Failed(
            eventSink = {},
        )
    )
}