package com.yuyulife.assistant.ui.todo

import com.yuyulife.assistant.domain.model.TodoItem

data class TodoUiState(
    val items: List<TodoItem> = emptyList(),
) {
    val remainingCount: Int
        get() = items.count { !it.isCompleted }
}

