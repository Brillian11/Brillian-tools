package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String, // TASK, NOTE, WIDGET
    val entityId: String,
    val action: String, // CREATE, UPDATE, DELETE
    val payload: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // PENDING, SYNCING, COMPLETED, ERROR
)
