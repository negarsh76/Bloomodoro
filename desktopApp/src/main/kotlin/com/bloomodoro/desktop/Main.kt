package com.bloomodoro.desktop

import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import com.bloomodoro.AppContainer
import com.bloomodoro.platform.PlatformContext
import com.bloomodoro.ui.BloomodoroApp

fun main() = application {
    val container = remember { AppContainer(PlatformContext()) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Bloomodoro",
        state = WindowState(size = DpSize(420.dp, 760.dp)),
    ) {
        BloomodoroApp(container = container)
    }
}
