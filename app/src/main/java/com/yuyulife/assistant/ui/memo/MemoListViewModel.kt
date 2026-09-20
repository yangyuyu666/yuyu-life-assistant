package com.yuyulife.assistant.ui.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuyulife.assistant.data.repository.MemoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MemoListEvent {
    data class OpenThread(val threadId: Long) : MemoListEvent
    data class Error(val message: String) : MemoListEvent
}

class MemoListViewModel(private val repository: MemoRepository) : ViewModel() {
    val uiState = repository.threads
        .map(::MemoListUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MemoListUiState(),
        )

    private val eventChannel = Channel<MemoListEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun createThread(title: String) {
        viewModelScope.launch {
            runCatching { repository.createThread(title) }
                .onSuccess { eventChannel.send(MemoListEvent.OpenThread(it)) }
                .onFailure { eventChannel.send(MemoListEvent.Error(it.message ?: "创建失败")) }
        }
    }

    fun deleteThread(threadId: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteThread(threadId) }
                .onFailure { eventChannel.send(MemoListEvent.Error("删除失败")) }
        }
    }

    companion object {
        fun factory(repository: MemoRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MemoListViewModel(repository) as T
            }
    }
}
