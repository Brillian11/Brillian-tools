package com.example.ui.screens.woodworking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.ln

data class StudioWoodSpecies(
    val name: String,
    val botanicalName: String,
    val region: String, // North American, European, Asian & Southeast Asian
    val jankaHardness: Int, // lbf
    val density: Int, // kg/m^3
    val tangentialShrinkage: Double,
    val radialShrinkage: Double,
    val trRatio: Double,
    val toxicityWarning: String,
    val description: String,
    val faceGrainUrl: String = "",
    // Workability ratings (1 to 5 stars)
    val handPlaning: Int,
    val machineRouting: Int,
    val nailScrewHolding: Int,
    val glueAdhesion: Int,
    val steamBending: Int
)

data class WoodSpeciesStudioUiState(
    val searchQuery: String = "",
    val selectedRegion: String = "ALL", // ALL, North American, European, Asian
    val selectedSpecies: StudioWoodSpecies? = null,
    
    // EMC Calculator Inputs & Outputs
    val emcTemp: Double = 70.0, // °F
    val emcHumidity: Double = 50.0, // %
    val calculatedEmc: Double = 9.2, // %
    
    // Drying Sizer Inputs & Outputs
    val boardThicknessInches: Double = 1.0,
    val initialMoisture: Double = 30.0,
    val targetMoisture: Double = 12.0,
    val estAirDryingMonths: Double = 12.0,
    
    // Springback Calculator Inputs & Outputs
    val targetRadius: Double = 24.0, // inches or cm
    val plyThickness: Double = 0.125, // thickness of each laminate layer
    val plyCount: Int = 5,
    val computedSpringbackRadius: Double = 26.5 // adjusted radius to build on the form
)

class WoodSpeciesStudioViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WoodSpeciesStudioUiState())
    val uiState: StateFlow<WoodSpeciesStudioUiState> = _uiState.asStateFlow()

    val allSpecies = listOf(
        // North American
        StudioWoodSpecies(
            name = "Black Walnut",
            botanicalName = "Juglans nigra",
            region = "North American",
            jankaHardness = 1010,
            density = 610,
            tangentialShrinkage = 7.8,
            radialShrinkage = 5.5,
            trRatio = 1.4,
            toxicityWarning = "Walnut dust can act as a skin sensitizer. Toxic to horses.",
            description = "Highly prized premium furniture hardwood with rich chocolate-brown tones. Excellent carving and machining capabilities.",
            handPlaning = 5, machineRouting = 5, nailScrewHolding = 4, glueAdhesion = 5, steamBending = 4
        ),
        StudioWoodSpecies(
            name = "White Oak",
            botanicalName = "Quercus alba",
            region = "North American",
            jankaHardness = 1360,
            density = 770,
            tangentialShrinkage = 10.5,
            radialShrinkage = 5.6,
            trRatio = 1.88,
            toxicityWarning = "Sensitizer: Can cause respiratory irritation and asthma-like symptoms.",
            description = "Closed cellular structure due to tyloses makes it water/rot-resistant. Classic choice for outdoor timber framing, boats, and whiskey barrels.",
            handPlaning = 4, machineRouting = 4, nailScrewHolding = 5, glueAdhesion = 4, steamBending = 5
        ),
        StudioWoodSpecies(
            name = "Hard Maple",
            botanicalName = "Acer saccharum",
            region = "North American",
            jankaHardness = 1450,
            density = 705,
            tangentialShrinkage = 9.9,
            radialShrinkage = 4.8,
            trRatio = 2.06,
            toxicityWarning = "Low allergen. Heavy dust can cause respiratory irritation.",
            description = "Heavy, dense, abrasion-resistant, light-colored. Prone to tearout on figured/curly grain unless ultra-sharp planing angles are used.",
            handPlaning = 3, machineRouting = 4, nailScrewHolding = 4, glueAdhesion = 5, steamBending = 4
        ),
        StudioWoodSpecies(
            name = "Black Cherry",
            botanicalName = "Prunus serotina",
            region = "North American",
            jankaHardness = 950,
            density = 560,
            tangentialShrinkage = 7.1,
            radialShrinkage = 3.7,
            trRatio = 1.92,
            toxicityWarning = "Low allergen risk.",
            description = "Exquisite satin finish, ages into a deep reddish-bronze when exposed to UV/sunlight. Exceptionally stable once dried properly.",
            handPlaning = 5, machineRouting = 5, nailScrewHolding = 4, glueAdhesion = 5, steamBending = 3
        ),
        StudioWoodSpecies(
            name = "Eastern White Pine",
            botanicalName = "Pinus strobus",
            region = "North American",
            jankaHardness = 380,
            density = 400,
            tangentialShrinkage = 6.1,
            radialShrinkage = 2.1,
            trRatio = 2.9,
            toxicityWarning = "Pine sap can cause allergic contact dermatitis.",
            description = "Soft, lightweight, straight-grained. Easily carved and shaped with hand tools. Prone to excessive shrinkage on Tangential face.",
            handPlaning = 5, machineRouting = 4, nailScrewHolding = 3, glueAdhesion = 4, steamBending = 2
        ),
        StudioWoodSpecies(
            name = "Douglas Fir",
            botanicalName = "Pseudotsuga menziesii",
            region = "North American",
            jankaHardness = 710,
            density = 530,
            tangentialShrinkage = 7.6,
            radialShrinkage = 4.8,
            trRatio = 1.58,
            toxicityWarning = "Slivers easily cause skin infections and festering.",
            description = "Harder than most softwoods, exceptionally high tensile and bending strength. The premier wood for structural joists and rustic log timber.",
            handPlaning = 3, machineRouting = 4, nailScrewHolding = 4, glueAdhesion = 4, steamBending = 3
        ),

        // European
        StudioWoodSpecies(
            name = "European Beech",
            botanicalName = "Fagus sylvatica",
            region = "European",
            jankaHardness = 1300,
            density = 710,
            tangentialShrinkage = 11.8,
            radialShrinkage = 5.8,
            trRatio = 2.03,
            toxicityWarning = "Sensitizer: Dust can cause congestion and mild dermatitis.",
            description = "Highly uniform, dense, heavy timber. Steam-bends spectacularly. The gold-standard in Europe for heavy workbench tops and hand tool handles.",
            handPlaning = 4, machineRouting = 4, nailScrewHolding = 5, glueAdhesion = 5, steamBending = 5
        ),
        StudioWoodSpecies(
            name = "European Ash",
            botanicalName = "Fraxinus excelsior",
            region = "European",
            jankaHardness = 1320,
            density = 680,
            tangentialShrinkage = 9.6,
            radialShrinkage = 5.7,
            trRatio = 1.68,
            toxicityWarning = "Sensitizer: Dust can cause minor respiratory issues.",
            description = "Tremendous shock resistance, flexibility, and straight grain. Ideal for handles of impact tools (axes, hammers) and bentwood hoops.",
            handPlaning = 4, machineRouting = 4, nailScrewHolding = 4, glueAdhesion = 5, steamBending = 5
        ),
        StudioWoodSpecies(
            name = "European Birch",
            botanicalName = "Betula pendula",
            region = "European",
            jankaHardness = 1210,
            density = 650,
            tangentialShrinkage = 9.0,
            radialShrinkage = 5.3,
            trRatio = 1.7,
            toxicityWarning = "Low allergen risk.",
            description = "Base timber of Baltic Birch multi-ply plywood. Heavy, void-free core stability. Resonates cleanly for architectural paneling.",
            handPlaning = 4, machineRouting = 4, nailScrewHolding = 4, glueAdhesion = 5, steamBending = 4
        ),
        StudioWoodSpecies(
            name = "Sweet Chestnut",
            botanicalName = "Castanea sativa",
            region = "European",
            jankaHardness = 680,
            density = 560,
            tangentialShrinkage = 7.9,
            radialShrinkage = 4.3,
            trRatio = 1.83,
            toxicityWarning = "High tannin content can stain skin and corrode iron fasteners.",
            description = "Rich in natural rot-resistant tannins. Does not require chemical pressure treatments for permanent outdoor fencing, cladding, and posts.",
            handPlaning = 4, machineRouting = 4, nailScrewHolding = 3, glueAdhesion = 4, steamBending = 3
        ),

        // Asian & Southeast Asian
        StudioWoodSpecies(
            name = "Teak (Jati)",
            botanicalName = "Tectona grandis",
            region = "Asian & Southeast Asian",
            jankaHardness = 1070,
            density = 655,
            tangentialShrinkage = 5.8,
            radialShrinkage = 2.5,
            trRatio = 2.32,
            toxicityWarning = "High silica/dust causes skin rashes and respiratory allergies.",
            description = "Loaded with heavy natural oils and silica. Highly immune to water, wood rot, termites. Extremely abrasive on standard steel planer blades.",
            handPlaning = 3, machineRouting = 3, nailScrewHolding = 4, glueAdhesion = 3, steamBending = 3
        ),
        StudioWoodSpecies(
            name = "Sonokeling",
            botanicalName = "Dalbergia latifolia",
            region = "Asian & Southeast Asian",
            jankaHardness = 1720,
            density = 850,
            tangentialShrinkage = 5.9,
            radialShrinkage = 2.7,
            trRatio = 2.18,
            toxicityWarning = "Rosewood sensitizer: severe contact allergies and asthma risks.",
            description = "East Indian Rosewood. Gorgeous deep purple-chocolate streaks with high acoustic resonant clarity. Used for high-end musical instrument necks and luxury tables.",
            handPlaning = 2, machineRouting = 3, nailScrewHolding = 5, glueAdhesion = 3, steamBending = 3
        ),
        StudioWoodSpecies(
            name = "Mahogany (Mahoni)",
            botanicalName = "Swietenia macrophylla",
            region = "Asian & Southeast Asian",
            jankaHardness = 900,
            density = 590,
            tangentialShrinkage = 4.1,
            radialShrinkage = 3.0,
            trRatio = 1.37,
            toxicityWarning = "Slight respiratory irritant.",
            description = "Supremely stable, easy to carve, and rot-resistant. Traditional red-brown tone. Popularized for decorative interior joinery.",
            handPlaning = 5, machineRouting = 5, nailScrewHolding = 4, glueAdhesion = 5, steamBending = 4
        ),
        StudioWoodSpecies(
            name = "Acacia",
            botanicalName = "Acacia mangium",
            region = "Asian & Southeast Asian",
            jankaHardness = 1100,
            density = 640,
            tangentialShrinkage = 7.2,
            radialShrinkage = 3.2,
            trRatio = 2.25,
            toxicityWarning = "Dust causes mild asthma-like reactions.",
            description = "Tough, highly dense, fast-growing. Prone to severe cracking/checking if kiln-dried too fast. Ideal for chopping boards, rustic slabs, and outdoor framing.",
            handPlaning = 3, machineRouting = 4, nailScrewHolding = 4, glueAdhesion = 4, steamBending = 3
        ),
        StudioWoodSpecies(
            name = "Merbau",
            botanicalName = "Intsia bijuga",
            region = "Asian & Southeast Asian",
            jankaHardness = 1710,
            density = 830,
            tangentialShrinkage = 4.4,
            radialShrinkage = 2.7,
            trRatio = 1.63,
            toxicityWarning = "Heavy tannin bleeding when exposed to rainwater. Colors pools red.",
            description = "Exceptional hardness and weight. Highly durable outdoors. Naturally releases deep dark-brown tannins, which must be sealed thoroughly.",
            handPlaning = 2, machineRouting = 3, nailScrewHolding = 5, glueAdhesion = 4, steamBending = 3
        ),
        StudioWoodSpecies(
            name = "Ulin (Ironwood)",
            botanicalName = "Eusideroxylon zwageri",
            region = "Asian & Southeast Asian",
            jankaHardness = 3000,
            density = 1040,
            tangentialShrinkage = 6.2,
            radialShrinkage = 3.1,
            trRatio = 2.0,
            toxicityWarning = "Indestructible structure. Dust is very abrasive.",
            description = "Sinks in water. Practically immune to marine borers, fungal decay, and insects. Used for heavy-duty ocean pilings and sub-ground foundational structural logs.",
            handPlaning = 1, machineRouting = 1, nailScrewHolding = 5, glueAdhesion = 2, steamBending = 1
        ),
        StudioWoodSpecies(
            name = "Sungkai",
            botanicalName = "Peronema canescens",
            region = "Asian & Southeast Asian",
            jankaHardness = 820,
            density = 520,
            tangentialShrinkage = 6.8,
            radialShrinkage = 3.4,
            trRatio = 2.0,
            toxicityWarning = "Mild respiratory risk from dust.",
            description = "Striking straight lines, light golden tone, resembles European Ash. The premier choice in Modern minimalist Indonesian furniture and panelings.",
            handPlaning = 4, machineRouting = 4, nailScrewHolding = 4, glueAdhesion = 5, steamBending = 4
        ),
        StudioWoodSpecies(
            name = "Albasia / Sengon",
            botanicalName = "Albizia falcataria",
            region = "Asian & Southeast Asian",
            jankaHardness = 240,
            density = 310,
            tangentialShrinkage = 5.2,
            radialShrinkage = 2.1,
            trRatio = 2.48,
            toxicityWarning = "Low toxicity. Normal dust precautions.",
            description = "Extremely fast-growing Indonesian plantation lightweight timber. Very soft, easy to cut and assemble. Ideal for light packaging boxes, core panels, and light furniture.",
            handPlaning = 5, machineRouting = 4, nailScrewHolding = 3, glueAdhesion = 4, steamBending = 2
        ),
        StudioWoodSpecies(
            name = "Waru",
            botanicalName = "Hibiscus tiliaceus",
            region = "Asian & Southeast Asian",
            jankaHardness = 690,
            density = 550,
            tangentialShrinkage = 6.1,
            radialShrinkage = 3.0,
            trRatio = 2.03,
            toxicityWarning = "Low allergen risk.",
            description = "Traditional Javanese coastal timber with high toughness and flexibility. Historically prized for boat ribs, cart wheels, and traditional tool handles.",
            handPlaning = 4, machineRouting = 4, nailScrewHolding = 4, glueAdhesion = 5, steamBending = 4
        ),
        StudioWoodSpecies(
            name = "Keruing",
            botanicalName = "Dipterocarpus",
            region = "Asian & Southeast Asian",
            jankaHardness = 1250,
            density = 760,
            tangentialShrinkage = 8.5,
            radialShrinkage = 4.6,
            trRatio = 1.85,
            toxicityWarning = "Resin exudation can irritate skin and clog sandpapers.",
            description = "Heavy structural hardwood common in SE Asian tropical forests. High bending strength for heavy floor joists, truck beds, and bridge timbers.",
            handPlaning = 3, machineRouting = 3, nailScrewHolding = 5, glueAdhesion = 3, steamBending = 3
        ),
        StudioWoodSpecies(
            name = "Nangka (Jackfruit Wood)",
            botanicalName = "Artocarpus heterophyllus",
            region = "Asian & Southeast Asian",
            jankaHardness = 1170,
            density = 670,
            tangentialShrinkage = 6.5,
            radialShrinkage = 3.2,
            trRatio = 2.03,
            toxicityWarning = "Natural yellow dye can stain skin and clothes.",
            description = "Ages from bright yellow to a rich lustrous golden-brown. Highly durable against termites. Highly treasured in Java for traditional Gamelan instruments and carving.",
            handPlaning = 4, machineRouting = 4, nailScrewHolding = 4, glueAdhesion = 4, steamBending = 3
        ),
        StudioWoodSpecies(
            name = "Cengkih (Clove Wood)",
            botanicalName = "Syzygium aromaticum",
            region = "Asian & Southeast Asian",
            jankaHardness = 1420,
            density = 880,
            tangentialShrinkage = 7.0,
            radialShrinkage = 3.5,
            trRatio = 2.0,
            toxicityWarning = "Aromatic oils can cause mild sensitization.",
            description = "Dense fruitwood from mature spice plantations. Exceptionally hard and heavy with distinct aromatic grain patterns.",
            handPlaning = 3, machineRouting = 3, nailScrewHolding = 5, glueAdhesion = 4, steamBending = 2
        ),
        StudioWoodSpecies(
            name = "Bangkirai (Yellow Balau)",
            botanicalName = "Shorea laevis",
            region = "Asian & Southeast Asian",
            jankaHardness = 1790,
            density = 910,
            tangentialShrinkage = 7.8,
            radialShrinkage = 4.1,
            trRatio = 1.9,
            toxicityWarning = "Silica content can cause severe dulling of steel tools.",
            description = "Premier heavy-duty tropical timber for outdoor decking, heavy bridge framing, and marine construction. Exceptional natural weather resistance.",
            handPlaning = 2, machineRouting = 3, nailScrewHolding = 5, glueAdhesion = 3, steamBending = 2
        )
    )

    init {
        // Select Walnut as default
        _uiState.value = _uiState.value.copy(selectedSpecies = allSpecies.first())
        recalculateAll()
    }

    fun setSearchQuery(q: String) {
        _uiState.value = _uiState.value.copy(searchQuery = q)
    }

    fun setSelectedRegion(r: String) {
        _uiState.value = _uiState.value.copy(selectedRegion = r)
    }

    fun selectSpecies(species: StudioWoodSpecies) {
        _uiState.value = _uiState.value.copy(selectedSpecies = species)
    }

    fun updateEmcInputs(tempF: Double, humidityPct: Double) {
        _uiState.value = _uiState.value.copy(
            emcTemp = tempF.coerceIn(30.0, 130.0),
            emcHumidity = humidityPct.coerceIn(1.0, 99.0)
        )
        recalculateEmc()
    }

    fun updateDryingInputs(thickness: Double, initialMC: Double, targetMC: Double) {
        _uiState.value = _uiState.value.copy(
            boardThicknessInches = thickness.coerceIn(0.25, 6.0),
            initialMoisture = initialMC.coerceIn(5.0, 80.0),
            targetMoisture = targetMC.coerceIn(5.0, 40.0)
        )
        recalculateDrying()
    }

    fun updateSpringbackInputs(radius: Double, thickness: Double, count: Int) {
        _uiState.value = _uiState.value.copy(
            targetRadius = radius.coerceAtLeast(1.0),
            plyThickness = thickness.coerceIn(0.01, 1.0),
            plyCount = count.coerceIn(2, 50)
        )
        recalculateSpringback()
    }

    private fun recalculateAll() {
        recalculateEmc()
        recalculateDrying()
        recalculateSpringback()
    }

    private fun recalculateEmc() {
        // Traditional Hailwood-Horrobin EMC equation approximation
        val t = _uiState.value.emcTemp
        val h = _uiState.value.emcHumidity / 100.0
        
        // Convert F to C for calculation
        val tc = (t - 32.0) * (5.0 / 9.0)
        
        val w = 330.0 + 0.452 * tc + 0.00415 * tc * tc
        val k = 0.791 + 0.000463 * tc - 0.000000844 * tc * tc
        val kh = k * h
        val k1 = 1.03 + 0.000732 * tc - 0.0000134 * tc * tc
        val k2 = 0.178 + 0.000694 * tc + 0.0000185 * tc * tc
        
        val term1 = kh / (1.0 - kh)
        val term2 = (k1 * kh + 2.0 * k1 * k2 * kh * kh) / (1.0 + k1 * kh + k1 * k2 * kh * kh)
        val emcPct = (1800.0 / w) * (term1 + term2)
        
        _uiState.value = _uiState.value.copy(calculatedEmc = emcPct)
    }

    private fun recalculateDrying() {
        val s = _uiState.value
        // Air drying thumb rule: 1 year (12 months) per inch of board thickness, adjusted slightly for moisture delta
        val deltaMC = s.initialMoisture - s.targetMoisture
        val baseMonths = s.boardThicknessInches * 12.0
        val moistureFactor = if (deltaMC > 0) (deltaMC / 20.0).coerceIn(0.5, 2.0) else 1.0
        val totalMonths = baseMonths * moistureFactor
        
        _uiState.value = _uiState.value.copy(estAirDryingMonths = totalMonths)
    }

    private fun recalculateSpringback() {
        val s = _uiState.value
        // Bent lamination springback: R_form = R_target * (1 - (1 / N^2))
        // So R_actual (relaxed) will be larger than form. To get R_target, we must build on form with R_form.
        val n = s.plyCount.toDouble()
        val compensationFactor = 1.0 - (1.0 / (n * n))
        val adjustedRadius = s.targetRadius * compensationFactor
        
        _uiState.value = _uiState.value.copy(computedSpringbackRadius = adjustedRadius)
    }

    fun logSpeciesCalculation(activityTitle: String, details: String) {
        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "WOODWORKING_STUDIO",
                title = activityTitle,
                summary = details,
                value = 0.0
            )
        }
    }
}
