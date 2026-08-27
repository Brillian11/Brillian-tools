package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI

enum class SawMachineType(
    val label: String,
    val defaultMotorRpm: Double,
    val defaultMotorPulley: Double,
    val defaultArborPulley: Double,
    val defaultBladeDiameter: Double
) {
    BAND_SAW_14("14\" Woodworking Band Saw", 1725.0, 3.0, 6.0, 14.0),
    BAND_SAW_RESAW_18("18\" Heavy Duty Resaw Band Saw", 1725.0, 4.0, 5.0, 18.0),
    METAL_CUTTING_BANDSAW("Metal Cutting Horizontal/Vertical Band Saw", 1725.0, 1.5, 12.0, 14.0),
    TABLE_SAW_10("10\" Cabinet / Contractor Table Saw", 3450.0, 4.0, 3.5, 10.0),
    TABLE_SAW_12("12\" Industrial Table Saw", 3450.0, 4.5, 3.5, 12.0),
    MITER_SAW_12("12\" Compound Sliding Miter Saw (Direct)", 4000.0, 1.0, 1.0, 12.0),
    CUSTOM("Custom Saw Setup", 1725.0, 3.0, 4.0, 10.0)
}

enum class TargetMaterial(
    val label: String,
    val minSfpm: Double,
    val maxSfpm: Double,
    val recommendedSfpm: Double,
    val notes: String
) {
    HARDWOOD_SOFTWOOD("Wood (Hardwoods & Softwoods)", 2500.0, 5000.0, 3500.0, "Standard woodworking band saw and table saw range"),
    ALUMINUM_BRASS("Non-Ferrous Metals (Aluminum / Brass)", 800.0, 2500.0, 1500.0, "Use carbide-tipped or bimetal blade with wax lube"),
    PLASTICS_ACRYLIC("Plastics & Plexiglass / Acrylic", 1000.0, 3000.0, 2000.0, "Moderate speed prevents edge melting and chipping"),
    MILD_STEEL("Mild Carbon Steel (Tubing / Bar)", 100.0, 350.0, 200.0, "Requires heavy pulley reduction or gearbox"),
    STAINLESS_STEEL("Stainless Steel (304 / 316)", 50.0, 150.0, 90.0, "Very low speed with flood or mist coolant required"),
    TOOL_STEEL("High Alloy / Tool Steel", 40.0, 100.0, 60.0, "Slow speed prevents tooth tooth burnout")
}

enum class SpeedSafetyStatus(val label: String, val advisory: String, val colorHex: Long) {
    OPTIMAL("OPTIMAL SPEED", "Blade speed is within ideal cutting velocity for selected material.", 0xFF16A34A),
    HIGH("SPEED TOO HIGH (OVERHEAT HAZARD)", "Blade is spinning too fast for this material. Risk of blade burning, tooth stripping, or fire.", 0xFFDC2626),
    LOW("SPEED TOO LOW", "Blade speed is below recommended range. May cause tooth bogging, rough cuts, or motor stall.", 0xFFD97706)
}

data class BladeSpeedCalculation(
    val arborRpm: Double,
    val pulleyRatio: Double,
    val surfaceFeetPerMinute: Double,
    val metersPerMinute: Double,
    val metersPerSecond: Double,
    val toothImpactRateHz: Double, // for 40T blade
    val safetyStatus: SpeedSafetyStatus
)

class BladeSpeedViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    private val _machineType = MutableStateFlow(SawMachineType.BAND_SAW_14)
    val machineType: StateFlow<SawMachineType> = _machineType.asStateFlow()

    private val _targetMaterial = MutableStateFlow(TargetMaterial.HARDWOOD_SOFTWOOD)
    val targetMaterial: StateFlow<TargetMaterial> = _targetMaterial.asStateFlow()

    private val _motorRpm = MutableStateFlow(1725.0)
    val motorRpm: StateFlow<Double> = _motorRpm.asStateFlow()

    private val _motorPulleyDiameter = MutableStateFlow(3.0) // inches
    val motorPulleyDiameter: StateFlow<Double> = _motorPulleyDiameter.asStateFlow()

    private val _arborPulleyDiameter = MutableStateFlow(6.0) // inches
    val arborPulleyDiameter: StateFlow<Double> = _arborPulleyDiameter.asStateFlow()

    private val _bladeWheelDiameter = MutableStateFlow(14.0) // inches
    val bladeWheelDiameter: StateFlow<Double> = _bladeWheelDiameter.asStateFlow()

    private val _bladeToothCount = MutableStateFlow(40)
    val bladeToothCount: StateFlow<Int> = _bladeToothCount.asStateFlow()

    private val _calculation = MutableStateFlow(calculate())
    val calculation: StateFlow<BladeSpeedCalculation> = _calculation.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    init {
        applyMachineType(SawMachineType.BAND_SAW_14)
    }

    fun applyMachineType(type: SawMachineType) {
        _machineType.value = type
        if (type != SawMachineType.CUSTOM) {
            _motorRpm.value = type.defaultMotorRpm
            _motorPulleyDiameter.value = type.defaultMotorPulley
            _arborPulleyDiameter.value = type.defaultArborPulley
            _bladeWheelDiameter.value = type.defaultBladeDiameter
        }
        recalculate()
    }

    fun setTargetMaterial(mat: TargetMaterial) {
        _targetMaterial.value = mat
        recalculate()
    }

    fun updateInputs(
        motorRpm: Double = _motorRpm.value,
        motorPulley: Double = _motorPulleyDiameter.value,
        arborPulley: Double = _arborPulleyDiameter.value,
        bladeDiameter: Double = _bladeWheelDiameter.value,
        teeth: Int = _bladeToothCount.value
    ) {
        _motorRpm.value = motorRpm.coerceAtLeast(100.0)
        _motorPulleyDiameter.value = motorPulley.coerceAtLeast(0.5)
        _arborPulleyDiameter.value = arborPulley.coerceAtLeast(0.5)
        _bladeWheelDiameter.value = bladeDiameter.coerceAtLeast(1.0)
        _bladeToothCount.value = teeth.coerceAtLeast(1)
        _machineType.value = SawMachineType.CUSTOM
        recalculate()
    }

    private fun recalculate() {
        _calculation.value = calculate()
        _lastLogSaved.value = false
    }

    private fun calculate(): BladeSpeedCalculation {
        val mRpm = _motorRpm.value
        val dMotor = _motorPulleyDiameter.value
        val dArbor = _arborPulleyDiameter.value
        val dBlade = _bladeWheelDiameter.value
        val teeth = _bladeToothCount.value

        val ratio = dMotor / dArbor
        val arborRpm = mRpm * ratio

        // SFPM = (PI * D_inches * RPM) / 12
        val sfpm = (PI * dBlade * arborRpm) / 12.0
        val mPerMin = sfpm * 0.3048
        val mPerSec = mPerMin / 60.0

        val toothImpactRate = (arborRpm / 60.0) * teeth

        val mat = _targetMaterial.value
        val status = when {
            sfpm > mat.maxSfpm * 1.25 -> SpeedSafetyStatus.HIGH
            sfpm < mat.minSfpm * 0.75 -> SpeedSafetyStatus.LOW
            else -> SpeedSafetyStatus.OPTIMAL
        }

        return BladeSpeedCalculation(
            arborRpm = arborRpm,
            pulleyRatio = ratio,
            surfaceFeetPerMinute = sfpm,
            metersPerMinute = mPerMin,
            metersPerSecond = mPerSec,
            toothImpactRateHz = toothImpactRate,
            safetyStatus = status
        )
    }

    fun saveSpeedLog(sawName: String = "14\" Delta Bandsaw (Woodworking)") {
        viewModelScope.launch {
            val c = _calculation.value
            val mat = _targetMaterial.value

            toolLogRepository?.logToolActivity(
                toolType = "widget_blade_speed",
                title = "Blade Speed: $sawName (${mat.label})",
                summary = "SFPM: ${String.format("%,.0f ft/min", c.surfaceFeetPerMinute)} (${String.format("%.1f m/s", c.metersPerSecond)}), Arbor: ${String.format("%.0f RPM", c.arborRpm)}, Target: ${String.format("%,.0f - %,.0f SFPM", mat.minSfpm, mat.maxSfpm)}, Status: ${c.safetyStatus.label}",
                value = c.surfaceFeetPerMinute
            )
            _lastLogSaved.value = true
        }
    }
}
