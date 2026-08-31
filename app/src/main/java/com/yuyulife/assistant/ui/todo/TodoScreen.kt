package com.yuyulife.assistant.ui.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

    TodoScreen(
        uiState = uiState,
        onAdd = viewModel::addTodo,
        onCompletedChange = viewModel::setCompleted,
        onDelete = viewModel::delete,
        modifier = modifier,
    )
}

@Composable
fun TodoScreen(
    uiState: TodoUiState,
    onAdd: (String) -> Unit,
    onCompletedChange: (TodoItem, Boolean) -> Unit,
    onDelete: (TodoItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }

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
                    text = "剩余 ${uiState.remainingCount} 项 · 共 ${uiState.items.size} 项",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(onClick = { showAddDialog = true }) {
                Text("添加")
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
                    TodoItemRow(
                        item = item,
                        onCompletedChange = { onCompletedChange(item, it) },
                        onDelete = { onDelete(item) },
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
}
