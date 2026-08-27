package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SagulatorWoodSpecies(
    val id: String,
    val name: String,
    val category: String,
    val moePsi: Double, // Modulus of Elasticity in PSI
    val densityLbsCuFt: Double
)

enum class ShelfSupportType(val label: String, val uniformFactor: Double, val centerFactor: Double) {
    SIMPLY_SUPPORTED("Shelf Pins / Cleats (Floating Ends)", 5.0 / 384.0, 1.0 / 48.0),
    FIXED_DADO("Glued Dadoes / Screwed (Fixed Ends)", 1.0 / 384.0, 1.0 / 192.0)
}

enum class LoadDistribution(val label: String) {
    UNIFORMLY_DISTRIBUTED("Uniformly Distributed (Books / Cans)"),
    CENTER_POINT("Center Concentrated Load (Heavy Amp / Audio)")
}

enum class SagRating(val label: String, val advisory: String, val colorHex: Long) {
    IMPERCEPTIBLE("EXCELLENT (Imperceptible Sag)", "Deflection is under 0.02\" per foot. Completely invisible to the naked eye.", 0xFF16A34A),
    ACCEPTABLE("ACCEPTABLE (Architectural Standard)", "Deflection is within 0.02\" - 0.033\" per foot. Safe for standard book / tool storage.", 0xFFD97706),
    EXCESSIVE("EXCESSIVE SAG (Structural Failure Risk)", "Deflection exceeds 0.033\"/ft. Noticeable bow; add center support, edge stiffener, or increase thickness.", 0xFFDC2626)
}

data class SagResult(
    val totalDeflectionInches: Double,
    val deflectionPerFootInches: Double,
    val shelfWeightLbs: Double,
    val totalLoadWithShelfLbs: Double,
    val momentOfInertiaIn4: Double,
    val rating: SagRating
)

class SagulatorViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {

    val speciesList = listOf(
        SagulatorWoodSpecies("red_oak", "Red Oak (Northern)", "Hardwood", 1820000.0, 44.0),
        SagulatorWoodSpecies("white_oak", "White Oak", "Hardwood", 1780000.0, 47.0),
        SagulatorWoodSpecies("hard_maple", "Hard Maple (Sugar)", "Hardwood", 1830000.0, 44.0),
        SagulatorWoodSpecies("black_walnut", "Black Walnut", "Hardwood", 1680000.0, 38.0),
        SagulatorWoodSpecies("black_cherry", "Black Cherry", "Hardwood", 1490000.0, 35.0),
        SagulatorWoodSpecies("white_ash", "White Ash", "Hardwood", 1740000.0, 42.0),
        SagulatorWoodSpecies("douglas_fir", "Douglas Fir", "Softwood", 1950000.0, 32.0),
        SagulatorWoodSpecies("pine_eastern_white", "Eastern White Pine", "Softwood", 1240000.0, 25.0),
        SagulatorWoodSpecies("pine_southern_yellow", "Southern Yellow Pine", "Softwood", 1790000.0, 36.0),
        SagulatorWoodSpecies("plywood_baltic_birch", "Baltic Birch Plywood (Multi-ply)", "Sheet Goods", 1400000.0, 43.0),
        SagulatorWoodSpecies("plywood_hardwood_core", "Cabinet-Grade Hardwood Plywood", "Sheet Goods", 1200000.0, 36.0),
        SagulatorWoodSpecies("mdf", "Medium Density Fiberboard (MDF)", "Sheet Goods", 450000.0, 48.0),
        SagulatorWoodSpecies("particle_board", "Particle Board / Industrial Core", "Sheet Goods", 320000.0, 45.0),
        SagulatorWoodSpecies("melamine", "Melamine Particleboard", "Sheet Goods", 380000.0, 47.0)
    )

    private val _selectedSpecies = MutableStateFlow(speciesList[0])
    val selectedSpecies: StateFlow<SagulatorWoodSpecies> = _selectedSpecies.asStateFlow()

    private val _shelfSpanInches = MutableStateFlow(36.0)
    val shelfSpanInches: StateFlow<Double> = _shelfSpanInches.asStateFlow()

    private val _shelfDepthInches = MutableStateFlow(11.25)
    val shelfDepthInches: StateFlow<Double> = _shelfDepthInches.asStateFlow()

    private val _shelfThicknessInches = MutableStateFlow(0.75) // 3/4" nominal
    val shelfThicknessInches: StateFlow<Double> = _shelfThicknessInches.asStateFlow()

    private val _appliedLoadLbs = MutableStateFlow(60.0)
    val appliedLoadLbs: StateFlow<Double> = _appliedLoadLbs.asStateFlow()

    private val _loadDistribution = MutableStateFlow(LoadDistribution.UNIFORMLY_DISTRIBUTED)
    val loadDistribution: StateFlow<LoadDistribution> = _loadDistribution.asStateFlow()

    private val _supportType = MutableStateFlow(ShelfSupportType.SIMPLY_SUPPORTED)
    val supportType: StateFlow<ShelfSupportType> = _supportType.asStateFlow()

    private val _hasHardwoodEdging = MutableStateFlow(false)
    val hasHardwoodEdging: StateFlow<Boolean> = _hasHardwoodEdging.asStateFlow()

    private val _edgingHeightInches = MutableStateFlow(1.5) // 1.5" front apron lip
    val edgingHeightInches: StateFlow<Double> = _edgingHeightInches.asStateFlow()

    private val _edgingThicknessInches = MutableStateFlow(0.75)
    val edgingThicknessInches: StateFlow<Double> = _edgingThicknessInches.asStateFlow()

    private val _sagResult = MutableStateFlow(calculateDeflection())
    val sagResult: StateFlow<SagResult> = _sagResult.asStateFlow()

    private val _lastLogSaved = MutableStateFlow(false)
    val lastLogSaved: StateFlow<Boolean> = _lastLogSaved.asStateFlow()

    init {
        recalculate()
    }

    fun setSpecies(species: SagulatorWoodSpecies) {
        _selectedSpecies.value = species
        recalculate()
    }

    fun updateDimensions(
        span: Double = _shelfSpanInches.value,
        depth: Double = _shelfDepthInches.value,
        thickness: Double = _shelfThicknessInches.value,
        load: Double = _appliedLoadLbs.value,
        dist: LoadDistribution = _loadDistribution.value,
        support: ShelfSupportType = _supportType.value,
        edging: Boolean = _hasHardwoodEdging.value,
        edgeH: Double = _edgingHeightInches.value,
        edgeT: Double = _edgingThicknessInches.value
    ) {
        _shelfSpanInches.value = span.coerceAtLeast(6.0)
        _shelfDepthInches.value = depth.coerceAtLeast(2.0)
        _shelfThicknessInches.value = thickness.coerceAtLeast(0.125)
        _appliedLoadLbs.value = load.coerceAtLeast(0.0)
        _loadDistribution.value = dist
        _supportType.value = support
        _hasHardwoodEdging.value = edging
        _edgingHeightInches.value = edgeH.coerceAtLeast(0.25)
        _edgingThicknessInches.value = edgeT.coerceAtLeast(0.125)
        recalculate()
    }

    private fun recalculate() {
        _sagResult.value = calculateDeflection()
        _lastLogSaved.value = false
    }

    private fun calculateDeflection(): SagResult {
        val L = _shelfSpanInches.value
        val b = _shelfDepthInches.value
        val h = _shelfThicknessInches.value
        val E = _selectedSpecies.value.moePsi
        val density = _selectedSpecies.value.densityLbsCuFt

        // Self-weight of shelf
        val volumeCuFt = (L * b * h) / 1728.0
        var shelfWeight = volumeCuFt * density
        if (_hasHardwoodEdging.value) {
            val edgeVolCuFt = (L * _edgingThicknessInches.value * _edgingHeightInches.value) / 1728.0
            shelfWeight += edgeVolCuFt * 44.0 // Oak equivalent
        }

        val totalLoad = _appliedLoadLbs.value + shelfWeight

        // Moment of inertia calculation (I = b*h^3 / 12)
        var I = (b * Math.pow(h, 3.0)) / 12.0

        if (_hasHardwoodEdging.value) {
            // Adding front vertical stiffener lip
            val edgeI = (_edgingThicknessInches.value * Math.pow(_edgingHeightInches.value, 3.0)) / 12.0
            I += edgeI * 1.35 // effective composite stiffness boost
        }

        val factor = if (_loadDistribution.value == LoadDistribution.UNIFORMLY_DISTRIBUTED) {
            _supportType.value.uniformFactor
        } else {
            _supportType.value.centerFactor
        }

        // Deflection: delta = factor * (W * L^3) / (E * I)
        val deflection = factor * (totalLoad * Math.pow(L, 3.0)) / (E * I)

        val spanFeet = L / 12.0
        val sagPerFoot = if (spanFeet > 0) deflection / spanFeet else 0.0

        val rating = when {
            sagPerFoot <= 0.020 -> SagRating.IMPERCEPTIBLE
            sagPerFoot <= 0.033 -> SagRating.ACCEPTABLE
            else -> SagRating.EXCESSIVE
        }

        return SagResult(
            totalDeflectionInches = deflection,
            deflectionPerFootInches = sagPerFoot,
            shelfWeightLbs = shelfWeight,
            totalLoadWithShelfLbs = totalLoad,
            momentOfInertiaIn4 = I,
            rating = rating
        )
    }

    fun saveSagLog(bookcaseNote: String = "Living Room Bookcase Center Shelf") {
        viewModelScope.launch {
            val r = _sagResult.value
            val s = _selectedSpecies.value

            toolLogRepository?.logToolActivity(
                toolType = "widget_sagulator",
                title = "Lumber Sagulator: $bookcaseNote (${s.name})",
                summary = "Span: ${String.format("%.1f\"", _shelfSpanInches.value)}, Load: ${String.format("%.0f lbs", _appliedLoadLbs.value)}, Sag: ${String.format("%.3f\"", r.totalDeflectionInches)} (${String.format("%.3f\"/ft", r.deflectionPerFootInches)}), Status: ${r.rating.label}",
                value = r.totalDeflectionInches
            )
            _lastLogSaved.value = true
        }
    }
}
