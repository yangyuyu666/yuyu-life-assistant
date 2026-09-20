package com.yuyulife.assistant.domain.model

data class MemoMessage(
    val id: Long,
    val threadId: Long,
    val kind: MemoMessageKind,
    val text: String?,
    val attachmentPath: String?,
    val attachmentDisplayName: String?,
    val attachmentMimeType: String?,
    val attachmentSizeBytes: Long?,
    val createdAt: Long,
)
