package com.example.data.repository

import com.example.data.database.dao.DashboardDao
import com.example.data.database.entity.DashboardWidgetEntity
import com.example.domain.model.ToolDefinition
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart

class DashboardRepository(private val dashboardDao: DashboardDao) {

    val allWidgets: Flow<List<DashboardWidgetEntity>> = dashboardDao.getAllWidgets()
        .onStart { seedDefaultsIfEmpty() }

    val pinnedWidgets: Flow<List<DashboardWidgetEntity>> = dashboardDao.getPinnedWidgets()
        .onStart { seedDefaultsIfEmpty() }

    private suspend fun seedDefaultsIfEmpty() {
        if (dashboardDao.getWidgetCount() == 0) {
            applyProfileLayout("General")
        }
    }

    suspend fun applyProfileLayout(profile: String) {
        val defaultIds = setOf("widget_notes", "widget_tasks", "widget_timer")
        val widgets = ToolDefinition.ALL_TOOLS.mapIndexed { index, tool ->
            val shouldPin = when (profile) {
                "Woodworker" -> defaultIds.contains(tool.id) || tool.category == "Woodworking"
                "Civil Engineer" -> defaultIds.contains(tool.id) || tool.category == "Civil Engineering" || tool.category == "Site & Field" || tool.category == "Field Engineering"
                "Electrician" -> defaultIds.contains(tool.id) || tool.category == "Electrical" || tool.category == "Sensors"
                "Mechanical" -> defaultIds.contains(tool.id) || tool.category == "Mechanical & HVAC" || tool.category == "Plumbing & Maintenance"
                "Painter" -> defaultIds.contains(tool.id) || tool.category == "Painting & Coating" || tool.category == "Safety & Compliance" || tool.id == "widget_color_tools"
                "Metalworker" -> defaultIds.contains(tool.id) || tool.category == "Metalworks"
                else -> true
            }
            DashboardWidgetEntity(
                id = tool.id,
                title = tool.title,
                category = tool.category,
                isPinned = shouldPin,
                displayOrder = index,
                spanSize = tool.defaultSpan,
                iconName = tool.iconName,
                subtitle = tool.description
            )
        }
        dashboardDao.insertOrUpdateWidgets(widgets)
    }

    suspend fun togglePinWidget(widgetId: String, currentPinned: Boolean) {
        val existingList = dashboardDao.getAllWidgets().firstOrNull() ?: emptyList()
        val existing = existingList.find { it.id == widgetId }
        val toolDef = ToolDefinition.ALL_TOOLS.find { it.id == widgetId }
        
        if (existing != null) {
            dashboardDao.insertWidget(existing.copy(isPinned = !currentPinned))
        } else if (toolDef != null) {
            dashboardDao.insertWidget(
                DashboardWidgetEntity(
                    id = toolDef.id,
                    title = toolDef.title,
                    category = toolDef.category,
                    isPinned = !currentPinned,
                    displayOrder = 99,
                    spanSize = toolDef.defaultSpan,
                    iconName = toolDef.iconName,
                    subtitle = toolDef.description
                )
            )
        }
    }

    suspend fun updateWidgetSpan(widgetId: String, newSpan: Int) {
        val existingList = dashboardDao.getAllWidgets().firstOrNull() ?: emptyList()
        val existing = existingList.find { it.id == widgetId }
        if (existing != null) {
            dashboardDao.insertWidget(existing.copy(spanSize = newSpan))
        }
    }

    suspend fun updateWidgetStyle(
        widgetId: String,
        bgHex: String,
        strokeHex: String,
        strokeWidthDp: Int,
        iconName: String,
        thumbnailPattern: String
    ) {
        val existingList = dashboardDao.getAllWidgets().firstOrNull() ?: emptyList()
        val existing = existingList.find { it.id == widgetId }
        val toolDef = ToolDefinition.ALL_TOOLS.find { it.id == widgetId }

        if (existing != null) {
            dashboardDao.insertWidget(
                existing.copy(
                    backgroundColorHex = bgHex,
                    strokeColorHex = strokeHex,
                    strokeWidthDp = strokeWidthDp,
                    iconName = iconName.ifBlank { existing.iconName },
                    thumbnailPattern = thumbnailPattern
                )
            )
        } else if (toolDef != null) {
            dashboardDao.insertWidget(
                DashboardWidgetEntity(
                    id = toolDef.id,
                    title = toolDef.title,
                    category = toolDef.category,
                    isPinned = true,
                    displayOrder = 0,
                    spanSize = toolDef.defaultSpan,
                    iconName = iconName.ifBlank { toolDef.iconName },
                    subtitle = toolDef.description,
                    backgroundColorHex = bgHex,
                    strokeColorHex = strokeHex,
                    strokeWidthDp = strokeWidthDp,
                    thumbnailPattern = thumbnailPattern
                )
            )
        }
    }

    suspend fun reorderWidgets(newList: List<DashboardWidgetEntity>) {
        val updated = newList.mapIndexed { index, item ->
            item.copy(displayOrder = index)
        }
        dashboardDao.insertOrUpdateWidgets(updated)
    }
}

