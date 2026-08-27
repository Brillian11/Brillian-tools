package com.example.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.DashboardWidgetEntity
import com.example.data.database.entity.QuickNoteEntity
import com.example.data.database.entity.QuickTaskEntity
import com.example.data.repository.DashboardRepository
import com.example.data.repository.NoteRepository
import com.example.data.repository.SettingsRepository
import com.example.data.repository.SyncRepository
import com.example.data.repository.TaskRepository
import com.example.data.repository.ToolLogRepository
import com.example.data.repository.UserSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val taskRepository: TaskRepository,
    private val noteRepository: NoteRepository,
    private val syncRepository: SyncRepository,
    private val toolLogRepository: ToolLogRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val userSettings: StateFlow<UserSettings> = settingsRepository.settings

    val pinnedWidgets: StateFlow<List<DashboardWidgetEntity>> = dashboardRepository.pinnedWidgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val quickTasks: StateFlow<List<QuickTaskEntity>> = taskRepository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val quickNotes: StateFlow<List<QuickNoteEntity>> = noteRepository.allNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isOnlineMode: StateFlow<Boolean> = syncRepository.isOnlineMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val pendingSyncCount: StateFlow<Int> = syncRepository.pendingCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val isSyncing: StateFlow<Boolean> = syncRepository.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun addTaskQuick(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepository.addTask(title, "HIGH", "Dashboard")
        }
    }

    fun toggleTask(task: QuickTaskEntity) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(task)
        }
    }

    fun quickLogExpense(amount: Double, cat: String) {
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "EXPENSE",
                title = "Expense: $$amount",
                summary = "$cat (Quick Log)",
                value = amount
            )
        }
    }

    fun completeOnboarding(profile: String, unitSystem: String) {
        viewModelScope.launch {
            settingsRepository.completeOnboarding(profile, unitSystem)
            dashboardRepository.applyProfileLayout(profile)
        }
    }
}
