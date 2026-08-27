package com.example.ui.screens.electrical

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow

enum class ResistorBandMode {
    BAND_4,
    BAND_5,
    BAND_6
}

data class ResistorColor(
    val name: String,
    val digit: Int?,
    val multiplier: Double?,
    val tolerancePct: Double?,
    val tempCoeffPpm: Int?,
    val color: Color
)

data class ResistorColorCodeUiState(
    val bandMode: ResistorBandMode = ResistorBandMode.BAND_4,

    // Selected Band Indices
    val band1Index: Int = 4, // Yellow (4)
    val band2Index: Int = 7, // Violet (7)
    val band3Index: Int = 0, // Black (0) for 5/6 band
    val multiplierIndex: Int = 4, // Red (x100) -> 4.7k
    val toleranceIndex: Int = 6, // Gold (±5%)
    val tcrIndex: Int = 0, // Brown (100 ppm)

    // Calculated Nominal & Range
    val nominalResistanceOhms: Double = 4700.0,
    val formattedResistance: String = "4.70 kΩ",
    val tolerancePct: Double = 5.0,
    val minResistanceOhms: Double = 4465.0,
    val maxResistanceOhms: Double = 4935.0,
    val tcrPpm: Int = 100,

    // SMD Decoder Mode
    val smdInputCode: String = "472",
    val smdDecodedResistance: String = "4.7 kΩ (±5%)"
) {
    companion object {
        val DIGIT_COLORS = listOf(
            ResistorColor("Black", 0, 1.0, null, null, Color(0xFF212121)),
            ResistorColor("Brown", 1, 10.0, 1.0, 100, Color(0xFF6D4C41)),
            ResistorColor("Red", 2, 100.0, 2.0, 50, Color(0xFFE53935)),
            ResistorColor("Orange", 3, 1000.0, null, 15, Color(0xFFFB8C00)),
            ResistorColor("Yellow", 4, 10000.0, null, 25, Color(0xFFFDD835)),
            ResistorColor("Green", 5, 100000.0, 0.5, null, Color(0xFF43A047)),
            ResistorColor("Blue", 6, 1000000.0, 0.25, 10, Color(0xFF1E88E5)),
            ResistorColor("Violet", 7, 10000000.0, 0.1, 5, Color(0xFF8E24AA)),
            ResistorColor("Gray", 8, 100000000.0, 0.05, null, Color(0xFF757575)),
            ResistorColor("White", 9, 1000000000.0, null, null, Color(0xFFEEEEEE))
        )

        val MULTIPLIER_COLORS = listOf(
            ResistorColor("Silver", null, 0.01, 10.0, null, Color(0xFFB0BEC5)),
            ResistorColor("Gold", null, 0.1, 5.0, null, Color(0xFFFFD54F)),
            ResistorColor("Black", null, 1.0, null, null, Color(0xFF212121)),
            ResistorColor("Brown", null, 10.0, 1.0, null, Color(0xFF6D4C41)),
            ResistorColor("Red", null, 100.0, 2.0, null, Color(0xFFE53935)),
            ResistorColor("Orange", null, 1000.0, null, null, Color(0xFFFB8C00)),
            ResistorColor("Yellow", null, 10000.0, null, null, Color(0xFFFDD835)),
            ResistorColor("Green", null, 100000.0, 0.5, null, Color(0xFF43A047)),
            ResistorColor("Blue", null, 1000000.0, 0.25, null, Color(0xFF1E88E5)),
            ResistorColor("Violet", null, 10000000.0, 0.1, null, Color(0xFF8E24AA)),
            ResistorColor("Gray", null, 100000000.0, 0.05, null, Color(0xFF757575)),
            ResistorColor("White", null, 1000000000.0, null, null, Color(0xFFEEEEEE))
        )

        val TOLERANCE_COLORS = listOf(
            ResistorColor("Brown (±1%)", null, null, 1.0, null, Color(0xFF6D4C41)),
            ResistorColor("Red (±2%)", null, null, 2.0, null, Color(0xFFE53935)),
            ResistorColor("Green (±0.5%)", null, null, 0.5, null, Color(0xFF43A047)),
            ResistorColor("Blue (±0.25%)", null, null, 0.25, null, Color(0xFF1E88E5)),
            ResistorColor("Violet (±0.1%)", null, null, 0.1, null, Color(0xFF8E24AA)),
            ResistorColor("Gray (±0.05%)", null, null, 0.05, null, Color(0xFF757575)),
            ResistorColor("Gold (±5%)", null, null, 5.0, null, Color(0xFFFFD54F)),
            ResistorColor("Silver (±10%)", null, null, 10.0, null, Color(0xFFB0BEC5))
        )

        val TCR_COLORS = listOf(
            ResistorColor("Brown (100 ppm/K)", null, null, null, 100, Color(0xFF6D4C41)),
            ResistorColor("Red (50 ppm/K)", null, null, null, 50, Color(0xFFE53935)),
            ResistorColor("Orange (15 ppm/K)", null, null, null, 15, Color(0xFFFB8C00)),
            ResistorColor("Yellow (25 ppm/K)", null, null, null, 25, Color(0xFFFDD835)),
            ResistorColor("Blue (10 ppm/K)", null, null, null, 10, Color(0xFF1E88E5)),
            ResistorColor("Violet (5 ppm/K)", null, null, null, 5, Color(0xFF8E24AA))
        )
    }
}

class ResistorColorCodeViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResistorColorCodeUiState())
    val uiState: StateFlow<ResistorColorCodeUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setBandMode(mode: ResistorBandMode) {
        _uiState.value = _uiState.value.copy(bandMode = mode)
        recalculate()
    }

    fun setBand1(index: Int) {
        _uiState.value = _uiState.value.copy(band1Index = index)
        recalculate()
    }

    fun setBand2(index: Int) {
        _uiState.value = _uiState.value.copy(band2Index = index)
        recalculate()
    }

    fun setBand3(index: Int) {
        _uiState.value = _uiState.value.copy(band3Index = index)
        recalculate()
    }

    fun setMultiplier(index: Int) {
        _uiState.value = _uiState.value.copy(multiplierIndex = index)
        recalculate()
    }

    fun setTolerance(index: Int) {
        _uiState.value = _uiState.value.copy(toleranceIndex = index)
        recalculate()
    }

    fun setTcr(index: Int) {
        _uiState.value = _uiState.value.copy(tcrIndex = index)
        recalculate()
    }

    fun decodeSmd(code: String) {
        val clean = code.trim().uppercase()
        val decoded = when {
            clean.length == 3 && clean.all { it.isDigit() } -> {
                val valDigits = clean.substring(0, 2).toDouble()
                val exp = clean[2].digitToInt()
                val ohms = valDigits * 10.0.pow(exp)
                formatOhms(ohms) + " (±5%)"
            }
            clean.length == 4 && clean.all { it.isDigit() } -> {
                val valDigits = clean.substring(0, 3).toDouble()
                val exp = clean[3].digitToInt()
                val ohms = valDigits * 10.0.pow(exp)
                formatOhms(ohms) + " (±1%)"
            }
            clean.contains('R') -> {
                val ohms = clean.replace('R', '.').toDoubleOrNull() ?: 0.0
                formatOhms(ohms)
            }
            else -> "Invalid SMD Code"
        }
        _uiState.value = _uiState.value.copy(smdInputCode = code, smdDecodedResistance = decoded)
    }

    private fun recalculate() {
        val s = _uiState.value
        val d1 = ResistorColorCodeUiState.DIGIT_COLORS[s.band1Index].digit ?: 0
        val d2 = ResistorColorCodeUiState.DIGIT_COLORS[s.band2Index].digit ?: 0
        val d3 = ResistorColorCodeUiState.DIGIT_COLORS[s.band3Index].digit ?: 0
        val mult = ResistorColorCodeUiState.MULTIPLIER_COLORS[s.multiplierIndex].multiplier ?: 1.0
        val tol = ResistorColorCodeUiState.TOLERANCE_COLORS[s.toleranceIndex].tolerancePct ?: 5.0
        val tcr = ResistorColorCodeUiState.TCR_COLORS[s.tcrIndex].tempCoeffPpm ?: 100

        val baseDigits = if (s.bandMode == ResistorBandMode.BAND_4) {
            (d1 * 10) + d2
        } else {
            (d1 * 100) + (d2 * 10) + d3
        }

        val ohms = baseDigits.toDouble() * mult
        val tolDelta = ohms * (tol / 100.0)
        val minOhms = ohms - tolDelta
        val maxOhms = ohms + tolDelta

        _uiState.value = _uiState.value.copy(
            nominalResistanceOhms = ohms,
            formattedResistance = formatOhms(ohms),
            tolerancePct = tol,
            minResistanceOhms = minOhms,
            maxResistanceOhms = maxOhms,
            tcrPpm = tcr
        )
    }

    private fun formatOhms(ohms: Double): String {
        return when {
            ohms >= 1_000_000_000 -> String.format("%.2f GΩ", ohms / 1_000_000_000.0)
            ohms >= 1_000_000 -> String.format("%.2f MΩ", ohms / 1_000_000.0)
            ohms >= 1_000 -> String.format("%.2f kΩ", ohms / 1_000.0)
            else -> String.format("%.2f Ω", ohms)
        }
    }

    fun logResistor() {
        val s = _uiState.value
        val summary = "${s.bandMode.name}: ${s.formattedResistance} ±${s.tolerancePct}% (Range: ${formatOhms(s.minResistanceOhms)} - ${formatOhms(s.maxResistanceOhms)})"
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "ELECTRICAL",
                title = "Resistor Color Decoder",
                summary = summary,
                value = s.nominalResistanceOhms
            )
        }
    }
}
