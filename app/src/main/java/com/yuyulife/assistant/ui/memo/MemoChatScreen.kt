package com.yuyulife.assistant.ui.memo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.MemoMessage
import com.yuyulife.assistant.util.formatMemoTimestamp
import com.yuyulife.assistant.util.shouldShowMemoTimestamp
import java.io.File

@Composable
fun MemoChatScreen(
    uiState: MemoChatUiState,
    resolveAttachment: (String?) -> File?,
    onBack: () -> Unit,
    onRename: (String) -> Unit,
    onSendText: (String) -> Unit,
    onChooseFiles: () -> Unit,
    onDeleteMessage: (Long) -> Unit,
    onOpenAttachment: (MemoMessage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current
    var draft by remember { mutableStateOf("") }
    var showRenameDialog by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<MemoMessage?>(null) }
    var previewFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(uiState.messages.lastOrNull()?.id) {
        if (uiState.messages.isNotEmpty()) {
            listState.scrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onBack) { Text("‹ 返回") }
            Text(
                text = uiState.thread?.title.orEmpty(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            TextButton(onClick = { showRenameDialog = true }) { Text("改名") }
        }
        HorizontalDivider()

        if (uiState.messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("发送第一条文字或文件吧", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(
                    items = uiState.messages,
                    key = { _, message -> message.id },
                ) { index, message ->
                    val previousTime = uiState.messages.getOrNull(index - 1)?.createdAt
                    if (shouldShowMemoTimestamp(previousTime, message.createdAt)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = formatMemoTimestamp(message.createdAt),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    val attachmentFile = resolveAttachment(message.attachmentPath)
                    MemoMessageBubble(
                        message = message,
                        attachmentFile = attachmentFile,
                        onLongClick = { selectedMessage = message },
                        onOpenImage = { previewFile = it },
                        onOpenAttachment = { onOpenAttachment(message) },
                    )
                }
            }
        }

        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(onClick = onChooseFiles) { Text("＋文件") }
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("写点什么…") },
                maxLines = 5,
            )
            Button(
                onClick = {
                    val message = draft.trim()
                    if (message.isNotEmpty()) {
                        draft = ""
                        onSendText(message)
                    }
                },
                enabled = draft.isNotBlank(),
            ) { Text("发送") }
        }
    }

    if (showRenameDialog && uiState.thread != null) {
        MemoNameDialog(
            title = "修改名称",
            initialName = uiState.thread.title,
            confirmLabel = "保存",
            onDismiss = { showRenameDialog = false },
            onConfirm = {
                showRenameDialog = false
                onRename(it)
            },
        )
    }

    selectedMessage?.let { message ->
        MemoMessageActionsDialog(
            message = message,
            onDismiss = { selectedMessage = null },
            onCopy = {
                clipboard.setText(AnnotatedString(message.text.orEmpty()))
                selectedMessage = null
            },
            onDelete = {
                selectedMessage = null
                onDeleteMessage(message.id)
            },
        )
    }

    previewFile?.let { file ->
        MemoImagePreviewDialog(file = file, onDismiss = { previewFile = null })
    }
}
