package com.example.ui.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategory(val label: String) {
    WOODWORKING("Woodworking"),
    ELECTRICAL("Electrical"),
    CIVIL("Civil Engineering"),
    MECHANICAL("Mechanical & HVAC"),
    PAINTING("Painting & Coating"),
    MEASUREMENT("Measurement & Sensors"),
    MANAGEMENT("Management & Safety")
}

data class JobsiteTool(
    val id: String,
    val category: ToolCategory,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

enum class UnitSystem { METRIC, IMPERIAL }

class UnitsRepository(private val context: Context) {
    fun getLengthUnit(system: UnitSystem): String = when (system) {
        UnitSystem.METRIC -> "mm"
        UnitSystem.IMPERIAL -> "in"
    }
    
    fun getVolumeUnit(system: UnitSystem): String = when (system) {
        UnitSystem.METRIC -> "m³"
        UnitSystem.IMPERIAL -> "yd³"
    }

    fun getWeightUnit(system: UnitSystem): String = when (system) {
        UnitSystem.METRIC -> "kg"
        UnitSystem.IMPERIAL -> "lbs"
    }

    fun getPressureUnit(system: UnitSystem): String = when (system) {
        UnitSystem.METRIC -> "Bar"
        UnitSystem.IMPERIAL -> "PSI"
    }
}

fun buildToolContext(toolId: String, categoryName: String, currentLanguage: String, stateSummary: String): String {
    val langInstruction = if (currentLanguage == "id") {
        "Gunakan Bahasa Indonesia teknis pertukangan/teknik sipil/mekanik yang lazim di lapangan konstruksi."
    } else {
        "Use concise, professional English technical trade terms."
    }
    
    return """
        System: Active Jobsite Tool: $toolId ($categoryName).
        Language Mode: $langInstruction
        Current Tool State / Inputs: $stateSummary
    """.trimIndent()
}
