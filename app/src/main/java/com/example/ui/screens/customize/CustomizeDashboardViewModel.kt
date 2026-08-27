package com.example.ui.screens.customize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.DashboardWidgetEntity
import com.example.data.repository.DashboardRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomizeDashboardViewModel(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    val allWidgets: StateFlow<List<DashboardWidgetEntity>> = dashboardRepository.allWidgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun togglePin(widget: DashboardWidgetEntity) {
        viewModelScope.launch {
            dashboardRepository.togglePinWidget(widget.id, widget.isPinned)
        }
    }

    fun togglePinById(widgetId: String, currentlyPinned: Boolean) {
        viewModelScope.launch {
            dashboardRepository.togglePinWidget(widgetId, currentlyPinned)
        }
    }

    fun setSpanSize(widget: DashboardWidgetEntity, span: Int) {
        viewModelScope.launch {
            dashboardRepository.updateWidgetSpan(widget.id, span)
        }
    }

    fun moveUp(index: Int) {
        val current = allWidgets.value.toMutableList()
        if (index > 0 && index < current.size) {
            val tmp = current[index]
            current[index] = current[index - 1]
            current[index - 1] = tmp
            viewModelScope.launch {
                dashboardRepository.reorderWidgets(current)
            }
        }
    }

    fun moveDown(index: Int) {
        val current = allWidgets.value.toMutableList()
        if (index >= 0 && index < current.size - 1) {
            val tmp = current[index]
            current[index] = current[index + 1]
            current[index + 1] = tmp
            viewModelScope.launch {
                dashboardRepository.reorderWidgets(current)
            }
        }
    }

    fun updateWidgetStyle(
        widgetId: String,
        bgHex: String,
        strokeHex: String,
        strokeWidthDp: Int,
        iconName: String,
        thumbnailPattern: String
    ) {
        viewModelScope.launch {
            dashboardRepository.updateWidgetStyle(
                widgetId,
                bgHex,
                strokeHex,
                strokeWidthDp,
                iconName,
                thumbnailPattern
            )
        }
    }
}

