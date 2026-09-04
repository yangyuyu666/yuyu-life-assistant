package com.yuyulife.assistant.ui.todo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuyulife.assistant.data.repository.TodoRepository
import com.yuyulife.assistant.domain.model.TodoItem
import com.yuyulife.assistant.ui.component.EmptyState

@Composable
fun TodoRoute(
    repository: TodoRepository,
    modifier: Modifier = Modifier,
) {
    val factory = remember(repository) { TodoViewModel.factory(repository) }
    val viewModel: TodoViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(viewModel) {
        onDispose { viewModel.clearSelection() }
    }

    TodoScreen(
        uiState = uiState,
        onAdd = viewModel::addTodo,
        onDelete = viewModel::delete,
        onEnterSelection = viewModel::enterSelection,
        onToggleSelection = viewModel::toggleSelection,
        onClearSelection = viewModel::clearSelection,
        onDeleteSelected = viewModel::deleteSelected,
        onToggleSelectAll = viewModel::toggleSelectAll,
        onUpdateDeadline = viewModel::updateDeadline,
        modifier = modifier,
    )
}

@Composable
fun TodoScreen(
    uiState: TodoUiState,
    onAdd: (String, Long) -> Unit,
    onDelete: (TodoItem) -> Unit,
    onEnterSelection: (Long) -> Unit,
    onToggleSelection: (Long) -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onUpdateDeadline: (TodoItem, Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var revealedTodo by remember { mutableStateOf<Pair<Long, TodoSwipeAction>?>(null) }
    var editingTodo by remember { mutableStateOf<TodoItem?>(null) }

    BackHandler(enabled = uiState.isSelectionMode, onBack = onClearSelection)
    LaunchedEffect(uiState.isSelectionMode) {
        if (uiState.isSelectionMode) revealedTodo = null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "我的待办",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "共 ${uiState.items.size} 项事务",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (uiState.isSelectionMode) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onToggleSelectAll) {
                        Text(if (uiState.selectedCount == uiState.items.size) "取消全选" else "全选")
                    }
                    Button(
                        onClick = onDeleteSelected,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                        ),
                    ) {
                        Text("删除（${uiState.selectedCount}）")
                    }
                }
            } else {
                Button(onClick = { showAddDialog = true }) {
                    Text("添加")
                }
            }
        }

        if (uiState.items.isEmpty()) {
            EmptyState(
                title = "今天还没有待办",
                message = "记下一件要完成的小事，让生活更从容。",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = uiState.items,
                    key = TodoItem::id,
                ) { item ->
                    SwipeRevealTodoRow(
                        item = item,
                        selectionMode = uiState.isSelectionMode,
                        selected = item.id in uiState.selectedIds,
                        revealedAction = revealedTodo?.takeIf { it.first == item.id }?.second,
                        onRevealChange = { action ->
                            revealedTodo = action?.let { item.id to it }
                        },
                        onClick = {
                            if (uiState.isSelectionMode) {
                                onToggleSelection(item.id)
                            } else if (revealedTodo?.first == item.id) {
                                revealedTodo = null
                            }
                        },
                        onLongClick = {
                            if (uiState.isSelectionMode) {
                                onToggleSelection(item.id)
                            } else {
                                onEnterSelection(item.id)
                            }
                        },
                        onDelete = { onDelete(item) },
                        onEditDeadline = {
                            revealedTodo = null
                            editingTodo = item
                        },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = { showAddDialog = false },
            onAdd = onAdd,
        )
    }

    editingTodo?.let { item ->
        EditTodoDeadlineDialog(
            item = item,
            onDismiss = { editingTodo = null },
            onSave = { deadline -> onUpdateDeadline(item, deadline) },
        )
    }
}
