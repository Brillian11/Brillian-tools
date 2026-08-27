package com.example.data.repository

import com.example.data.database.dao.QuickTaskDao
import com.example.data.database.dao.SyncQueueDao
import com.example.data.database.entity.QuickTaskEntity
import com.example.data.database.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class TaskRepository(
    private val taskDao: QuickTaskDao,
    private val syncQueueDao: SyncQueueDao
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
        val id = taskDao.insertTask(task)
        syncQueueDao.enqueue(
            SyncQueueEntity(
                entityType = "TASK",
                entityId = id.toString(),
                action = "CREATE",
                payload = "Task: $title ($priority)",
                status = "PENDING"
            )
        )
    }

    suspend fun toggleTaskCompletion(task: QuickTaskEntity) {
        val updated = task.copy(
            isCompleted = !task.isCompleted,
            updatedAt = System.currentTimeMillis(),
            syncStatus = "PENDING_SYNC"
        )
        taskDao.updateTask(updated)
        syncQueueDao.enqueue(
            SyncQueueEntity(
                entityType = "TASK",
                entityId = task.id.toString(),
                action = "UPDATE",
                payload = "Completed status changed to ${updated.isCompleted}",
                status = "PENDING"
            )
        )
    }

    suspend fun deleteTask(taskId: Long) {
        taskDao.deleteTask(taskId)
        syncQueueDao.enqueue(
            SyncQueueEntity(
                entityType = "TASK",
                entityId = taskId.toString(),
                action = "DELETE",
                payload = "Deleted Task #$taskId",
                status = "PENDING"
            )
        )
    }
}
