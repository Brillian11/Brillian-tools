package com.example.ui.screens.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.log10
import kotlin.math.pow

enum class ConversionCategory(
    val title: String,
    val units: List<String>,
    val iconName: String
) {
    LENGTH("Length", listOf("Meters (m)", "Kilometers (km)", "Miles (mi)", "Feet (ft)", "Inches (in)", "Yards (yd)", "Millimeters (mm)", "Centimeters (cm)"), "straighten"),
    MASS("Mass", listOf("Kilograms (kg)", "Grams (g)", "Metric Tonnes (t)", "Pounds (lbs)", "Ounces (oz)", "US Short Tons"), "scale"),
    ELECTRICIAN("Electrician & Power", listOf("Volts (V)", "Millivolts (mV)", "Kilovolts (kV)", "Amperes (A)", "Watts (W)", "Kilowatts (kW)", "Horsepower (HP)", "Kilowatt-hours (kWh)", "Joules (J)", "AWG 14 Wire (2.08 mm²)", "AWG 12 Wire (3.31 mm²)", "AWG 10 Wire (5.26 mm²)", "AWG 8 Wire (8.37 mm²)"), "bolt"),
    WOODWORKER("Woodworker & Timber", listOf("Board Feet (BF)", "Linear Feet (LF)", "Cubic Meters (m³)", "Square Feet (sq ft)", "Nominal 2x4 (Actual 1.5\" x 3.5\")", "Nominal 2x6 (Actual 1.5\" x 5.5\")", "Nominal 4x4 (Actual 3.5\" x 3.5\")"), "carpenter"),
    ARCHITECT_ENGINEER("Architect & Structural", listOf("Pascal (Pa)", "Kilopascal (kPa)", "Megapascal (MPa)", "PSI (lb/in²)", "Bar", "Newton-meter (N·m)", "kN·m", "lb·ft", "Liters/sec (L/s)", "Cubic meters/hr (m³/h)", "GPM (US Gal/Min)"), "architecture"),
    STONE_MASONRY("Stone & Masonry", listOf("Crushed Stone Tonne (t)", "Stone Volume (m³)", "Cubic Yards (yd³)", "Standard Pavers Count (100x200mm)", "Mortar 25kg Bags (for Masonry)"), "foundation"),
    GROUND_EARTHWORK("Groundwork & Soil", listOf("Bank Volume in-situ (m³)", "Loose Excavated Volume (m³)", "Compacted Soil Volume (m³)", "Excavator Bucket 1.0 m³", "Excavator Bucket 0.5 yd³"), "terrain"),
    DATA_NETWORK("Data & Network", listOf("Megabytes (MB)", "Gigabytes (GB)", "Terabytes (TB)", "Bytes (B)", "Megabits/sec (Mbps)", "Gigabits/sec (Gbps)", "Transfer Time 1GB File (sec)"), "lan"),
    SATELLITE_RF("Satellite & RF", listOf("Frequency (GHz)", "Wavelength (cm)", "RF Power (dBm)", "RF Power (Watts)", "RF Power (mW)", "FSPL 36000km GEO Loss (dB)"), "satellite_alt")
}

data class UnitConverterUiState(
    val category: ConversionCategory = ConversionCategory.LENGTH,
    val inputValue: String = "1.0",
    val fromUnitIndex: Int = 0,
    val toUnitIndex: Int = 1,
    val result: String = "",
    val extraDescription: String = ""
)

class UnitConverterViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnitConverterUiState())
    val uiState: StateFlow<UnitConverterUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setCategory(category: ConversionCategory) {
        _uiState.value = _uiState.value.copy(
            category = category,
            fromUnitIndex = 0,
            toUnitIndex = 1
        )
        recalculate()
    }

    fun setInputValue(value: String) {
        _uiState.value = _uiState.value.copy(inputValue = value)
        recalculate()
    }

    fun setFromUnitIndex(index: Int) {
        _uiState.value = _uiState.value.copy(fromUnitIndex = index)
        recalculate()
    }

    fun setToUnitIndex(index: Int) {
        _uiState.value = _uiState.value.copy(toUnitIndex = index)
        recalculate()
    }

    fun swapUnits() {
        val current = _uiState.value
        _uiState.value = current.copy(
            fromUnitIndex = current.toUnitIndex,
            toUnitIndex = current.fromUnitIndex
        )
        recalculate()
    }

    private fun recalculate() {
        val state = _uiState.value
        val num = state.inputValue.toDoubleOrNull() ?: 0.0
        val (converted, desc) = calculateConversion(state.category, num, state.fromUnitIndex, state.toUnitIndex)
        val formattedResult = if (converted >= 10000 || (converted < 0.001 && converted > 0)) {
            String.format("%.4e", converted)
        } else {
            String.format("%.4f", converted)
        }

        _uiState.value = state.copy(
            result = formattedResult,
            extraDescription = desc
        )

        if (num > 0) {
            viewModelScope.launch {
                toolLogRepository.logToolActivity(
                    toolType = "CONVERTER",
                    title = "Converted ${state.category.title}",
                    summary = "$num ${state.category.units[state.fromUnitIndex]} = $formattedResult ${state.category.units[state.toUnitIndex]}",
                    value = converted
                )
            }
        }
    }

    private fun calculateConversion(category: ConversionCategory, value: Double, from: Int, to: Int): Pair<Double, String> {
        if (from == to && category != ConversionCategory.WOODWORKER && category != ConversionCategory.ELECTRICIAN) {
            return Pair(value, "")
        }
        return when (category) {
            ConversionCategory.LENGTH -> Pair(convertLength(value, from, to), "")
            ConversionCategory.MASS -> Pair(convertMass(value, from, to), "")
            ConversionCategory.ELECTRICIAN -> convertElectrical(value, from, to)
            ConversionCategory.WOODWORKER -> convertWoodworking(value, from, to)
            ConversionCategory.ARCHITECT_ENGINEER -> convertArchitectural(value, from, to)
            ConversionCategory.STONE_MASONRY -> convertMasonry(value, from, to)
            ConversionCategory.GROUND_EARTHWORK -> convertEarthwork(value, from, to)
            ConversionCategory.DATA_NETWORK -> convertDataNetwork(value, from, to)
            ConversionCategory.SATELLITE_RF -> convertSatelliteRf(value, from, to)
        }
    }

    private fun convertLength(valIn: Double, from: Int, to: Int): Double {
        val meters = when (from) {
            0 -> valIn
            1 -> valIn * 1000.0
            2 -> valIn * 1609.34
            3 -> valIn * 0.3048
            4 -> valIn * 0.0254
            5 -> valIn * 0.9144
            6 -> valIn * 0.001
            7 -> valIn * 0.01
            else -> valIn
        }
        return when (to) {
            0 -> meters
            1 -> meters / 1000.0
            2 -> meters / 1609.34
            3 -> meters / 0.3048
            4 -> meters / 0.0254
            5 -> meters / 0.9144
            6 -> meters * 1000.0
            7 -> meters * 100.0
            else -> meters
        }
    }

    private fun convertMass(valIn: Double, from: Int, to: Int): Double {
        val kg = when (from) {
            0 -> valIn
            1 -> valIn / 1000.0
            2 -> valIn * 1000.0
            3 -> valIn * 0.453592
            4 -> valIn * 0.0283495
            5 -> valIn * 907.185
            else -> valIn
        }
        return when (to) {
            0 -> kg
            1 -> kg * 1000.0
            2 -> kg / 1000.0
            3 -> kg / 0.453592
            4 -> kg / 0.0283495
            5 -> kg / 907.185
            else -> kg
        }
    }

    private fun convertElectrical(valIn: Double, from: Int, to: Int): Pair<Double, String> {
        // Units: 0: V, 1: mV, 2: kV, 3: A, 4: W, 5: kW, 6: HP, 7: kWh, 8: J, 9: AWG 14, 10: AWG 12, 11: AWG 10, 12: AWG 8
        if (from in 9..12 || to in 9..12) {
            val awgInfo = mapOf(
                9 to Pair(2.08, "AWG 14: 15A Max Ampacity | Diameter 1.63mm"),
                10 to Pair(3.31, "AWG 12: 20A Max Ampacity | Diameter 2.05mm"),
                11 to Pair(5.26, "AWG 10: 30A Max Ampacity | Diameter 2.59mm"),
                12 to Pair(8.37, "AWG 8: 40A Max Ampacity | Diameter 3.26mm")
            )
            val info = awgInfo[from] ?: awgInfo[to] ?: Pair(2.08, "Standard Copper Conductor")
            return Pair(valIn * info.first, info.second)
        }

        // Power conversions (W base)
        val watts = when (from) {
            4 -> valIn
            5 -> valIn * 1000.0
            6 -> valIn * 745.7
            else -> valIn
        }
        val outVal = when (to) {
            4 -> watts
            5 -> watts / 1000.0
            6 -> watts / 745.7
            7 -> watts * 0.001 // kWh per 1 hour run
            8 -> watts * 3600.0 // Joules per 1 hour
            else -> valIn
        }
        return Pair(outVal, "Electrical Power / Conductor conversion")
    }

    private fun convertWoodworking(valIn: Double, from: Int, to: Int): Pair<Double, String> {
        // 0: BF, 1: LF, 2: m3, 3: sq ft, 4: 2x4, 5: 2x6, 6: 4x4
        if (from == 4 || to == 4) return Pair(valIn * 0.0036, "Nominal 2x4 = 1.5\" x 3.5\" (38 x 89 mm actual)")
        if (from == 5 || to == 5) return Pair(valIn * 0.0054, "Nominal 2x6 = 1.5\" x 5.5\" (38 x 140 mm actual)")
        if (from == 6 || to == 6) return Pair(valIn * 0.0079, "Nominal 4x4 = 3.5\" x 3.5\" (89 x 89 mm actual)")

        // BF to m3: 1 BF = 0.00235974 m3
        val m3 = when (from) {
            0 -> valIn * 0.00235974
            1 -> valIn * 0.00235974 // assuming 2x12
            2 -> valIn
            3 -> valIn * 0.00235974 * 12.0
            else -> valIn
        }
        val res = when (to) {
            0 -> m3 / 0.00235974
            1 -> m3 / 0.00235974
            2 -> m3
            3 -> m3 / 0.00235974 / 12.0
            else -> m3
        }
        return Pair(res, "1 Board Foot = 1\" x 12\" x 12\" = 144 cubic inches = 0.00236 m³")
    }

    private fun convertArchitectural(valIn: Double, from: Int, to: Int): Pair<Double, String> {
        // Pa base
        val pa = when (from) {
            0 -> valIn
            1 -> valIn * 1000.0
            2 -> valIn * 1_000_000.0
            3 -> valIn * 6894.76
            4 -> valIn * 100_000.0
            else -> valIn
        }
        val out = when (to) {
            0 -> pa
            1 -> pa / 1000.0
            2 -> pa / 1_000_000.0
            3 -> pa / 6894.76
            4 -> pa / 100_000.0
            else -> pa
        }
        return Pair(out, "Structural Stress & Hydraulic Pressure conversion")
    }

    private fun convertMasonry(valIn: Double, from: Int, to: Int): Pair<Double, String> {
        // Tonne crushed stone density ~ 1.6 t/m3
        if (from == 0 && to == 1) return Pair(valIn / 1.6, "1 Tonne Crushed Stone ≈ 0.625 m³ volume (Density 1.6 t/m³)")
        if (from == 1 && to == 0) return Pair(valIn * 1.6, "1 m³ Crushed Stone ≈ 1.6 Tonnes")
        if (from == 3 || to == 3) return Pair(valIn * 50.0, "Standard 100x200mm Paver = 50 pavers per m²")
        if (from == 4 || to == 4) return Pair(valIn * 8.0, "Approx 8 bags of 25kg mortar per m³ brickwork")
        return Pair(valIn * 1.30795, "1 m³ ≈ 1.308 Cubic Yards")
    }

    private fun convertEarthwork(valIn: Double, from: Int, to: Int): Pair<Double, String> {
        // Bank (1.0) -> Loose (1.25 swell factor) -> Compacted (0.90 shrink)
        val bankM3 = when (from) {
            0 -> valIn
            1 -> valIn / 1.25 // Swell factor 25%
            2 -> valIn / 0.90 // Compaction factor 10%
            3 -> valIn * 1.0  // 1m3 bucket
            else -> valIn
        }
        val res = when (to) {
            0 -> bankM3
            1 -> bankM3 * 1.25 // Loose volume
            2 -> bankM3 * 0.90 // Compacted volume
            3 -> bankM3 / 1.0  // Excavator bucket cycles
            else -> bankM3
        }
        return Pair(res, "Earthwork Soil Swell Factor: Bank +25% = Loose, Compacted = -10%")
    }

    private fun convertDataNetwork(valIn: Double, from: Int, to: Int): Pair<Double, String> {
        // MB base
        val mb = when (from) {
            0 -> valIn
            1 -> valIn * 1024.0
            2 -> valIn * 1024.0 * 1024.0
            3 -> valIn / (1024.0 * 1024.0)
            4 -> valIn * 0.125
            else -> valIn
        }
        val res = when (to) {
            0 -> mb
            1 -> mb / 1024.0
            2 -> mb / (1024.0 * 1024.0)
            3 -> mb * 1024.0 * 1024.0
            4 -> mb * 8.0 // Megabits
            6 -> (1024.0) / (mb * 0.125).coerceAtLeast(0.001) // Sec for 1GB at given Mbps
            else -> mb
        }
        return Pair(res, "Data Transfer: 1 Byte = 8 bits | 1 GB = 1024 MB")
    }

    private fun convertSatelliteRf(valIn: Double, from: Int, to: Int): Pair<Double, String> {
        // Frequency GHz to Wavelength cm
        if (from == 0 && to == 1) {
            val wavelengthCm = (30.0 / valIn.coerceAtLeast(0.01))
            return Pair(wavelengthCm, "Wavelength λ = c / f = 30 cm / f(GHz)")
        }
        if (from == 1 && to == 0) {
            val freqGhz = (30.0 / valIn.coerceAtLeast(0.01))
            return Pair(freqGhz, "Frequency f = c / λ")
        }

        // dBm to mW: mW = 10^(dBm / 10)
        if (from == 2 && to == 4) {
            val mw = 10.0.pow(valIn / 10.0)
            return Pair(mw, "RF Power P(mW) = 10^(dBm / 10)")
        }
        if (from == 2 && to == 3) {
            val watts = 10.0.pow((valIn - 30.0) / 10.0)
            return Pair(watts, "RF Power P(W) = 10^((dBm - 30) / 10)")
        }

        // FSPL @ 36000km GEO link: FSPL(dB) = 92.45 + 20*log10(d_km) + 20*log10(f_GHz)
        if (from == 0 && to == 5) {
            val fsplDb = 92.45 + 20.0 * log10(35786.0) + 20.0 * log10(valIn.coerceAtLeast(0.1))
            return Pair(fsplDb, "Free Space Path Loss @ 35,786 km Geostationary Orbit")
        }

        return Pair(valIn, "RF & Microwave Transmission Parameter")
    }
}
