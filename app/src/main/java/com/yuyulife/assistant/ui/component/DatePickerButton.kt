package com.yuyulife.assistant.ui.component

import android.app.DatePickerDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import com.yuyulife.assistant.util.dateParts
import com.yuyulife.assistant.util.formatDate
import com.yuyulife.assistant.util.monthTimestamp
import java.util.Calendar

@Composable
fun DatePickerButton(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    labelPrefix: String = "日期：",
) {
    val context = LocalContext.current
    val (year, month, day) = dateParts(selectedDate)

    OutlinedButton(
        modifier = modifier,
        onClick = {
            DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = monthTimestamp(selectedYear, selectedMonth)
                        set(Calendar.DAY_OF_MONTH, selectedDay)
                    }
                    onDateSelected(calendar.timeInMillis)
                },
                year,
                month,
                day,
            ).show()
        },
    ) {
        Text("$labelPrefix${formatDate(selectedDate)}")
    }
}
