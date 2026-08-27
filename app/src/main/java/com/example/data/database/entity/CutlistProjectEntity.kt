package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cutlist_projects")
data class CutlistProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectName: String,
    val kerfWidthMm: Double,
    val trimMarginMm: Double,
    val createdAt: Long = System.currentTimeMillis()
)
