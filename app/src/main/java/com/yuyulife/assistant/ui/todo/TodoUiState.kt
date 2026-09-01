package com.yuyulife.assistant.ui.todo

import com.yuyulife.assistant.domain.model.TodoItem

data class TodoUiState(
    val items: List<TodoItem> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
) {
    val isSelectionMode: Boolean
        get() = selectedIds.isNotEmpty()

    val selectedCount: Int
        get() = selectedIds.size
}
