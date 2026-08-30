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

    // --- Tab 8: Color Recommendation & Combination Score ---
    val selectedCozyVibe = MutableStateFlow("Warm Hygge") // "Warm Hygge", "Rustic Cabin", "Classic Elegance", "Coastal Calm", "Forest Retreat", "Modern Industrial", "High Gloss Luxury"
    val selectedCommercialBrand = MutableStateFlow("Sherwin-Williams") // "Sherwin-Williams", "Benjamin Moore", "Jotun", "Nippon Paint", "Duco / Danagloss", "Propan", "Avian Brands", "Dulux"

    // --- Tab 9: Laminates, HPL & Architectural Interior Films ---
    val laminateMaterialCategory = MutableStateFlow("HPL (High Pressure Laminate - Taco Brand)") 
    val laminateTexturePattern = MutableStateFlow("Taco Woodgrain Natural Oak")
    val laminateSurfaceLength = MutableStateFlow("2400") // mm or in
    val laminateSurfaceWidth = MutableStateFlow("600")  // mm or in
    val laminateQuantity = MutableStateFlow("4")
    val laminateWastePercent = MutableStateFlow("15") // %
    val edgeBandingLength = MutableStateFlow("18.0") // meters or feet
    val edgeBandingWidth = MutableStateFlow("22") // mm
    val edgeBandingThickness = MutableStateFlow("1.0") // mm
    val adhesiveGlueType = MutableStateFlow("Contact Cement / Yellow Glue (Fox / Aibon)") // "Contact Cement / Yellow Glue (Fox / Aibon)", "Water-based Contact Adhesive", "Hot Melt EVA Glue (Edgebander)", "3M Primer 94 (Kertasive/Vinyl)"

    data class ColorRecommendation(
        val name: String,
        val baseName: String,
        val baseHex: String,
        val accentName: String,
        val accentHex: String,
        val score: Int,
        val brand: String,
        val category: String,
        val description: String
    )

    val commercialPalettes = listOf(
        ColorRecommendation(
            name = "Warm Hug Cozy",
            baseName = "Alabaster (SW 7008)",
            baseHex = "#F2F0EB",
            accentName = "Urbane Bronze (SW 7048)",
            accentHex = "#534F4A",
            score = 98,
            brand = "Sherwin-Williams",
            category = "Warm Hygge",
            description = "A comforting combination of creamy Alabaster and deep Urbane Bronze, generating maximum cozy warmth with high contrast."
        ),
        ColorRecommendation(
            name = "Modern Hearth",
            baseName = "Repose Gray (SW 7015)",
            baseHex = "#D5D2C9",
            accentName = "Naval Navy (SW 6244)",
            accentHex = "#2F3E4E",
            score = 93,
            brand = "Sherwin-Williams",
            category = "Classic Elegance",
            description = "A sophisticated balance of warm neutral gray with rich executive navy, offering serene, timeless comfort."
        ),
        ColorRecommendation(
            name = "Classic Soft Dove",
            baseName = "White Dove (OC-17)",
            baseHex = "#F2F3EC",
            accentName = "Hale Navy (HC-154)",
            accentHex = "#2B3541",
            score = 96,
            brand = "Benjamin Moore",
            category = "Warm Hygge",
            description = "Soft, warm, light cream paired with iconic Hale Navy. Creates a classic, deeply comforting living room feel."
        ),
        ColorRecommendation(
            name = "Earth & Stone Sanctuary",
            baseName = "Revere Pewter (HC-172)",
            baseHex = "#CCC7B9",
            accentName = "Chelsea Gray (HC-168)",
            accentHex = "#7D7A73",
            score = 92,
            brand = "Benjamin Moore",
            category = "Rustic Cabin",
            description = "Warm earth pewter layered with deep cobblestone gray. Brings natural textures and rustic safety inside."
        ),
        ColorRecommendation(
            name = "Nordic Clean Calm",
            baseName = "Skylight (1624)",
            baseHex = "#EDE9E0",
            accentName = "Deco Blue (4477)",
            accentHex = "#26384C",
            score = 97,
            brand = "Jotun",
            category = "Coastal Calm",
            description = "Crisp Norwegian morning white married to Nordic Deco Blue. Light-reflective and exceptionally calming."
        ),
        ColorRecommendation(
            name = "Desert Velvet Wood",
            baseName = "Space Grey (10678)",
            baseHex = "#CEBEA5",
            accentName = "Exotic Beige (1141)",
            accentHex = "#9E886A",
            score = 94,
            brand = "Jotun",
            category = "Rustic Cabin",
            description = "Slightly sun-kissed desert gray paired with deep earthy exotic beige. Warm, safe, and organic."
        ),
        ColorRecommendation(
            name = "Sweet Blossom Jasmine",
            baseName = "Orchid White (OW1001)",
            baseHex = "#F9F7F1",
            accentName = "Sandalwood Wood (N1874)",
            accentHex = "#8C7A63",
            score = 91,
            brand = "Nippon Paint",
            category = "Warm Hygge",
            description = "Delicate orchid cream matched with deep sandalwood brown. Exceptionally soft and welcoming."
        ),
        ColorRecommendation(
            name = "Zen Bamboo Retreat",
            baseName = "Lily White (OW1007)",
            baseHex = "#F0F3EC",
            accentName = "Sienna Forest (N1815)",
            accentHex = "#6A5F4E",
            score = 95,
            brand = "Nippon Paint",
            category = "Forest Retreat",
            description = "Light, natural forest lily cream paired with rich earthy sienna wood. Encourages relaxation and mindfulness."
        ),
        // --- DUCO & HIGH GLOSS LACQUER ---
        ColorRecommendation(
            name = "Piano Super Gloss Duco",
            baseName = "Danagloss Pure Super White Duco",
            baseHex = "#FFFFFF",
            accentName = "Danagloss Jet Midnight Black Duco",
            accentHex = "#121212",
            score = 99,
            brand = "Duco / Danagloss",
            category = "High Gloss Luxury",
            description = "High mirror-sheen automotive & cabinetry 2K Polyurethane Duco finish. Mirror-like gloss with supreme surface hardness."
        ),
        ColorRecommendation(
            name = "Executive Satin Anthracite",
            baseName = "Belkote 2K Satin Platinum Gray",
            baseHex = "#C4C8CC",
            accentName = "Belkote 2K Deep Carbon Anthracite",
            accentHex = "#282B30",
            score = 96,
            brand = "Duco / Danagloss",
            category = "Modern Industrial",
            description = "Silky satin-matte Duco finish. Smooth tactile feel with excellent scratch, chemical, and solvent resistance."
        ),
        // --- PROPAN WOOD & SPECIALTY ---
        ColorRecommendation(
            name = "Golden Teak Lasur",
            baseName = "Ultran Lasur Clear Matt (EL-501)",
            baseHex = "#D8B276",
            accentName = "Ultran Lasur Teak Brown (EL-504)",
            accentHex = "#7A431D",
            score = 97,
            brand = "Propan",
            category = "Rustic Cabin",
            description = "Deep penetrating wood preservation stain with rich natural grain enhancement and UV barrier protection."
        ),
        ColorRecommendation(
            name = "Minimalist Architectural Matt",
            baseName = "Propan Decosafe Minimalist Off-White",
            baseHex = "#F5F5F0",
            accentName = "Propan PU-91 Duco Emerald Forest",
            accentHex = "#1C3F34",
            score = 94,
            brand = "Propan",
            category = "Forest Retreat",
            description = "Pristine matte interior wall coating complemented by Propan PU-91 high-durability green cabinetry accents."
        ),
        // --- AVIAN BRANDS ---
        ColorRecommendation(
            name = "Gloss Heritage Enamel",
            baseName = "Avitex Super White Interior",
            baseHex = "#FAFAFA",
            accentName = "Avian Gloss Synthetic Royal Marine",
            accentHex = "#1A365D",
            score = 95,
            brand = "Avian Brands",
            category = "Classic Elegance",
            description = "Crisp, odorless wall finish accented by high-gloss synthetic enamel doors and trim with outstanding scrub resistance."
        ),
        // --- DULUX / AKZONOBEL ---
        ColorRecommendation(
            name = "Coastal Breeze Weathershield",
            baseName = "Dulux Pentalite Morning Frost",
            baseHex = "#E8EEF2",
            accentName = "Dulux Weathershield Deep Sapphire",
            accentHex = "#1F3A52",
            score = 96,
            brand = "Dulux",
            category = "Coastal Calm",
            description = "Cool light-reflective blue-gray paired with robust weather-resistant exterior/interior sapphire accents."
        )
    )

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
