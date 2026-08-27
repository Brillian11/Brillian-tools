package com.example.ui.screens.painting

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

class PaintingCoatingStudioViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    // Global Metric/Imperial Toggle
    private val _isImperial = MutableStateFlow(true)
    val isImperial: StateFlow<Boolean> = _isImperial.asStateFlow()

    fun toggleUnits() {
        _isImperial.value = !_isImperial.value
    }

    // --- Tab 1: Coverage & Material Sizing ---
    val areaInput = MutableStateFlow("400")
    val substrateType = MutableStateFlow("Smooth Timber") // "Smooth Timber", "Porous Brick", "Non-Porous Iron", "Drywall"
    val primerRequired = MutableStateFlow(true)
    val numCoats = MutableStateFlow(2)

    val targetDft = MutableStateFlow("3.0") // mils or microns
    val volumeSolids = MutableStateFlow("50") // %
    val thinnerPercent = MutableStateFlow("10") // %

    val trimLength = MutableStateFlow("120") // ft or m
    val trimWidth = MutableStateFlow("4.0") // inches or cm
    val sidingLength = MutableStateFlow("50")
    val sidingHeight = MutableStateFlow("10")
    val lapOverlap = MutableStateFlow("1.5") // inches or cm

    val steelProfile = MutableStateFlow("I-Beam") // "I-Beam", "H-Section", "Round Pipe", "Corrugated"
    val steelLength = MutableStateFlow("40") // ft or m
    val steelDiameterOrWidth = MutableStateFlow("12") // inches or cm

    // --- Tab 2: Mixing, Ratios & Viscosity ---
    val mixBaseRatio = MutableStateFlow("4") // 4:1
    val mixActivatorRatio = MutableStateFlow("1")
    val mixThinnerPercent = MutableStateFlow("10") // %
    val mixTargetVolume = MutableStateFlow("1000") // mL or fl oz

    val viscosityCupType = MutableStateFlow("Ford #4") // "Ford #4", "Zahn #2", "DIN 4"
    val viscositySeconds = MutableStateFlow("25") // seconds

    val ambientTemp = MutableStateFlow("77") // °F or °C
    val applicationMethod = MutableStateFlow("HVLP Spray") // "Brush/Roller", "Conventional Spray", "HVLP Spray", "Airless Spray"

    // --- Tab 3: Spray Equipment & Tip Sizing ---
    val coatingTypeForTip = MutableStateFlow("Latex Paint") // "Stain/Varnish", "Lacquer/Enamel", "Latex Paint", "Heavy Elastomeric"
    val structureComplexity = MutableStateFlow("Flat Wall") // "Flat Wall", "Open Steel Frame", "Lattice / Railing"

    val nozzleSizeMm = MutableStateFlow("1.4")
    val paintViscosityClass = MutableStateFlow("Medium (Latex)") // "Low (Stain)", "Medium (Latex)", "High (Gelcoat)"

    // --- Tab 4: Environmental Curing ---
    val envRelativeHumidity = MutableStateFlow("65") // %
    val envSurfaceTemp = MutableStateFlow("72") // °F or °C
    val envCoatingChemistry = MutableStateFlow("2K Epoxy") // "Alkyd Enamel", "2K Epoxy", "Waterborne Acrylic", "Polyurethane"
    val envAirflow = MutableStateFlow("Medium / Normal") // "Stagnant", "Medium / Normal", "Strong / Outdoor"

    // --- Tab 5: Wood Finishing ---
    val woodSurfaceType = MutableStateFlow("Hardwood") // "Softwood", "Hardwood", "MDF/Veneer", "Inter-coat scuff"
    val woodSpecies = MutableStateFlow("Pine") // "Pine", "Birch", "Cherry", "Red Oak", "Maple"
    val woodConditionerApplied = MutableStateFlow(false)
    val woodGrainType = MutableStateFlow("Open Pore") // "Open Pore", "Closed Pore"

    // --- Tab 6: Metal & Iron ---
    val steelRustGrade = MutableStateFlow("B - Rusted") // "A - Light Rust", "B - Rusted", "C - Heavy Pit", "D - Scale/Spall"
    val blastStandard = MutableStateFlow("SSPC-SP 6 Commercial") // "SSPC-SP 2 Hand", "SSPC-SP 3 Power", "SSPC-SP 6 Commercial", "SSPC-SP 10 Near-White"
    val rustSeverity = MutableStateFlow("Moderate") // "Light", "Moderate", "Heavy Scale"
    val primerSubstrateMetal = MutableStateFlow("Carbon Steel") // "Galvanized Zinc", "Carbon Steel", "Aluminum", "Stainless"
    val primerPaintType = MutableStateFlow("Alkyd / Oil-based") // "Alkyd / Oil-based", "Epoxy Primer", "Zinc-Rich Epoxy", "Acrylic"

    // --- Tab 7: Wall & Masonry ---
    val puttyThicknessMm = MutableStateFlow("2.0") // mm
    val moistureReadingPercent = MutableStateFlow("10") // %
    val concretePhLevel = MutableStateFlow("11.0")
    val concreteAgeWeeks = MutableStateFlow("2") // weeks
    val jointWidthMm = MutableStateFlow("10") // mm or eighths of inch
    val jointDepthMm = MutableStateFlow("8") // mm or eighths of inch
    val jointLengthFt = MutableStateFlow("50") // ft or m
    val cartridgeSizeMl = MutableStateFlow("300") // 300ml or 600ml

    // Active Tab Index
    private val _activeTab = MutableStateFlow(0)
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    fun setActiveTab(index: Int) {
        _activeTab.value = index
    }

    // --- LOGS ---
    fun logCalculation(toolName: String, summaryText: String) {
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "PAINTING_COATING",
                title = "Painting & Coating Studio - $toolName",
                summary = summaryText,
                value = 0.0
            )
        }
    }
}
