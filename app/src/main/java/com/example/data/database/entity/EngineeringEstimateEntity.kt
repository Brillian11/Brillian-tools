package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "engineering_estimates")
data class EngineeringEstimateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // BEAM, CONCRETE, EARTHWORK, FRAMING, KERF
    val inputsJson: String,
    val outputsJson: String,
    val timestamp: Long = System.currentTimeMillis()
)
