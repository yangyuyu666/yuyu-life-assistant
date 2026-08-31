package com.yuyulife.assistant.domain.model

data class TodoItem(
    val id: Long,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: Long,
    val deadlineAt: Long?,
)
