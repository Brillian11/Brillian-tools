package com.example.ui.screens.customize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.DashboardWidgetEntity
import com.example.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomizeDashboardViewModel(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    companion object {
        private var savedSearchQuery: String = ""
        private var savedSelectedCategory: String = "ALL"
        private var savedScrollIndex: Int = 0
        private var savedScrollOffset: Int = 0
        private var savedCategoryScrollOffset: Int = 0
    }

    private val _catalogSearchQuery = MutableStateFlow(savedSearchQuery)
    val catalogSearchQuery: StateFlow<String> = _catalogSearchQuery.asStateFlow()

    private val _catalogSelectedCategory = MutableStateFlow(savedSelectedCategory)
    val catalogSelectedCategory: StateFlow<String> = _catalogSelectedCategory.asStateFlow()

    var catalogScrollIndex: Int
        get() = savedScrollIndex
        set(value) {
            savedScrollIndex = value
        }

    var catalogScrollOffset: Int
        get() = savedScrollOffset
        set(value) {
            savedScrollOffset = value
        }

    var catalogCategoryScrollOffset: Int
        get() = savedCategoryScrollOffset
        set(value) {
            savedCategoryScrollOffset = value
        }

    fun setCatalogSearchQuery(query: String) {
        savedSearchQuery = query
        _catalogSearchQuery.value = query
    }

    fun setCatalogSelectedCategory(category: String) {
        savedSelectedCategory = category
        _catalogSelectedCategory.value = category
    }

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

