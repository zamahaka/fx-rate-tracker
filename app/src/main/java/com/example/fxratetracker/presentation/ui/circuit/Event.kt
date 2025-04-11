package com.example.fxratetracker.presentation.ui.circuit

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.runtime.CircuitUiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

@Composable
inline fun <E : CircuitUiEvent> wrapEventSink(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    crossinline eventSink: CoroutineScope.(E) -> Unit,
): (E) -> Unit = { event ->
    if (coroutineScope.isActive) {
        coroutineScope.eventSink(event)
    } else {
        Log.i(
            "EventSink",
            "Received event, but CoroutineScope is no longer active. See stack trace for caller.",
        )
    }
}