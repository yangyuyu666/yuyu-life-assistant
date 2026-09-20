package com.yuyulife.assistant.ui.memo

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuyulife.assistant.data.repository.MemoRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface MemoChatEvent {
    data class Message(val text: String) : MemoChatEvent
}

class MemoChatViewModel(
    private val repository: MemoRepository,
    private val threadId: Long,
) : ViewModel() {
    val uiState = combine(
        repository.observeThread(threadId),
        repository.observeMessages(threadId),
    ) { thread, messages ->
        MemoChatUiState(thread = thread, messages = messages, isLoaded = true)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MemoChatUiState(),
    )

    private val eventChannel = Channel<MemoChatEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    fun sendText(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            runCatching { repository.sendText(threadId, text) }
                .onFailure { eventChannel.send(MemoChatEvent.Message("发送失败")) }
        }
    }

    fun importFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val result = repository.importFiles(threadId, uris)
            val message = when {
                result.failed == 0 -> "已加入 ${result.succeeded} 个文件"
                result.succeeded == 0 -> "文件加入失败，请检查文件或手机空间"
                else -> "已加入 ${result.succeeded} 个文件，${result.failed} 个失败"
            }
            eventChannel.send(MemoChatEvent.Message(message))
        }
    }

    fun renameThread(title: String) {
        viewModelScope.launch {
            runCatching { repository.renameThread(threadId, title) }
                .onFailure { eventChannel.send(MemoChatEvent.Message(it.message ?: "改名失败")) }
        }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteMessage(messageId) }
                .onFailure { eventChannel.send(MemoChatEvent.Message("删除失败")) }
        }
    }

    companion object {
        fun factory(repository: MemoRepository, threadId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    MemoChatViewModel(repository, threadId) as T
            }
    }
}
