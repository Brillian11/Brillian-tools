package com.example.ui.screens.work

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SettingsRepository
import com.example.data.repository.WorkProject
import com.example.data.repository.WorkSubtask
import com.example.data.repository.WorkTask
import com.example.data.repository.WorkTrackingRepository
import com.example.ui.utils.WorkNotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class WorkTrackingViewModel(
    private val context: Context,
    private val workRepository: WorkTrackingRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _projects = MutableStateFlow<List<WorkProject>>(emptyList())
    val projects: StateFlow<List<WorkProject>> = _projects.asStateFlow()

    private val _tasks = MutableStateFlow<List<WorkTask>>(emptyList())
    val tasks: StateFlow<List<WorkTask>> = _tasks.asStateFlow()

    private val _subtasks = MutableStateFlow<List<WorkSubtask>>(emptyList())
    val subtasks: StateFlow<List<WorkSubtask>> = _subtasks.asStateFlow()

    // Focus Mode
    private val _isFocusModeActive = MutableStateFlow(false)
    val isFocusModeActive: StateFlow<Boolean> = _isFocusModeActive.asStateFlow()

    // Session Timer
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private var timerJob: Job? = null
    private val reminderJobs = mutableMapOf<String, Job>()

    init {
        loadAllData()
    }

    fun loadAllData() {
        _projects.value = workRepository.loadProjects()
        _tasks.value = workRepository.loadTasks()
        _subtasks.value = workRepository.loadSubtasks()
    }

    // Projects CRUD
    fun addProject(name: String) {
        val newProj = WorkProject(id = UUID.randomUUID().toString(), name = name)
        val updated = _projects.value + newProj
        _projects.value = updated
        workRepository.saveProjects(updated)
    }

    fun deleteProject(projectId: String) {
        val updatedProj = _projects.value.filter { it.id != projectId }
        _projects.value = updatedProj
        workRepository.saveProjects(updatedProj)

        // Cascade delete tasks
        val tasksToDelete = _tasks.value.filter { it.projectId == projectId }
        val updatedTasks = _tasks.value.filter { it.projectId != projectId }
        _tasks.value = updatedTasks
        workRepository.saveTasks(updatedTasks)

        // Cascade delete subtasks
        val taskIdsToDelete = tasksToDelete.map { it.id }.toSet()
        val updatedSub = _subtasks.value.filter { !taskIdsToDelete.contains(it.taskId) }
        _subtasks.value = updatedSub
        workRepository.saveSubtasks(updatedSub)
    }

    // Tasks CRUD
    fun addTask(projectId: String, name: String) {
        val newTask = WorkTask(id = UUID.randomUUID().toString(), projectId = projectId, name = name)
        val updated = _tasks.value + newTask
        _tasks.value = updated
        workRepository.saveTasks(updated)
    }

    fun toggleTaskCompletion(taskId: String) {
        val updated = _tasks.value.map {
            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        _tasks.value = updated
        workRepository.saveTasks(updated)
    }

    fun deleteTask(taskId: String) {
        val updatedTasks = _tasks.value.filter { it.id != taskId }
        _tasks.value = updatedTasks
        workRepository.saveTasks(updatedTasks)

        // Cascade delete subtasks
        val updatedSubs = _subtasks.value.filter { it.taskId != taskId }
        _subtasks.value = updatedSubs
        workRepository.saveSubtasks(updatedSubs)

        // Cancel pending reminders for this task
        reminderJobs[taskId]?.cancel()
        reminderJobs.remove(taskId)
    }

    // Subtasks CRUD
    fun addSubtask(taskId: String, name: String) {
        val newSub = WorkSubtask(id = UUID.randomUUID().toString(), taskId = taskId, name = name)
        val updated = _subtasks.value + newSub
        _subtasks.value = updated
        workRepository.saveSubtasks(updated)
    }

    fun toggleSubtaskCompletion(subtaskId: String) {
        val updated = _subtasks.value.map {
            if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted) else it
        }
        _subtasks.value = updated
        workRepository.saveSubtasks(updated)
    }

    fun deleteSubtask(subtaskId: String) {
        val updated = _subtasks.value.filter { it.id != subtaskId }
        _subtasks.value = updated
        workRepository.saveSubtasks(updated)
    }

    // Focus Mode toggles
    fun setFocusMode(active: Boolean) {
        _isFocusModeActive.value = active
        if (active) {
            WorkNotificationHelper.showNotification(
                context,
                "Focus Mode Activated",
                "All device interruptions minimized. Staying in the zone."
            )
        } else {
            WorkNotificationHelper.showNotification(
                context,
                "Focus Mode Deactivated",
                "Returned to normal broadcast alert level."
            )
        }
    }

    // Session Timer Control
    fun startSession() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
        WorkNotificationHelper.showNotification(
            context,
            "Working Session Started",
            "Keep it up! Live cost tracking active."
        )
    }

    fun pauseSession() {
        if (!_isTimerRunning.value) return
        _isTimerRunning.value = false
        timerJob?.cancel()
        WorkNotificationHelper.showNotification(
            context,
            "Working Session Paused",
            "Timer suspended. Current accrued: ${getAccruedCostFormatted()}."
        )
    }

    fun stopSession() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        val totalAccrued = getAccruedCostFormatted()
        val totalSecs = _elapsedSeconds.value
        _elapsedSeconds.value = 0
        WorkNotificationHelper.showNotification(
            context,
            "Working Session Stopped",
            "Completed! Time: ${formatDuration(totalSecs)}. Total Labor Cost: $totalAccrued."
        )
    }

    fun getAccruedCost(): Double {
        val hourlyRate = settingsRepository.settings.value.laborCostPerHour
        return (_elapsedSeconds.value / 3600.0) * hourlyRate
    }

    fun getAccruedCostFormatted(): String {
        val currency = settingsRepository.settings.value.currencyCode
        val cost = getAccruedCost()
        return when (currency) {
            "IDR" -> "Rp %,.0f".format(cost)
            "EUR" -> "€%.2f".format(cost)
            "GBP" -> "£%.2f".format(cost)
            "AUD" -> "A$%.2f".format(cost)
            else -> "$%.2f".format(cost)
        }
    }

    fun formatDuration(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return "%02d:%02d:%02d".format(h, m, s)
    }

    // Reminders Scheduler
    fun setTaskReminder(taskId: String, delaySeconds: Long) {
        val task = _tasks.value.find { it.id == taskId } ?: return
        
        // Update task reminder state
        val updated = _tasks.value.map {
            if (it.id == taskId) it.copy(hasReminder = true, reminderTime = System.currentTimeMillis() + (delaySeconds * 1000)) else it
        }
        _tasks.value = updated
        workRepository.saveTasks(updated)

        // Cancel previous reminder job if exists
        reminderJobs[taskId]?.cancel()

        // Start new countdown job
        val job = viewModelScope.launch {
            delay(delaySeconds * 1000)
            WorkNotificationHelper.showNotification(
                context,
                "Task Reminder",
                "Reminder for task: ${task.name}"
            )
            // Reset task reminder state upon completion
            val resetTasks = _tasks.value.map {
                if (it.id == taskId) it.copy(hasReminder = false, reminderTime = null) else it
            }
            _tasks.value = resetTasks
            workRepository.saveTasks(resetTasks)
            reminderJobs.remove(taskId)
        }
        reminderJobs[taskId] = job
    }
}
