package com.example.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.MaterialEntity
import com.example.data.repository.MaterialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MaterialInventoryViewModel(
    private val materialRepository: MaterialRepository
) : ViewModel() {

    val materials: StateFlow<List<MaterialEntity>> = materialRepository.allMaterials
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addMaterial(
        name: String,
        category: String,
        lengthMm: Double,
        widthMm: Double,
        thicknessMm: Double,
        cost: Double,
        density: Double?
    ) {
        viewModelScope.launch {
            val entity = MaterialEntity(
                name = name,
                category = category,
                lengthMm = lengthMm,
                widthMm = widthMm,
                thicknessMm = thicknessMm,
                costPerUnit = cost,
                speciesDensityKgM3 = density
            )
            materialRepository.insertMaterial(entity)
        }
    }

    fun deleteMaterial(id: Long) {
        viewModelScope.launch {
            materialRepository.deleteMaterial(id)
        }
    }
}
