package com.example.ui.screens.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.QuickTaskEntity
import com.example.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskChecklistUiState(
    val filterPriority: String = "ALL",
    val showAddDialog: Boolean = false
)

class TaskChecklistViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskChecklistUiState())
    val uiState: StateFlow<TaskChecklistUiState> = _uiState.asStateFlow()

    val tasks: StateFlow<List<QuickTaskEntity>> = combine(
        taskRepository.allTasks,
        _uiState
    ) { allTasks, state ->
        if (state.filterPriority == "ALL") {
            allTasks
        } else {
            allTasks.filter { it.priority.equals(state.filterPriority, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilterPriority(priority: String) {
        _uiState.value = _uiState.value.copy(filterPriority = priority)
    }

    fun setShowAddDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAddDialog = show)
    }

    fun addTask(title: String, priority: String, category: String) {
        viewModelScope.launch {
            taskRepository.addTask(title, priority, category)
            setShowAddDialog(false)
        }
    }

    fun toggleTaskCompletion(task: QuickTaskEntity) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
        }
    }
}
