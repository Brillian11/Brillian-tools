package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_logs")
data class ToolLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toolType: String, // TIMER, CONVERTER, EXPENSE, COLOR
    val title: String,
    val summary: String,
    val value: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis()
)
