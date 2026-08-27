package com.example.ui.screens.maintenance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

enum class HvacClimateZone(
    val zoneName: String,
    val coolingBaseBtuPerSqFt: Double,
    val heatingBaseBtuPerSqFt: Double,
    val desc: String
) {
    ZONE_1_TROPICAL("Zone 1: Tropical / Hot-Humid (e.g. Florida, Indonesia)", 38.0, 25.0, "High humidity & heavy cooling demand"),
    ZONE_2_DEEP_SOUTH("Zone 2: Deep South (e.g. Texas, Gulf Coast)", 32.0, 32.0, "Long hot summers, mild winters"),
    ZONE_3_SOUTHERN("Zone 3: Southern / Mild (e.g. Georgia, California)", 28.0, 38.0, "Balanced cooling and mild heating"),
    ZONE_4_CENTRAL("Zone 4: Mixed-Humid (e.g. Virginia, Ohio)", 24.0, 45.0, "Moderate summer cooling, cold winter heating"),
    ZONE_5_NORTHERN("Zone 5: Cool / Northern (e.g. Chicago, New York)", 21.0, 50.0, "Heavy winter heating, moderate cooling"),
    ZONE_6_7_COLD("Zone 6-7: Very Cold / Subarctic (e.g. Minnesota, Canada)", 18.0, 58.0, "Extreme winter freeze & deep heating demand")
}

enum class InsulationQuality(val multiplier: Double, val label: String, val desc: String) {
    POOR_UNINSULATED(1.28, "Poor / Uninsulated", "Old drafty walls, single pane glass, unsealed attic"),
    STANDARD_AVERAGE(1.00, "Standard / Code Minimum", "R-13 wall batt, R-30 attic, double pane clear glass"),
    GOOD_ENERGY_STAR(0.85, "Good / Well-Insulated", "R-21 walls, R-49 attic, Low-E Argon double pane windows"),
    PASSIVE_HIGH_PERF(0.70, "Passive House / Spray Foam", "Airtight envelope, R-30+ continuous foam, triple pane")
}

enum class SunExposure(val coolingFactor: Double, val label: String) {
    NORTH_SHADED(0.92, "North Facing / Heavily Shaded"),
    EAST_MORNING(1.00, "East Facing (Morning Sun)"),
    SOUTH_BALANCED(1.02, "South Facing (Balanced Daylight)"),
    WEST_AFTERNOON(1.16, "West Facing (Intense Afternoon Solar Heat)")
}

data class HvacLoadUiState(
    val isMetric: Boolean = false,
    
    // Room Dimensions
    val roomLength: Double = 20.0, // ft or m
    val roomWidth: Double = 15.0,  // ft or m
    val ceilingHeight: Double = 8.0, // ft or m
    
    // Environmental & Envelope Specs
    val climateZone: HvacClimateZone = HvacClimateZone.ZONE_1_TROPICAL,
    val insulation: InsulationQuality = InsulationQuality.STANDARD_AVERAGE,
    val sunExposure: SunExposure = SunExposure.WEST_AFTERNOON,
    
    // Windows & Internal Loads
    val windowCount: Int = 2,
    val occupantCount: Int = 2, // 2 baseline
    val isKitchenOrCooking: Boolean = false,
    val hasHeavyElectronics: Boolean = false,

    // Calculated Area & Volume
    val floorAreaSqFt: Double = 300.0,
    val floorAreaSqM: Double = 27.87,
    val roomVolumeCuFt: Double = 2400.0,
    val roomVolumeCuM: Double = 67.96,

    // Thermal Loads
    val coolingLoadBtuHr: Double = 12600.0,
    val coolingTons: Double = 1.05,
    val coolingKw: Double = 3.69,
    
    val heatingLoadBtuHr: Double = 9500.0,
    val heatingKw: Double = 2.78,
    
    val requiredAirflowCfm: Int = 420,
    val requiredAirflowM3h: Int = 714,
    
    // Equipment Recommendations
    val recommendedMiniSplitBtu: String = "12,000 BTU (1.0 Ton)",
    val recommendedMiniSplitKw: String = "3.5 kW",
    val recommendedBaseboardLengthFt: Double = 19.0,
    val recommendedBaseboardLengthM: Double = 5.8
)

class HvacLoadViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HvacLoadUiState())
    val uiState: StateFlow<HvacLoadUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(
                isMetric = metric,
                roomLength = if (metric) 6.0 else 20.0, // 6m vs 20ft
                roomWidth = if (metric) 4.5 else 15.0,  // 4.5m vs 15ft
                ceilingHeight = if (metric) 2.5 else 8.0 // 2.5m vs 8ft
            )
            recalculate()
        }
    }

    fun setClimateZone(zone: HvacClimateZone) {
        _uiState.value = _uiState.value.copy(climateZone = zone)
        recalculate()
    }

    fun setInsulation(ins: InsulationQuality) {
        _uiState.value = _uiState.value.copy(insulation = ins)
        recalculate()
    }

    fun setSunExposure(exp: SunExposure) {
        _uiState.value = _uiState.value.copy(sunExposure = exp)
        recalculate()
    }

    fun updateDimensions(
        length: Double? = null,
        width: Double? = null,
        height: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            roomLength = length ?: _uiState.value.roomLength,
            roomWidth = width ?: _uiState.value.roomWidth,
            ceilingHeight = height ?: _uiState.value.ceilingHeight
        )
        recalculate()
    }

    fun updateLoads(
        windows: Int? = null,
        occupants: Int? = null,
        kitchen: Boolean? = null,
        electronics: Boolean? = null
    ) {
        _uiState.value = _uiState.value.copy(
            windowCount = (windows ?: _uiState.value.windowCount).coerceAtLeast(0),
            occupantCount = (occupants ?: _uiState.value.occupantCount).coerceAtLeast(1),
            isKitchenOrCooking = kitchen ?: _uiState.value.isKitchenOrCooking,
            hasHeavyElectronics = electronics ?: _uiState.value.hasHeavyElectronics
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val isM = s.isMetric

        val areaSqFt = if (isM) (s.roomLength * s.roomWidth) * 10.7639 else (s.roomLength * s.roomWidth).coerceAtLeast(10.0)
        val areaSqM = areaSqFt / 10.7639

        val heightFt = if (isM) s.ceilingHeight * 3.28084 else s.ceilingHeight.coerceAtLeast(6.0)
        val heightM = heightFt * 0.3048

        val volumeCuFt = areaSqFt * heightFt
        val volumeCuM = volumeCuFt / 35.3147

        // Ceiling height adjustment factor (8 ft baseline)
        val ceilingFactor = (heightFt / 8.0).coerceAtLeast(0.85)

        // 1. Base Cooling Calculation
        val baseCooling = areaSqFt * s.climateZone.coolingBaseBtuPerSqFt * s.insulation.multiplier * ceilingFactor * s.sunExposure.coolingFactor
        val windowCoolingLoad = s.windowCount * 650.0 // approx 650 BTU/hr per standard window solar gain
        val occupantCoolingLoad = max(0, s.occupantCount - 2) * 600.0 // +600 BTU/hr per extra occupant
        val kitchenLoad = if (s.isKitchenOrCooking) 4000.0 else 0.0
        val electronicsLoad = if (s.hasHeavyElectronics) 1800.0 else 0.0

        val totalCoolingBtu = baseCooling + windowCoolingLoad + occupantCoolingLoad + kitchenLoad + electronicsLoad
        val coolingTons = totalCoolingBtu / 12000.0
        val coolingKw = totalCoolingBtu / 3412.14

        // 2. Base Heating Calculation
        val baseHeating = areaSqFt * s.climateZone.heatingBaseBtuPerSqFt * s.insulation.multiplier * ceilingFactor
        val windowHeatingLoss = s.windowCount * 450.0
        val totalHeatingBtu = baseHeating + windowHeatingLoss
        val heatingKw = totalHeatingBtu / 3412.14

        // Airflow (Nominal 400 CFM per Ton of cooling)
        val airflowCfm = (coolingTons * 400.0).roundToInt()
        val airflowM3h = (airflowCfm * 1.699).roundToInt()

        // Equipment recommendation (Mini-Split Standard Classes: 9k, 12k, 18k, 24k, 30k, 36k, 48k)
        val miniSplitSize = when {
            totalCoolingBtu <= 9500 -> "9,000 BTU (0.75 Ton)" to "2.6 kW"
            totalCoolingBtu <= 13500 -> "12,000 BTU (1.0 Ton)" to "3.5 kW"
            totalCoolingBtu <= 19500 -> "18,000 BTU (1.5 Ton)" to "5.2 kW"
            totalCoolingBtu <= 26000 -> "24,000 BTU (2.0 Ton)" to "7.0 kW"
            totalCoolingBtu <= 32000 -> "30,000 BTU (2.5 Ton)" to "8.8 kW"
            totalCoolingBtu <= 38000 -> "36,000 BTU (3.0 Ton)" to "10.5 kW"
            else -> "${(ceil(totalCoolingBtu / 6000.0) * 6000).toInt()} BTU (${String.format("%.1f", totalCoolingBtu / 12000.0)} Tons)" to "${String.format("%.1f", coolingKw)} kW"
        }

        // Baseboard hydronic / electric heating length (~500 BTU/ft or ~500 W/m)
        val baseboardFt = totalHeatingBtu / 500.0
        val baseboardM = (heatingKw * 1000.0) / 500.0

        _uiState.value = s.copy(
            floorAreaSqFt = areaSqFt,
            floorAreaSqM = areaSqM,
            roomVolumeCuFt = volumeCuFt,
            roomVolumeCuM = volumeCuM,
            coolingLoadBtuHr = totalCoolingBtu,
            coolingTons = coolingTons,
            coolingKw = coolingKw,
            heatingLoadBtuHr = totalHeatingBtu,
            heatingKw = heatingKw,
            requiredAirflowCfm = airflowCfm,
            requiredAirflowM3h = airflowM3h,
            recommendedMiniSplitBtu = miniSplitSize.first,
            recommendedMiniSplitKw = miniSplitSize.second,
            recommendedBaseboardLengthFt = baseboardFt,
            recommendedBaseboardLengthM = baseboardM
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val summary = "HVAC Load (${String.format("%.1f", if (s.isMetric) s.floorAreaSqM else s.floorAreaSqFt)} ${if (s.isMetric) "m²" else "sq.ft"}): Cooling ${s.recommendedMiniSplitBtu} / ${String.format("%.1f", s.coolingKw)} kW, Heating ${String.format("%.0f", s.heatingLoadBtuHr)} BTU/hr"

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "HVAC",
                title = "HVAC BTU & Room Load Sizer",
                summary = summary,
                value = s.coolingLoadBtuHr
            )
        }
    }
}
