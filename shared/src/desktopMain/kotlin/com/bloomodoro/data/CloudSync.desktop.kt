package com.bloomodoro.data

import com.bloomodoro.platform.PlatformContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Desktop has no Firebase Kotlin SDK target yet, so backup on desktop stays a documented no-op
 * until phase 2 — the app is fully usable locally either way.
 */
actual class CloudSync actual constructor(context: PlatformContext) {
    actual val authState: Flow<AuthState> = flowOf(AuthState.SignedOut)
    actual suspend fun signUpWithEmail(email: String, password: String): SyncResult =
        SyncResult.Error("بک‌آپ ابری روی دسکتاپ هنوز فعال نیست")
    actual suspend fun signInWithEmail(email: String, password: String): SyncResult =
        SyncResult.Error("بک‌آپ ابری روی دسکتاپ هنوز فعال نیست")
    actual suspend fun signOut() = Unit
    actual suspend fun backupNow(payloadJson: String): SyncResult =
        SyncResult.Error("بک‌آپ ابری روی دسکتاپ هنوز فعال نیست")
    actual suspend fun restoreLatestBackup(): Result<String> =
        Result.failure(UnsupportedOperationException("desktop backup not wired yet"))
}
