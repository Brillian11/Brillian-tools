package com.example.ui.screens.civil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

enum class HaulerRigType(
    val label: String,
    val numAxles: Int,
    val tareSteerLbs: Double,
    val tareDriveLbs: Double,
    val tareTrailerLbs: Double,
    val deckLengthFt: Double,
    val kingpinToTrailerAxleDistFt: Double,
    val tractorWheelbaseFt: Double
) {
    COMBO_5_AXLE("3-Axle Tractor + 2-Axle Lowboy (5-Axle)", 5, 11500.0, 16500.0, 14000.0, 24.0, 38.0, 18.5),
    COMBO_6_AXLE_RGN("3-Axle Tractor + 3-Axle RGN Lowboy (6-Axle)", 6, 12000.0, 17500.0, 18500.0, 26.0, 42.0, 19.5),
    SUPERLOAD_8_AXLE("4-Axle Heavy Hauler + 3-Axle + Booster (8-Axle)", 8, 14000.0, 26000.0, 24000.0, 29.0, 48.0, 22.0),
    TAG_TRAILER_5_AXLE("Dump Truck + 3-Axle Tag Trailer", 5, 12000.0, 20000.0, 11000.0, 22.0, 30.0, 16.0)
}

enum class HeavyEquipmentPreset(
    val label: String,
    val weightLbs: Double,
    val machineLengthFt: Double,
    val machineWidthFt: Double,
    val machineHeightFt: Double,
    val defaultCgOffsetFt: Double
) {
    CAT_320_EXCAVATOR("CAT 320 Medium Excavator (20-Ton)", 49600.0, 31.3, 10.4, 10.3, 11.5),
    CAT_336_EXCAVATOR("CAT 336 Heavy Excavator (36-Ton)", 82000.0, 36.8, 11.2, 11.8, 13.0),
    CAT_D6_DOZER("CAT D6 Bulldozer with Blade", 48500.0, 19.5, 11.0, 10.5, 9.2),
    CAT_950_LOADER("CAT 950 Wheel Loader", 42500.0, 26.5, 9.5, 11.3, 12.0),
    JD_310_BACKHOE("John Deere 310 Backhoe Loader", 15800.0, 23.5, 7.8, 9.2, 10.5),
    CAT_140_GRADER("CAT 140 Motor Grader", 42000.0, 32.5, 8.5, 10.8, 14.5),
    CUSTOM("Custom Machinery / Load", 50000.0, 24.0, 8.5, 10.0, 12.0)
}

data class EquipmentHaulingUiState(
    val isMetric: Boolean = false,
    val rigType: HaulerRigType = HaulerRigType.COMBO_5_AXLE,
    val equipmentPreset: HeavyEquipmentPreset = HeavyEquipmentPreset.CAT_320_EXCAVATOR,

    // Payload Machine Inputs
    val payloadWeight: Double = 49600.0, // lbs or kg
    val machineLength: Double = 31.3, // ft or m
    val machineWidth: Double = 10.4, // ft or m
    val machineHeight: Double = 10.3, // ft or m
    val trailerDeckHeight: Double = 2.0, // ft (RGN lowboy well deck height)
    val loadCgPlacementOnDeck: Double = 12.0, // ft from front of deck (Kingpin side)

    // Fifth Wheel Offset
    val fifthWheelAheadOfDriveCenterInches: Double = 2.0, // inches ahead of tandem center

    // Computed Axle Group Loads
    val steerAxleLoad: Double = 0.0, // lbs or kg
    val driveAxleGroupLoad: Double = 0.0,
    val trailerAxleGroupLoad: Double = 0.0,
    val kingpinLoad: Double = 0.0,
    val grossVehicleWeight: Double = 0.0,

    // Federal Bridge & DOT Legal Limits
    val steerLegalLimit: Double = 12000.0,
    val driveLegalLimit: Double = 34000.0,
    val trailerLegalLimit: Double = 34000.0,
    val gvwLegalLimit: Double = 80000.0,
    val federalBridgeFormulaLimit: Double = 0.0,

    // Overload & Compliance Flags
    val isSteerOverloaded: Boolean = false,
    val isDriveOverloaded: Boolean = false,
    val isTrailerOverloaded: Boolean = false,
    val isGvwOverloaded: Boolean = false,
    val isBridgeFormulaExceeded: Boolean = false,

    // Oversize / Overweight Permit & Escort Rules
    val totalTravelHeight: Double = 0.0,
    val requiresOverweightPermit: Boolean = false,
    val requiresOversizeWidthPermit: Boolean = false,
    val requiresOversizeHeightPermit: Boolean = false,
    val requiresOversizeSignsBanners: Boolean = false,
    val requiresPilotEscortCar: Boolean = false,

    // Optimization
    val recommendedCgShiftInches: Double = 0.0 // + slide back, - slide forward
)

class EquipmentHaulingViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquipmentHaulingUiState())
    val uiState: StateFlow<EquipmentHaulingUiState> = _uiState.asStateFlow()

    init {
        recalculate()
    }

    fun setMetric(metric: Boolean) {
        _uiState.value = _uiState.value.copy(isMetric = metric)
        recalculate()
    }

    fun setRigType(rig: HaulerRigType) {
        _uiState.value = _uiState.value.copy(
            rigType = rig,
            loadCgPlacementOnDeck = (rig.deckLengthFt / 2.0)
        )
        recalculate()
    }

    fun setEquipmentPreset(preset: HeavyEquipmentPreset) {
        _uiState.value = _uiState.value.copy(
            equipmentPreset = preset,
            payloadWeight = if (_uiState.value.isMetric) (preset.weightLbs * 0.453592) else preset.weightLbs,
            machineLength = if (_uiState.value.isMetric) (preset.machineLengthFt * 0.3048) else preset.machineLengthFt,
            machineWidth = if (_uiState.value.isMetric) (preset.machineWidthFt * 0.3048) else preset.machineWidthFt,
            machineHeight = if (_uiState.value.isMetric) (preset.machineHeightFt * 0.3048) else preset.machineHeightFt,
            loadCgPlacementOnDeck = (_uiState.value.rigType.deckLengthFt / 2.0)
        )
        recalculate()
    }

    fun updateInputs(
        payloadWeight: Double? = null,
        machineLength: Double? = null,
        machineWidth: Double? = null,
        machineHeight: Double? = null,
        trailerDeckHeight: Double? = null,
        loadCgPlacementOnDeck: Double? = null,
        fifthWheelAheadOfDriveCenterInches: Double? = null
    ) {
        _uiState.value = _uiState.value.copy(
            payloadWeight = payloadWeight ?: _uiState.value.payloadWeight,
            machineLength = machineLength ?: _uiState.value.machineLength,
            machineWidth = machineWidth ?: _uiState.value.machineWidth,
            machineHeight = machineHeight ?: _uiState.value.machineHeight,
            trailerDeckHeight = trailerDeckHeight ?: _uiState.value.trailerDeckHeight,
            loadCgPlacementOnDeck = loadCgPlacementOnDeck ?: _uiState.value.loadCgPlacementOnDeck,
            fifthWheelAheadOfDriveCenterInches = fifthWheelAheadOfDriveCenterInches ?: _uiState.value.fifthWheelAheadOfDriveCenterInches
        )
        recalculate()
    }

    private fun recalculate() {
        val s = _uiState.value
        val rig = s.rigType

        val weightLbs = if (s.isMetric) (s.payloadWeight / 0.453592) else s.payloadWeight
        val machineLenFt = if (s.isMetric) (s.machineLength / 0.3048) else s.machineLength
        val machineWidthFt = if (s.isMetric) (s.machineWidth / 0.3048) else s.machineWidth
        val machineHeightFt = if (s.isMetric) (s.machineHeight / 0.3048) else s.machineHeight
        val deckHeightFt = if (s.isMetric) (s.trailerDeckHeight / 0.3048) else s.trailerDeckHeight
        val cgPlacementFt = if (s.isMetric) (s.loadCgPlacementOnDeck / 0.3048) else s.loadCgPlacementOnDeck

        // 1. Trailer Statics (Kingpin vs Trailer Bogie)
        // Distance from Kingpin to Trailer Axle Group Center = rig.kingpinToTrailerAxleDistFt
        val kingpinDist = rig.kingpinToTrailerAxleDistFt
        // Payload CG distance from Kingpin: Assume front of deck is ~6 ft behind kingpin
        val frontOfDeckToKingpin = 5.5
        val loadCgFromKingpin = frontOfDeckToKingpin + cgPlacementFt
        val distRatio = (loadCgFromKingpin / kingpinDist).coerceIn(0.0, 1.0)

        val payloadOnTrailerBogies = weightLbs * distRatio
        val payloadOnKingpin = weightLbs * (1.0 - distRatio)

        val totalTrailerBogiesLbs = rig.tareTrailerLbs + payloadOnTrailerBogies
        val totalKingpinLbs = payloadOnKingpin

        // 2. Tractor Statics (Steer Axle vs Drive Tandem)
        // Fifth wheel offset ahead of drive axle group center (in ft)
        val fifthOffsetFt = (s.fifthWheelAheadOfDriveCenterInches / 12.0)
        val tractorWb = rig.tractorWheelbaseFt

        // Additional load onto Steer Axle from Kingpin: (Kingpin * fifthOffset / tractorWb)
        val steerAddedFromKp = (totalKingpinLbs * fifthOffsetFt) / tractorWb
        val driveAddedFromKp = totalKingpinLbs - steerAddedFromKp

        val totalSteerLbs = rig.tareSteerLbs + steerAddedFromKp
        val totalDriveLbs = rig.tareDriveLbs + driveAddedFromKp

        val totalGvwLbs = totalSteerLbs + totalDriveLbs + totalTrailerBogiesLbs

        // 3. Legal Limits (Federal & Bridge Formula B)
        val steerLimit = 12000.0
        val driveLimit = if (rig.numAxles >= 8) 46000.0 else 34000.0
        val trailerLimit = when {
            rig.numAxles >= 8 -> 55000.0 // Tridem + Booster
            rig == HaulerRigType.COMBO_6_AXLE_RGN -> 42000.0 // Tridem group
            else -> 34000.0 // Tandem group
        }
        val gvwLimit = when {
            rig.numAxles >= 8 -> 115000.0
            rig == HaulerRigType.COMBO_6_AXLE_RGN -> 92000.0
            else -> 80000.0
        }

        // Federal Bridge Formula B: W = 500 * ( (L * N) / (N - 1) + 12N + 36 )
        val outerWheelbaseL = tractorWb + kingpinDist
        val numAxlesN = rig.numAxles.toDouble()
        val bridgeFormulaCap = 500.0 * (((outerWheelbaseL * numAxlesN) / (numAxlesN - 1.0)) + 12.0 * numAxlesN + 36.0)

        // 4. Overload & Oversize Rules
        val totalHeightFt = deckHeightFt + machineHeightFt
        val totalTravelHeight = if (s.isMetric) (totalHeightFt * 0.3048) else totalHeightFt

        val isSteerOver = totalSteerLbs > steerLimit
        val isDriveOver = totalDriveLbs > driveLimit
        val isTrailerOver = totalTrailerBogiesLbs > trailerLimit
        val isGvwOver = totalGvwLbs > gvwLimit
        val isBridgeOver = totalGvwLbs > bridgeFormulaCap

        val reqOwPermit = totalGvwLbs > 80000.0 || isDriveOver || isTrailerOver
        val reqOsWidth = machineWidthFt > 8.5 // 102 inches standard legal width
        val reqOsHeight = totalHeightFt > 13.5 // 13'6" standard legal height
        val reqSigns = reqOsWidth || reqOsHeight || reqOwPermit
        val reqEscort = machineWidthFt > 12.0 || totalHeightFt > 14.5

        // Optimal CG Shift Recommendation
        // Balance Drive vs Trailer loading ratio
        val targetDriveFraction = driveLimit / (driveLimit + trailerLimit)
        val currentDriveFraction = if (totalDriveLbs + totalTrailerBogiesLbs > 0) totalDriveLbs / (totalDriveLbs + totalTrailerBogiesLbs) else 0.5
        val diffFraction = currentDriveFraction - targetDriveFraction
        val recommendedShiftInches = (diffFraction * kingpinDist * 12.0).coerceIn(-48.0, 48.0)

        val finalSteer = if (s.isMetric) totalSteerLbs * 0.453592 else totalSteerLbs
        val finalDrive = if (s.isMetric) totalDriveLbs * 0.453592 else totalDriveLbs
        val finalTrailer = if (s.isMetric) totalTrailerBogiesLbs * 0.453592 else totalTrailerBogiesLbs
        val finalKingpin = if (s.isMetric) totalKingpinLbs * 0.453592 else totalKingpinLbs
        val finalGvw = if (s.isMetric) totalGvwLbs * 0.453592 else totalGvwLbs

        val finalSteerLim = if (s.isMetric) steerLimit * 0.453592 else steerLimit
        val finalDriveLim = if (s.isMetric) driveLimit * 0.453592 else driveLimit
        val finalTrailerLim = if (s.isMetric) trailerLimit * 0.453592 else trailerLimit
        val finalGvwLim = if (s.isMetric) gvwLimit * 0.453592 else gvwLimit

        _uiState.value = s.copy(
            steerAxleLoad = finalSteer,
            driveAxleGroupLoad = finalDrive,
            trailerAxleGroupLoad = finalTrailer,
            kingpinLoad = finalKingpin,
            grossVehicleWeight = finalGvw,
            steerLegalLimit = finalSteerLim,
            driveLegalLimit = finalDriveLim,
            trailerLegalLimit = finalTrailerLim,
            gvwLegalLimit = finalGvwLim,
            federalBridgeFormulaLimit = if (s.isMetric) bridgeFormulaCap * 0.453592 else bridgeFormulaCap,
            isSteerOverloaded = isSteerOver,
            isDriveOverloaded = isDriveOver,
            isTrailerOverloaded = isTrailerOver,
            isGvwOverloaded = isGvwOver,
            isBridgeFormulaExceeded = isBridgeOver,
            totalTravelHeight = totalTravelHeight,
            requiresOverweightPermit = reqOwPermit,
            requiresOversizeWidthPermit = reqOsWidth,
            requiresOversizeHeightPermit = reqOsHeight,
            requiresOversizeSignsBanners = reqSigns,
            requiresPilotEscortCar = reqEscort,
            recommendedCgShiftInches = recommendedShiftInches
        )
    }

    fun saveToLog() {
        val s = _uiState.value
        val uWeight = if (s.isMetric) "kg" else "lbs"
        toolLogRepository?.let { repo ->
            viewModelScope.launch {
                val summary = "Hauling ${s.equipmentPreset.label} GVW: ${String.format("%.0f", s.grossVehicleWeight)} $uWeight (Steer: ${String.format("%.0f", s.steerAxleLoad)}, Drive: ${String.format("%.0f", s.driveAxleGroupLoad)}, Trailer: ${String.format("%.0f", s.trailerAxleGroupLoad)})"
                repo.logToolActivity(
                    toolType = "equipment_hauling",
                    title = "Equipment Hauling & Axle Load",
                    summary = summary,
                    value = s.grossVehicleWeight
                )
            }
        }
    }
}
