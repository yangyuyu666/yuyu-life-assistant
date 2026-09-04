package com.yuyulife.assistant

import android.os.Bundle
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yuyulife.assistant.ui.home.YuyuLifeApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermissionIfNeeded()

        val app = application as YuyuLifeApplication
        setContent {
            YuyuLifeApp(
                todoRepository = app.todoRepository,
                ledgerRepository = app.ledgerRepository,
                ledgerCategoryRepository = app.ledgerCategoryRepository,
                settingsRepository = app.settingsRepository,
            )
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST)
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST = 1001
    }
}
