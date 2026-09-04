package com.yuyulife.assistant.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuyulife.assistant.data.repository.TodoRepository
import com.yuyulife.assistant.domain.model.TodoItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(
    private val repository: TodoRepository,
) : ViewModel() {
    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())

    val uiState = combine(repository.items, selectedIds) { items, selection ->
        TodoUiState(
            items = items,
            selectedIds = selection.intersect(items.mapTo(mutableSetOf()) { it.id }),
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodoUiState(),
        )

    fun addTodo(title: String, deadlineAt: Long) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.add(title, deadlineAt) }
    }

    fun enterSelection(itemId: Long) {
        selectedIds.value = setOf(itemId)
    }

    fun toggleSelection(itemId: Long) {
        selectedIds.value = toggleSelection(selectedIds.value, itemId)
    }

    fun clearSelection() {
        selectedIds.value = emptySet()
    }

    fun toggleSelectAll() {
        val allIds = uiState.value.items.mapTo(mutableSetOf()) { it.id }
        selectedIds.value = toggleSelectAll(selectedIds.value, allIds)
    }

    fun delete(item: TodoItem) {
        viewModelScope.launch { repository.delete(item) }
    }

    fun updateDeadline(item: TodoItem, deadlineAt: Long) {
        if (deadlineAt <= System.currentTimeMillis()) return
        viewModelScope.launch { repository.updateDeadline(item, deadlineAt) }
    }

    fun deleteSelected() {
        val items = uiState.value.items.filter { it.id in uiState.value.selectedIds }
        clearSelection()
        viewModelScope.launch { repository.delete(items) }
    }

    companion object {
        fun factory(repository: TodoRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TodoViewModel(repository) as T
                }
            }
    }
}
