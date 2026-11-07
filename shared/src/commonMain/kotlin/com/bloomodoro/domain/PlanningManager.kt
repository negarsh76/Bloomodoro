package com.bloomodoro.domain

import com.bloomodoro.data.LocalStore
import com.bloomodoro.model.*
import com.bloomodoro.notif.NotificationIds
import com.bloomodoro.notif.NotificationScheduler
import com.bloomodoro.notif.ScheduledNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

/**
 * Everything for the "planning" side of the app: today/weekly todos, the recurring schedule,
 * class/deadline alarms, and the goal pyramid (year -> season -> month -> week). Kept as one
 * manager since these all read/write small lists and none of them need their own lifecycle.
 */
class PlanningManager(
    private val localStore: LocalStore,
    private val notifier: NotificationScheduler,
) {
    private val _todos = MutableStateFlow(localStore.loadTodos())
    val todos: StateFlow<List<TodoItem>> = _todos

    private val _schedule = MutableStateFlow(localStore.loadSchedule())
    val schedule: StateFlow<List<ScheduleBlock>> = _schedule

    private val _deadlines = MutableStateFlow(localStore.loadDeadlines())
    val deadlines: StateFlow<List<ClassOrDeadline>> = _deadlines

    private val _goals = MutableStateFlow(localStore.loadGoals())
    val goals: StateFlow<List<Goal>> = _goals

    private val _journeys = MutableStateFlow(localStore.loadJourneys())
    val journeys: StateFlow<List<GoalJourney>> = _journeys

    private val _journeySteps = MutableStateFlow(localStore.loadJourneySteps())
    val journeySteps: StateFlow<List<JourneyStep>> = _journeySteps

    // --- Todos ---
    fun addTodo(title: String, mode: AppMode, dueEpochDay: Long?, nowMs: Long) {
        _todos.value = _todos.value + TodoItem(id = newId(), title = title, mode = mode, dueEpochDay = dueEpochDay, createdAtEpochMs = nowMs)
        localStore.saveTodos(_todos.value)
    }

    fun toggleTodo(id: String) {
        _todos.value = _todos.value.map { if (it.id == id) it.copy(isDone = !it.isDone) else it }
        localStore.saveTodos(_todos.value)
    }

    fun deleteTodo(id: String) {
        _todos.value = _todos.value.filterNot { it.id == id }
        localStore.saveTodos(_todos.value)
    }

    // --- Schedule ---
    fun addScheduleBlock(block: ScheduleBlock) {
        _schedule.value = _schedule.value + block
        localStore.saveSchedule(_schedule.value)
    }

    fun deleteScheduleBlock(id: String) {
        _schedule.value = _schedule.value.filterNot { it.id == id }
        localStore.saveSchedule(_schedule.value)
    }

    // --- Classes & deadlines (these actually ring an alarm) ---
    fun addDeadline(item: ClassOrDeadline) {
        _deadlines.value = _deadlines.value + item
        localStore.saveDeadlines(_deadlines.value)
        notifier.schedule(
            ScheduledNotification(
                requestId = NotificationIds.forDeadline(item.id.hashCode()),
                triggerAtEpochMs = item.dueEpochMs,
                title = if (item.kind == DeadlineKind.CLASS) "امروز کلاس داری 🎓" else "ددلاین رسیده ⏰",
                body = item.title,
            ),
        )
    }

    fun toggleDeadlineDone(id: String) {
        _deadlines.value = _deadlines.value.map { if (it.id == id) it.copy(isDone = !it.isDone) else it }
        localStore.saveDeadlines(_deadlines.value)
    }

    fun deleteDeadline(id: String) {
        notifier.cancel(NotificationIds.forDeadline(id.hashCode()))
        _deadlines.value = _deadlines.value.filterNot { it.id == id }
        localStore.saveDeadlines(_deadlines.value)
    }

    // --- Goal pyramid ---
    fun addGoal(goal: Goal) {
        _goals.value = _goals.value + goal
        localStore.saveGoals(_goals.value)
    }

    fun updateGoalProgress(id: String, percent: Int) {
        _goals.value = _goals.value.map {
            if (it.id == id) it.copy(progressPercent = percent.coerceIn(0, 100), isDone = percent >= 100) else it
        }
        localStore.saveGoals(_goals.value)
    }

    fun deleteGoal(id: String) {
        // Also drop anything nested under it, so the pyramid never has dangling steps.
        val toRemove = generateSequence(listOf(id)) { ids ->
            val children = _goals.value.filter { it.parentId in ids }.map { it.id }
            children.ifEmpty { null }
        }.flatten().toSet()
        _goals.value = _goals.value.filterNot { it.id in toRemove }
        localStore.saveGoals(_goals.value)
    }

    fun childrenOf(parentId: String?): List<Goal> = _goals.value.filter { it.parentId == parentId }

    // --- Goal Journeys ---
    fun addJourney(journey: GoalJourney) {
        _journeys.value = _journeys.value + journey
        localStore.saveJourneys(_journeys.value)
    }

    fun deleteJourney(id: String) {
        _journeys.value = _journeys.value.filterNot { it.id == id }
        _journeySteps.value = _journeySteps.value.filterNot { it.journeyId == id }
        localStore.saveJourneys(_journeys.value)
        localStore.saveJourneySteps(_journeySteps.value)
    }

    fun addJourneyStep(journeyId: String, title: String) {
        val nextOrder = (_journeySteps.value.filter { it.journeyId == journeyId }.maxOfOrNull { it.order } ?: -1) + 1
        _journeySteps.value = _journeySteps.value + JourneyStep(id = newId(), journeyId = journeyId, title = title, order = nextOrder)
        localStore.saveJourneySteps(_journeySteps.value)
    }

    fun toggleJourneyStep(id: String) {
        _journeySteps.value = _journeySteps.value.map { if (it.id == id) it.copy(isDone = !it.isDone) else it }
        localStore.saveJourneySteps(_journeySteps.value)
    }

    fun deleteJourneyStep(id: String) {
        _journeySteps.value = _journeySteps.value.filterNot { it.id == id }
        localStore.saveJourneySteps(_journeySteps.value)
    }

    fun stepsFor(journeyId: String): List<JourneyStep> =
        _journeySteps.value.filter { it.journeyId == journeyId }.sortedBy { it.order }

    private fun newId(): String = "id_${Random.nextLong()}"
}
