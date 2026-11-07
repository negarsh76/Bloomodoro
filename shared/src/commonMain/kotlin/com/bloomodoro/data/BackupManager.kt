package com.bloomodoro.data

import com.bloomodoro.model.AppMode
import com.bloomodoro.model.ClassOrDeadline
import com.bloomodoro.model.CustomPhrase
import com.bloomodoro.model.FocusHistoryEntry
import com.bloomodoro.model.GardenEntry
import com.bloomodoro.model.Goal
import com.bloomodoro.model.GoalJourney
import com.bloomodoro.model.JourneyStep
import com.bloomodoro.model.ScheduleBlock
import com.bloomodoro.model.TimerConfig
import com.bloomodoro.model.TodoItem
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/** Everything backed up to the cloud — one flat snapshot of all of [LocalStore]'s collections. */
@Serializable
data class BackupSnapshot(
    val version: Int = 1,
    val studyConfig: TimerConfig,
    val workConfig: TimerConfig,
    val phrases: List<CustomPhrase>,
    val garden: List<GardenEntry>,
    val history: List<FocusHistoryEntry>,
    val todos: List<TodoItem>,
    val schedule: List<ScheduleBlock>,
    val deadlines: List<ClassOrDeadline>,
    val goals: List<Goal>,
    val journeys: List<GoalJourney> = emptyList(),
    val journeySteps: List<JourneyStep> = emptyList(),
)

/** Turns [LocalStore]'s scattered collections into one [BackupSnapshot] JSON string and back. */
class BackupManager(private val localStore: LocalStore) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    fun exportSnapshotJson(): String {
        val snapshot = BackupSnapshot(
            studyConfig = localStore.loadTimerConfig(AppMode.STUDY),
            workConfig = localStore.loadTimerConfig(AppMode.WORK),
            phrases = localStore.loadPhrases(),
            garden = localStore.loadGarden(),
            history = localStore.loadHistory(),
            todos = localStore.loadTodos(),
            schedule = localStore.loadSchedule(),
            deadlines = localStore.loadDeadlines(),
            goals = localStore.loadGoals(),
            journeys = localStore.loadJourneys(),
            journeySteps = localStore.loadJourneySteps(),
        )
        return json.encodeToString(snapshot)
    }

    /** Overwrites local data with what's in the snapshot — used right after restoring on a new device. */
    fun importSnapshotJson(snapshotJson: String): Result<Unit> = runCatching {
        val snapshot = json.decodeFromString<BackupSnapshot>(snapshotJson)
        localStore.saveTimerConfig(AppMode.STUDY, snapshot.studyConfig)
        localStore.saveTimerConfig(AppMode.WORK, snapshot.workConfig)
        localStore.savePhrases(snapshot.phrases)
        localStore.saveGarden(snapshot.garden)
        localStore.saveHistory(snapshot.history)
        localStore.saveTodos(snapshot.todos)
        localStore.saveSchedule(snapshot.schedule)
        localStore.saveDeadlines(snapshot.deadlines)
        localStore.saveGoals(snapshot.goals)
        localStore.saveJourneys(snapshot.journeys)
        localStore.saveJourneySteps(snapshot.journeySteps)
    }
}
