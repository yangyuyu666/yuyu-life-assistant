package com.yuyulife.assistant.ui.memo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.MemoMessage
import com.yuyulife.assistant.domain.model.MemoMessageKind
import com.yuyulife.assistant.util.formatMemoExactTimestamp

@Composable
fun MemoMessageActionsDialog(
    message: MemoMessage,
    onDismiss: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("消息操作") },
        text = { Text("记录时间：${formatMemoExactTimestamp(message.createdAt)}") },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (message.kind == MemoMessageKind.TEXT) {
                    TextButton(onClick = onCopy) { Text("复制") }
                }
                TextButton(onClick = onDelete) { Text("删除") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
