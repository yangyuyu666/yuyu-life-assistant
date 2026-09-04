package com.yuyulife.assistant.ui.settings

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.yuyulife.assistant.data.repository.CategoryChangeResult
import com.yuyulife.assistant.data.repository.LedgerCategoryRepository

@Composable
fun CategoryNameDialog(
    title: String,
    initialName: String,
    onSave: (String, (CategoryChangeResult) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(initialName) { mutableStateOf(initialName) }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    fun submit() {
        val normalized = name.trim()
        if (normalized.isEmpty() || normalized.length > LedgerCategoryRepository.MAX_NAME_LENGTH) {
            error = "分类名称需要 1–20 个字符"
            return
        }
        saving = true
        onSave(normalized) { result ->
            saving = false
            when (result) {
                CategoryChangeResult.SUCCESS -> onDismiss()
                CategoryChangeResult.DUPLICATE -> error = "该分类已经存在"
                CategoryChangeResult.INVALID_NAME -> error = "分类名称需要 1–20 个字符"
                CategoryChangeResult.IN_USE -> error = "该分类已被账目使用"
                CategoryChangeResult.NOT_FOUND -> error = "分类不存在，请重试"
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.take(LedgerCategoryRepository.MAX_NAME_LENGTH)
                    error = null
                },
                label = { Text("分类名称") },
                supportingText = { error?.let { Text(it) } },
                isError = error != null,
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = ::submit, enabled = !saving && name.isNotBlank()) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !saving) { Text("取消") }
        },
    )
}
