package com.yuyulife.assistant.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.ReminderMode
import com.yuyulife.assistant.reminder.ReminderPermissionManager

@Composable
fun ReminderPermissionCard(
    mode: ReminderMode,
    onPermissionSettingsReturned: () -> Unit,
) {
    if (!mode.requiresExactAlarm && !mode.requiresFullScreen) return

    val context = LocalContext.current
    val permissionManager = remember(context) { ReminderPermissionManager(context) }
    var refresh by remember { mutableIntStateOf(0) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        refresh++
        onPermissionSettingsReturned()
    }
    val exactAllowed = remember(refresh, mode) { permissionManager.canScheduleExactAlarms() }
    val fullScreenAllowed = remember(refresh, mode) { permissionManager.canUseFullScreenIntent() }
    val ready = exactAllowed && (!mode.requiresFullScreen || fullScreenAllowed)

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                if (ready) "提醒权限已就绪" else "提醒权限未完整开启",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                if (ready) {
                    "将按“${mode.label}”执行"
                } else {
                    "授权前会自动使用普通提醒，避免漏掉待办"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!exactAllowed) {
                OutlinedButton(
                    onClick = { launcher.launch(permissionManager.exactAlarmSettingsIntent()) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("允许精确闹钟") }
            }
            if (mode.requiresFullScreen && !fullScreenAllowed) {
                OutlinedButton(
                    onClick = { launcher.launch(permissionManager.fullScreenSettingsIntent()) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("允许全屏提醒") }
            }
        }
    }
}
