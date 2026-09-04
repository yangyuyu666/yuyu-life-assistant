package com.yuyulife.assistant.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuyulife.assistant.data.repository.SettingsRepository
import com.yuyulife.assistant.data.repository.LedgerCategoryRepository
import com.yuyulife.assistant.data.repository.CategoryChangeResult
import com.yuyulife.assistant.domain.model.TransactionType
import com.yuyulife.assistant.domain.model.ReminderMode
import com.yuyulife.assistant.data.repository.TodoRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val todoRepository: TodoRepository,
    private val categoryRepository: LedgerCategoryRepository,
) : ViewModel() {
    val uiState = combine(
        settingsRepository.settings,
        categoryRepository.categories,
    ) { settings, categories -> SettingsUiState(settings, categories) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun setTodoReminderLeadMinutes(minutes: Int) {
        settingsRepository.setTodoReminderLeadMinutes(minutes)
        viewModelScope.launch { todoRepository.reschedulePendingReminders() }
    }

    fun setReminderMode(mode: ReminderMode) {
        settingsRepository.setReminderMode(mode)
        viewModelScope.launch { todoRepository.reschedulePendingReminders() }
    }

    fun refreshReminderSchedule() {
        viewModelScope.launch { todoRepository.reschedulePendingReminders() }
    }

    fun setCustomBackgroundEnabled(enabled: Boolean) {
        settingsRepository.setCustomBackgroundEnabled(enabled)
    }

    fun setCustomBackground(uri: String) {
        settingsRepository.setCustomBackground(uri)
    }

    fun addCategory(
        type: TransactionType,
        name: String,
        onResult: (CategoryChangeResult) -> Unit,
    ) {
        viewModelScope.launch { onResult(categoryRepository.add(type, name)) }
    }

    fun renameCategory(
        id: Long,
        name: String,
        onResult: (CategoryChangeResult) -> Unit,
    ) {
        viewModelScope.launch { onResult(categoryRepository.rename(id, name)) }
    }

    fun deleteCategory(id: Long, onResult: (CategoryChangeResult) -> Unit) {
        viewModelScope.launch { onResult(categoryRepository.delete(id)) }
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            todoRepository: TodoRepository,
            categoryRepository: LedgerCategoryRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(settingsRepository, todoRepository, categoryRepository) as T
            }
        }
    }
}
