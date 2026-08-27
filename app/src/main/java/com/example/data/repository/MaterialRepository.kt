package com.example.data.repository

import com.example.data.database.dao.MaterialDao
import com.example.data.database.entity.MaterialEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onStart

class MaterialRepository(private val materialDao: MaterialDao) {

    val allMaterials: Flow<List<MaterialEntity>> = materialDao.getAllMaterials()
        .onStart {
            // Seed initial material inventory if database is empty
            ensureSeedData()
        }

    private suspend fun ensureSeedData() {
        // We insert seed items using REPLACE strategy so it runs smoothly
        val defaults = listOf(
            MaterialEntity(1, "2x4 S4S Lumber (8 ft)", "LUMBER", 2438.4, 88.9, 38.1, 8.50, 530.0),
            MaterialEntity(2, "2x6 S4S Lumber (10 ft)", "LUMBER", 3048.0, 139.7, 38.1, 14.20, 570.0),
            MaterialEntity(3, "3/4 Walnut Plywood (4x8 ft)", "SHEET", 2438.4, 1219.2, 19.05, 115.00, 650.0),
            MaterialEntity(4, "1/2 MDF Sheet (4x8 ft)", "SHEET", 2438.4, 1219.2, 12.7, 38.00, 750.0),
            MaterialEntity(5, "White Oak 4/4 Board (6 ft)", "LUMBER", 1828.8, 152.4, 25.4, 42.00, 750.0)
        )
        defaults.forEach { materialDao.insertMaterial(it) }
    }

    suspend fun insertMaterial(material: MaterialEntity): Long {
        return materialDao.insertMaterial(material)
    }

    suspend fun deleteMaterial(id: Long) {
        materialDao.deleteMaterialById(id)
    }
}
