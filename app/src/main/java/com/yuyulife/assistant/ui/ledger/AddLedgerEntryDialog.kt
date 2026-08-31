package com.yuyulife.assistant.ui.ledger

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.LedgerCategories
import com.yuyulife.assistant.domain.model.TransactionType
import com.yuyulife.assistant.util.parseAmountToCents

@Composable
fun AddLedgerEntryDialog(
    onDismiss: () -> Unit,
    onAdd: (TransactionType, Long, String, String) -> Unit,
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(LedgerCategories.forType(type).first()) }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun selectType(newType: TransactionType) {
        type = newType
        category = LedgerCategories.forType(newType).first()
    }

    fun submit() {
        val cents = parseAmountToCents(amount)
        if (cents == null) {
            errorMessage = "请输入大于 0 的有效金额"
            return
        }
        onAdd(type, cents, category, note)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("记一笔") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TransactionType.entries) { item ->
                        FilterChip(
                            selected = type == item,
                            onClick = { selectType(item) },
                            label = { Text(item.displayName) },
                        )
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        errorMessage = null
                    },
                    label = { Text("金额") },
                    prefix = { Text("¥") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    supportingText = {
                        errorMessage?.let { Text(it) }
                    },
                    singleLine = true,
                )

                Text("分类")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(LedgerCategories.forType(type)) { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item) },
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（可选）") },
                    singleLine = true,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = ::submit) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
