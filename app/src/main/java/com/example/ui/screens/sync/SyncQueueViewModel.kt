package com.example.ui.screens.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.SyncQueueEntity
import com.example.data.repository.SyncRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SyncQueueViewModel(
    private val syncRepository: SyncRepository
) : ViewModel() {

    val isOnline: StateFlow<Boolean> = syncRepository.isOnlineMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isSyncing: StateFlow<Boolean> = syncRepository.isSyncing
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val pendingCount: StateFlow<Int> = syncRepository.pendingCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val queueLogs: StateFlow<List<SyncQueueEntity>> = syncRepository.allQueueLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleOnlineMode(online: Boolean) {
        syncRepository.setOnlineMode(online)
    }

    fun triggerManualSync() {
        viewModelScope.launch {
            syncRepository.triggerSync()
        }
    }

    fun clearCompletedLogs() {
        viewModelScope.launch {
            syncRepository.clearCompletedSyncLogs()
        }
    }
}
