package com.yuyulife.assistant.ui.ledger

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.LedgerEntry
import kotlin.math.roundToInt

@Composable
fun SwipeRevealLedgerRow(
    entry: LedgerEntry,
    revealed: Boolean,
    onRevealChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionWidth = 88.dp
    val actionWidthPx = with(LocalDensity.current) { actionWidth.toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val displayedOffset by animateFloatAsState(
        targetValue = when {
            dragging -> dragOffset
            revealed -> -actionWidthPx
            else -> 0f
        },
        label = "ledgerSwipeOffset",
    )
    val dragState = rememberDraggableState { delta ->
        dragOffset = (dragOffset + delta).coerceIn(-actionWidthPx, 0f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                modifier = Modifier
                    .width(actionWidth)
                    .fillMaxHeight()
                    .clickable {
                        onRevealChange(false)
                        onDelete()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text("删除", color = MaterialTheme.colorScheme.onError)
            }
        }

        LedgerEntryRow(
            entry = entry,
            modifier = Modifier
                .offset { IntOffset(displayedOffset.roundToInt(), 0) }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    onDragStarted = {
                        dragging = true
                        dragOffset = if (revealed) -actionWidthPx else 0f
                    },
                    onDragStopped = {
                        dragging = false
                        onRevealChange(dragOffset <= -actionWidthPx * 0.4f)
                    },
                ),
        )
    }
}
