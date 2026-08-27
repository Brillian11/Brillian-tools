package com.example.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dashboard_widgets")
data class DashboardWidgetEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val isPinned: Boolean,
    val displayOrder: Int,
    val spanSize: Int = 1, // 1: Half screen, 2: Full width
    val iconName: String,
    val subtitle: String = "",
    val backgroundColorHex: String = "",
    val strokeColorHex: String = "",
    val strokeWidthDp: Int = 1,
    val thumbnailPattern: String = "none"
)

