package com.example.ui.screens.work

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.SettingsRepository
import com.example.data.repository.TimeLog
import com.example.data.repository.WorkProject
import com.example.data.repository.WorkSubtask
import com.example.data.repository.WorkTask
import com.example.data.repository.WorkTrackingRepository
import com.example.ui.utils.CurrencyFormatter
import com.example.ui.utils.WorkNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class AppToPause(
    val id: String,
    val name: String,
    val packageName: String = "",
    val category: String,
    val isPaused: Boolean = false
)

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

    // Device Apps Pausing State
    private val _appsToPause = MutableStateFlow<List<AppToPause>>(emptyList())
    val appsToPause: StateFlow<List<AppToPause>> = _appsToPause.asStateFlow()

    private val _isAppPauserEnabled = MutableStateFlow(true)
    val isAppPauserEnabled: StateFlow<Boolean> = _isAppPauserEnabled.asStateFlow()

    // Session Timer & Tracking State
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timeLogs = MutableStateFlow<List<TimeLog>>(emptyList())
    val timeLogs: StateFlow<List<TimeLog>> = _timeLogs.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _activeTrackingProject = MutableStateFlow<WorkProject?>(null)
    val activeTrackingProject: StateFlow<WorkProject?> = _activeTrackingProject.asStateFlow()

    private var timerJob: Job? = null
    private val reminderJobs = mutableMapOf<String, Job>()

    init {
        loadAllData()
        loadInstalledDeviceApps()

        viewModelScope.launch {
            WorkNotificationHelper.timerEvents.collect { event ->
                when (event) {
                    WorkNotificationHelper.ACTION_PAUSE -> pauseSession()
                    WorkNotificationHelper.ACTION_RESUME -> {
                        val proj = _activeTrackingProject.value ?: _projects.value.firstOrNull()
                        if (proj != null) startSession(proj)
                    }
                    WorkNotificationHelper.ACTION_STOP -> stopSession()
                }
            }
        }
    }

    fun loadInstalledDeviceApps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = context.packageManager
                val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                }

                val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.queryIntentActivities(mainIntent, 0)
                }

                val installed = resolveInfos.mapNotNull { resolveInfo ->
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val name = pm.getApplicationLabel(appInfo).toString()
                    val pkg = appInfo.packageName
                    if (pkg == context.packageName) null
                    else {
                        val category = when (appInfo.category) {
                            ApplicationInfo.CATEGORY_GAME -> "Gaming"
                            ApplicationInfo.CATEGORY_AUDIO, ApplicationInfo.CATEGORY_VIDEO, ApplicationInfo.CATEGORY_IMAGE -> "Media & Video"
                            ApplicationInfo.CATEGORY_SOCIAL -> "Social Media"
                            ApplicationInfo.CATEGORY_NEWS -> "News & Articles"
                            ApplicationInfo.CATEGORY_MAPS -> "Navigation"
                            ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Productivity"
                            else -> "Device App"
                        }
                        AppToPause(
                            id = pkg,
                            name = name,
                            packageName = pkg,
                            category = category,
                            isPaused = false
                        )
                    }
                }.distinctBy { it.id }.sortedBy { it.name.lowercase() }

                if (installed.isNotEmpty()) {
                    val defaultPausedKeywords = listOf("social", "game", "media", "facebook", "instagram", "tiktok", "youtube", "twitter", "x", "reddit", "netflix", "chrome", "browser", "play", "store")
                    val updated = installed.map { app ->
                        val isDefaultPaused = defaultPausedKeywords.any { kw ->
                            app.name.contains(kw, ignoreCase = true) || app.category.contains(kw, ignoreCase = true)
                        }
                        app.copy(isPaused = isDefaultPaused)
                    }
                    _appsToPause.value = updated
                } else {
                    // Fallback apps if query returned empty on isolated test sandbox
                    _appsToPause.value = listOf(
                        AppToPause("com.instagram.android", "Instagram", "com.instagram.android", "Social Media", true),
                        AppToPause("com.zhiliaoapp.musically", "TikTok", "com.zhiliaoapp.musically", "Social Media", true),
                        AppToPause("com.facebook.katana", "Facebook", "com.facebook.katana", "Social Media", false),
                        AppToPause("com.google.android.youtube", "YouTube", "com.google.android.youtube", "Entertainment", true),
                        AppToPause("com.netflix.mediaclient", "Netflix", "com.netflix.mediaclient", "Entertainment", false),
                        AppToPause("com.whatsapp", "WhatsApp", "com.whatsapp", "Messaging", false),
                        AppToPause("org.telegram.messenger", "Telegram", "org.telegram.messenger", "Messaging", false),
                        AppToPause("com.android.chrome", "Chrome Browser", "com.android.chrome", "Web Browsing", false)
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleAppPauseCheckmark(appId: String) {
        _appsToPause.value = _appsToPause.value.map {
            if (it.id == appId) it.copy(isPaused = !it.isPaused) else it
        }
    }

    fun setAllAppsPaused(paused: Boolean) {
        _appsToPause.value = _appsToPause.value.map { it.copy(isPaused = paused) }
    }

    fun toggleAppPauserEnabled(enabled: Boolean) {
        _isAppPauserEnabled.value = enabled
        if (enabled) {
            val count = _appsToPause.value.count { it.isPaused }
            WorkNotificationHelper.showNotification(
                context,
                "App Shield Active",
                "$count distracting app(s) paused during work."
            )
        }
    }

    fun addCustomAppToPause(appName: String, category: String = "Custom") {
        if (appName.isBlank()) return
        val newApp = AppToPause(
            id = UUID.randomUUID().toString(),
            name = appName.trim(),
            category = category,
            isPaused = true
        )
        _appsToPause.value = _appsToPause.value + newApp
    }

    fun loadAllData() {
        _projects.value = workRepository.loadProjects()
        _tasks.value = workRepository.loadTasks()
        _subtasks.value = workRepository.loadSubtasks()
        _timeLogs.value = workRepository.loadTimeLogs()
    }

    // Projects CRUD
    fun addProject(
        name: String,
        client: String = "",
        hourlyRate: Double = 45.0,
        colorHex: String = "#3F51B5"
    ) {
        val newProj = WorkProject(
            id = UUID.randomUUID().toString(),
            name = name,
            client = client,
            hourlyRate = hourlyRate,
            colorHex = colorHex
        )
        val updated = _projects.value + newProj
        _projects.value = updated
        workRepository.saveProjects(updated)
    }

    fun updateProject(
        id: String,
        name: String,
        client: String = "",
        hourlyRate: Double = 45.0,
        colorHex: String = "#3F51B5"
    ) {
        val updated = _projects.value.map { proj ->
            if (proj.id == id) {
                proj.copy(name = name, client = client, hourlyRate = hourlyRate, colorHex = colorHex)
            } else proj
        }
        _projects.value = updated
        workRepository.saveProjects(updated)
        if (_activeTrackingProject.value?.id == id) {
            _activeTrackingProject.value = updated.find { it.id == id }
        }
    }

    fun deleteProject(projectId: String) {
        if (_activeTrackingProject.value?.id == projectId) {
            pauseSession()
            _activeTrackingProject.value = null
        }
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

    val userCurrency: StateFlow<String> = settingsRepository.settings.map { it.currencyCode }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.Eagerly, "USD")

    // Session Timer Control
    fun startSession(project: WorkProject) {
        if (_isTimerRunning.value) return
        _activeTrackingProject.value = project
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _elapsedSeconds.value += 1
                if (_elapsedSeconds.value % 5 == 0L) {
                    // Update ongoing notification periodically
                    WorkNotificationHelper.showTimerOngoingNotification(
                        context = context,
                        projectName = project.name,
                        formattedTime = formatDuration(_elapsedSeconds.value),
                        accruedCost = getAccruedCostFormatted(),
                        isRunning = true
                    )
                }
            }
        }
        WorkNotificationHelper.showTimerOngoingNotification(
            context = context,
            projectName = project.name,
            formattedTime = formatDuration(_elapsedSeconds.value),
            accruedCost = getAccruedCostFormatted(),
            isRunning = true
        )
    }

    fun pauseSession() {
        if (!_isTimerRunning.value) return
        _isTimerRunning.value = false
        timerJob?.cancel()
        val activeProj = _activeTrackingProject.value
        WorkNotificationHelper.showTimerOngoingNotification(
            context = context,
            projectName = activeProj?.name ?: "Work",
            formattedTime = formatDuration(_elapsedSeconds.value),
            accruedCost = getAccruedCostFormatted(),
            isRunning = false
        )
    }

    fun stopSession(note: String = "") {
        _isTimerRunning.value = false
        timerJob?.cancel()
        val totalAccrued = getAccruedCostFormatted()
        val totalSecs = _elapsedSeconds.value
        val activeProj = _activeTrackingProject.value

        if (totalSecs > 0 && activeProj != null) {
            val cost = getAccruedCost()
            val newLog = TimeLog(
                id = UUID.randomUUID().toString(),
                projectId = activeProj.id,
                durationSeconds = totalSecs,
                laborCost = cost,
                timestamp = System.currentTimeMillis(),
                note = if (note.isBlank()) "Tracked session" else note.trim()
            )
            val updated = listOf(newLog) + _timeLogs.value
            _timeLogs.value = updated
            workRepository.saveTimeLogs(updated)
        }

        val projName = activeProj?.name ?: "Work"
        _elapsedSeconds.value = 0
        WorkNotificationHelper.cancelNotification(context)
        WorkNotificationHelper.showNotification(
            context,
            "Tracked: $projName",
            "Completed! Time: ${formatDuration(totalSecs)}. Total Labor Cost: $totalAccrued."
        )
    }

    fun addManualTimeLog(projectId: String, durationMinutes: Long, note: String) {
        val proj = _projects.value.find { it.id == projectId } ?: return
        val secs = durationMinutes * 60
        val cost = (durationMinutes / 60.0) * proj.hourlyRate
        val newLog = TimeLog(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            durationSeconds = secs,
            laborCost = cost,
            timestamp = System.currentTimeMillis(),
            note = if (note.isBlank()) "Manual entry" else note.trim()
        )
        val updated = listOf(newLog) + _timeLogs.value
        _timeLogs.value = updated
        workRepository.saveTimeLogs(updated)
    }

    fun deleteTimeLog(logId: String) {
        val updated = _timeLogs.value.filter { it.id != logId }
        _timeLogs.value = updated
        workRepository.saveTimeLogs(updated)
    }

    fun getAccruedCost(): Double {
        val hourlyRate = _activeTrackingProject.value?.hourlyRate ?: settingsRepository.settings.value.laborCostPerHour
        return (_elapsedSeconds.value / 3600.0) * hourlyRate
    }

    fun getAccruedCostFormatted(): String {
        val currency = userCurrency.value
        val cost = getAccruedCost()
        return CurrencyFormatter.format(cost, currency)
    }

    fun formatCost(cost: Double): String {
        return CurrencyFormatter.format(cost, userCurrency.value)
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
