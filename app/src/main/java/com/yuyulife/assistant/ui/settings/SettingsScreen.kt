package com.yuyulife.assistant.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yuyulife.assistant.data.repository.SettingsRepository
import com.yuyulife.assistant.data.repository.TodoRepository
import com.yuyulife.assistant.domain.model.ReminderLeadTime

@Composable
fun SettingsRoute(
    settingsRepository: SettingsRepository,
    todoRepository: TodoRepository,
    modifier: Modifier = Modifier,
) {
    val factory = remember(settingsRepository, todoRepository) {
        SettingsViewModel.factory(settingsRepository, todoRepository)
    }
    val viewModel: SettingsViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onReminderLeadTimeSelected = viewModel::setTodoReminderLeadMinutes,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onReminderLeadTimeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showReminderDialog by remember { mutableStateOf(false) }
    val selectedMinutes = uiState.settings.todoReminderLeadMinutes

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("设置", style = MaterialTheme.typography.headlineMedium)
            Text(
                "统一管理应用功能",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Text("待办管理", style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showReminderDialog = true },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text("截止提醒", style = MaterialTheme.typography.titleMedium)
                    Text(
                        ReminderLeadTime.labelFor(selectedMinutes),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Text("›", style = MaterialTheme.typography.headlineSmall)
            }
        }
    }

    if (showReminderDialog) {
        ReminderLeadTimeDialog(
            selectedMinutes = selectedMinutes,
            onSelect = onReminderLeadTimeSelected,
            onDismiss = { showReminderDialog = false },
        )
    }
}
