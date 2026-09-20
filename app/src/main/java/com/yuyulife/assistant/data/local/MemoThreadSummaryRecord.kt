package com.yuyulife.assistant.data.local

data class MemoThreadSummaryRecord(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val lastMessageKind: String?,
    val lastText: String?,
    val lastAttachmentDisplayName: String?,
    val lastMessageAt: Long?,
)
