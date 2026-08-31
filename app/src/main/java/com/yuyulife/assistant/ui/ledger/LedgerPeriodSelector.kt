package com.yuyulife.assistant.ui.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.ui.component.DatePickerButton
import com.yuyulife.assistant.util.formatMonth

@Composable
fun LedgerPeriodSelector(
    mode: LedgerPeriodMode,
    selectedPeriod: Long,
    onModeSelected: (LedgerPeriodMode) -> Unit,
    onPeriodSelected: (Long) -> Unit,
    onMovePeriod: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMonthPicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LedgerPeriodMode.entries.forEach { item ->
                FilterChip(
                    selected = mode == item,
                    onClick = { onModeSelected(item) },
                    label = { Text(item.label) },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconButton(onClick = { onMovePeriod(-1) }) { Text("‹") }
            if (mode == LedgerPeriodMode.DAY) {
                DatePickerButton(
                    selectedDate = selectedPeriod,
                    onDateSelected = onPeriodSelected,
                    modifier = Modifier.weight(1f),
                    labelPrefix = "",
                )
            } else {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { showMonthPicker = true },
                ) {
                    Text(formatMonth(selectedPeriod))
                }
            }
            IconButton(onClick = { onMovePeriod(1) }) { Text("›") }
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            selectedMonth = selectedPeriod,
            onDismiss = { showMonthPicker = false },
            onMonthSelected = onPeriodSelected,
        )
    }
}
