package com.yuyulife.assistant.reminder

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.yuyulife.assistant.domain.model.TodoItem
import com.yuyulife.assistant.ui.theme.YuyuLifeTheme
import com.yuyulife.assistant.util.formatDateTime

class AlarmActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private lateinit var todo: TodoItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showAboveLockScreen()
        todo = TodoItem(
            id = intent.getLongExtra(TodoReminderScheduler.EXTRA_TODO_ID, 0),
            title = intent.getStringExtra(TodoReminderScheduler.EXTRA_TODO_TITLE) ?: "待办提醒",
            createdAt = System.currentTimeMillis(),
            deadlineAt = intent.getLongExtra(TodoReminderScheduler.EXTRA_TODO_DEADLINE, 0)
                .takeIf { it > 0 },
        )
        startAlert()
        setContent {
            YuyuLifeTheme {
                BackHandler { stopAndClose() }
                AlarmScreen(
                    todo = todo,
                    onStop = ::stopAndClose,
                    onSnooze = ::snoozeAndClose,
                )
            }
        }
    }

    override fun onDestroy() {
        stopAlert()
        super.onDestroy()
    }

    private fun showAboveLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startAlert() {
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        mediaPlayer = runCatching {
            MediaPlayer().apply {
                setDataSource(this@AlarmActivity, alarmUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build(),
                )
                isLooping = true
                prepare()
                start()
            }
        }.getOrNull()

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 900, 300, 900)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopAlert() {
        mediaPlayer?.runCatching { stop() }
        mediaPlayer?.release()
        mediaPlayer = null
        vibrator?.cancel()
        vibrator = null
    }

    private fun stopAndClose() {
        stopAlert()
        getSystemService(NotificationManager::class.java)
            .cancel(TodoReminderScheduler.notificationId(todo.id))
        finishAndRemoveTask()
    }

    private fun snoozeAndClose() {
        stopAlert()
        TodoReminderScheduler(applicationContext).snooze(todo)
        finishAndRemoveTask()
    }
}

@Composable
private fun AlarmScreen(
    todo: TodoItem,
    onStop: () -> Unit,
    onSnooze: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("待办闹钟", style = MaterialTheme.typography.headlineLarge)
        Text(
            text = todo.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 24.dp),
        )
        todo.deadlineAt?.let {
            Text(
                text = "截止时间：${formatDateTime(it)}",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp, bottom = 32.dp),
            )
        }
        Button(onClick = onStop) { Text("关闭闹钟") }
        OutlinedButton(
            onClick = onSnooze,
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text("5 分钟后提醒")
        }
    }
}
