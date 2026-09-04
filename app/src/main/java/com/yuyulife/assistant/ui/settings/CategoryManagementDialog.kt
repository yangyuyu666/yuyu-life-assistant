package com.yuyulife.assistant.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.data.repository.CategoryChangeResult
import com.yuyulife.assistant.domain.model.LedgerCategory
import com.yuyulife.assistant.domain.model.TransactionType

@Composable
fun CategoryManagementDialog(
    categories: List<LedgerCategory>,
    onAdd: (TransactionType, String, (CategoryChangeResult) -> Unit) -> Unit,
    onRename: (Long, String, (CategoryChangeResult) -> Unit) -> Unit,
    onDelete: (Long, (CategoryChangeResult) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var editingCategory by remember { mutableStateOf<LedgerCategory?>(null) }
    var adding by remember { mutableStateOf(false) }
    var deletingCategory by remember { mutableStateOf<LedgerCategory?>(null) }
    var operationError by remember { mutableStateOf<String?>(null) }
    val visibleCategories = categories.filter { it.type == selectedType }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("收支分类") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransactionType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = {
                                selectedType = type
                                operationError = null
                            },
                            label = { Text(type.displayName) },
                        )
                    }
                }

                Button(
                    onClick = { adding = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("新增${selectedType.displayName}分类")
                }

                operationError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                if (visibleCategories.isEmpty()) {
                    Text("还没有${selectedType.displayName}分类")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(visibleCategories, key = LedgerCategory::id) { category ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(category.name)
                                        if (category.usageCount > 0) {
                                            Text(
                                                "已有 ${category.usageCount} 笔记录，不能删除",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    TextButton(onClick = { editingCategory = category }) {
                                        Text("重命名")
                                    }
                                    TextButton(
                                        onClick = { deletingCategory = category },
                                        enabled = category.usageCount == 0,
                                    ) {
                                        Text("删除")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完成") }
        },
    )

    if (adding) {
        CategoryNameDialog(
            title = "新增${selectedType.displayName}分类",
            initialName = "",
            onSave = { name, callback -> onAdd(selectedType, name, callback) },
            onDismiss = { adding = false },
        )
    }

    editingCategory?.let { category ->
        CategoryNameDialog(
            title = "重命名分类",
            initialName = category.name,
            onSave = { name, callback -> onRename(category.id, name, callback) },
            onDismiss = { editingCategory = null },
        )
    }

    deletingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = { deletingCategory = null },
            title = { Text("删除分类") },
            text = { Text("确定删除“${category.name}”吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(category.id) { result ->
                            deletingCategory = null
                            if (result != CategoryChangeResult.SUCCESS) {
                                operationError = if (result == CategoryChangeResult.IN_USE) {
                                    "该分类已有账目，不能删除"
                                } else {
                                    "删除失败，请重试"
                                }
                            }
                        }
                    },
                ) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deletingCategory = null }) { Text("取消") }
            },
        )
    }
}
