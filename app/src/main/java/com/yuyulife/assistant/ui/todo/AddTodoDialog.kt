package com.yuyulife.assistant.ui.todo

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.ui.component.DatePickerButton
import com.yuyulife.assistant.ui.component.TimePickerButton
import com.yuyulife.assistant.util.dateWithTime
import com.yuyulife.assistant.util.nextWholeHour

@Composable
fun AddTodoDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Long) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var deadlineAt by remember { mutableStateOf(nextWholeHour()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun submit() {
        if (title.isBlank()) return
        if (deadlineAt <= System.currentTimeMillis()) {
            errorMessage = "截止时间必须晚于当前时间"
            return
        }
        onAdd(title.trim(), deadlineAt)
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加待办") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("要做什么？") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                )
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
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = ::submit,
                enabled = title.isNotBlank(),
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
    )
}
