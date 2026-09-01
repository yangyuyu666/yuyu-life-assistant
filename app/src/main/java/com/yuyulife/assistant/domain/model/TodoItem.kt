package com.yuyulife.assistant.domain.model

data class TodoItem(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val deadlineAt: Long?,
)
