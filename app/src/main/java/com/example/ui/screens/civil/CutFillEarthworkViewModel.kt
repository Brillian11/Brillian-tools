package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.ceil

enum class EarthworkMode(val label: String) {
    TRENCH("Trench & Pipe Backfill"),
    AVERAGE_END_AREA("Grading Cut & Fill (End Areas)"),
    PIT_BASEMENT("Foundation Pit / Basement"),
    EMBANKMENT_BERM("Embankment / Soil Berm")
}

enum class SoilType(val swellPct: Double, val shrinkPct: Double, val densityLbsCuYd: Double, val label: String) {
    COMMON_EARTH(25.0, 15.0, 2600.0, "Common Earth (Loam / Silt)"),
    DENSE_CLAY(30.0, 20.0, 2900.0, "Heavy Clay"),
    SAND_GRAVEL(15.0, 10.0, 2800.0, "Sand & Clean Gravel"),
    CRUSHED_ROCK(50.0, 0.0, 3100.0, "Blasted / Crushed Rock"),
    TOPSOIL(20.0, 15.0, 2200.0, "Topsoil / Organic Overburden")
}

data class CutFillUiState(
    val isMetric: Boolean = false,
    val mode: EarthworkMode = EarthworkMode.TRENCH,
    val soilType: SoilType = SoilType.COMMON_EARTH,

    // Trench inputs (ft or m)
    val trenchLength: Double = 100.0,
    val trenchTopWidth: Double = 3.0,
    val trenchBottomWidth: Double = 2.0,
    val trenchDepth: Double = 4.0,
    val pipeOutsideDiameterInOrCm: Double = 8.0, // 8 in pipe displacement
    val gravelBeddingDepthInOrCm: Double = 6.0, // 6 in bedding

    // End Area inputs
    val station1CutArea: Double = 50.0, // sq ft or sq m
    val station1FillArea: Double = 10.0,
    val station2CutArea: Double = 70.0,
    val station2FillArea: Double = 15.0,
    val stationDistance: Double = 100.0, // ft or m

    // Pit / Basement inputs
    val pitLength: Double = 40.0,
    val pitWidth: Double = 30.0,
    val pitDepth: Double = 8.0,
    val pitClearanceMargin: Double = 2.0, // 2 ft over-dig workspace
    val pitSideSlopeRatio: Double = 0.5, // 0.5:1 (H:V)

    // Embankment inputs
    val bermLength: Double = 60.0,
    val bermCrestWidth: Double = 6.0,
    val bermHeight: Double = 4.0,
    val bermSideSlopeRatio: Double = 2.0, // 2:1 (H:V)

    val dumpTruckCapacityCuYdOrM3: Double = 12.0, // 12 yd3 or 9 m3 truck

    // Calculated Outputs
    val bankVolumeCuYards: Double = 37.04,
    val bankVolumeCuMeters: Double = 28.32,
    val looseVolumeCuYards: Double = 46.30, // Bank * (1 + swell)
    val looseVolumeCuMeters: Double = 35.40,
    val compactedVolumeCuYards: Double = 31.48, // Bank * (1 - shrink)
    val compactedVolumeCuMeters: Double = 24.07,
    val dumpTruckLoads: Int = 4,
    val gravelBackfillTons: Double = 4.8,
    val soilWeightTons: Double = 48.15
)

class CutFillEarthworkViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CutFillUiState())
    val uiState: StateFlow<CutFillUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setUnitSystem(metric: Boolean) {
        if (_uiState.value.isMetric != metric) {
            _uiState.value = _uiState.value.copy(
                isMetric = metric,
                trenchLength = if (metric) 30.0 else 100.0,
                trenchTopWidth = if (metric) 1.0 else 3.0,
                trenchBottomWidth = if (metric) 0.6 else 2.0,
                trenchDepth = if (metric) 1.2 else 4.0,
                pipeOutsideDiameterInOrCm = if (metric) 20.0 else 8.0,
                gravelBeddingDepthInOrCm = if (metric) 15.0 else 6.0,
                station1CutArea = if (metric) 5.0 else 50.0,
                station1FillArea = if (metric) 1.0 else 10.0,
                station2CutArea = if (metric) 7.0 else 70.0,
                station2FillArea = if (metric) 1.5 else 15.0,
                stationDistance = if (metric) 30.0 else 100.0,
                pitLength = if (metric) 12.0 else 40.0,
                pitWidth = if (metric) 10.0 else 30.0,
                pitDepth = if (metric) 2.5 else 8.0,
                pitClearanceMargin = if (metric) 0.6 else 2.0,
                bermLength = if (metric) 20.0 else 60.0,
                bermCrestWidth = if (metric) 2.0 else 6.0,
                bermHeight = if (metric) 1.5 else 4.0,
                dumpTruckCapacityCuYdOrM3 = if (metric) 9.0 else 12.0
            )
            recalculate()
        }
    }

    fun setMode(m: EarthworkMode) {
        _uiState.value = _uiState.value.copy(mode = m)
        recalculate()
    }

    fun setSoilType(s: SoilType) {
        _uiState.value = _uiState.value.copy(soilType = s)
        recalculate()
    }

    fun updateInputs(
        tLength: Double? = null,
        tTopW: Double? = null,
        tBotW: Double? = null,
        tDepth: Double? = null,
        pipeDia: Double? = null,
        gravelBed: Double? = null,
        s1Cut: Double? = null,
        s1Fill: Double? = null,
        s2Cut: Double? = null,
        s2Fill: Double? = null,
        sDist: Double? = null,
        pLen: Double? = null,
        pWid: Double? = null,
        pDep: Double? = null,
        pClear: Double? = null,
        bLen: Double? = null,
        bCrest: Double? = null,
        bHeight: Double? = null,
        truckCap: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            trenchLength = tLength ?: _uiState.value.trenchLength,
            trenchTopWidth = tTopW ?: _uiState.value.trenchTopWidth,
            trenchBottomWidth = tBotW ?: _uiState.value.trenchBottomWidth,
            trenchDepth = tDepth ?: _uiState.value.trenchDepth,
            pipeOutsideDiameterInOrCm = pipeDia ?: _uiState.value.pipeOutsideDiameterInOrCm,
            gravelBeddingDepthInOrCm = gravelBed ?: _uiState.value.gravelBeddingDepthInOrCm,
            station1CutArea = s1Cut ?: _uiState.value.station1CutArea,
            station1FillArea = s1Fill ?: _uiState.value.station1FillArea,
            station2CutArea = s2Cut ?: _uiState.value.station2CutArea,
            station2FillArea = s2Fill ?: _uiState.value.station2FillArea,
            stationDistance = sDist ?: _uiState.value.stationDistance,
            pitLength = pLen ?: _uiState.value.pitLength,
            pitWidth = pWid ?: _uiState.value.pitWidth,
            pitDepth = pDep ?: _uiState.value.pitDepth,
            pitClearanceMargin = pClear ?: _uiState.value.pitClearanceMargin,
            bermLength = bLen ?: _uiState.value.bermLength,
            bermCrestWidth = bCrest ?: _uiState.value.bermCrestWidth,
            bermHeight = bHeight ?: _uiState.value.bermHeight,
            dumpTruckCapacityCuYdOrM3 = truckCap ?: _uiState.value.dumpTruckCapacityCuYdOrM3
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val isM = s.isMetric

        var netBankCuYd = 0.0
        var netBankCuM = 0.0
        var gravelTons = 0.0

        when (s.mode) {
            EarthworkMode.TRENCH -> {
                if (isM) {
                    val avgWidthM = (s.trenchTopWidth + s.trenchBottomWidth) / 2.0
                    val grossM3 = avgWidthM * s.trenchDepth * s.trenchLength
                    netBankCuM = grossM3
                    netBankCuYd = grossM3 * 1.30795

                    // Bedding volume: bottom width * bed depth * length
                    val bedDepthM = s.gravelBeddingDepthInOrCm / 100.0
                    val bedVolM3 = s.trenchBottomWidth * bedDepthM * s.trenchLength
                    gravelTons = bedVolM3 * 1.65 // metric tonnes
                } else {
                    val avgWidthFt = (s.trenchTopWidth + s.trenchBottomWidth) / 2.0
                    val grossCuFt = avgWidthFt * s.trenchDepth * s.trenchLength
                    netBankCuYd = grossCuFt / 27.0
                    netBankCuM = netBankCuYd / 1.30795

                    val bedDepthFt = s.gravelBeddingDepthInOrCm / 12.0
                    val bedVolCuYd = (s.trenchBottomWidth * bedDepthFt * s.trenchLength) / 27.0
                    gravelTons = bedVolCuYd * 1.35 // US tons
                }
            }

            EarthworkMode.AVERAGE_END_AREA -> {
                if (isM) {
                    val avgCutArea = (s.station1CutArea + s.station2CutArea) / 2.0
                    val avgFillArea = (s.station1FillArea + s.station2FillArea) / 2.0
                    val cutVolM3 = avgCutArea * s.stationDistance
                    val fillVolM3 = avgFillArea * s.stationDistance
                    netBankCuM = cutVolM3
                    netBankCuYd = cutVolM3 * 1.30795
                } else {
                    val avgCutArea = (s.station1CutArea + s.station2CutArea) / 2.0
                    val cutVolCuYd = (avgCutArea * s.stationDistance) / 27.0
                    netBankCuYd = cutVolCuYd
                    netBankCuM = cutVolCuYd / 1.30795
                }
            }

            EarthworkMode.PIT_BASEMENT -> {
                if (isM) {
                    val totalBotL = s.pitLength + (2 * s.pitClearanceMargin)
                    val totalBotW = s.pitWidth + (2 * s.pitClearanceMargin)
                    val totalTopL = totalBotL + (2 * s.pitSideSlopeRatio * s.pitDepth)
                    val totalTopW = totalBotW + (2 * s.pitSideSlopeRatio * s.pitDepth)
                    val botArea = totalBotL * totalBotW
                    val topArea = totalTopL * totalTopW
                    // Prismoidal formula: V = (H / 6) * (A1 + 4*Am + A2)
                    val midArea = ((totalBotL + totalTopL) / 2.0) * ((totalBotW + totalTopW) / 2.0)
                    val volM3 = (s.pitDepth / 6.0) * (botArea + 4 * midArea + topArea)
                    netBankCuM = volM3
                    netBankCuYd = volM3 * 1.30795
                } else {
                    val totalBotL = s.pitLength + (2 * s.pitClearanceMargin)
                    val totalBotW = s.pitWidth + (2 * s.pitClearanceMargin)
                    val totalTopL = totalBotL + (2 * s.pitSideSlopeRatio * s.pitDepth)
                    val totalTopW = totalBotW + (2 * s.pitSideSlopeRatio * s.pitDepth)
                    val botArea = totalBotL * totalBotW
                    val topArea = totalTopL * totalTopW
                    val midArea = ((totalBotL + totalTopL) / 2.0) * ((totalBotW + totalTopW) / 2.0)
                    val volCuFt = (s.pitDepth / 6.0) * (botArea + 4 * midArea + topArea)
                    netBankCuYd = volCuFt / 27.0
                    netBankCuM = netBankCuYd / 1.30795
                }
            }

            EarthworkMode.EMBANKMENT_BERM -> {
                if (isM) {
                    val baseWidth = s.bermCrestWidth + (2 * s.bermSideSlopeRatio * s.bermHeight)
                    val xSection = ((s.bermCrestWidth + baseWidth) / 2.0) * s.bermHeight
                    val volM3 = xSection * s.bermLength
                    netBankCuM = volM3
                    netBankCuYd = volM3 * 1.30795
                } else {
                    val baseWidth = s.bermCrestWidth + (2 * s.bermSideSlopeRatio * s.bermHeight)
                    val xSection = ((s.bermCrestWidth + baseWidth) / 2.0) * s.bermHeight
                    val volCuFt = xSection * s.bermLength
                    netBankCuYd = volCuFt / 27.0
                    netBankCuM = netBankCuYd / 1.30795
                }
            }
        }

        val swellFactor = 1.0 + (s.soilType.swellPct / 100.0)
        val shrinkFactor = 1.0 - (s.soilType.shrinkPct / 100.0)

        val looseYd = netBankCuYd * swellFactor
        val looseM3 = netBankCuM * swellFactor
        val compactedYd = netBankCuYd * shrinkFactor
        val compactedM3 = netBankCuM * shrinkFactor

        val targetVol = if (isM) looseM3 else looseYd
        val truckLoads = if (s.dumpTruckCapacityCuYdOrM3 > 0) ceil(targetVol / s.dumpTruckCapacityCuYdOrM3).toInt() else 0
        val totalSoilTons = netBankCuYd * (s.soilType.densityLbsCuYd / 2000.0)

        _uiState.value = s.copy(
            bankVolumeCuYards = netBankCuYd,
            bankVolumeCuMeters = netBankCuM,
            looseVolumeCuYards = looseYd,
            looseVolumeCuMeters = looseM3,
            compactedVolumeCuYards = compactedYd,
            compactedVolumeCuMeters = compactedM3,
            dumpTruckLoads = truckLoads,
            gravelBackfillTons = gravelTons,
            soilWeightTons = totalSoilTons
        )
    }

    fun logCalculation() {
        val s = _uiState.value
        val summary = "${s.mode.label} (${s.soilType.label}): Bank ${String.format("%.1f", s.bankVolumeCuYards)} yd³ (${String.format("%.1f", s.bankVolumeCuMeters)} m³) -> Loose Haul: ${String.format("%.1f", s.looseVolumeCuYards)} yd³ (${s.dumpTruckLoads} Truckloads @ ${s.dumpTruckCapacityCuYdOrM3.toInt()}${if (s.isMetric) "m³" else "yd³"})"
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "CIVIL",
                title = "Cut & Fill Earthwork Volume",
                summary = summary,
                value = s.bankVolumeCuYards
            )
        }
    }
}
