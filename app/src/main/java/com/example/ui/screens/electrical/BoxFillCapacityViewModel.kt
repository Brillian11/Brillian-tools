package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class WireGauge(val awg: String, val volumeCuIn: Double) {
    AWG_18("18 AWG", 1.50),
    AWG_16("16 AWG", 1.75),
    AWG_14("14 AWG", 2.00),
    AWG_12("12 AWG", 2.25),
    AWG_10("10 AWG", 2.50),
    AWG_8("8 AWG", 3.00),
    AWG_6("6 AWG", 5.00)
}

data class ConductorEntry(
    val id: String,
    val gauge: WireGauge,
    val count: Int,
    val isPassThrough: Boolean = false // Uncut conductors passing through box
)

data class StandardBoxSpec(
    val name: String,
    val dimensions: String,
    val volumeCuIn: Double,
    val boxType: String // "Metallic Square", "Metallic Octagon", "1-Gang Device", "2-Gang Device", "3-Gang", "4-Gang"
)

data class BoxFillUiState(
    val conductors: List<ConductorEntry> = listOf(
        ConductorEntry("1", WireGauge.AWG_12, 4, false),
        ConductorEntry("2", WireGauge.AWG_14, 2, false)
    ),
    val internalClampCount: Int = 1, // Any internal clamps = 1 allowance of largest conductor
    val supportFittingCount: Int = 0, // Luminaire studs or hickeys = 1 allowance each
    val deviceYokeCountSingle: Int = 1, // Standard switch/receptacle single-gang = 2 allowances
    val deviceYokeCountDouble: Int = 0, // Double-gang device = 4 allowances
    val groundWireCount: Int = 3, // 1 allowance for up to 4 grounds, + 1/4 for each additional
    val isolatedGroundCount: Int = 0, // Isolated ground = 1 additional allowance
    val customBoxVolumeCuIn: Double = 0.0,
    val selectedStandardBox: StandardBoxSpec? = null,
    val plasterRingExtensionVolume: Double = 0.0, // e.g. 5.5 cu in plaster mud ring

    // Calculation Results
    val totalConductorVolumeCuIn: Double = 0.0,
    val clampAllowanceVolumeCuIn: Double = 0.0,
    val supportFittingVolumeCuIn: Double = 0.0,
    val deviceAllowanceVolumeCuIn: Double = 0.0,
    val groundAllowanceVolumeCuIn: Double = 0.0,
    val totalRequiredVolumeCuIn: Double = 0.0,
    val totalRequiredVolumeCm3: Double = 0.0,
    val effectiveAvailableVolumeCuIn: Double = 0.0,
    val isOverfilled: Boolean = false,
    val fillPercentage: Double = 0.0,
    val recommendedBoxes: List<StandardBoxSpec> = emptyList(),
    val largestConductorPresent: WireGauge = WireGauge.AWG_12,
    val calculationSummary: String = ""
)

class BoxFillCapacityViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    val standardBoxes = listOf(
        StandardBoxSpec("4\" Square (1-1/4\" deep)", "4 x 4 x 1-1/4\"", 18.0, "Metallic Square"),
        StandardBoxSpec("4\" Square (1-1/2\" deep)", "4 x 4 x 1-1/2\"", 21.0, "Metallic Square"),
        StandardBoxSpec("4\" Square (2-1/8\" deep)", "4 x 4 x 2-1/8\"", 30.3, "Metallic Square"),
        StandardBoxSpec("4-11/16\" Square (1-1/2\" deep)", "4-11/16 x 4-11/16 x 1-1/2\"", 29.5, "Metallic Square"),
        StandardBoxSpec("4-11/16\" Square (2-1/8\" deep)", "4-11/16 x 4-11/16 x 2-1/8\"", 42.0, "Metallic Square"),
        StandardBoxSpec("4\" Octagon / Round (1-1/2\" deep)", "4 x 1-1/2\"", 15.5, "Metallic Octagon"),
        StandardBoxSpec("4\" Octagon (2-1/8\" deep)", "4 x 2-1/8\"", 21.5, "Metallic Octagon"),
        StandardBoxSpec("1-Gang Device Box (2\" deep)", "3 x 2 x 2\"", 10.0, "1-Gang Device"),
        StandardBoxSpec("1-Gang Device Box (2-1/2\" deep)", "3 x 2 x 2-1/2\"", 12.5, "1-Gang Device"),
        StandardBoxSpec("1-Gang Device Box (2-3/4\" deep)", "3 x 2 x 2-3/4\"", 14.0, "1-Gang Device"),
        StandardBoxSpec("1-Gang Device Box (3-1/2\" deep)", "3 x 2 x 3-1/2\"", 18.0, "1-Gang Device"),
        StandardBoxSpec("2-Gang Device Box (2-1/2\" deep)", "3 x 2 x 2-1/2\" (x2)", 25.0, "2-Gang Device"),
        StandardBoxSpec("2-Gang Device Box (3-1/2\" deep)", "3 x 2 x 3-1/2\" (x2)", 36.0, "2-Gang Device"),
        StandardBoxSpec("3-Gang Device Box (3-1/2\" deep)", "3 x 2 x 3-1/2\" (x3)", 54.0, "3-Gang Device"),
        StandardBoxSpec("4-Gang Device Box (3-1/2\" deep)", "3 x 2 x 3-1/2\" (x4)", 72.0, "4-Gang Device"),
        StandardBoxSpec("Handy / Utility Box (1-7/8\" deep)", "4 x 2-1/8 x 1-7/8\"", 13.0, "Handy Box"),
        StandardBoxSpec("Handy / Utility Box (2-1/8\" deep)", "4 x 2-1/8 x 2-1/8\"", 14.5, "Handy Box")
    )

    private val _uiState = MutableStateFlow(BoxFillUiState(selectedStandardBox = standardBoxes[1]))
    val uiState: StateFlow<BoxFillUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun addConductor(gauge: WireGauge, count: Int = 1) {
        val current = _uiState.value.conductors.toMutableList()
        current.add(ConductorEntry(System.currentTimeMillis().toString(), gauge, count))
        _uiState.value = _uiState.value.copy(conductors = current)
        recalculate()
    }

    fun removeConductor(id: String) {
        val current = _uiState.value.conductors.filterNot { it.id == id }
        _uiState.value = _uiState.value.copy(conductors = current)
        recalculate()
    }

    fun updateConductorCount(id: String, count: Int) {
        if (count < 1) return
        val current = _uiState.value.conductors.map {
            if (it.id == id) it.copy(count = count) else it
        }
        _uiState.value = _uiState.value.copy(conductors = current)
        recalculate()
    }

    fun setInternalClamps(count: Int) {
        _uiState.value = _uiState.value.copy(internalClampCount = count.coerceAtLeast(0))
        recalculate()
    }

    fun setSupportFittings(count: Int) {
        _uiState.value = _uiState.value.copy(supportFittingCount = count.coerceAtLeast(0))
        recalculate()
    }

    fun setDeviceYokes(single: Int, double: Int) {
        _uiState.value = _uiState.value.copy(
            deviceYokeCountSingle = single.coerceAtLeast(0),
            deviceYokeCountDouble = double.coerceAtLeast(0)
        )
        recalculate()
    }

    fun setGroundWires(grounds: Int, isolated: Int) {
        _uiState.value = _uiState.value.copy(
            groundWireCount = grounds.coerceAtLeast(0),
            isolatedGroundCount = isolated.coerceAtLeast(0)
        )
        recalculate()
    }

    fun selectStandardBox(box: StandardBoxSpec?) {
        _uiState.value = _uiState.value.copy(selectedStandardBox = box)
        recalculate()
    }

    fun setCustomBoxVolume(cuIn: Double) {
        _uiState.value = _uiState.value.copy(
            customBoxVolumeCuIn = cuIn.coerceAtLeast(0.0),
            selectedStandardBox = null
        )
        recalculate()
    }

    fun setPlasterRingExtension(cuIn: Double) {
        _uiState.value = _uiState.value.copy(plasterRingExtensionVolume = cuIn.coerceAtLeast(0.0))
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        val conductors = state.conductors

        // Determine largest conductor present in the box
        val largestGauge = conductors.maxByOrNull { it.gauge.volumeCuIn }?.gauge ?: WireGauge.AWG_12

        // 1. Conductor volume: Each conductor originating outside and terminating or spliced = 1 allowance
        var conductorVol = 0.0
        for (c in conductors) {
            conductorVol += c.count * c.gauge.volumeCuIn
        }

        // 2. Clamp deduction (NEC 314.16(B)(2)): 1 allowance of the largest conductor present, regardless of clamp count
        val clampVol = if (state.internalClampCount > 0) largestGauge.volumeCuIn else 0.0

        // 3. Support fittings / luminaire studs (NEC 314.16(B)(3)): 1 allowance for each type based on largest conductor
        val supportVol = state.supportFittingCount * largestGauge.volumeCuIn

        // 4. Device/Equipment yokes (NEC 314.16(B)(4)): Double volume allowance for each single-gang yoke (2 allowances), 4 allowances for double-gang
        // based on the largest conductor connected to device
        val deviceVol = (state.deviceYokeCountSingle * 2 + state.deviceYokeCountDouble * 4) * largestGauge.volumeCuIn

        // 5. Equipment grounding conductors (NEC 314.16(B)(5)):
        // 1 allowance based on largest EGC for up to 4 ground conductors.
        // + 0.25 (1/4) allowance for EACH additional ground conductor over 4.
        val groundAllowanceCount = when {
            state.groundWireCount <= 0 -> 0.0
            state.groundWireCount <= 4 -> 1.0
            else -> 1.0 + (state.groundWireCount - 4) * 0.25
        }
        val isolatedGroundAllowance = if (state.isolatedGroundCount > 0) 1.0 else 0.0
        val totalGroundAllowance = groundAllowanceCount + isolatedGroundAllowance
        val groundVol = totalGroundAllowance * largestGauge.volumeCuIn

        val totalReqVol = conductorVol + clampVol + supportVol + deviceVol + groundVol
        val totalReqCm3 = totalReqVol * 16.387064

        // Effective available volume
        val baseBoxVol = state.selectedStandardBox?.volumeCuIn ?: state.customBoxVolumeCuIn
        val effectiveVol = baseBoxVol + state.plasterRingExtensionVolume
        val isOver = if (effectiveVol > 0) totalReqVol > effectiveVol else false
        val fillPct = if (effectiveVol > 0) (totalReqVol / effectiveVol) * 100.0 else 0.0

        // Find standard compliant boxes
        val compliant = standardBoxes.filter { it.volumeCuIn + state.plasterRingExtensionVolume >= totalReqVol }

        val summary = "NEC 314.16 Required: ${String.format("%.2f", totalReqVol)} cu in (${String.format("%.1f", totalReqCm3)} cm³). " +
                "Available: ${String.format("%.2f", effectiveVol)} cu in (${String.format("%.1f", fillPct)}% fill). " +
                if (isOver) "WARNING: BOX OVERFILLED!" else "COMPLIANT."

        _uiState.value = state.copy(
            totalConductorVolumeCuIn = conductorVol,
            clampAllowanceVolumeCuIn = clampVol,
            supportFittingVolumeCuIn = supportVol,
            deviceAllowanceVolumeCuIn = deviceVol,
            groundAllowanceVolumeCuIn = groundVol,
            totalRequiredVolumeCuIn = totalReqVol,
            totalRequiredVolumeCm3 = totalReqCm3,
            effectiveAvailableVolumeCuIn = effectiveVol,
            isOverfilled = isOver,
            fillPercentage = fillPct,
            recommendedBoxes = compliant,
            largestConductorPresent = largestGauge,
            calculationSummary = summary
        )
    }

    fun saveToLogs() {
        viewModelScope.launch {
            val state = _uiState.value
            val details = "Box Fill Calculation (NEC 314.16):\n" +
                    "Required Volume: ${String.format("%.2f", state.totalRequiredVolumeCuIn)} cu in (${String.format("%.1f", state.totalRequiredVolumeCm3)} cm³)\n" +
                    "Selected Box: ${state.selectedStandardBox?.name ?: "Custom"} (${String.format("%.1f", state.effectiveAvailableVolumeCuIn)} cu in)\n" +
                    "Fill Percentage: ${String.format("%.1f", state.fillPercentage)}%\n" +
                    "Conductors: ${state.conductors.joinToString(", ") { "${it.count}x ${it.gauge.awg}" }}\n" +
                    "Status: ${if (state.isOverfilled) "OVERFILL VIOLATION" else "COMPLIANT"}"

            toolLogRepository.logToolActivity(
                toolType = "widget_box_fill_capacity",
                title = "Box Fill Capacity Calculator (NEC 314.16)",
                summary = "${state.conductors.sumOf { it.count }} conductors, ${state.deviceYokeCountSingle} devices, ${state.groundWireCount} grounds -> ${String.format("%.2f", state.totalRequiredVolumeCuIn)} cu in (${String.format("%.1f", state.fillPercentage)}% fill)",
                value = state.totalRequiredVolumeCuIn
            )
        }
    }
}
