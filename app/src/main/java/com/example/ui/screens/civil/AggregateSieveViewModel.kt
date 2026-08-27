package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.pow

data class SieveItem(
    val id: String,
    val name: String,
    val sizeMm: Double,
    val massRetainedGrams: Double,
    val isStandardFmSieve: Boolean = false,
    val astmMinPassing: Double? = null,
    val astmMaxPassing: Double? = null,
    // Calculated fields
    val percentRetained: Double = 0.0,
    val cumulativePercentRetained: Double = 0.0,
    val percentPassing: Double = 0.0,
    val isWithinSpec: Boolean = true
)

enum class AggregateType(val label: String) {
    CONCRETE_SAND("Concrete Sand (ASTM C33 Fine Aggregate)"),
    COARSE_AGGREGATE_57("Coarse Aggregate #57 Stone (1\" to #4)"),
    ROAD_BASE_CRUSHED("Crushed Stone Base Course (AASHTO M147)"),
    CUSTOM("Custom Sieve Analysis")
}

data class AggregateSieveUiState(
    val aggregateType: AggregateType = AggregateType.CONCRETE_SAND,
    val sieves: List<SieveItem> = emptyList(),
    val totalSampleMassGrams: Double = 0.0,
    val washLossGrams: Double = 0.0,

    // Fineness Modulus
    val finenessModulus: Double = 0.0,
    val isFmWithinAstmC33: Boolean = true, // 2.30 to 3.10 for sand

    // Soil & Grain Size Characteristics
    val d10Mm: Double = 0.0,
    val d30Mm: Double = 0.0,
    val d60Mm: Double = 0.0,
    val uniformityCoeffCu: Double = 0.0,
    val curvatureCoeffCc: Double = 0.0,
    val gradationClassification: String = "Well-Graded"
)

class AggregateSieveViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(AggregateSieveUiState())
    val uiState: StateFlow<AggregateSieveUiState> = _uiState.asStateFlow()

    init {
        loadPreset(AggregateType.CONCRETE_SAND)
    }

    fun setAggregateType(type: AggregateType) {
        loadPreset(type)
    }

    fun updateMassRetained(sieveId: String, mass: Double) {
        val updated = _uiState.value.sieves.map { s ->
            if (s.id == sieveId) s.copy(massRetainedGrams = mass.coerceAtLeast(0.0)) else s
        }
        _uiState.value = _uiState.value.copy(sieves = updated)
        recalculate()
    }

    fun updateWashLoss(loss: Double) {
        _uiState.value = _uiState.value.copy(washLossGrams = loss.coerceAtLeast(0.0))
        recalculate()
    }

    private fun loadPreset(type: AggregateType) {
        val sieves = when (type) {
            AggregateType.CONCRETE_SAND -> listOf(
                SieveItem("3_8", "3/8\" (9.5 mm)", 9.5, 0.0, isStandardFmSieve = true, astmMinPassing = 100.0, astmMaxPassing = 100.0),
                SieveItem("no4", "No. 4 (4.75 mm)", 4.75, 12.5, isStandardFmSieve = true, astmMinPassing = 95.0, astmMaxPassing = 100.0),
                SieveItem("no8", "No. 8 (2.36 mm)", 2.36, 68.0, isStandardFmSieve = true, astmMinPassing = 80.0, astmMaxPassing = 100.0),
                SieveItem("no16", "No. 16 (1.18 mm)", 1.18, 115.0, isStandardFmSieve = true, astmMinPassing = 50.0, astmMaxPassing = 85.0),
                SieveItem("no30", "No. 30 (600 µm)", 0.600, 142.0, isStandardFmSieve = true, astmMinPassing = 25.0, astmMaxPassing = 60.0),
                SieveItem("no50", "No. 50 (300 µm)", 0.300, 108.0, isStandardFmSieve = true, astmMinPassing = 5.0, astmMaxPassing = 30.0),
                SieveItem("no100", "No. 100 (150 µm)", 0.150, 42.0, isStandardFmSieve = true, astmMinPassing = 0.0, astmMaxPassing = 10.0),
                SieveItem("no200", "No. 200 (75 µm)", 0.075, 8.5, isStandardFmSieve = false, astmMinPassing = 0.0, astmMaxPassing = 3.0),
                SieveItem("pan", "Pan (< 75 µm)", 0.001, 4.0, isStandardFmSieve = false)
            )
            AggregateType.COARSE_AGGREGATE_57 -> listOf(
                SieveItem("1_5", "1.5\" (37.5 mm)", 37.5, 0.0, isStandardFmSieve = true, astmMinPassing = 100.0, astmMaxPassing = 100.0),
                SieveItem("1_0", "1.0\" (25.0 mm)", 25.0, 25.0, isStandardFmSieve = false, astmMinPassing = 95.0, astmMaxPassing = 100.0),
                SieveItem("1_2", "1/2\" (12.5 mm)", 12.5, 260.0, isStandardFmSieve = false, astmMinPassing = 25.0, astmMaxPassing = 60.0),
                SieveItem("no4", "No. 4 (4.75 mm)", 4.75, 185.0, isStandardFmSieve = true, astmMinPassing = 0.0, astmMaxPassing = 10.0),
                SieveItem("no8", "No. 8 (2.36 mm)", 2.36, 20.0, isStandardFmSieve = true, astmMinPassing = 0.0, astmMaxPassing = 5.0),
                SieveItem("pan", "Pan", 0.001, 10.0, isStandardFmSieve = false)
            )
            AggregateType.ROAD_BASE_CRUSHED -> listOf(
                SieveItem("1_5", "1.5\" (37.5 mm)", 37.5, 0.0, isStandardFmSieve = true, astmMinPassing = 100.0, astmMaxPassing = 100.0),
                SieveItem("1_0", "1.0\" (25.0 mm)", 25.0, 50.0, isStandardFmSieve = false, astmMinPassing = 70.0, astmMaxPassing = 100.0),
                SieveItem("3_4", "3/4\" (19.0 mm)", 19.0, 90.0, isStandardFmSieve = true, astmMinPassing = 55.0, astmMaxPassing = 85.0),
                SieveItem("no4", "No. 4 (4.75 mm)", 4.75, 140.0, isStandardFmSieve = true, astmMinPassing = 30.0, astmMaxPassing = 60.0),
                SieveItem("no40", "No. 40 (425 µm)", 0.425, 120.0, isStandardFmSieve = false, astmMinPassing = 10.0, astmMaxPassing = 30.0),
                SieveItem("no200", "No. 200 (75 µm)", 0.075, 75.0, isStandardFmSieve = false, astmMinPassing = 4.0, astmMaxPassing = 12.0),
                SieveItem("pan", "Pan", 0.001, 25.0, isStandardFmSieve = false)
            )
            AggregateType.CUSTOM -> listOf(
                SieveItem("3_8", "3/8\" (9.5 mm)", 9.5, 0.0, isStandardFmSieve = true),
                SieveItem("no4", "No. 4 (4.75 mm)", 4.75, 20.0, isStandardFmSieve = true),
                SieveItem("no8", "No. 8 (2.36 mm)", 2.36, 75.0, isStandardFmSieve = true),
                SieveItem("no16", "No. 16 (1.18 mm)", 1.18, 120.0, isStandardFmSieve = true),
                SieveItem("no30", "No. 30 (600 µm)", 0.600, 130.0, isStandardFmSieve = true),
                SieveItem("no50", "No. 50 (300 µm)", 0.300, 100.0, isStandardFmSieve = true),
                SieveItem("no100", "No. 100 (150 µm)", 0.150, 40.0, isStandardFmSieve = true),
                SieveItem("pan", "Pan", 0.001, 15.0, isStandardFmSieve = false)
            )
        }
        _uiState.value = _uiState.value.copy(
            aggregateType = type,
            sieves = sieves
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val sumRetained = s.sieves.sumOf { it.massRetainedGrams } + s.washLossGrams
        val totalMass = if (sumRetained > 0) sumRetained else 500.0

        var runningCumPct = 0.0
        var fmSum = 0.0

        val processedSieves = s.sieves.map { item ->
            val pctRetained = (item.massRetainedGrams / totalMass) * 100.0
            runningCumPct += pctRetained
            val pctPassing = (100.0 - runningCumPct).coerceIn(0.0, 100.0)

            if (item.isStandardFmSieve && item.id != "pan") {
                fmSum += runningCumPct
            }

            val withinSpec = if (item.astmMinPassing != null && item.astmMaxPassing != null) {
                pctPassing >= item.astmMinPassing - 0.01 && pctPassing <= item.astmMaxPassing + 0.01
            } else true

            item.copy(
                percentRetained = pctRetained,
                cumulativePercentRetained = runningCumPct,
                percentPassing = pctPassing,
                isWithinSpec = withinSpec
            )
        }

        val fm = fmSum / 100.0
        val isFmPass = fm in 2.30..3.10

        // Interpolate D10, D30, D60 for grain distribution curve
        val d10 = interpolateDiameter(processedSieves, 10.0)
        val d30 = interpolateDiameter(processedSieves, 30.0)
        val d60 = interpolateDiameter(processedSieves, 60.0)

        val cu = if (d10 > 0) (d60 / d10) else 0.0
        val cc = if (d10 > 0 && d60 > 0) (d30.pow(2) / (d10 * d60)) else 0.0

        val classification = when {
            cu >= 6.0 && cc in 1.0..3.0 -> "SW (Well-Graded Sand)"
            cu >= 4.0 && cc in 1.0..3.0 -> "GW (Well-Graded Gravel)"
            cu < 6.0 && cc !in 1.0..3.0 -> "SP (Poorly-Graded / Uniform Sand)"
            else -> "GP / Gap-Graded Aggregate"
        }

        _uiState.value = s.copy(
            sieves = processedSieves,
            totalSampleMassGrams = totalMass,
            finenessModulus = fm,
            isFmWithinAstmC33 = isFmPass,
            d10Mm = d10,
            d30Mm = d30,
            d60Mm = d60,
            uniformityCoeffCu = cu,
            curvatureCoeffCc = cc,
            gradationClassification = classification
        )
    }

    private fun interpolateDiameter(sieves: List<SieveItem>, targetPercentPassing: Double): Double {
        for (i in 0 until sieves.size - 1) {
            val upper = sieves[i]
            val lower = sieves[i + 1]
            if (upper.percentPassing >= targetPercentPassing && lower.percentPassing <= targetPercentPassing) {
                if (upper.sizeMm <= 0 || lower.sizeMm <= 0) return lower.sizeMm
                val logUpper = log10(upper.sizeMm)
                val logLower = log10(lower.sizeMm)
                val range = upper.percentPassing - lower.percentPassing
                if (range == 0.0) return upper.sizeMm
                val frac = (targetPercentPassing - lower.percentPassing) / range
                val logTarget = logLower + frac * (logUpper - logLower)
                return 10.0.pow(logTarget)
            }
        }
        return sieves.firstOrNull()?.sizeMm ?: 1.0
    }

    fun saveToLog() {
        val s = _uiState.value
        toolLogRepository?.let { repo ->
            viewModelScope.launch {
                val summary = "Sieve Analysis: ${s.aggregateType.label} FM=${String.format("%.2f", s.finenessModulus)} (${if (s.isFmWithinAstmC33) "ASTM C33 PASS" else "FAIL"}), Cu=${String.format("%.2f", s.uniformityCoeffCu)}, Cc=${String.format("%.2f", s.curvatureCoeffCc)}"
                repo.logToolActivity(
                    toolType = "aggregate_sieve",
                    title = "Aggregate Sieve & Fineness Modulus",
                    summary = summary,
                    value = s.finenessModulus
                )
            }
        }
    }
}
