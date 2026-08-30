package com.example.ui.screens.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.log10
import kotlin.math.pow

enum class ConversionCategory(
    val title: String,
    val units: List<String>,
    val iconName: String
) {
    CURRENCY("Currency", listOf("USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)", "CAD (C$)", "AUD (A$)", "CHF (Fr)", "CNY (¥)", "INR (₹)", "IDR (Rp)", "SGD (S$)", "NZD (NZ$)", "BRL (R$)", "MXN (Mex$)"), "attach_money"),
    VOLUME("Volume", listOf("Liters (L)", "Milliliters (mL)", "Cubic Meters (m³)", "Cubic Feet (ft³)", "Cubic Inches (in³)", "US Gallons (gal)", "UK Gallons (imp gal)", "Fluid Ounces (fl oz)", "US Cups", "US Pints", "US Quarts"), "view_in_ar"),
    LENGTH("Length", listOf("Meters (m)", "Kilometers (km)", "Miles (mi)", "Feet (ft)", "Inches (in)", "Yards (yd)", "Millimeters (mm)", "Centimeters (cm)", "Nautical Miles (nmi)"), "straighten"),
    MASS("Weight and mass", listOf("Kilograms (kg)", "Grams (g)", "Milligrams (mg)", "Metric Tonnes (t)", "Pounds (lbs)", "Ounces (oz)", "Stone (st)", "US Short Tons"), "scale"),
    TEMPERATURE("Temperature", listOf("Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)"), "thermostat"),
    ENERGY("Energy", listOf("Joules (J)", "Kilojoules (kJ)", "Calories (cal)", "Kilocalories (kcal)", "Watt-hours (Wh)", "Kilowatt-hours (kWh)", "Electronvolts (eV)", "BTU"), "local_fire_department"),
    AREA("Area", listOf("Square Meters (m²)", "Square Kilometers (km²)", "Square Feet (sq ft)", "Square Inches (sq in)", "Acres (ac)", "Hectares (ha)", "Square Yards (sq yd)"), "grid_on"),
    SPEED("Speed", listOf("Meters/sec (m/s)", "Kilometers/hr (km/h)", "Miles/hr (mph)", "Knots (kt)", "Feet/sec (ft/s)"), "directions_run"),
    TIME("Time", listOf("Seconds (s)", "Milliseconds (ms)", "Minutes (min)", "Hours (h)", "Days (d)", "Weeks (wk)", "Months (mo)", "Years (yr)"), "schedule"),
    POWER("Power", listOf("Watts (W)", "Kilowatts (kW)", "Megawatts (MW)", "Horsepower (HP)", "BTU/hr"), "electric_bolt"),
    DATA_NETWORK("Data", listOf("Bytes (B)", "Kilobytes (KB)", "Megabytes (MB)", "Gigabytes (GB)", "Terabytes (TB)", "Bits (b)", "Megabits (Mbps)", "Gigabits (Gbps)"), "dns"),
    PRESSURE("Pressure", listOf("Pascal (Pa)", "Kilopascal (kPa)", "Megapascal (MPa)", "Bar", "PSI (lb/in²)", "Atmosphere (atm)", "Torr (mmHg)"), "speed"),
    ANGLE("Angle", listOf("Degrees (°)", "Radians (rad)", "Gradians (grad)", "Arcseconds (\")", "Arcminutes (')"), "change_history"),
    ELECTRICIAN("Electrician & Power", listOf("Volts (V)", "Millivolts (mV)", "Kilovolts (kV)", "Amperes (A)", "Watts (W)", "Kilowatts (kW)", "Horsepower (HP)", "Kilowatt-hours (kWh)", "Joules (J)", "AWG 14 Wire (2.08 mm²)", "AWG 12 Wire (3.31 mm²)", "AWG 10 Wire (5.26 mm²)", "AWG 8 Wire (8.37 mm²)"), "bolt"),
    WOODWORKER("Woodworker & Timber", listOf("Board Feet (BF)", "Linear Feet (LF)", "Cubic Meters (m³)", "Square Feet (sq ft)", "Nominal 2x4 (Actual 1.5\" x 3.5\")", "Nominal 2x6 (Actual 1.5\" x 5.5\")", "Nominal 4x4 (Actual 3.5\" x 3.5\")"), "carpenter"),
    ARCHITECT_ENGINEER("Architect & Structural", listOf("Pascal (Pa)", "Kilopascal (kPa)", "Megapascal (MPa)", "PSI (lb/in²)", "Bar", "Newton-meter (N·m)", "kN·m", "lb·ft", "Liters/sec (L/s)", "Cubic meters/hr (m³/h)", "GPM (US Gal/Min)"), "architecture"),
    STONE_MASONRY("Stone & Masonry", listOf("Crushed Stone Tonne (t)", "Stone Volume (m³)", "Cubic Yards (yd³)", "Standard Pavers Count (100x200mm)", "Mortar 25kg Bags (for Masonry)"), "foundation"),
    GROUND_EARTHWORK("Groundwork & Soil", listOf("Bank Volume in-situ (m³)", "Loose Excavated Volume (m³)", "Compacted Soil Volume (m³)", "Excavator Bucket 1.0 m³", "Excavator Bucket 0.5 yd³"), "terrain"),
    SATELLITE_RF("Satellite & RF", listOf("Frequency (GHz)", "Wavelength (cm)", "RF Power (dBm)", "RF Power (Watts)", "RF Power (mW)", "FSPL 36000km GEO Loss (dB)"), "satellite_alt")
}

data class UnitConverterUiState(
    val category: ConversionCategory = ConversionCategory.LENGTH,
    val inputValue: String = "1.0",
    val fromUnitIndex: Int = 0,
    val toUnitIndex: Int = 1,
    val result: String = "",
    val extraDescription: String = "",
    val isCurrencyLive: Boolean = false,
    val lastUpdatedText: String = "Offline Estimated Rates"
)

class UnitConverterViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UnitConverterUiState())
    val uiState: StateFlow<UnitConverterUiState> = _uiState.asStateFlow()

    // Default currency rates relative to 1 USD
    private var currencyRates: Map<String, Double> = mapOf(
        "USD ($)" to 1.0,
        "EUR (€)" to 0.92,
        "GBP (£)" to 0.78,
        "JPY (¥)" to 155.2,
        "CAD (C$)" to 1.36,
        "AUD (A$)" to 1.51,
        "CHF (Fr)" to 0.90,
        "CNY (¥)" to 7.23,
        "INR (₹)" to 83.3,
        "IDR (Rp)" to 16250.0,
        "SGD (S$)" to 1.35,
        "NZD (NZ$)" to 1.63,
        "BRL (R$)" to 5.15,
        "MXN (Mex$)" to 17.5
    )

    init {
        recalculate()
        fetchLatestCurrencyRates()
    }

    fun fetchLatestCurrencyRates() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonStr = URL("https://open.er-api.com/v6/latest/USD").readText()
                val jsonObj = JSONObject(jsonStr)
                if (jsonObj.optString("result") == "success") {
                    val ratesObj = jsonObj.getJSONObject("rates")
                    val newRates = mutableMapOf<String, Double>()
                    val keysMap = mapOf(
                        "USD ($)" to "USD",
                        "EUR (€)" to "EUR",
                        "GBP (£)" to "GBP",
                        "JPY (¥)" to "JPY",
                        "CAD (C$)" to "CAD",
                        "AUD (A$)" to "AUD",
                        "CHF (Fr)" to "CHF",
                        "CNY (¥)" to "CNY",
                        "INR (₹)" to "INR",
                        "IDR (Rp)" to "IDR",
                        "SGD (S$)" to "SGD",
                        "NZD (NZ$)" to "NZD",
                        "BRL (R$)" to "BRL",
                        "MXN (Mex$)" to "MXN"
                    )
                    keysMap.forEach { (unitName, apiCode) ->
                        if (ratesObj.has(apiCode)) {
                            newRates[unitName] = ratesObj.getDouble(apiCode)
                        }
                    }
                    if (newRates.isNotEmpty()) {
                        currencyRates = currencyRates + newRates
                        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                        _uiState.value = _uiState.value.copy(
                            isCurrencyLive = true,
                            lastUpdatedText = "Live API rates updated at $timeStr"
                        )
                        recalculate()
                    }
                }
            } catch (_: Exception) {
                // Keep default offline rates
            }
        }
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

    fun onNumpadPress(key: String) {
        val current = _uiState.value.inputValue
        val newText = when (key) {
            "C" -> "0"
            "⌫" -> if (current.length <= 1 || (current.length == 2 && current.startsWith("-"))) "0" else current.dropLast(1)
            "±" -> if (current.startsWith("-")) current.substring(1) else if (current != "0") "-$current" else "0"
            "." -> if (current.contains(".")) current else "$current."
            else -> {
                if (current == "0" && key != ".") key
                else "$current$key"
            }
        }
        setInputValue(newText)
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
        val formattedResult = if (converted >= 1_000_000 || (converted < 0.0001 && converted > 0)) {
            String.format("%.4e", converted)
        } else if (converted == converted.toLong().toDouble()) {
            String.format("%.0f", converted)
        } else {
            String.format("%.4f", converted).trimEnd('0').trimEnd('.')
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
                    summary = "$num ${state.category.units.getOrElse(state.fromUnitIndex) { "" }} = $formattedResult ${state.category.units.getOrElse(state.toUnitIndex) { "" }}",
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
            ConversionCategory.CURRENCY -> convertCurrency(value, from, to)
            ConversionCategory.VOLUME -> Pair(convertVolume(value, from, to), "")
            ConversionCategory.LENGTH -> Pair(convertLength(value, from, to), "")
            ConversionCategory.MASS -> Pair(convertMass(value, from, to), "")
            ConversionCategory.TEMPERATURE -> Pair(convertTemperature(value, from, to), "")
            ConversionCategory.ENERGY -> Pair(convertEnergy(value, from, to), "")
            ConversionCategory.AREA -> Pair(convertArea(value, from, to), "")
            ConversionCategory.SPEED -> Pair(convertSpeed(value, from, to), "")
            ConversionCategory.TIME -> Pair(convertTime(value, from, to), "")
            ConversionCategory.POWER -> Pair(convertPower(value, from, to), "")
            ConversionCategory.DATA_NETWORK -> convertDataNetwork(value, from, to)
            ConversionCategory.PRESSURE -> Pair(convertPressure(value, from, to), "")
            ConversionCategory.ANGLE -> Pair(convertAngle(value, from, to), "")
            ConversionCategory.ELECTRICIAN -> convertElectrical(value, from, to)
            ConversionCategory.WOODWORKER -> convertWoodworking(value, from, to)
            ConversionCategory.ARCHITECT_ENGINEER -> convertArchitectural(value, from, to)
            ConversionCategory.STONE_MASONRY -> convertMasonry(value, from, to)
            ConversionCategory.GROUND_EARTHWORK -> convertEarthwork(value, from, to)
            ConversionCategory.SATELLITE_RF -> convertSatelliteRf(value, from, to)
        }
    }

    private fun convertCurrency(valIn: Double, from: Int, to: Int): Pair<Double, String> {
        val units = ConversionCategory.CURRENCY.units
        val fromName = units.getOrElse(from) { "USD ($)" }
        val toName = units.getOrElse(to) { "EUR (€)" }

        val rateFrom = currencyRates[fromName] ?: 1.0
        val rateTo = currencyRates[toName] ?: 1.0

        val valInUsd = valIn / rateFrom
        val result = valInUsd * rateTo
        val statusText = _uiState.value.lastUpdatedText
        return Pair(result, statusText)
    }

    private fun convertVolume(valIn: Double, from: Int, to: Int): Double {
        // Base: Liters
        val liters = when (from) {
            0 -> valIn
            1 -> valIn / 1000.0
            2 -> valIn * 1000.0
            3 -> valIn * 28.3168
            4 -> valIn * 0.0163871
            5 -> valIn * 3.78541
            6 -> valIn * 4.54609
            7 -> valIn * 0.0295735
            8 -> valIn * 0.236588
            9 -> valIn * 0.473176
            10 -> valIn * 0.946353
            else -> valIn
        }
        return when (to) {
            0 -> liters
            1 -> liters * 1000.0
            2 -> liters / 1000.0
            3 -> liters / 28.3168
            4 -> liters / 0.0163871
            5 -> liters / 3.78541
            6 -> liters / 4.54609
            7 -> liters / 0.0295735
            8 -> liters / 0.236588
            9 -> liters / 0.473176
            10 -> liters / 0.946353
            else -> liters
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
            8 -> valIn * 1852.0
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
            8 -> meters / 1852.0
            else -> meters
        }
    }

    private fun convertMass(valIn: Double, from: Int, to: Int): Double {
        val kg = when (from) {
            0 -> valIn
            1 -> valIn / 1000.0
            2 -> valIn / 1_000_000.0
            3 -> valIn * 1000.0
            4 -> valIn * 0.453592
            5 -> valIn * 0.0283495
            6 -> valIn * 6.35029
            7 -> valIn * 907.185
            else -> valIn
        }
        return when (to) {
            0 -> kg
            1 -> kg * 1000.0
            2 -> kg * 1_000_000.0
            3 -> kg / 1000.0
            4 -> kg / 0.453592
            5 -> kg / 0.0283495
            6 -> kg / 6.35029
            7 -> kg / 907.185
            else -> kg
        }
    }

    private fun convertTemperature(valIn: Double, from: Int, to: Int): Double {
        val celsius = when (from) {
            0 -> valIn
            1 -> (valIn - 32.0) * (5.0 / 9.0)
            2 -> valIn - 273.15
            else -> valIn
        }
        return when (to) {
            0 -> celsius
            1 -> (celsius * 9.0 / 5.0) + 32.0
            2 -> celsius + 273.15
            else -> celsius
        }
    }

    private fun convertEnergy(valIn: Double, from: Int, to: Int): Double {
        // Base: Joules
        val joules = when (from) {
            0 -> valIn
            1 -> valIn * 1000.0
            2 -> valIn * 4.184
            3 -> valIn * 4184.0
            4 -> valIn * 3600.0
            5 -> valIn * 3_600_000.0
            6 -> valIn * 1.60218e-19
            7 -> valIn * 1055.06
            else -> valIn
        }
        return when (to) {
            0 -> joules
            1 -> joules / 1000.0
            2 -> joules / 4.184
            3 -> joules / 4184.0
            4 -> joules / 3600.0
            5 -> joules / 3_600_000.0
            6 -> joules / 1.60218e-19
            7 -> joules / 1055.06
            else -> joules
        }
    }

    private fun convertArea(valIn: Double, from: Int, to: Int): Double {
        // Base: Square Meters m2
        val sqM = when (from) {
            0 -> valIn
            1 -> valIn * 1_000_000.0
            2 -> valIn * 0.092903
            3 -> valIn * 0.00064516
            4 -> valIn * 4046.86
            5 -> valIn * 10000.0
            6 -> valIn * 0.836127
            else -> valIn
        }
        return when (to) {
            0 -> sqM
            1 -> sqM / 1_000_000.0
            2 -> sqM / 0.092903
            3 -> sqM / 0.00064516
            4 -> sqM / 4046.86
            5 -> sqM / 10000.0
            6 -> sqM / 0.836127
            else -> sqM
        }
    }

    private fun convertSpeed(valIn: Double, from: Int, to: Int): Double {
        // Base: m/s
        val ms = when (from) {
            0 -> valIn
            1 -> valIn / 3.6
            2 -> valIn * 0.44704
            3 -> valIn * 0.514444
            4 -> valIn * 0.3048
            else -> valIn
        }
        return when (to) {
            0 -> ms
            1 -> ms * 3.6
            2 -> ms / 0.44704
            3 -> ms / 0.514444
            4 -> ms / 0.3048
            else -> ms
        }
    }

    private fun convertTime(valIn: Double, from: Int, to: Int): Double {
        // Base: Seconds
        val sec = when (from) {
            0 -> valIn
            1 -> valIn / 1000.0
            2 -> valIn * 60.0
            3 -> valIn * 3600.0
            4 -> valIn * 86400.0
            5 -> valIn * 604800.0
            6 -> valIn * 2629746.0
            7 -> valIn * 31556952.0
            else -> valIn
        }
        return when (to) {
            0 -> sec
            1 -> sec * 1000.0
            2 -> sec / 60.0
            3 -> sec / 3600.0
            4 -> sec / 86400.0
            5 -> sec / 604800.0
            6 -> sec / 2629746.0
            7 -> sec / 31556952.0
            else -> sec
        }
    }

    private fun convertPower(valIn: Double, from: Int, to: Int): Double {
        // Base: Watts
        val watts = when (from) {
            0 -> valIn
            1 -> valIn * 1000.0
            2 -> valIn * 1_000_000.0
            3 -> valIn * 745.7
            4 -> valIn * 0.293071
            else -> valIn
        }
        return when (to) {
            0 -> watts
            1 -> watts / 1000.0
            2 -> watts / 1_000_000.0
            3 -> watts / 745.7
            4 -> watts / 0.293071
            else -> watts
        }
    }

    private fun convertPressure(valIn: Double, from: Int, to: Int): Double {
        // Base: Pascal Pa
        val pa = when (from) {
            0 -> valIn
            1 -> valIn * 1000.0
            2 -> valIn * 1_000_000.0
            3 -> valIn * 100000.0
            4 -> valIn * 6894.76
            5 -> valIn * 101325.0
            6 -> valIn * 133.322
            else -> valIn
        }
        return when (to) {
            0 -> pa
            1 -> pa / 1000.0
            2 -> pa / 1_000_000.0
            3 -> pa / 100000.0
            4 -> pa / 6894.76
            5 -> pa / 101325.0
            6 -> pa / 133.322
            else -> pa
        }
    }

    private fun convertAngle(valIn: Double, from: Int, to: Int): Double {
        // Base: Degrees
        val deg = when (from) {
            0 -> valIn
            1 -> valIn * (180.0 / PI)
            2 -> valIn * 0.9
            3 -> valIn / 3600.0
            4 -> valIn / 60.0
            else -> valIn
        }
        return when (to) {
            0 -> deg
            1 -> deg * (PI / 180.0)
            2 -> deg / 0.9
            3 -> deg * 3600.0
            4 -> deg * 60.0
            else -> deg
        }
    }

    private fun convertElectrical(valIn: Double, from: Int, to: Int): Pair<Double, String> {
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
            7 -> watts * 0.001
            8 -> watts * 3600.0
            else -> valIn
        }
        return Pair(outVal, "Electrical Power / Conductor conversion")
    }

    private fun convertWoodworking(valIn: Double, from: Int, to: Int): Pair<Double, String> {
        if (from == 4 || to == 4) return Pair(valIn * 0.0036, "Nominal 2x4 = 1.5\" x 3.5\" (38 x 89 mm actual)")
        if (from == 5 || to == 5) return Pair(valIn * 0.0054, "Nominal 2x6 = 1.5\" x 5.5\" (38 x 140 mm actual)")
        if (from == 6 || to == 6) return Pair(valIn * 0.0079, "Nominal 4x4 = 3.5\" x 3.5\" (89 x 89 mm actual)")

        val m3 = when (from) {
            0 -> valIn * 0.00235974
            1 -> valIn * 0.00235974
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
        if (from == 0 && to == 1) return Pair(valIn / 1.6, "1 Tonne Crushed Stone ≈ 0.625 m³ volume (Density 1.6 t/m³)")
        if (from == 1 && to == 0) return Pair(valIn * 1.6, "1 m³ Crushed Stone ≈ 1.6 Tonnes")
        if (from == 3 || to == 3) return Pair(valIn * 50.0, "Standard 100x200mm Paver = 50 pavers per m²")
        if (from == 4 || to == 4) return Pair(valIn * 8.0, "Approx 8 bags of 25kg mortar per m³ brickwork")
        return Pair(valIn * 1.30795, "1 m³ ≈ 1.308 Cubic Yards")
    }

    private fun convertEarthwork(valIn: Double, from: Int, to: Int): Pair<Double, String> {
        val bankM3 = when (from) {
            0 -> valIn
            1 -> valIn / 1.25
            2 -> valIn / 0.90
            3 -> valIn * 1.0
            else -> valIn
        }
        val res = when (to) {
            0 -> bankM3
            1 -> bankM3 * 1.25
            2 -> bankM3 * 0.90
            3 -> bankM3 / 1.0
            else -> bankM3
        }
        return Pair(res, "Earthwork Soil Swell Factor: Bank +25% = Loose, Compacted = -10%")
    }

    private fun convertDataNetwork(valIn: Double, from: Int, to: Int): Pair<Double, String> {
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
            4 -> mb * 8.0
            6 -> (1024.0) / (mb * 0.125).coerceAtLeast(0.001)
            else -> mb
        }
        return Pair(res, "Data Transfer: 1 Byte = 8 bits | 1 GB = 1024 MB")
    }

    private fun convertSatelliteRf(valIn: Double, from: Int, to: Int): Pair<Double, String> {
        if (from == 0 && to == 1) {
            val wavelengthCm = (30.0 / valIn.coerceAtLeast(0.01))
            return Pair(wavelengthCm, "Wavelength λ = c / f = 30 cm / f(GHz)")
        }
        if (from == 1 && to == 0) {
            val freqGhz = (30.0 / valIn.coerceAtLeast(0.01))
            return Pair(freqGhz, "Frequency f = c / λ")
        }

        if (from == 2 && to == 4) {
            val mw = 10.0.pow(valIn / 10.0)
            return Pair(mw, "RF Power P(mW) = 10^(dBm / 10)")
        }
        if (from == 2 && to == 3) {
            val watts = 10.0.pow((valIn - 30.0) / 10.0)
            return Pair(watts, "RF Power P(W) = 10^((dBm - 30) / 10)")
        }

        if (from == 0 && to == 5) {
            val fsplDb = 92.45 + 20.0 * log10(35786.0) + 20.0 * log10(valIn.coerceAtLeast(0.1))
            return Pair(fsplDb, "Free Space Path Loss @ 35,786 km Geostationary Orbit")
        }

        return Pair(valIn, "RF & Microwave Transmission Parameter")
    }
}
