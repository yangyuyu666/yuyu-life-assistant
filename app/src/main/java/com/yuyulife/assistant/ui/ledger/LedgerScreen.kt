package com.yuyulife.assistant.ui.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuyulife.assistant.data.repository.LedgerRepository
import com.yuyulife.assistant.domain.model.LedgerEntry
import com.yuyulife.assistant.domain.model.TransactionType
import com.yuyulife.assistant.ui.component.EmptyState

@Composable
fun LedgerRoute(
    repository: LedgerRepository,
    modifier: Modifier = Modifier,
) {
    val factory = remember(repository) { LedgerViewModel.factory(repository) }
    val viewModel: LedgerViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LedgerScreen(
        uiState = uiState,
        onAdd = viewModel::addEntry,
        onDelete = viewModel::deleteEntry,
        onModeSelected = viewModel::selectMode,
        onPeriodSelected = viewModel::selectPeriod,
        onMovePeriod = viewModel::movePeriod,
        modifier = modifier,
    )
}

@Composable
fun LedgerScreen(
    uiState: LedgerUiState,
    onAdd: (TransactionType, Long, String, String, Long) -> Unit,
    onDelete: (Long) -> Unit,
    onModeSelected: (LedgerPeriodMode) -> Unit,
    onPeriodSelected: (Long) -> Unit,
    onMovePeriod: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "生活账本",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "每一笔都心中有数",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Button(onClick = { showAddDialog = true }) {
                Text("记一笔")
            }
        }

        LedgerPeriodSelector(
            mode = uiState.periodMode,
            selectedPeriod = uiState.selectedPeriod,
            onModeSelected = onModeSelected,
            onPeriodSelected = onPeriodSelected,
            onMovePeriod = onMovePeriod,
        )

        LedgerSummaryCard(
            summary = uiState.summary,
            periodLabel = if (uiState.periodMode == LedgerPeriodMode.DAY) "当日" else "当月",
        )

        if (uiState.entries.isEmpty()) {
            EmptyState(
                title = if (uiState.periodMode == LedgerPeriodMode.DAY) {
                    "这一天还没有收支记录"
                } else {
                    "这个月还没有收支记录"
                },
                message = "可以切换日期或月份，也可以立即记一笔。",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(
                    items = uiState.entries,
                    key = LedgerEntry::id,
                ) { entry ->
                    LedgerEntryRow(
                        entry = entry,
                        onDelete = { onDelete(entry.id) },
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddLedgerEntryDialog(
            onDismiss = { showAddDialog = false },
            onAdd = onAdd,
            initialDate = uiState.selectedPeriod,
        )
    }
}
