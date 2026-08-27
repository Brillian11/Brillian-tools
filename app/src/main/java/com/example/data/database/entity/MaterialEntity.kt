package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // LUMBER, SHEET, STEEL, MASONRY
    val lengthMm: Double,
    val widthMm: Double,
    val thicknessMm: Double,
    val costPerUnit: Double,
    val speciesDensityKgM3: Double? = null
)
