package com.bloomodoro.android

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.bloomodoro.AppContainer
import com.bloomodoro.platform.PlatformContext
import com.bloomodoro.ui.BloomodoroApp

class MainActivity : ComponentActivity() {

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op: if denied, in-app countdown still works, just without OS notifications */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val container = AppContainer(PlatformContext(applicationContext))

        setContent {
            BloomodoroApp(container = container)
        }
    }
}
