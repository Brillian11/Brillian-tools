package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ConduitType(val label: String) {
    EMT("EMT (Electrical Metallic Tubing)"),
    PVC_SCH40("PVC Schedule 40"),
    PVC_SCH80("PVC Schedule 80"),
    RMC("RMC (Rigid Metal Conduit)"),
    FMC("FMC (Flexible Metal Conduit)")
}

data class ConduitSpec(
    val tradeSize: String,
    val totalAreaSqIn: Double
)

data class WireInsulation(
    val name: String,
    val gauge: String,
    val areaSqIn: Double
)

data class WireEntry(
    val id: String,
    val wireType: String, // e.g. "12 AWG THHN"
    val count: Int,
    val areaPerWireSqIn: Double
)

data class ConduitFillUiState(
    val conduitType: ConduitType = ConduitType.EMT,
    val selectedTradeSizeIndex: Int = 1, // 3/4" default
    val wireList: List<WireEntry> = listOf(
        WireEntry("1", "12 AWG THHN/THWN-2", 3, 0.0133),
        WireEntry("2", "12 AWG THHN Ground", 1, 0.0133)
    ),

    // Ampacity Sizer Inputs
    val targetAmps: Double = 20.0,
    val ambientTempC: Int = 30,
    val isContinuousLoad: Boolean = true, // 125% factor

    // Calculated Sizing Outputs
    val totalConductorsCount: Int = 4,
    val totalWiresAreaSqIn: Double = 0.0532,
    val conduitTotalAreaSqIn: Double = 0.533,
    val maxAllowedFillPct: Double = 40.0, // 53% (1 wire), 31% (2 wires), 40% (3+ wires)
    val maxAllowedAreaSqIn: Double = 0.2132,
    val actualFillPct: Double = 9.98,
    val isConduitOverfilled: Boolean = false,
    val recommendedTradeSize: String = "1/2\"",

    // Wire Ampacity Output
    val requiredAmpacity: Double = 25.0,
    val recommendedWireGauge: String = "10 AWG THHN (30A @ 75°C)"
) {
    companion object {
        val TRADE_SIZES = listOf("1/2\"", "3/4\"", "1\"", "1-1/4\"", "1-1/2\"", "2\"", "2-1/2\"", "3\"", "3-1/2\"", "4\"")

        // Total Internal Areas (sq in) from NEC Chapter 9 Table 4
        val CONDUIT_AREAS = mapOf(
            ConduitType.EMT to listOf(0.304, 0.533, 0.864, 1.496, 2.036, 3.356, 5.858, 8.846, 11.545, 14.753),
            ConduitType.PVC_SCH40 to listOf(0.285, 0.508, 0.832, 1.453, 1.986, 3.287, 4.695, 7.268, 9.779, 12.598),
            ConduitType.PVC_SCH80 to listOf(0.224, 0.419, 0.707, 1.272, 1.755, 2.971, 4.271, 6.679, 9.030, 11.696),
            ConduitType.RMC to listOf(0.314, 0.549, 0.887, 1.526, 2.071, 3.408, 4.869, 7.499, 10.072, 12.946),
            ConduitType.FMC to listOf(0.307, 0.519, 0.817, 1.327, 1.948, 3.142, 4.909, 7.069, 9.621, 12.566)
        )

        // Wire Areas (sq in) from NEC Chapter 9 Table 5 (THHN/THWN-2)
        val WIRE_LIBRARY = listOf(
            WireInsulation("14 AWG THHN", "14 AWG", 0.0097),
            WireInsulation("12 AWG THHN", "12 AWG", 0.0133),
            WireInsulation("10 AWG THHN", "10 AWG", 0.0211),
            WireInsulation("8 AWG THHN", "8 AWG", 0.0366),
            WireInsulation("6 AWG THHN", "6 AWG", 0.0507),
            WireInsulation("4 AWG THHN", "4 AWG", 0.0824),
            WireInsulation("3 AWG THHN", "3 AWG", 0.0973),
            WireInsulation("2 AWG THHN", "2 AWG", 0.1158),
            WireInsulation("1 AWG THHN", "1 AWG", 0.1562),
            WireInsulation("1/0 AWG THHN", "1/0 AWG", 0.1855),
            WireInsulation("2/0 AWG THHN", "2/0 AWG", 0.2223),
            WireInsulation("3/0 AWG THHN", "3/0 AWG", 0.2679),
            WireInsulation("4/0 AWG THHN", "4/0 AWG", 0.3237),
            WireInsulation("250 kcmil THHN", "250 kcmil", 0.3970),
            WireInsulation("350 kcmil THHN", "350 kcmil", 0.5242),
            WireInsulation("500 kcmil THHN", "500 kcmil", 0.7073)
        )
    }
}

class ConduitFillViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConduitFillUiState())
    val uiState: StateFlow<ConduitFillUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setConduitType(type: ConduitType) {
        _uiState.value = _uiState.value.copy(conduitType = type)
        recalculate()
    }

    fun setTradeSizeIndex(index: Int) {
        if (index in ConduitFillUiState.TRADE_SIZES.indices) {
            _uiState.value = _uiState.value.copy(selectedTradeSizeIndex = index)
            recalculate()
        }
    }

    fun addWire(name: String, gauge: String, count: Int, area: Double) {
        val entry = WireEntry(
            id = System.currentTimeMillis().toString(),
            wireType = name,
            count = count.coerceAtLeast(1),
            areaPerWireSqIn = area
        )
        _uiState.value = _uiState.value.copy(wireList = _uiState.value.wireList + entry)
        recalculate()
    }

    fun updateWireCount(id: String, newCount: Int) {
        if (newCount <= 0) {
            removeWire(id)
        } else {
            val updated = _uiState.value.wireList.map {
                if (it.id == id) it.copy(count = newCount) else it
            }
            _uiState.value = _uiState.value.copy(wireList = updated)
            recalculate()
        }
    }

    fun removeWire(id: String) {
        val updated = _uiState.value.wireList.filter { it.id != id }
        _uiState.value = _uiState.value.copy(wireList = updated)
        recalculate()
    }

    fun updateAmpacityInputs(targetAmps: Double, tempC: Int, continuous: Boolean) {
        _uiState.value = _uiState.value.copy(
            targetAmps = targetAmps.coerceAtLeast(1.0),
            ambientTempC = tempC,
            isContinuousLoad = continuous
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val totalCount = s.wireList.sumOf { it.count }
        val totalWireArea = s.wireList.sumOf { it.count * it.areaPerWireSqIn }

        val areaList = ConduitFillUiState.CONDUIT_AREAS[s.conduitType] ?: ConduitFillUiState.CONDUIT_AREAS[ConduitType.EMT]!!
        val conduitTotalArea = areaList[s.selectedTradeSizeIndex]

        // NEC Table 1 Max Fill % Rules: 1 wire = 53%, 2 wires = 31%, 3+ wires = 40%
        val maxFillPct = when {
            totalCount == 1 -> 53.0
            totalCount == 2 -> 31.0
            totalCount >= 3 -> 40.0
            else -> 40.0
        }

        val maxAllowedArea = conduitTotalArea * (maxFillPct / 100.0)
        val fillPct = if (conduitTotalArea > 0) (totalWireArea / conduitTotalArea) * 100.0 else 0.0
        val isOverfilled = fillPct > maxFillPct

        // Find recommended minimum conduit size
        var recIndex = s.selectedTradeSizeIndex
        for (i in areaList.indices) {
            val totalA = areaList[i]
            val maxA = totalA * (maxFillPct / 100.0)
            if (totalWireArea <= maxA) {
                recIndex = i
                break
            }
        }
        val recTradeSize = ConduitFillUiState.TRADE_SIZES[recIndex]

        // Ampacity Sizing Calculation
        val reqAmpacity = if (s.isContinuousLoad) s.targetAmps * 1.25 else s.targetAmps
        val recWireGauge = when {
            reqAmpacity <= 15.0 -> "14 AWG THHN (15A @ 60°C/75°C)"
            reqAmpacity <= 20.0 -> "12 AWG THHN (20A @ 75°C)"
            reqAmpacity <= 30.0 -> "10 AWG THHN (30A @ 75°C)"
            reqAmpacity <= 50.0 -> "8 AWG THHN (50A @ 75°C)"
            reqAmpacity <= 65.0 -> "6 AWG THHN (65A @ 75°C)"
            reqAmpacity <= 85.0 -> "4 AWG THHN (85A @ 75°C)"
            reqAmpacity <= 100.0 -> "3 AWG THHN (100A @ 75°C)"
            reqAmpacity <= 115.0 -> "2 AWG THHN (115A @ 75°C)"
            reqAmpacity <= 130.0 -> "1 AWG THHN (130A @ 75°C)"
            reqAmpacity <= 150.0 -> "1/0 AWG THHN (150A @ 75°C)"
            reqAmpacity <= 175.0 -> "2/0 AWG THHN (175A @ 75°C)"
            reqAmpacity <= 200.0 -> "3/0 AWG THHN (200A @ 75°C)"
            reqAmpacity <= 230.0 -> "4/0 AWG THHN (230A @ 75°C)"
            reqAmpacity <= 255.0 -> "250 kcmil THHN (255A @ 75°C)"
            else -> "500 kcmil THHN (380A @ 75°C)"
        }

        _uiState.value = _uiState.value.copy(
            totalConductorsCount = totalCount,
            totalWiresAreaSqIn = totalWireArea,
            conduitTotalAreaSqIn = conduitTotalArea,
            maxAllowedFillPct = maxFillPct,
            maxAllowedAreaSqIn = maxAllowedArea,
            actualFillPct = fillPct,
            isConduitOverfilled = isOverfilled,
            recommendedTradeSize = recTradeSize,
            requiredAmpacity = reqAmpacity,
            recommendedWireGauge = recWireGauge
        )
    }

    fun logConduitFill() {
        val s = _uiState.value
        val sizeName = ConduitFillUiState.TRADE_SIZES[s.selectedTradeSizeIndex]
        val summary = "${sizeName} ${s.conduitType.name}: ${s.totalConductorsCount} conductors, Fill = ${String.format("%.1f", s.actualFillPct)}% / Max ${String.format("%.0f", s.maxAllowedFillPct)}% (Status: ${if (s.isConduitOverfilled) "OVERFILLED" else "OK"})"
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "ELECTRICAL",
                title = "Conduit Fill Sizing",
                summary = summary,
                value = s.actualFillPct
            )
        }
    }
}
