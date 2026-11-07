package com.bloomodoro.data

import com.bloomodoro.platform.PlatformContext
import kotlinx.coroutines.flow.Flow

sealed class AuthState {
    data object SignedOut : AuthState()
    data class SignedIn(val uid: String, val email: String, val displayName: String) : AuthState()
}

sealed class SyncResult {
    data object Ok : SyncResult()
    data class Error(val message: String) : SyncResult()
}

/**
 * Backup & cross-device sync via email/password login. NOTE: this is intentionally a thin,
 * mostly-stubbed interface for now — per the agreed plan, real cloud backup ships in the next
 * phase, after the timer/garden/planning features above are solid. The shape is here so wiring
 * it in later doesn't require touching any UI code, only the platform actual{} bodies below.
 */
expect class CloudSync(context: PlatformContext) {
    val authState: Flow<AuthState>
    suspend fun signUpWithEmail(email: String, password: String): SyncResult
    suspend fun signInWithEmail(email: String, password: String): SyncResult
    suspend fun signOut()

    /** Pushes the full local snapshot (garden, history, todos, schedule, goals) to the cloud. */
    suspend fun backupNow(payloadJson: String): SyncResult

    /** Pulls the latest cloud snapshot back down, e.g. after logging in on a new device. */
    suspend fun restoreLatestBackup(): Result<String>
}
