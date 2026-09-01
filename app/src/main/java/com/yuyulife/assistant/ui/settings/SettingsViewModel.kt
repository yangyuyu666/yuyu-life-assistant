package com.yuyulife.assistant.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuyulife.assistant.data.repository.SettingsRepository
import com.yuyulife.assistant.data.repository.TodoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val todoRepository: TodoRepository,
) : ViewModel() {
    val uiState = settingsRepository.settings
        .map(::SettingsUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun setTodoReminderLeadMinutes(minutes: Int) {
        settingsRepository.setTodoReminderLeadMinutes(minutes)
        viewModelScope.launch { todoRepository.reschedulePendingReminders() }
    }

    fun setCustomBackgroundEnabled(enabled: Boolean) {
        settingsRepository.setCustomBackgroundEnabled(enabled)
    }

    fun setCustomBackground(uri: String) {
        settingsRepository.setCustomBackground(uri)
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            todoRepository: TodoRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(settingsRepository, todoRepository) as T
            }
        }
    }
}
