package com.example.ui.screens.electrical

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.floor

enum class BatteryChemistry(
    val label: String,
    val dod: Double, // Depth of Discharge
    val efficiency: Double,
    val nominalCellVoltage: Double,
    val cycleLife: Int
) {
    LIFEPO4("LiFePO4 (Lithium Iron Phosphate)", 0.85, 0.95, 3.2, 6000),
    NMC("NMC Lithium-Ion", 0.80, 0.93, 3.7, 3000),
    AGM_SEALED("AGM Sealed Lead-Acid", 0.50, 0.85, 2.0, 800),
    GEL_LEAD("Gel Cell Lead-Acid", 0.50, 0.85, 2.0, 1000),
    FLOODED_LEAD("Flooded Deep-Cycle Lead-Acid", 0.50, 0.80, 2.0, 1200)
}

data class SolarBatteryUiState(
    // Energy Demand & Site
    val dailyKwh: Double = 12.0, // kWh / day
    val peakSunHours: Double = 4.5, // hrs/day
    val daysOfAutonomy: Double = 2.0, // days without sun
    val systemDcVoltage: Int = 48, // 12V, 24V, 48V
    val systemLossFactor: Double = 0.80, // Inverter, wiring, soiling (80% efficiency)
    val batteryChemistry: BatteryChemistry = BatteryChemistry.LIFEPO4,

    // PV Panel Specs
    val panelWatts: Int = 450, // W per panel
    val panelVoc: Double = 49.8, // V
    val panelVmp: Double = 41.5, // V
    val panelImp: Double = 10.85, // A
    val minWinterTempC: Int = -10, // Coldest design temp
    val vocTempCoeffPctPerC: Double = -0.28, // % / °C

    // Charge Controller Specs
    val mpptMaxVoc: Double = 250.0, // V (e.g. 150V, 250V, 450V, 600V)
    val mpptMaxAmps: Int = 80, // A

    // Sizing Calculation Results
    val requiredPvWattage: Double = 0.0,
    val totalPanelsCount: Int = 0,
    val panelsInSeriesPerString: Int = 0,
    val parallelStringsCount: Int = 0,
    val actualInstalledPvWatts: Int = 0,
    val maxStringVocCold: Double = 0.0,
    val stringVmpOperating: Double = 0.0,
    val totalArrayIscAmps: Double = 0.0,
    val mpptControllersCount: Int = 1,
    val requiredRoofAreaSqFt: Double = 0.0,
    val requiredRoofAreaM2: Double = 0.0,

    // Battery Bank Results
    val usableBatteryKwh: Double = 0.0,
    val totalBatteryKwh: Double = 0.0,
    val batteryAmpHoursAtDcVoltage: Double = 0.0,
    val standardServerRackBattCount: Int = 0, // e.g. 48V 100Ah (5.12 kWh) units
    val recommendedInverterContinuousKw: Double = 0.0,
    val calculationSummary: String = ""
)

class SolarBatterySizerViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SolarBatteryUiState())
    val uiState: StateFlow<SolarBatteryUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setDailyKwh(kwh: Double) {
        _uiState.value = _uiState.value.copy(dailyKwh = kwh.coerceAtLeast(0.5))
        recalculate()
    }

    fun setPeakSunHours(psh: Double) {
        _uiState.value = _uiState.value.copy(peakSunHours = psh.coerceIn(1.0, 10.0))
        recalculate()
    }

    fun setDaysOfAutonomy(days: Double) {
        _uiState.value = _uiState.value.copy(daysOfAutonomy = days.coerceIn(0.5, 10.0))
        recalculate()
    }

    fun setSystemVoltage(v: Int) {
        _uiState.value = _uiState.value.copy(systemDcVoltage = v)
        recalculate()
    }

    fun setBatteryChemistry(chem: BatteryChemistry) {
        _uiState.value = _uiState.value.copy(batteryChemistry = chem)
        recalculate()
    }

    fun setPanelSpecs(watts: Int, voc: Double, vmp: Double, imp: Double) {
        _uiState.value = _uiState.value.copy(
            panelWatts = watts,
            panelVoc = voc,
            panelVmp = vmp,
            panelImp = imp
        )
        recalculate()
    }

    fun setMpptMaxVoc(voc: Double) {
        _uiState.value = _uiState.value.copy(mpptMaxVoc = voc)
        recalculate()
    }

    fun setWinterTemp(tempC: Int) {
        _uiState.value = _uiState.value.copy(minWinterTempC = tempC)
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value

        // 1. PV Array Required Peak Wattage
        // Required Wp = (Daily kWh * 1000) / (Peak Sun Hours * System Loss Factor)
        val reqPvWatts = (s.dailyKwh * 1000.0) / (s.peakSunHours * s.systemLossFactor)

        // Total panels needed based on panel wattage
        val minPanels = ceil(reqPvWatts / s.panelWatts).toInt().coerceAtLeast(1)

        // Temperature-compensated Voc at coldest winter temp (NEC 690.7)
        // Delta T = WinterTemp - 25°C
        val deltaT = s.minWinterTempC - 25.0
        val tempCorrectionFactor = 1.0 + (s.vocTempCoeffPctPerC / 100.0) * deltaT
        val vocColdSingle = s.panelVoc * tempCorrectionFactor

        // Max panels in series per string so string Voc_cold <= MPPT Max Voc
        val maxSeries = floor(s.mpptMaxVoc / vocColdSingle).toInt().coerceAtLeast(1)

        // Parallel strings required
        val strings = ceil(minPanels.toDouble() / maxSeries).toInt().coerceAtLeast(1)
        val totalPanels = strings * maxSeries
        val actualInstalledWatts = totalPanels * s.panelWatts

        val stringVocCold = maxSeries * vocColdSingle
        val stringVmp = maxSeries * s.panelVmp
        val totalArrayImp = strings * s.panelImp

        // MPPT Charge Controller sizing
        // Max charge current into battery = (Actual Array Watts * 0.98) / System Battery Voltage
        val maxChargeCurrentToBattery = (actualInstalledWatts * 0.98) / s.systemDcVoltage
        val mpptControllers = ceil(maxChargeCurrentToBattery / s.mpptMaxAmps).toInt().coerceAtLeast(1)

        // Estimated roof area: Standard 400-550W panel is ~2.0 m² (~21.5 sq ft)
        val areaM2 = totalPanels * 2.1
        val areaSqFt = areaM2 * 10.7639

        // 2. Battery Bank Sizing
        // Usable energy = Daily kWh * Days of Autonomy
        val usableKwh = s.dailyKwh * s.daysOfAutonomy
        // Total capacity = Usable / (DoD * efficiency)
        val totalKwh = usableKwh / (s.batteryChemistry.dod * s.batteryChemistry.efficiency)
        // Battery Amp-Hours = (Total kWh * 1000) / System DC Voltage
        val battAh = (totalKwh * 1000.0) / s.systemDcVoltage

        // Standard 5.12 kWh (48V 100Ah) or 2.56 kWh (24V 100Ah) Server Rack Batteries
        val moduleKwh = (s.systemDcVoltage * 100.0) / 1000.0
        val moduleCount = ceil(totalKwh / moduleKwh).toInt().coerceAtLeast(1)

        // Recommended Continuous Inverter Power: Peak Daily usage heuristic / continuous max load (typically 1.5 - 2x average)
        val avgHourlyLoadKw = s.dailyKwh / 24.0
        val recInverterKw = (avgHourlyLoadKw * 3.5).coerceIn(3.0, 20.0)

        val summary = "Solar PV: ${totalPanels}x ${s.panelWatts}W (${String.format("%.2f", actualInstalledWatts / 1000.0)} kWp) arranged in ${strings} strings of ${maxSeries} series.\n" +
                "Battery Bank: ${String.format("%.1f", totalKwh)} kWh (${String.format("%.0f", battAh)} Ah @ ${s.systemDcVoltage}V) [${moduleCount}x 100Ah Modules, ${s.batteryChemistry.label}].\n" +
                "Daily Output: ${s.dailyKwh} kWh/day @ ${s.peakSunHours} PSH (Autonomy: ${s.daysOfAutonomy} days)."

        _uiState.value = s.copy(
            requiredPvWattage = reqPvWatts,
            totalPanelsCount = totalPanels,
            panelsInSeriesPerString = maxSeries,
            parallelStringsCount = strings,
            actualInstalledPvWatts = actualInstalledWatts,
            maxStringVocCold = stringVocCold,
            stringVmpOperating = stringVmp,
            totalArrayIscAmps = totalArrayImp,
            mpptControllersCount = mpptControllers,
            requiredRoofAreaSqFt = areaSqFt,
            requiredRoofAreaM2 = areaM2,
            usableBatteryKwh = usableKwh,
            totalBatteryKwh = totalKwh,
            batteryAmpHoursAtDcVoltage = battAh,
            standardServerRackBattCount = moduleCount,
            recommendedInverterContinuousKw = recInverterKw,
            calculationSummary = summary
        )
    }

    fun saveToLogs() {
        viewModelScope.launch {
            val s = _uiState.value
            toolLogRepository.logToolActivity(
                toolType = "widget_solar_battery_sizer",
                title = "Solar PV & Battery Bank Sizer",
                summary = "${s.dailyKwh} kWh/day @ ${s.systemDcVoltage}V DC -> PV: ${s.actualInstalledPvWatts}Wp (${s.totalPanelsCount} panels) | Battery: ${String.format("%.1f", s.totalBatteryKwh)} kWh (${String.format("%.0f", s.batteryAmpHoursAtDcVoltage)}Ah)",
                value = s.actualInstalledPvWatts.toDouble()
            )
        }
    }
}
