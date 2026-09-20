package com.yuyulife.assistant.ui.memo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.MemoThreadSummary
import com.yuyulife.assistant.ui.component.EmptyState

@Composable
fun MemoListScreen(
    uiState: MemoListUiState,
    onCreate: (String) -> Unit,
    onOpen: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var revealedThreadId by remember { mutableStateOf<Long?>(null) }
    var pendingDelete by remember { mutableStateOf<MemoThreadSummary?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("备忘录", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "像聊天一样记录生活",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(onClick = { showCreateDialog = true }) { Text("新建") }
        }

        if (uiState.threads.isEmpty()) {
            EmptyState(
                title = "还没有备忘录",
                message = "新建一个会话，随时追加文字或文件。",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(uiState.threads, key = { it.thread.id }) { summary ->
                    SwipeRevealMemoThreadRow(
                        summary = summary,
                        revealed = revealedThreadId == summary.thread.id,
                        onRevealChange = { revealed ->
                            revealedThreadId = if (revealed) summary.thread.id else {
                                revealedThreadId.takeUnless { it == summary.thread.id }
                            }
                        },
                        onOpen = { onOpen(summary.thread.id) },
                        onDeleteRequested = { pendingDelete = summary },
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        MemoNameDialog(
            title = "新建备忘录",
            confirmLabel = "创建",
            onDismiss = { showCreateDialog = false },
            onConfirm = {
                showCreateDialog = false
                onCreate(it)
            },
        )
    }

    pendingDelete?.let { summary ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除“${summary.thread.title}”？") },
            text = { Text("其中的全部消息和文件都会永久删除，无法撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDelete = null
                        onDelete(summary.thread.id)
                    },
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
        )
    }
}
