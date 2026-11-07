package com.bloomodoro

import androidx.compose.ui.window.ComposeUIViewController
import com.bloomodoro.data.SyncResult
import com.bloomodoro.platform.PlatformContext
import com.bloomodoro.ui.BloomodoroApp
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

/** Created once for the whole process — Swift calls MainViewController() exactly once at launch. */
private val container by lazy { AppContainer(PlatformContext()) }

fun MainViewController(): UIViewController = ComposeUIViewController {
    BloomodoroApp(container = container)
}

// --- Phase 2 (email backup) bridge functions — safe to call once that UI is wired in Settings ---

fun signUpWithEmailFromSwift(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
    container.scope.launch {
        when (val result = container.cloudSync.signUpWithEmail(email, password)) {
            is SyncResult.Ok -> onResult(true, null)
            is SyncResult.Error -> onResult(false, result.message)
        }
    }
}

fun signInWithEmailFromSwift(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
    container.scope.launch {
        when (val result = container.cloudSync.signInWithEmail(email, password)) {
            is SyncResult.Ok -> onResult(true, null)
            is SyncResult.Error -> onResult(false, result.message)
        }
    }
}

/** Kept as a callable no-op for AppDelegate; real push isn't part of the current feature set. */
fun registerPushTokenFromSwift() = Unit
