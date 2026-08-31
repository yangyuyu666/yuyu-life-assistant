package com.yuyulife.assistant.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuyulife.assistant.data.repository.LedgerRepository
import com.yuyulife.assistant.domain.model.LedgerSummary
import com.yuyulife.assistant.domain.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LedgerViewModel(
    private val repository: LedgerRepository,
) : ViewModel() {
    val uiState = repository.entries
        .map { entries ->
            LedgerUiState(
                entries = entries,
                summary = LedgerSummary.from(entries),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LedgerUiState(),
        )

    fun addEntry(
        type: TransactionType,
        amountCents: Long,
        category: String,
        note: String,
    ) {
        if (amountCents <= 0 || category.isBlank()) return
        viewModelScope.launch {
            repository.add(type, amountCents, category, note)
        }
    }

    fun deleteEntry(entryId: Long) {
        val entry = uiState.value.entries.firstOrNull { it.id == entryId } ?: return
        viewModelScope.launch { repository.delete(entry) }
    }

    companion object {
        fun factory(repository: LedgerRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LedgerViewModel(repository) as T
                }
            }
    }
}

