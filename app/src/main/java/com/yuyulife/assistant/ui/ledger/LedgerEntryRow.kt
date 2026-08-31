package com.yuyulife.assistant.ui.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.LedgerEntry
import com.yuyulife.assistant.domain.model.TransactionType
import com.yuyulife.assistant.util.formatCents
import com.yuyulife.assistant.util.formatTimestamp

@Composable
fun LedgerEntryRow(
    entry: LedgerEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = entry.note.ifBlank { entry.category },
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "${entry.category} · ${formatTimestamp(entry.occurredAt)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                val isIncome = entry.type == TransactionType.INCOME
                Text(
                    text = (if (isIncome) "+" else "-") + formatCents(entry.amountCents),
                    color = if (isIncome) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
                TextButton(onClick = onDelete) {
                    Text("删除")
                }
            }
        }
    }
}

