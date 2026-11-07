package com.bloomodoro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.bloomodoro.data.AuthState
import com.bloomodoro.data.BackupManager
import com.bloomodoro.data.CloudSync
import com.bloomodoro.data.SyncResult
import kotlinx.coroutines.launch

/**
 * Sign up / sign in with email, then back up or restore the full local snapshot (garden,
 * history, todos, schedule, goals, both modes' settings) to/from Firestore. Requires a real
 * Firebase project wired in (see docs/SETUP.md) — until then this shows a clear error instead
 * of silently doing nothing.
 */
@Composable
fun BackupSection(cloudSync: CloudSync, backupManager: BackupManager) {
    val authState by cloudSync.authState.collectAsState(initial = AuthState.SignedOut)
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    fun runAction(action: suspend () -> String?) {
        if (isBusy) return
        isBusy = true
        statusMessage = null
        scope.launch {
            statusMessage = action()
            isBusy = false
        }
    }

    Card {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("بک‌آپ و همگام‌سازی", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))

            when (val state = authState) {
                is AuthState.SignedOut -> {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ایمیل") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("رمز عبور") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            runAction {
                                when (val r = cloudSync.signInWithEmail(email, password)) {
                                    is SyncResult.Ok -> "وارد شدی ✅"
                                    is SyncResult.Error -> r.message
                                }
                            }
                        }) { Text("ورود") }
                        TextButton(onClick = {
                            runAction {
                                when (val r = cloudSync.signUpWithEmail(email, password)) {
                                    is SyncResult.Ok -> "حساب ساخته شد ✅"
                                    is SyncResult.Error -> r.message
                                }
                            }
                        }) { Text("ساخت حساب جدید") }
                    }
                }

                is AuthState.SignedIn -> {
                    Text("وارد شدی با: ${state.email}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            runAction {
                                val snapshot = backupManager.exportSnapshotJson()
                                when (val r = cloudSync.backupNow(snapshot)) {
                                    is SyncResult.Ok -> "بک‌آپ گرفته شد ✅"
                                    is SyncResult.Error -> r.message
                                }
                            }
                        }) { Text("بک‌آپ بگیر الان") }
                        TextButton(onClick = {
                            runAction {
                                cloudSync.restoreLatestBackup().fold(
                                    onSuccess = { json ->
                                        backupManager.importSnapshotJson(json).fold(
                                            onSuccess = { "بازیابی شد ✅ — اپ رو ببند و دوباره باز کن" },
                                            onFailure = { "فایل بک‌آپ خراب بود: ${it.message}" },
                                        )
                                    },
                                    onFailure = { "بازیابی ناموفق: ${it.message}" },
                                )
                            }
                        }) { Text("بازیابی از بک‌آپ") }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { scope.launch { cloudSync.signOut() } }) { Text("خروج از حساب") }
                }
            }

            if (isBusy) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.height(20.dp))
            }
            statusMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
