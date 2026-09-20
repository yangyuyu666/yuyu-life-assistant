package com.yuyulife.assistant.domain.model

data class MemoThreadSummary(
    val thread: MemoThread,
    val lastMessageKind: MemoMessageKind?,
    val lastText: String?,
    val lastAttachmentDisplayName: String?,
    val lastMessageAt: Long?,
) {
    val preview: String
        get() = when (lastMessageKind) {
            MemoMessageKind.TEXT -> lastText.orEmpty().replace('\n', ' ')
            MemoMessageKind.FILE -> {
                val name = lastAttachmentDisplayName.orEmpty()
                if (name.isBlank()) "[文件]" else "[文件] $name"
            }
            null -> "还没有消息"
        }
}
