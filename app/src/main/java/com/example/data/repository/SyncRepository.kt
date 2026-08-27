package com.example.data.repository

import com.example.data.database.dao.QuickNoteDao
import com.example.data.database.dao.QuickTaskDao
import com.example.data.database.dao.SyncQueueDao
import com.example.data.database.entity.SyncQueueEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncRepository(
    private val syncQueueDao: SyncQueueDao,
    private val taskDao: QuickTaskDao,
    private val noteDao: QuickNoteDao
) {
    val pendingCount: Flow<Int> = syncQueueDao.getPendingCount()
    val allQueueLogs: Flow<List<SyncQueueEntity>> = syncQueueDao.getAllQueueLogs()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _isOnlineMode = MutableStateFlow(true)
    val isOnlineMode: StateFlow<Boolean> = _isOnlineMode.asStateFlow()

    fun setOnlineMode(online: Boolean) {
        _isOnlineMode.value = online
    }

    suspend fun triggerSync() {
        if (_isSyncing.value) return
        _isSyncing.value = true
        try {
            // Simulate network synchronization roundtrip
            delay(1200)
            syncQueueDao.markAllCompleted()
            taskDao.markAllSynced()
            noteDao.markAllSynced()
        } finally {
            _isSyncing.value = false
        }
    }

    suspend fun clearCompletedSyncLogs() {
        syncQueueDao.purgeCompleted()
    }
}
