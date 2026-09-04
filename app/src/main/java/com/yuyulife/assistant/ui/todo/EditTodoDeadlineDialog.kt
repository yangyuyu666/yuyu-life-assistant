package com.yuyulife.assistant.ui.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.TodoItem
import com.yuyulife.assistant.ui.component.DatePickerButton
import com.yuyulife.assistant.ui.component.TimePickerButton
import com.yuyulife.assistant.util.dateWithTime
import com.yuyulife.assistant.util.nextWholeHour

@Composable
fun EditTodoDeadlineDialog(
    item: TodoItem,
    onDismiss: () -> Unit,
    onSave: (Long) -> Unit,
) {
    var deadlineAt by remember(item.id) {
        mutableStateOf(item.deadlineAt ?: nextWholeHour())
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submit() {
        if (deadlineAt <= System.currentTimeMillis()) {
            errorMessage = "截止时间必须晚于当前时间"
            return
        }
        onSave(deadlineAt)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("修改截止时间") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                DatePickerButton(
                    selectedDate = deadlineAt,
                    onDateSelected = {
                        deadlineAt = dateWithTime(it, deadlineAt)
                        errorMessage = null
                    },
                    labelPrefix = "截止：",
                )
                TimePickerButton(
                    selectedTime = deadlineAt,
                    onTimeSelected = {
                        deadlineAt = it
                        errorMessage = null
                    },
                )
                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = ::submit) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}
