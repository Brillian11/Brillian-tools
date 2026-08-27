package com.example.ui.screens.safety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ChemicalCategory(val label: String) {
    ALL("All Chemicals"),
    SOLVENTS_THINNERS("Solvents & Thinners"),
    ADHESIVES_GLUES("Adhesives & Glues"),
    EPOXIES_RESINS("Epoxies & Resins"),
    PAINTS_FINISHES("Paints & Finishes"),
    ACIDS_ETCHANTS("Acids & Etchants"),
    CONCRETE_MASONRY("Concrete & Masonry"),
    LUBRICANTS_AEROSOLS("Lubricants & Aerosols"),
    CAULKS_SEALANTS("Caulks & Sealants")
}

enum class GhsHazard(val label: String, val description: String, val iconColorHex: Long) {
    FLAME("Flammable", "Catches fire easily from heat, sparks, or flames", 0xFFDC2626),
    HEALTH_HAZARD("Health Hazard", "Carcinogenicity, respiratory sensitizer, organ toxicity", 0xFF7E22CE),
    CORROSIVE("Corrosive", "Causes severe skin burns and serious eye damage", 0xFFD97706),
    TOXICITY("Acute Toxicity", "Fatal or toxic if swallowed, inhaled, or absorbed", 0xFF991B1B),
    EXCLAMATION("Harmful / Irritant", "Skin/eye irritant, skin sensitization, narcotic effects", 0xFFCA8A04),
    ENVIRONMENT("Aquatic Toxicity", "Toxic to aquatic life with long lasting effects", 0xFF059669),
    GAS_CYLINDER("Compressed Gas", "Contains gas under pressure; may explode if heated", 0xFF2563EB)
}

data class ChemicalFirstAid(
    val inhalation: String,
    val eyeContact: String,
    val skinContact: String,
    val ingestion: String
)

data class ChemicalPpe(
    val eye: String,
    val respiratory: String,
    val gloves: String,
    val clothing: String
)

data class ChemicalProfile(
    val id: String,
    val name: String,
    val commonNames: String,
    val casNumber: String,
    val category: ChemicalCategory,
    val ghsHazards: List<GhsHazard>,
    val signalWord: String, // DANGER or WARNING
    val nfpaHealth: Int,
    val nfpaFlammability: Int,
    val nfpaInstability: Int,
    val nfpaSpecial: String? = null,
    val flashPoint: String,
    val boilingPoint: String,
    val vaporDensity: String,
    val vocContent: String,
    val criticalHazards: List<String>,
    val ppe: ChemicalPpe,
    val firstAid: ChemicalFirstAid,
    val fireSpill: String,
    val storageDisposal: String
)

data class IncompatibilityRule(
    val chemA: String,
    val chemB: String,
    val dangerousResult: String,
    val explanation: String
)

data class ChemicalMsdsUiState(
    val searchQuery: String = "",
    val selectedCategory: ChemicalCategory = ChemicalCategory.ALL,
    val selectedChemicalId: String = "chem_acetone",
    val bookmarkedIds: Set<String> = setOf("chem_acetone", "chem_blo", "chem_muriatic"),
    val filteredChemicals: List<ChemicalProfile> = emptyList(),

    // Incompatibility Calculator
    val incompChemA: String = "chem_muriatic",
    val incompChemB: String = "chem_bleach",
    val incompatibilityWarning: IncompatibilityRule? = null
)

class ChemicalMsdsViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChemicalMsdsUiState())
    val uiState: StateFlow<ChemicalMsdsUiState> = _uiState.asStateFlow()

    init {
        filterChemicals()
        checkIncompatibility()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterChemicals()
    }

    fun setCategory(category: ChemicalCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        filterChemicals()
    }

    fun selectChemical(id: String) {
        _uiState.value = _uiState.value.copy(selectedChemicalId = id)
    }

    fun toggleBookmark(id: String) {
        val current = _uiState.value.bookmarkedIds.toMutableSet()
        if (current.contains(id)) current.remove(id) else current.add(id)
        _uiState.value = _uiState.value.copy(bookmarkedIds = current)
    }

    fun setIncompatibilityPair(chemA: String? = null, chemB: String? = null) {
        _uiState.value = _uiState.value.copy(
            incompChemA = chemA ?: _uiState.value.incompChemA,
            incompChemB = chemB ?: _uiState.value.incompChemB
        )
        checkIncompatibility()
    }

    fun getSelectedChemical(): ChemicalProfile {
        return ALL_CHEMICALS.find { it.id == _uiState.value.selectedChemicalId } ?: ALL_CHEMICALS.first()
    }

    private fun filterChemicals() {
        val q = _uiState.value.searchQuery.trim().lowercase()
        val cat = _uiState.value.selectedCategory

        val filtered = ALL_CHEMICALS.filter { chem ->
            val matchCat = (cat == ChemicalCategory.ALL || chem.category == cat)
            val matchQuery = q.isEmpty() ||
                chem.name.lowercase().contains(q) ||
                chem.commonNames.lowercase().contains(q) ||
                chem.casNumber.lowercase().contains(q) ||
                chem.criticalHazards.any { it.lowercase().contains(q) }
            matchCat && matchQuery
        }

        _uiState.value = _uiState.value.copy(filteredChemicals = filtered)
    }

    private fun checkIncompatibility() {
        val a = _uiState.value.incompChemA
        val b = _uiState.value.incompChemB

        val match = INCOMPATIBILITY_RULES.find { rule ->
            (rule.chemA == a && rule.chemB == b) || (rule.chemA == b && rule.chemB == a)
        }

        _uiState.value = _uiState.value.copy(incompatibilityWarning = match)
    }

    fun logSafetyReference() {
        val chem = getSelectedChemical()
        val summary = "SDS Lookup: ${chem.name} (CAS ${chem.casNumber}) - Signal: ${chem.signalWord}, NFPA: [H${chem.nfpaHealth}, F${chem.nfpaFlammability}, R${chem.nfpaInstability}]. PPE: ${chem.ppe.respiratory}, ${chem.ppe.gloves}"

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "SAFETY",
                title = "SDS: ${chem.name}",
                summary = summary,
                value = chem.nfpaHealth.toDouble()
            )
        }
    }

    companion object {
        val ALL_CHEMICALS = listOf(
            ChemicalProfile(
                id = "chem_acetone",
                name = "Acetone",
                commonNames = "2-Propanone, Dimethyl Ketone, Lacquer Thinner Base",
                casNumber = "67-64-1",
                category = ChemicalCategory.SOLVENTS_THINNERS,
                ghsHazards = listOf(GhsHazard.FLAME, GhsHazard.EXCLAMATION),
                signalWord = "DANGER",
                nfpaHealth = 1,
                nfpaFlammability = 3,
                nfpaInstability = 0,
                flashPoint = "-20°C (-4°F) Closed Cup",
                boilingPoint = "56.1°C (133°F)",
                vaporDensity = "2.0 (Heavier than air - flows along floor)",
                vocContent = "Exempt VOC in USA (High volatility)",
                criticalHazards = listOf(
                    "Extremely flammable liquid and vapor with low flash point",
                    "Breaks through standard disposable Nitrile gloves in under 2 minutes (Requires Butyl rubber)",
                    "Vapors may travel along ground to distant ignition sources and flash back"
                ),
                ppe = ChemicalPpe(
                    eye = "Chemical splash goggles (ANSI Z87.1 D3)",
                    respiratory = "Organic vapor cartridge (NIOSH OV) if ventilation < 250 ppm",
                    gloves = "Butyl rubber or Silver Shield (Do NOT use thin Nitrile for immersion)",
                    clothing = "Cotton antistatic clothing; remove synthetic fleece"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Move victim to fresh air. If dizzy or drowsy, provide rest. Seek medical attention if symptoms persist.",
                    eyeContact = "Rinse cautiously with water for at least 15 minutes holding eyelids open. Remove contact lenses.",
                    skinContact = "Wash skin with soap and copious water. Apply skin moisturizer (causes severe defatting/dryness).",
                    ingestion = "Rinse mouth. Do NOT induce vomiting due to aspiration risk. Call Poison Control immediately."
                ),
                fireSpill = "Use carbon dioxide (CO2), dry chemical, or alcohol-resistant foam. Dilute spills with water spray or absorb with non-combustible vermiculite/sand.",
                storageDisposal = "Store in dedicated flammable storage cabinet below 30°C. Keep containers tightly closed. Ground containers when transferring."
            ),

            ChemicalProfile(
                id = "chem_blo",
                name = "Boiled Linseed Oil (BLO)",
                commonNames = "Linseed Oil with Metallic Driers, Flaxseed Oil Finish",
                casNumber = "68553-15-1",
                category = ChemicalCategory.PAINTS_FINISHES,
                ghsHazards = listOf(GhsHazard.FLAME, GhsHazard.EXCLAMATION),
                signalWord = "DANGER",
                nfpaHealth = 1,
                nfpaFlammability = 2,
                nfpaInstability = 2,
                flashPoint = "104°C (220°F)",
                boilingPoint = "> 200°C (> 392°F)",
                vaporDensity = "Heavier than air",
                vocContent = "Low (< 50 g/L depending on added thinners)",
                criticalHazards = listOf(
                    "⚠️ SPONTANEOUS COMBUSTION: Oily rags in a pile generate exothermic oxidation heat that ignites without an external flame!",
                    "Contains heavy metal cobalt/manganese drying catalysts",
                    "Skin contact may cause allergic contact dermatitis"
                ),
                ppe = ChemicalPpe(
                    eye = "Safety glasses with side shields (ANSI Z87.1)",
                    respiratory = "Organic vapor / particulate mask if heated or sprayed",
                    gloves = "Nitrile or Neoprene chemical resistant gloves",
                    clothing = "Standard work apron; dispose of soaked clothing"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Move to fresh air. Supply oxygen if breathing becomes labored.",
                    eyeContact = "Flush eyes with water for at least 15 minutes. Consult a physician.",
                    skinContact = "Wash thoroughly with soap and warm water. Avoid mineral spirits directly on skin.",
                    ingestion = "Do NOT induce vomiting. Seek immediate medical aid."
                ),
                fireSpill = "Class B / Dry Chemical / CO2 / Foam. Do NOT use direct high-pressure water jet.",
                storageDisposal = "CRITICAL: Immediately submerge all application rags and towels in a water-filled metal bucket or lay flat in a single layer outdoors to cure before disposal."
            ),

            ChemicalProfile(
                id = "chem_muriatic",
                name = "Muriatic Acid (Hydrochloric Acid 31.45%)",
                commonNames = "Spirits of Salt, Hydrogen Chloride Aqueous, Masonry Acid",
                casNumber = "7647-01-0",
                category = ChemicalCategory.ACIDS_ETCHANTS,
                ghsHazards = listOf(GhsHazard.CORROSIVE, GhsHazard.TOXICITY, GhsHazard.EXCLAMATION),
                signalWord = "DANGER",
                nfpaHealth = 3,
                nfpaFlammability = 0,
                nfpaInstability = 1,
                nfpaSpecial = "COR",
                flashPoint = "Non-combustible",
                boilingPoint = "108.5°C (227°F)",
                vaporDensity = "1.26 (Corrosive choking acid vapor sinks)",
                vocContent = "0 g/L (Inorganic Acid)",
                criticalHazards = listOf(
                    "Causes irreversible chemical eye burns and severe skin necrosis",
                    "Acid vapors corrode nearby metal tools, electrical panels and ductwork within hours",
                    "NEVER add water to concentrated acid (Exothermic boiling spatter). ALWAYS ADD ACID TO WATER (AAA rule)"
                ),
                ppe = ChemicalPpe(
                    eye = "Full face shield combined with indirect-vent chemical splash goggles",
                    respiratory = "Acid gas cartridge respirator (NIOSH AG / Yellow/Magenta filter)",
                    gloves = "Heavy Neoprene, Butyl, or PVC gauntlet gloves (min 14 mil)",
                    clothing = "Acid-resistant rubber apron and rubber safety boots"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Evacuate immediately to fresh air. Administer oxygen if trained. Immediate hospital transport.",
                    eyeContact = "Flush eyes with saline or clean water for a minimum of 20-30 minutes. Seek emergency ophthalmology care.",
                    skinContact = "Drench skin immediately with water for 15+ minutes under emergency shower. Remove all contaminated clothing.",
                    ingestion = "Rinse mouth with water. Drink 1-2 glasses of water or milk. NEVER induce vomiting or give baking soda (gas rupture)."
                ),
                fireSpill = "Neutralize spill with sodium bicarbonate (baking soda) or slaked lime until bubbling stops. Absorb residue with clay kitty litter.",
                storageDisposal = "Store in corrosion-proof polyethylene container in a cool, ventilated area away from metals, chlorine bleach, and bases."
            ),

            ChemicalProfile(
                id = "chem_epoxy_part_a",
                name = "Epoxy Resin (Bisphenol-A Diglycidyl Ether)",
                commonNames = "Epoxy Base (Part A), DGEBA Resin, Casting Resin Base",
                casNumber = "25068-38-6",
                category = ChemicalCategory.EPOXIES_RESINS,
                ghsHazards = listOf(GhsHazard.EXCLAMATION, GhsHazard.ENVIRONMENT),
                signalWord = "WARNING",
                nfpaHealth = 2,
                nfpaFlammability = 1,
                nfpaInstability = 0,
                flashPoint = "> 200°C (> 392°F)",
                boilingPoint = "> 260°C (> 500°F)",
                vaporDensity = "Heavier than air",
                vocContent = "< 5 g/L (100% Solids)",
                criticalHazards = listOf(
                    "Potent skin sensitizer: Repeated contact can trigger lifelong irreversible epoxy allergies",
                    "Causes serious eye irritation and moderate skin redness",
                    "Toxic to aquatic life with long-term effects; do not drain to sewers"
                ),
                ppe = ChemicalPpe(
                    eye = "Chemical splash goggles or safety glasses with side shields",
                    respiratory = "Organic vapor mask recommended when heating or using in confined room",
                    gloves = "Heavy Nitrile gloves (8-mil minimum) or double-glove",
                    clothing = "Long sleeves and disposable apron"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Move to fresh air. If coughing or throat irritation develops, rest and consult doctor.",
                    eyeContact = "Flush thoroughly with gentle stream of water for 15 minutes. Remove contact lenses.",
                    skinContact = "Wash immediately with waterless hand cleaner followed by soap and water. Never clean skin with acetone!",
                    ingestion = "Rinse mouth. Do NOT induce vomiting without medical guidance."
                ),
                fireSpill = "Use foam, CO2, or dry chemical. Scrape up resin using cardboard or scrapers; absorb remainder with sawdust/clay.",
                storageDisposal = "Store tightly closed between 15°C - 30°C. Fully cure mixed resin before disposing as standard solid trash."
            ),

            ChemicalProfile(
                id = "chem_epoxy_hardener",
                name = "Epoxy Hardener / Curing Agent (Polyamines)",
                commonNames = "Epoxy Part B, Amine Activator, Cycloaliphatic Amine Hardener",
                casNumber = "2855-13-2",
                category = ChemicalCategory.EPOXIES_RESINS,
                ghsHazards = listOf(GhsHazard.CORROSIVE, GhsHazard.TOXICITY, GhsHazard.EXCLAMATION),
                signalWord = "DANGER",
                nfpaHealth = 3,
                nfpaFlammability = 1,
                nfpaInstability = 1,
                flashPoint = "110°C (230°F)",
                boilingPoint = "247°C (476°F)",
                vaporDensity = "5.9 (Heavy pungent amine vapors)",
                vocContent = "< 10 g/L",
                criticalHazards = listOf(
                    "Exothermic Runaway: Mixing large batches (>500ml) in deep cups can overheat (>200°C), melt plastic cups and release toxic smoke!",
                    "Corrosive to skin, causing chemical burns and severe sensitization",
                    "Strong fishy/ammonia odor that irritates respiratory tract"
                ),
                ppe = ChemicalPpe(
                    eye = "Chemical splash goggles (ANSI Z87.1 D3)",
                    respiratory = "Half-mask respirator with Organic Vapor/Amine cartridges (NIOSH OV/AM)",
                    gloves = "Nitrile (8+ mil) or Butyl rubber (Change immediately if contaminated)",
                    clothing = "Impervious protective lab coat or Tyvek sleeves"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Remove to fresh air immediately. Keep warm and quiet. Seek immediate medical aid if wheezing.",
                    eyeContact = "Hold eye open and flush continuously for 20 minutes with clean water. Seek immediate medical attention.",
                    skinContact = "Immediately flush skin with plenty of soap and water for 15 minutes. Remove contaminated shoes/clothes.",
                    ingestion = "Drink milk or water. Do NOT induce vomiting. Contact poison center immediately."
                ),
                fireSpill = "Class B / CO2 / Alcohol-resistant foam. Pour in shallow tray to dissipate heat if batch starts smoking.",
                storageDisposal = "Store in cool, dry place. Keep separated from strong oxidizing acids and isocyanates."
            ),

            ChemicalProfile(
                id = "chem_mineral_spirits",
                name = "Mineral Spirits (Stoddard Solvent)",
                commonNames = "Paint Thinner, White Spirits, Odorless Mineral Spirits (OMS)",
                casNumber = "64742-88-7",
                category = ChemicalCategory.SOLVENTS_THINNERS,
                ghsHazards = listOf(GhsHazard.FLAME, GhsHazard.HEALTH_HAZARD, GhsHazard.EXCLAMATION),
                signalWord = "DANGER",
                nfpaHealth = 1,
                nfpaFlammability = 2,
                nfpaInstability = 0,
                flashPoint = "40°C - 43°C (104°F - 110°F)",
                boilingPoint = "150°C - 200°C (302°F - 392°F)",
                vaporDensity = "4.9 (Much heavier than air)",
                vocContent = "100% (780 - 800 g/L)",
                criticalHazards = listOf(
                    "Combustible liquid with heavy lingering vapors",
                    "Aspiration hazard: Inhaling solvent liquid into lungs during vomiting causes chemical pneumonitis",
                    "Chronic overexposure affects central nervous system"
                ),
                ppe = ChemicalPpe(
                    eye = "Safety glasses with side shields",
                    respiratory = "Organic vapor respirator (NIOSH OV) for poor ventilation or prolonged brushing",
                    gloves = "Nitrile, Neoprene, or Polyvinyl Alcohol (PVA)",
                    clothing = "Standard shop workwear"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Move exposed person to fresh air. If dizzy, lie down in shade.",
                    eyeContact = "Flush thoroughly with water for 15 minutes.",
                    skinContact = "Wash with mild soap and warm water. Apply hand lotion to restore lipid layer.",
                    ingestion = "DO NOT INDUCE VOMITING! Aspiration of petroleum distillate into lungs is life-threatening. Emergency 911."
                ),
                fireSpill = "Carbon dioxide, dry chemical powder, or regular foam. Contain spill with sand or dirt.",
                storageDisposal = "Store in cool, well-ventilated area away from pilot lights. Recycle clean solvent by settling sludge."
            ),

            ChemicalProfile(
                id = "chem_denatured_alcohol",
                name = "Denatured Alcohol (Ethanol + Methanol)",
                commonNames = "Methylated Spirits, Shellac Thinner, Alcohol Stove Fuel",
                casNumber = "64-17-5 / 67-56-1",
                category = ChemicalCategory.SOLVENTS_THINNERS,
                ghsHazards = listOf(GhsHazard.FLAME, GhsHazard.HEALTH_HAZARD, GhsHazard.TOXICITY),
                signalWord = "DANGER",
                nfpaHealth = 2,
                nfpaFlammability = 3,
                nfpaInstability = 0,
                flashPoint = "13°C (55°F) Closed Cup",
                boilingPoint = "78°C (172°F)",
                vaporDensity = "1.59 (Heavier than air)",
                vocContent = "100% (790 g/L)",
                criticalHazards = listOf(
                    "Highly flammable with near-invisible blue/pale flame in daylight",
                    "Methanol additive is toxic if absorbed through skin or swallowed (can cause blindness and organ failure)",
                    "Rapid evaporation rate"
                ),
                ppe = ChemicalPpe(
                    eye = "Chemical splash goggles",
                    respiratory = "Organic vapor respirator (NIOSH OV) in enclosed spaces",
                    gloves = "Butyl rubber or Nitrile (Change promptly if saturated)",
                    clothing = "Flame retardant cotton shop clothing"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Move victim to fresh air. If unconscious, place in recovery position and call EMS.",
                    eyeContact = "Rinse with plenty of water for 15 minutes. Seek medical evaluation.",
                    skinContact = "Wash immediately with soap and water.",
                    ingestion = "POISONOUS. Call emergency services and poison control immediately. Do not induce vomiting without instructions."
                ),
                fireSpill = "Alcohol-resistant foam, CO2, or dry chemical. Flammable vapor is invisible.",
                storageDisposal = "Keep away from open flames, electrical sparks, and hot surfaces. Store in cool flammables cabinet."
            ),

            ChemicalProfile(
                id = "chem_contact_cement",
                name = "Contact Cement (Solvent-Based Polychloroprene)",
                commonNames = "Laminate Glue, Formica Adhesive, Rubber Contact Adhesive",
                casNumber = "Mixture (Toluene 108-88-3, Hexane 110-54-3, Acetone)",
                category = ChemicalCategory.ADHESIVES_GLUES,
                ghsHazards = listOf(GhsHazard.FLAME, GhsHazard.HEALTH_HAZARD, GhsHazard.EXCLAMATION),
                signalWord = "DANGER",
                nfpaHealth = 2,
                nfpaFlammability = 3,
                nfpaInstability = 0,
                flashPoint = "-20°C (-4°F)",
                boilingPoint = "56°C (133°F)",
                vaporDensity = "3.1 (Extremely heavy solvent vapors sink to floor)",
                vocContent = "High (550 - 650 g/L) unless labeled Water-Based",
                criticalHazards = listOf(
                    "Extremely volatile solvent vapors accumulate rapidly in rooms and ignite from water heater pilot lights",
                    "Toluene and Hexane target nervous system, kidneys and reproductive health",
                    "Instant high-strength grip on contact; cannot be repositioned"
                ),
                ppe = ChemicalPpe(
                    eye = "Chemical safety goggles (ANSI Z87.1)",
                    respiratory = "Dual-cartridge Organic Vapor Respirator (NIOSH OV) MANDATORY",
                    gloves = "PVA (Polyvinyl Alcohol) or Heavy Nitrile",
                    clothing = "Long-sleeved shirt, no open flames or smoking within 50 feet"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Move patient to fresh outdoor air immediately. Provide oxygen if breathing is difficult.",
                    eyeContact = "Flush thoroughly with clean water for 15 minutes.",
                    skinContact = "Allow adhesive to dry and peel off gently, or remove with vegetable oil/soap. Avoid heavy solvent washing on raw skin.",
                    ingestion = "Never induce vomiting due to petroleum hydrocarbon aspiration. Transport to emergency hospital."
                ),
                fireSpill = "Extinguish all pilot lights and open flames before opening can. Use Class B / Dry chemical / CO2.",
                storageDisposal = "Store below 38°C (100°F) in a well-ventilated flammable cabinet. Dispose of dried glue residue as solid waste."
            ),

            ChemicalProfile(
                id = "chem_ca_glue",
                name = "Cyanoacrylate Adhesive (CA / Super Glue)",
                commonNames = "Instant Glue, Krazy Glue, Fast-Set Acrylic Adhesive",
                casNumber = "7085-85-0 (Ethyl 2-Cyanoacrylate)",
                category = ChemicalCategory.ADHESIVES_GLUES,
                ghsHazards = listOf(GhsHazard.EXCLAMATION),
                signalWord = "WARNING",
                nfpaHealth = 2,
                nfpaFlammability = 2,
                nfpaInstability = 2,
                flashPoint = "85°C (185°F)",
                boilingPoint = "150°C (302°F)",
                vaporDensity = "3.0",
                vocContent = "< 20 g/L",
                criticalHazards = listOf(
                    "Bonds skin and eyelids instantly in seconds",
                    "⚠️ EXOTHERMIC REACTION WITH COTTON/WOOL: Applying CA glue to cotton clothes/gloves creates intense heat, smoke, and skin burns!",
                    "Vapors cause strong eye, nasal and throat stinging"
                ),
                ppe = ChemicalPpe(
                    eye = "Safety glasses (ANSI Z87.1)",
                    respiratory = "Good cross ventilation; avoid leaning directly over curing glue lines",
                    gloves = "Polyethylene or Nitrile gloves (NEVER WEAR COTTON OR WOOL GLOVES)",
                    clothing = "Synthetic/smooth shop clothing"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Move to fresh air. Vapors dissipate quickly.",
                    eyeContact = "Do NOT force eyes open! Flush with warm water and cover with sterile gauze patch. Eye will unseal naturally in 1-3 days.",
                    skinContact = "Do not pull skin apart! Soak in warm soapy water or apply acetone with Q-tip, then gently roll skin apart.",
                    ingestion = "Saliva polymerizes glue almost instantly in mouth, preventing swallowing. Peel off cured plastic film carefully."
                ),
                fireSpill = "Polymerize spill by flooding with excess water, then scrape up hardened plastic. Fire: Water spray, dry chemical, CO2.",
                storageDisposal = "Store tightly capped in cool, dry place (refrigeration extends shelf life, warm to room temp before opening)."
            ),

            ChemicalProfile(
                id = "chem_pvc_primer_cement",
                name = "PVC Primer & Solvent Cement (MEK + THF + Cyclohexanone)",
                commonNames = "Purple Pipe Primer, PVC Glue, CPVC Medium Cement",
                casNumber = "Mixture (109-99-9 THF, 78-93-3 MEK, 108-94-1 Cyclohexanone)",
                category = ChemicalCategory.ADHESIVES_GLUES,
                ghsHazards = listOf(GhsHazard.FLAME, GhsHazard.HEALTH_HAZARD, GhsHazard.EXCLAMATION),
                signalWord = "DANGER",
                nfpaHealth = 2,
                nfpaFlammability = 3,
                nfpaInstability = 1,
                flashPoint = "-14°C (6.8°F)",
                boilingPoint = "66°C (151°F)",
                vaporDensity = "2.5 (Heavy sweet-smelling solvent fumes)",
                vocContent = "< 510 g/L (SCAQMD Rule 1168 Compliant)",
                criticalHazards = listOf(
                    "Highly volatile flash fire hazard in plumbing trenches and crawlspaces",
                    "THF and MEK are suspected carcinogens and central nervous system depressants",
                    "Purple dye permanently stains surfaces and clothes"
                ),
                ppe = ChemicalPpe(
                    eye = "Splash-proof safety goggles (ANSI Z87.1)",
                    respiratory = "NIOSH Organic Vapor mask mandatory when working in trenches or basements",
                    gloves = "PVA or Heavy Nitrile gloves",
                    clothing = "Flame resistant cotton clothing"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Immediately remove worker from trench/confined space to fresh air.",
                    eyeContact = "Flush eyes with clean running water for 15+ minutes.",
                    skinContact = "Wipe off wet cement with dry cloth, wash thoroughly with soap and water.",
                    ingestion = "Do not induce vomiting. Seek immediate medical assistance."
                ),
                fireSpill = "Class B / CO2 / Dry Chemical. Turn off all blowtorches and electrical tools before opening can.",
                storageDisposal = "Store between 4°C - 43°C. Keep lid tightly closed to prevent solvent dry-out."
            ),

            ChemicalProfile(
                id = "chem_portland_cement",
                name = "Portland Cement (Dry Powder & Wet Concrete)",
                commonNames = "Hydraulic Cement, Ready-Mix Concrete Dust, Masonry Mortar",
                casNumber = "65997-15-1 (Contains Hexavalent Chromium & Silica)",
                category = ChemicalCategory.CONCRETE_MASONRY,
                ghsHazards = listOf(GhsHazard.CORROSIVE, GhsHazard.HEALTH_HAZARD, GhsHazard.EXCLAMATION),
                signalWord = "DANGER",
                nfpaHealth = 3,
                nfpaFlammability = 0,
                nfpaInstability = 0,
                flashPoint = "Non-combustible",
                boilingPoint = "> 1000°C",
                vaporDensity = "N/A (Solid Dust)",
                vocContent = "0 g/L",
                criticalHazards = listOf(
                    "Wet cement has an extreme alkaline pH of 12.5 - 13.5 and causes painless, deep third-degree chemical burns inside boots and gloves!",
                    "Dry dust contains Crystalline Silica (quartz), causing Silicosis lung scarring and lung cancer",
                    "Hexavalent Chromium Cr(VI) trace content causes severe allergic contact dermatitis"
                ),
                ppe = ChemicalPpe(
                    eye = "Sealed dust goggles or face shield (ANSI Z87.1)",
                    respiratory = "N95 particulate respirator for light dust; P100 / HEPA half-mask for mixing and cutting",
                    gloves = "Heavy waterproof alkali-resistant Nitrile or Neoprene gloves",
                    clothing = "Waterproof knee pads, rubber boots (tuck pants OUTSIDE boots to prevent mud entry)"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Move to fresh air. Rinse nose and throat with clean water. Seek medical check if coughing persists.",
                    eyeContact = "Flush eyes immediately and continuously with water for at least 20 minutes. Chemical emergency.",
                    skinContact = "Wash skin immediately with cool water and pH-neutral soap. Soak burned areas in diluted white vinegar to neutralize alkali.",
                    ingestion = "Do NOT induce vomiting. Drink large quantities of water. Seek immediate medical attention."
                ),
                fireSpill = "Dry sweep with HEPA vacuum or damp mop to prevent airborne respirable silica dust. Non-flammable.",
                storageDisposal = "Keep in dry, sealed bags off damp ground. Dispose of hardened concrete as non-hazardous aggregate rubble."
            ),

            ChemicalProfile(
                id = "chem_xylene",
                name = "Xylene (Dimethylbenzene)",
                commonNames = "Xylol, Concrete Sealer Thinner, Alkyd Solvent",
                casNumber = "1330-20-7",
                category = ChemicalCategory.SOLVENTS_THINNERS,
                ghsHazards = listOf(GhsHazard.FLAME, GhsHazard.HEALTH_HAZARD, GhsHazard.EXCLAMATION),
                signalWord = "DANGER",
                nfpaHealth = 2,
                nfpaFlammability = 3,
                nfpaInstability = 0,
                flashPoint = "27°C (81°F) Closed Cup",
                boilingPoint = "138°C (280°F)",
                vaporDensity = "3.7 (Heavy sweet aromatic hydrocarbon vapor)",
                vocContent = "100% (860 g/L)",
                criticalHazards = listOf(
                    "Flammable aromatic solvent with lingering fumes",
                    "Absorbed through skin and causes dizziness, headache, and memory loss with acute exposure",
                    "Standard for re-emulsifying and thinning acrylic decorative concrete sealers"
                ),
                ppe = ChemicalPpe(
                    eye = "Chemical splash goggles (ANSI Z87.1)",
                    respiratory = "Organic vapor respirator (NIOSH OV) with active ventilation",
                    gloves = "Viton or Polyvinyl Alcohol (PVA) gloves (Nitrile provides short breakthrough time)",
                    clothing = "Antistatic coveralls"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Move to fresh air. If breathing is shallow, administer artificial respiration and transport to ER.",
                    eyeContact = "Flush thoroughly with clean water for 15 minutes.",
                    skinContact = "Wash with soap and water. Remove contaminated clothing immediately.",
                    ingestion = "DO NOT induce vomiting. Call emergency medical team immediately."
                ),
                fireSpill = "Foam, dry chemical, or carbon dioxide. Avoid washing into drains.",
                storageDisposal = "Store in approved safety cans in an explosion-proof storage room."
            ),

            ChemicalProfile(
                id = "chem_spray_polyurethane",
                name = "Polyurethane Spray Foam (MDI / Polymeric Isocyanates)",
                commonNames = "Expanding Foam, Great Stuff, Two-Component Closed Cell Foam",
                casNumber = "9016-87-9 (Methylene Diphenyl Diisocyanate)",
                category = ChemicalCategory.CAULKS_SEALANTS,
                ghsHazards = listOf(GhsHazard.HEALTH_HAZARD, GhsHazard.EXCLAMATION, GhsHazard.GAS_CYLINDER),
                signalWord = "DANGER",
                nfpaHealth = 3,
                nfpaFlammability = 2,
                nfpaInstability = 1,
                flashPoint = "> 110°C (> 230°F)",
                boilingPoint = "> 200°C",
                vaporDensity = "8.5 (Heavy isocyanate aerosol mist)",
                vocContent = "< 50 g/L once fully cured",
                criticalHazards = listOf(
                    "Isocyanates are the #1 cause of occupational asthma: Even microscopic inhalation can trigger lifelong asthma attacks",
                    "Aerosol can under pressure: May burst if heated above 50°C (122°F)",
                    "Extremely tenacious adhesion to skin and eyes; cures rock-hard within 15 minutes"
                ),
                ppe = ChemicalPpe(
                    eye = "Full wrap-around safety goggles or full facepiece",
                    respiratory = "Half-mask with Organic Vapor + P100 particulate filters (Supplied-Air for commercial 2-part spraying)",
                    gloves = "Heavy Nitrile or Neoprene gloves",
                    clothing = "Disposable Tyvek suit with hood"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Move to fresh air immediately. If asthmatic wheezing occurs, administer inhaler if prescribed and call 911.",
                    eyeContact = "Flush with water for 15 minutes. Cured foam particles must be removed by an eye specialist.",
                    skinContact = "Uncured foam: Wipe off with dry cloth, wash with acetone or mineral spirits quickly. Cured foam: Must wear off naturally over days.",
                    ingestion = "Drink water. Cured foam is biologically inert; uncured foam may cause gastrointestinal blockage. Seek medical advice."
                ),
                fireSpill = "Water fog, foam, CO2, dry powder. Scrape uncured foam with acetone; let cure and slice off with knife.",
                storageDisposal = "Store upright between 15°C - 30°C. Never pierce, burn or incinerate aerosol cans."
            ),

            ChemicalProfile(
                id = "chem_penetrating_oil",
                name = "Penetrating Lubricant & Water Displacer (WD-40 / Aerosol)",
                commonNames = "Penetrating Oil, Multi-Use Spray, Rust Release Aerosol",
                casNumber = "Mixture (Aliphatic Hydrocarbons, Petroleum Base, CO2 Propellant)",
                category = ChemicalCategory.LUBRICANTS_AEROSOLS,
                ghsHazards = listOf(GhsHazard.FLAME, GhsHazard.GAS_CYLINDER, GhsHazard.EXCLAMATION),
                signalWord = "DANGER",
                nfpaHealth = 1,
                nfpaFlammability = 4,
                nfpaInstability = 0,
                flashPoint = "43°C (109°F) Tag Closed Cup (Propellant < 0°C)",
                boilingPoint = "149°C - 193°C (300°F - 380°F)",
                vaporDensity = "> 1.0 (Heavier than air)",
                vocContent = "24.5% VOC (US Compliant Formulation)",
                criticalHazards = listOf(
                    "Extremely flammable aerosol: Do NOT spray onto hot manifolds, live circuit breakers or welding sparks",
                    "Pressurized container: Burst hazard if exposed to sunlight or heat > 50°C",
                    "Excessive inhalation in enclosed spaces causes lightheadedness and nausea"
                ),
                ppe = ChemicalPpe(
                    eye = "Safety glasses with side shields (ANSI Z87.1)",
                    respiratory = "Adequate room ventilation (exhaust fan)",
                    gloves = "Nitrile gloves for prolonged contact",
                    clothing = "Standard shop workwear"
                ),
                firstAid = ChemicalFirstAid(
                    inhalation = "Move to fresh air. Provide oxygen if dizziness occurs.",
                    eyeContact = "Rinse with plenty of water for 15 minutes.",
                    skinContact = "Wash thoroughly with soap and water.",
                    ingestion = "DO NOT induce vomiting due to petroleum aspiration hazard. Seek immediate medical aid."
                ),
                fireSpill = "Class B / CO2 / Dry Chemical / Foam. Protect from explosive cylinder shrapnel in hot fires.",
                storageDisposal = "Store in cool dry area away from direct sunlight. Do not puncture or incinerate."
            )
        )

        val INCOMPATIBILITY_RULES = listOf(
            IncompatibilityRule(
                "chem_muriatic",
                "chem_bleach",
                "☠️ DEADLY CHLORINE GAS (Cl2)",
                "Mixing Hydrochloric/Muriatic Acid with Sodium Hypochlorite (Bleach) releases dense, green toxic Chlorine gas which causes severe pulmonary edema, chemical suffocation and death in enclosed spaces."
            ),
            IncompatibilityRule(
                "chem_blo",
                "chem_epoxy_hardener",
                "🔥 RAPID EXOTHERMIC RUNAWAY",
                "Combining drying oils with amine catalysts accelerates polymerization and oxidative heating, creating extreme fire and smoke hazards."
            ),
            IncompatibilityRule(
                "chem_muriatic",
                "chem_epoxy_hardener",
                "💥 VIOLENT ACID-BASE NEUTRALIZATION",
                "Amine hardeners are strong organic bases. Direct contact with concentrated acid generates boiling spatter and corrosive aerosols."
            ),
            IncompatibilityRule(
                "chem_acetone",
                "chem_muriatic",
                "⚠️ TOXIC CHLORINATED SOLVENT HAZARD",
                "Mixing strong acids with ketones can trigger condensation reactions and release toxic vapors."
            )
        )
    }
}
