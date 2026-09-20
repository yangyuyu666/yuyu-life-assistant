package com.yuyulife.assistant.ui.memo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.MemoMessage
import com.yuyulife.assistant.domain.model.MemoMessageKind
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MemoMessageBubble(
    message: MemoMessage,
    attachmentFile: File?,
    onLongClick: () -> Unit,
    onOpenImage: (File) -> Unit,
    onOpenAttachment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .combinedClickable(onClick = {}, onLongClick = onLongClick),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.medium,
        ) {
            when (message.kind) {
                MemoMessageKind.TEXT -> Text(
                    text = message.text.orEmpty(),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )

                MemoMessageKind.FILE -> {
                    val isImage = message.attachmentMimeType?.startsWith("image/") == true
                    if (isImage && attachmentFile != null) {
                        MemoImageMessage(
                            file = attachmentFile,
                            onOpen = { onOpenImage(attachmentFile) },
                            onLongClick = onLongClick,
                            modifier = Modifier.padding(4.dp),
                        )
                    } else {
                        MemoAttachmentCard(
                            message = message,
                            available = attachmentFile != null,
                            onOpen = onOpenAttachment,
                            onLongClick = onLongClick,
                            modifier = Modifier.padding(4.dp),
                        )
                    }
                }
            }
        }
    }
}
