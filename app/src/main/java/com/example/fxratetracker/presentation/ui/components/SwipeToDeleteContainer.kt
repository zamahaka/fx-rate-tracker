@file:OptIn(ExperimentalFoundationApi::class)

package com.example.fxratetracker.presentation.ui.components

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private enum class DragValue { Deleting, Idle }

@Composable
fun SwipeToDeleteContainer(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
    deleteSize: Dp = 48.dp,
    deletePadding: Dp = 8.dp,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val deleteSpace = with(density) { deleteSize.toPx() + deletePadding.toPx() }

    val anchors = DraggableAnchors {
        DragValue.Deleting at -deleteSpace
        DragValue.Idle at 0f
    }

    val positionalThreshold: (distance: Float) -> Float = { it * 0.5f }
    val velocityThreshold = { with(density) { 126.dp.toPx() } }
    val snapAnimationSpec = spring<Float>()
    val decayAnimationSpec = exponentialDecay<Float>()
    val draggableState = rememberSaveable(
        saver = AnchoredDraggableState.Saver(
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold,
            snapAnimationSpec = snapAnimationSpec,
            decayAnimationSpec = decayAnimationSpec,
        ),
    ) {
        AnchoredDraggableState(
            DragValue.Idle,
            positionalThreshold = positionalThreshold,
            velocityThreshold = velocityThreshold,
            snapAnimationSpec = snapAnimationSpec,
            decayAnimationSpec = decayAnimationSpec,
        )
    }.also {
        // Update anchors here, as otherwise saver crashes
        it.updateAnchors(anchors)
    }

    // Do not use requireOffset, as it crashes after restore during configuration change
    val draggedOffset = draggableState.offset.takeUnless { it.isNaN() }
        ?.roundToInt() ?: 0

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .wrapContentSize()
            .anchoredDraggable(draggableState, Orientation.Horizontal)
            .offset { IntOffset(draggedOffset, 0) }
    ) {
        FilledIconButton(
            onClick = onDelete,
            colors = IconButtonDefaults.filledIconButtonColors().copy(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
            ),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(deleteSize)
                .offset { IntOffset(-draggedOffset, 0) }
        ) {
            Icon(Icons.Rounded.Delete, contentDescription = "Delete")
        }

        content()
    }
}