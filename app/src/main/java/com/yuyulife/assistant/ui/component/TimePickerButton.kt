package com.yuyulife.assistant.ui.component

import android.app.TimePickerDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.yuyulife.assistant.util.dateParts
import com.yuyulife.assistant.util.formatTime
import com.yuyulife.assistant.util.monthTimestamp
import java.util.Calendar

@Composable
fun TimePickerButton(
    selectedTime: Long,
    onTimeSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance().apply { timeInMillis = selectedTime }

    OutlinedButton(
        modifier = modifier,
        onClick = {
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val (year, month, day) = dateParts(selectedTime)
                    val updated = Calendar.getInstance().apply {
                        timeInMillis = monthTimestamp(year, month)
                        set(Calendar.DAY_OF_MONTH, day)
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onTimeSelected(updated.timeInMillis)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true,
            ).show()
        },
    ) {
        Text("时间：${formatTime(selectedTime)}")
    }
}
