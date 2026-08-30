package com.example.ui.screens.metalworks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.entity.ToolLogEntity
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.*

class MetalworksStudioViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    // Active tool / tab selector
    private val _selectedToolId = MutableStateFlow("widget_weld_heat_input")
    val selectedToolId: StateFlow<String> = _selectedToolId.asStateFlow()

    fun selectTool(toolId: String) {
        if (toolId.isNotBlank()) {
            _selectedToolId.value = toolId
        }
    }

    // Global Metric/Imperial Toggle
    private val _isImperial = MutableStateFlow(true)
    val isImperial: StateFlow<Boolean> = _isImperial.asStateFlow()

    fun toggleUnits() {
        _isImperial.value = !_isImperial.value
    }

    // --- 1. Heat Input Calculator ---
    val heatVoltage = MutableStateFlow("24.0")
    val heatCurrent = MutableStateFlow("180.0")
    val heatTravelSpeed = MutableStateFlow("250.0") // mm/min or in/min
    val heatEfficiency = MutableStateFlow("0.8") // 0.8 GMAW/FCAW, 0.6 GTAW, 1.0 SAW

    // --- 2. Carbon Equivalent & Pre-Heat ---
    val ceCarbon = MutableStateFlow("0.18")
    val ceManganese = MutableStateFlow("1.20")
    val ceChromium = MutableStateFlow("0.15")
    val ceMolybdenum = MutableStateFlow("0.05")
    val ceVanadium = MutableStateFlow("0.02")
    val ceNickel = MutableStateFlow("0.10")
    val ceCopper = MutableStateFlow("0.05")
    val ceSilicon = MutableStateFlow("0.25")
    val ceBoron = MutableStateFlow("0.0005")

    // --- 3. Electrode & Filler Metal Selector ---
    val baseMetal = MutableStateFlow("Carbon Steel (A36/1020)")

    // --- 4. Weld Deposition Estimator ---
    val depJointType = MutableStateFlow("V-Groove")
    val depThickness = MutableStateFlow("10.0") // mm or in
    val depRootGap = MutableStateFlow("2.0")
    val depBevelAngle = MutableStateFlow("60.0") // deg
    val depWeldLength = MutableStateFlow("1000.0") // mm or in
    val depEfficiency = MutableStateFlow("85.0") // %

    // --- 5. Shielding Gas Flow & Bottle Runtime ---
    val gasPressureBar = MutableStateFlow("150.0") // Bar or PSI
    val gasCylinderVolumeL = MutableStateFlow("50.0") // Liters or cu ft
    val gasFlowRate = MutableStateFlow("12.0") // L/min or CFH

    // --- 6. Sheet Metal K-Factor & Bend Allowance ---
    val kThickness = MutableStateFlow("2.0") // mm or in
    val kRadius = MutableStateFlow("3.0")
    val kAngle = MutableStateFlow("90.0")
    val kFactor = MutableStateFlow("0.38")

    // --- 7. Bend Deduction & Setback ---
    val bdThickness = MutableStateFlow("2.0")
    val bdRadius = MutableStateFlow("3.0")
    val bdAngle = MutableStateFlow("90.0")
    val bdKFactor = MutableStateFlow("0.38")

    // --- 8. Press Brake Tonnage ---
    val pbTensile = MutableStateFlow("450.0") // MPa or PSI
    val pbThickness = MutableStateFlow("3.0") // mm or in
    val pbLength = MutableStateFlow("1000.0") // mm or in
    val pbDieOpening = MutableStateFlow("24.0") // mm or in

    // --- 9. Cone & Frustum Unfolder ---
    val coneTopDia = MutableStateFlow("100.0")
    val coneBottomDia = MutableStateFlow("250.0")
    val coneHeight = MutableStateFlow("200.0")

    // --- 10. Square-to-Round Transition ---
    val sqBaseLen = MutableStateFlow("200.0")
    val sqBaseWidth = MutableStateFlow("200.0")
    val sqTopDia = MutableStateFlow("150.0")
    val sqHeight = MutableStateFlow("250.0")

    // --- 11. Pipe Miter & Saddle Cut ---
    val miterMainOd = MutableStateFlow("114.3") // 4" pipe
    val miterBranchOd = MutableStateFlow("114.3")
    val miterAngle = MutableStateFlow("90.0")

    // --- 12. Rolling Offset Calculator ---
    val rollRise = MutableStateFlow("300.0")
    val rollRoll = MutableStateFlow("400.0")
    val rollRun = MutableStateFlow("600.0")

    // --- 13. Flange PCD Bolt Circle ---
    val pcdDia = MutableStateFlow("180.0")
    val pcdHoles = MutableStateFlow("8")

    // --- 14. Orange Peel Cap Layout ---
    val peelPipeOd = MutableStateFlow("168.3") // 6" pipe
    val peelPetals = MutableStateFlow("6")

    // --- 15. Thermal Distortion Compensator ---
    val distType = MutableStateFlow("Single-V Butt")
    val distThickness = MutableStateFlow("12.0")
    val distPasses = MutableStateFlow("4")

    // --- 16. Structural Steel Profiles Lookup ---
    val selectedProfileGroup = MutableStateFlow("Universal Beams (UB / W-Shape)")
    val selectedProfile = MutableStateFlow("W8x31 / UB 203x133x30")

    // --- 17. Plasma & Oxy-Fuel Cutting Chart ---
    val cutMaterial = MutableStateFlow("Mild Steel")
    val cutThickness = MutableStateFlow("6.0") // mm or in

    // --- 18. Flame Straightening Guide ---
    val heatPatternType = MutableStateFlow("Triangular Heat (Beam Flange)")

    // --- 19. Fillet Weld Throat Sizer ---
    val filletLeg = MutableStateFlow("8.0") // mm or in

    // --- 20. Weld Defects Guide ---
    val defectSearchQuery = MutableStateFlow("")

    // --- 21. Welding Symbol Blueprint Decoder ---
    val symbolType = MutableStateFlow("Fillet Weld")
    val symbolSide = MutableStateFlow("Arrow Side")
    val symbolSize = MutableStateFlow("6")
    val symbolLength = MutableStateFlow("50")
    val symbolPitch = MutableStateFlow("150")

    // --- 22. Schaeffler Diagram Tool ---
    val schaefflerCr = MutableStateFlow("19.5")
    val schaefflerNi = MutableStateFlow("10.2")
    val schaefflerMo = MutableStateFlow("0.5")
    val schaefflerSi = MutableStateFlow("0.6")
    val schaefflerMn = MutableStateFlow("1.5")
    val schaefflerC = MutableStateFlow("0.05")
    val schaefflerN = MutableStateFlow("0.03")

    // --- 23. Surface Plate Flatness Map ---
    val flatP1 = MutableStateFlow("0.2")
    val flatP2 = MutableStateFlow("-0.1")
    val flatP3 = MutableStateFlow("0.4")
    val flatP4 = MutableStateFlow("0.0")

    // --- 24. Tungsten Electrode Guide ---
    val tungstenAlloy = MutableStateFlow("2% Thoriated (Red - EWTh-2)")

    // --- 25. Hydrostatic Test & Hoop Stress ---
    val hydroPipeOd = MutableStateFlow("219.1") // 8" pipe
    val hydroWallThick = MutableStateFlow("8.18")
    val hydroYield = MutableStateFlow("241.0") // MPa or 35,000 PSI

    fun logActivity(toolTitle: String, summary: String, value: Double = 0.0) {
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "METALWORKS",
                title = toolTitle,
                summary = summary,
                value = value
            )
        }
    }
}
