package com.example.data.repository

import com.example.data.database.dao.QuickTaskDao
import com.example.data.database.entity.QuickTaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class TaskRepository(
    private val taskDao: QuickTaskDao
) {
    val allTasks: Flow<List<QuickTaskEntity>> = taskDao.getAllTasks()
        .onStart { seedDefaultsIfEmpty() }

    val pendingTasks: Flow<List<QuickTaskEntity>> = taskDao.getPendingTasks()

    val pendingSyncCount: Flow<Int> = taskDao.getPendingSyncCount()

    private suspend fun seedDefaultsIfEmpty() {
        // Seed default sample tasks if empty
    }

    suspend fun addTask(title: String, priority: String = "MEDIUM", category: String = "General") {
        val task = QuickTaskEntity(
            title = title,
            isCompleted = false,
            priority = priority,
            category = category,
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING_SYNC"
        )
        taskDao.insertTask(task)
    }

    suspend fun toggleTaskCompletion(task: QuickTaskEntity) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING_SYNC"
        )
        taskDao.updateTask(updated)
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTask(taskId)
    }
}
