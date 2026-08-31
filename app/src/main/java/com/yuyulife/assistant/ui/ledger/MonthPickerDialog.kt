package com.yuyulife.assistant.ui.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.util.dateParts
import com.yuyulife.assistant.util.monthTimestamp

@Composable
fun MonthPickerDialog(
    selectedMonth: Long,
    onDismiss: () -> Unit,
    onMonthSelected: (Long) -> Unit,
) {
    val (initialYear, initialMonth, _) = dateParts(selectedMonth)
    var year by remember { mutableIntStateOf(initialYear) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { year-- }) { Text("‹") }
                Text("${year}年")
                IconButton(onClick = { year++ }) { Text("›") }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { rowIndex ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        repeat(3) { columnIndex ->
                            val month = rowIndex * 3 + columnIndex
                            FilterChip(
                                modifier = Modifier.weight(1f),
                                selected = year == initialYear && month == initialMonth,
                                onClick = {
                                    onMonthSelected(monthTimestamp(year, month))
                                    onDismiss()
                                },
                                label = { Text("${month + 1}月") },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
