package com.yuyulife.assistant.ui.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yuyulife.assistant.data.repository.LedgerRepository
import com.yuyulife.assistant.data.repository.LedgerCategoryRepository
import com.yuyulife.assistant.domain.model.LedgerSummary
import com.yuyulife.assistant.domain.model.TransactionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.yuyulife.assistant.util.endOfSelectedDayExclusive
import com.yuyulife.assistant.util.endOfSelectedMonthExclusive
import com.yuyulife.assistant.util.shiftDays
import com.yuyulife.assistant.util.shiftMonths
import com.yuyulife.assistant.util.startOfDay
import com.yuyulife.assistant.util.startOfMonth

class LedgerViewModel(
    private val repository: LedgerRepository,
    private val categoryRepository: LedgerCategoryRepository,
) : ViewModel() {
    private val selectedMode = MutableStateFlow(LedgerPeriodMode.DAY)
    private val selectedPeriod = MutableStateFlow(startOfDay(System.currentTimeMillis()))
    private val selectedCategoryId = MutableStateFlow<Long?>(null)

    val uiState = combine(
        repository.entries,
        categoryRepository.categories,
        selectedMode,
        selectedPeriod,
        selectedCategoryId,
    ) { entries, categories, mode, period, requestedCategoryId ->
            val start = when (mode) {
                LedgerPeriodMode.DAY -> startOfDay(period)
                LedgerPeriodMode.MONTH -> startOfMonth(period)
            }
            val endExclusive = when (mode) {
                LedgerPeriodMode.DAY -> endOfSelectedDayExclusive(period)
                LedgerPeriodMode.MONTH -> endOfSelectedMonthExclusive(period)
            }
            val validCategoryId = requestedCategoryId?.takeIf { id ->
                categories.any { it.id == id }
            }
            val filteredEntries = filterLedgerEntries(entries, start, endExclusive, validCategoryId)
            LedgerUiState(
                entries = filteredEntries,
                summary = LedgerSummary.from(filteredEntries),
                periodMode = mode,
                selectedPeriod = start,
                categories = categories,
                selectedCategoryId = validCategoryId,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LedgerUiState(),
        )

    fun addEntry(
        amountCents: Long,
        categoryId: Long,
        note: String,
        occurredAt: Long,
    ) {
        if (amountCents <= 0 || uiState.value.categories.none { it.id == categoryId }) return
        viewModelScope.launch {
            repository.add(amountCents, categoryId, note, occurredAt)
        }
    }

    fun selectMode(mode: LedgerPeriodMode) {
        selectedMode.value = mode
        selectedPeriod.value = when (mode) {
            LedgerPeriodMode.DAY -> startOfDay(selectedPeriod.value)
            LedgerPeriodMode.MONTH -> startOfMonth(selectedPeriod.value)
        }
    }

    fun selectPeriod(timestamp: Long) {
        selectedPeriod.value = when (selectedMode.value) {
            LedgerPeriodMode.DAY -> startOfDay(timestamp)
            LedgerPeriodMode.MONTH -> startOfMonth(timestamp)
        }
    }

    fun movePeriod(amount: Int) {
        selectedPeriod.value = when (selectedMode.value) {
            LedgerPeriodMode.DAY -> shiftDays(selectedPeriod.value, amount)
            LedgerPeriodMode.MONTH -> shiftMonths(selectedPeriod.value, amount)
        }
    }

    fun selectCategory(categoryId: Long?) {
        selectedCategoryId.value = categoryId
    }

    fun deleteEntry(entryId: Long) {
        val entry = uiState.value.entries.firstOrNull { it.id == entryId } ?: return
        viewModelScope.launch { repository.delete(entry) }
    }

    companion object {
        fun factory(
            repository: LedgerRepository,
            categoryRepository: LedgerCategoryRepository,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LedgerViewModel(repository, categoryRepository) as T
                }
            }
    }
}
