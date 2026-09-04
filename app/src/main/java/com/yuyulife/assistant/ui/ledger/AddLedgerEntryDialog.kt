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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.LedgerCategory
import com.yuyulife.assistant.domain.model.TransactionType
import com.yuyulife.assistant.util.parseAmountToCents
import com.yuyulife.assistant.ui.component.DatePickerButton
import com.yuyulife.assistant.util.dateWithCurrentTime
import com.yuyulife.assistant.util.startOfDay

@Composable
fun AddLedgerEntryDialog(
    onDismiss: () -> Unit,
    onAdd: (Long, Long, String, Long) -> Unit,
    initialDate: Long,
    categories: List<LedgerCategory>,
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf("") }
    var occurredAt by remember(initialDate) { mutableStateOf(startOfDay(initialDate)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(type, categories) {
        if (categories.none { it.id == categoryId && it.type == type }) {
            categoryId = categories.firstOrNull { it.type == type }?.id
        }
    }

    fun selectType(newType: TransactionType) {
        type = newType
        categoryId = categories.firstOrNull { it.type == newType }?.id
    }

    fun submit() {
        val cents = parseAmountToCents(amount)
        if (cents == null) {
            errorMessage = "请输入大于 0 的有效金额"
            return
        }
        val selectedCategoryId = categoryId
        if (selectedCategoryId == null) {
            errorMessage = "请先到设置中添加${type.displayName}分类"
            return
        }
        onAdd(cents, selectedCategoryId, note, dateWithCurrentTime(occurredAt))
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

                DatePickerButton(
                    selectedDate = occurredAt,
                    onDateSelected = { occurredAt = startOfDay(it) },
                    labelPrefix = "记账日期：",
                )

                Text("分类")
                val availableCategories = categories.filter { it.type == type }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableCategories, key = LedgerCategory::id) { item ->
                        FilterChip(
                            selected = categoryId == item.id,
                            onClick = { categoryId = item.id },
                            label = { Text(item.name) },
                        )
                    }
                }
                if (availableCategories.isEmpty()) {
                    Text("请先到设置中添加${type.displayName}分类")
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
