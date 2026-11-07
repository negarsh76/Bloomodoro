package com.bloomodoro.data

import com.bloomodoro.platform.PlatformContext
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import platform.Foundation.NSDate

/**
 * Same Firebase-backed backup as Android — needs `iosApp/iosApp/GoogleService-Info.plist`
 * from your own Firebase project (Email/Password sign-in enabled) to actually work; see
 * docs/SETUP.md → "Turning on email backup".
 */
actual class CloudSync actual constructor(context: PlatformContext) {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    actual val authState: Flow<AuthState> = auth.authStateChanged.map { it.toAuthState() }.distinctUntilChanged()

    actual suspend fun signUpWithEmail(email: String, password: String): SyncResult = try {
        auth.createUserWithEmailAndPassword(email, password)
        SyncResult.Ok
    } catch (e: Exception) {
        SyncResult.Error(e.message ?: "ثبت‌نام ناموفق بود")
    }

    actual suspend fun signInWithEmail(email: String, password: String): SyncResult = try {
        auth.signInWithEmailAndPassword(email, password)
        SyncResult.Ok
    } catch (e: Exception) {
        SyncResult.Error(e.message ?: "ورود ناموفق بود")
    }

    actual suspend fun signOut() = auth.signOut()

    actual suspend fun backupNow(payloadJson: String): SyncResult = try {
        val uid = auth.currentUser?.uid ?: return SyncResult.Error("اول باید وارد شی")
        val nowMs = (NSDate().timeIntervalSince1970 * 1000).toLong()
        firestore.collection("backups").document(uid).set(mapOf("data" to payloadJson, "updatedAtEpochMs" to nowMs))
        SyncResult.Ok
    } catch (e: Exception) {
        SyncResult.Error(e.message ?: "بک‌آپ ناموفق بود")
    }

    actual suspend fun restoreLatestBackup(): Result<String> = runCatching {
        val uid = auth.currentUser?.uid ?: error("اول باید وارد شی")
        val doc = firestore.collection("backups").document(uid).get()
        if (!doc.exists) error("هنوز بک‌آپی برای این حساب ثبت نشده")
        doc.get<String>("data")
    }
}

private fun FirebaseUser?.toAuthState(): AuthState =
    this?.let { AuthState.SignedIn(it.uid, it.email ?: "", it.displayName ?: "") } ?: AuthState.SignedOut
