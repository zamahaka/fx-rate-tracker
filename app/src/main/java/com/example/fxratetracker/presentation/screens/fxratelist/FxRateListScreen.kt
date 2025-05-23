package com.example.fxratetracker.presentation.screens.fxratelist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.example.fxratetracker.domain.model.FxRate
import com.example.fxratetracker.domain.repository.SelectedAssetsRepository
import com.example.fxratetracker.domain.usecase.AutorefreshSelectedFxRates
import com.example.fxratetracker.domain.usecase.ObserveSelectedFxRates
import com.example.fxratetracker.presentation.screens.fxratelist.FxRateListScreen.Alert
import com.example.fxratetracker.presentation.screens.fxratelist.FxRateListScreen.AutorefreshState
import com.example.fxratetracker.presentation.screens.fxratelist.FxRateListScreen.Event.ClearAlert
import com.example.fxratetracker.presentation.screens.fxratelist.FxRateListScreen.Event.RemoveRate
import com.example.fxratetracker.presentation.screens.fxratelist.FxRateListScreen.Event.RestartAutorefresh
import com.example.fxratetracker.presentation.screens.fxratelist.FxRateListScreen.Event.SelectAssets
import com.example.fxratetracker.presentation.screens.selectassets.SelectAssetsScreen
import com.example.fxratetracker.presentation.ui.circuit.wrapEventSink
import com.example.fxratetracker.presentation.ui.components.FxRateItem
import com.example.fxratetracker.presentation.ui.components.SwipeToDeleteContainer
import com.example.fxratetracker.presentation.ui.preview.FxRateListPreviewParameterProvider
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
data object FxRateListScreen : Screen {
    data class State(
        val fxRates: List<FxRate>,
        val autorefreshState: AutorefreshState,
        val alert: Alert?,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed interface AutorefreshState {
        data object Stale : AutorefreshState
        data class Running(val refreshedAt: LocalDateTime) : AutorefreshState
        data object Failed : AutorefreshState
    }

    sealed interface Alert {
        data object FailedToRemoveRate : Alert
    }

    sealed interface Event : CircuitUiEvent {
        data object SelectAssets : Event
        data object RestartAutorefresh : Event
        data class RemoveRate(val rate: FxRate) : Event
        data object ClearAlert : Event
    }
}

class FxRateListPresenter(
    private val navigator: Navigator,
    private val observeSelectedRates: ObserveSelectedFxRates,
    private val autorefreshSelectedFxRates: AutorefreshSelectedFxRates,
    private val selectedAssetsRepository: SelectedAssetsRepository,
) : Presenter<FxRateListScreen.State> {


    @Composable
    override fun present(): FxRateListScreen.State {
        var autorefreshGeneration by remember { mutableIntStateOf(0) }
        val autoRefreshState by remember(autorefreshGeneration) { autorefreshSelectedFxRates() }
            .collectAsRetainedState(AutorefreshSelectedFxRates.State.Stale)
        val rates by remember { observeSelectedRates() }.collectAsRetainedState(emptyList())
        var alert by remember { mutableStateOf<Alert?>(null) }

        val eventSink = wrapEventSink { event: FxRateListScreen.Event ->
            when (event) {
                SelectAssets -> navigator.goTo(SelectAssetsScreen)
                RestartAutorefresh -> autorefreshGeneration++
                is RemoveRate -> launch {
                    selectedAssetsRepository.saveAssetSelected(
                        event.rate.referenceAsset.code, false
                    ).onLeft {
                        alert = Alert.FailedToRemoveRate
                    }
                }

                ClearAlert -> alert = null
            }
        }

        return FxRateListScreen.State(
            fxRates = rates,
            autorefreshState = when (val ars = autoRefreshState) {
                AutorefreshSelectedFxRates.State.Stale -> AutorefreshState.Stale
                is AutorefreshSelectedFxRates.State.Failed -> AutorefreshState.Failed
                is AutorefreshSelectedFxRates.State.Running -> AutorefreshState.Running(
                    refreshedAt = ars.refreshTime,
                )
            },
            alert = alert,
            eventSink = eventSink,
        )
    }

    class Factory(
        private val observeSelectedRates: ObserveSelectedFxRates,
        private val autorefreshSelectedFxRates: AutorefreshSelectedFxRates,
        private val selectedAssetsRepository: SelectedAssetsRepository,
    ) : Presenter.Factory {
        override fun create(
            screen: Screen,
            navigator: Navigator,
            context: CircuitContext,
        ): Presenter<*>? {
            return when (screen) {
                FxRateListScreen -> FxRateListPresenter(
                    navigator,
                    observeSelectedRates, autorefreshSelectedFxRates,
                    selectedAssetsRepository,
                )

                else -> null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FxRateListScreenUi(
    state: FxRateListScreen.State,
    modifier: Modifier = Modifier,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val alert = state.alert
    LaunchedEffect(alert) {
        if (alert != null) {
            val alertMessage = when (alert) {
                Alert.FailedToRemoveRate -> "Failed to remove rate"
            }

            snackBarHostState.showSnackbar(alertMessage)
            state.eventSink(ClearAlert)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackBarHostState, Modifier.navigationBarsPadding()) },
        topBar = {
            TopAppBar(
                title = { Text("Exchange Rates") },
                actions = {
                    IconButton(
                        onClick = { state.eventSink(SelectAssets) },
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Select assets",
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
        val navigationBarHeight =
            ScaffoldDefaults.contentWindowInsets.only(WindowInsetsSides.Bottom).asPaddingValues()
                .calculateBottomPadding()
        val listOffset = 8.dp

        LazyColumn(
            contentPadding = PaddingValues(
                start = listOffset,
                end = listOffset,
                top = listOffset,
                bottom = listOffset + navigationBarHeight,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item(key = "last_update") {
                val text = buildAnnotatedString {
                    when (val rs = state.autorefreshState) {
                        is AutorefreshState.Failed -> {
                            append("Auto refresh failed. Displaying stale data.")
                            append(" ")
                            withLink(LinkAnnotation.Clickable("refresh") {
                                state.eventSink(RestartAutorefresh)
                            }) {
                                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                                    append("Click to restart")
                                }
                            }
                        }
                        // TODO: Formater
                        is AutorefreshState.Running -> append("Last update: ${rs.refreshedAt}")
                        AutorefreshState.Stale -> append("Displaying stale data")
                    }
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.animateItem(),
                )
            }

            items(
                state.fxRates,
                key = { "${it.baseAsset.code}${it.referenceAsset.code}" },
            ) { fxRate ->
                SwipeToDeleteContainer(
                    onDelete = { state.eventSink(RemoveRate(fxRate)) },
                    modifier = Modifier.animateItem()
                ) {
                    FxRateItem(
                        fxRate = fxRate,
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun PreviewFxRateListScreenUi(
    @PreviewParameter(FxRateListPreviewParameterProvider::class, limit = 1) fxRates: List<FxRate>,
) {
    FxRateListScreenUi(
        state = FxRateListScreen.State(
            fxRates = fxRates,
            autorefreshState = AutorefreshState.Stale,
            alert = null,
            eventSink = {},
        )
    )
}