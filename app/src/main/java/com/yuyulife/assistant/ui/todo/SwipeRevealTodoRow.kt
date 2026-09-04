package com.yuyulife.assistant.ui.todo

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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.yuyulife.assistant.domain.model.TodoItem
import kotlin.math.roundToInt

@Composable
fun SwipeRevealTodoRow(
    item: TodoItem,
    selectionMode: Boolean,
    selected: Boolean,
    revealedAction: TodoSwipeAction?,
    onRevealChange: (TodoSwipeAction?) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onEditDeadline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val actionWidth = 88.dp
    val actionWidthPx = with(LocalDensity.current) { actionWidth.toPx() }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var dragging by remember { mutableStateOf(false) }
    val targetOffset = when {
        selectionMode -> 0f
        dragging -> dragOffset
        revealedAction == TodoSwipeAction.DELETE -> -actionWidthPx
        revealedAction == TodoSwipeAction.EDIT_DEADLINE -> actionWidthPx
        else -> 0f
    }
    val displayedOffset by animateFloatAsState(
        targetValue = targetOffset,
        label = "todoSwipeOffset",
    )
    val dragState = rememberDraggableState { delta ->
        dragOffset = (dragOffset + delta).coerceIn(-actionWidthPx, actionWidthPx)
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
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.CenterStart)
                    .clickable {
                        onRevealChange(null)
                        onEditDeadline()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "改时间",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Box(
                modifier = Modifier
                    .width(actionWidth)
                    .fillMaxHeight()
                    .clickable {
                        onRevealChange(null)
                        onDelete()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "删除",
                    color = MaterialTheme.colorScheme.onError,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        TodoItemRow(
            item = item,
            selectionMode = selectionMode,
            selected = selected,
            onClick = onClick,
            onLongClick = onLongClick,
            modifier = Modifier
                .offset { IntOffset(displayedOffset.roundToInt(), 0) }
                .draggable(
                    state = dragState,
                    orientation = Orientation.Horizontal,
                    enabled = !selectionMode,
                    onDragStarted = {
                        dragging = true
                        dragOffset = when (revealedAction) {
                            TodoSwipeAction.DELETE -> -actionWidthPx
                            TodoSwipeAction.EDIT_DEADLINE -> actionWidthPx
                            null -> 0f
                        }
                    },
                    onDragStopped = {
                        dragging = false
                        onRevealChange(
                            when {
                                dragOffset <= -actionWidthPx * 0.4f -> TodoSwipeAction.DELETE
                                dragOffset >= actionWidthPx * 0.4f -> TodoSwipeAction.EDIT_DEADLINE
                                else -> null
                            },
                        )
                    },
                ),
        )
    }
}
