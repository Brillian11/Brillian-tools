package com.example.domain.model

data class SyncState(
    val isOnline: Boolean = true,
    val pendingCount: Int = 0,
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)
