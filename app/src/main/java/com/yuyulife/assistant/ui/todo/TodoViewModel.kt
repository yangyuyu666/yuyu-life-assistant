package com.yuyulife.assistant.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuyulife.assistant.data.repository.TodoRepository
import com.yuyulife.assistant.domain.model.TodoItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TodoViewModel(
    private val repository: TodoRepository,
) : ViewModel() {
    val uiState = repository.items
        .map(::TodoUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodoUiState(),
        )

    fun addTodo(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.add(title) }
    }

    fun setCompleted(item: TodoItem, completed: Boolean) {
        viewModelScope.launch { repository.setCompleted(item, completed) }
    }

    fun delete(item: TodoItem) {
        viewModelScope.launch { repository.delete(item) }
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

