package com.bloomodoro

import com.bloomodoro.data.BackupManager
import com.bloomodoro.data.CloudSync
import com.bloomodoro.data.LocalStore
import com.bloomodoro.domain.PhraseProvider
import com.bloomodoro.domain.PlanningManager
import com.bloomodoro.domain.PomodoroEngine
import com.bloomodoro.calendar.CalendarSync
import com.bloomodoro.notif.NotificationScheduler
import com.bloomodoro.platform.PlatformContext
import com.bloomodoro.platform.createSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock

/**
 * Created once per app process (see MainActivity.kt / Main.kt / iosApp.swift for the one call site
 * per platform). Holding everything here means the three platform apps stay tiny.
 */
class AppContainer(platformContext: PlatformContext) {

    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val localStore = LocalStore(createSettings(platformContext))
    val notificationScheduler = NotificationScheduler(platformContext)
    val phraseProvider = PhraseProvider(localStore)
    val cloudSync = CloudSync(platformContext)
    val backupManager = BackupManager(localStore)
    val calendarSync = CalendarSync(platformContext)
    val planningManager = PlanningManager(localStore, notificationScheduler)

    val engine = PomodoroEngine(
        localStore = localStore,
        notifier = notificationScheduler,
        phraseProvider = phraseProvider,
        scope = scope,
        nowMs = { Clock.System.now().toEpochMilliseconds() },
    )

    init {
        engine.reconcile()
    }
}
