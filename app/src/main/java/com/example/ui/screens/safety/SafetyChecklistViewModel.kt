package com.example.ui.screens.safety

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class JsaTemplateType(val title: String, val subtitle: String, val oshaStandard: String) {
    GENERAL_CONSTRUCTION(
        "General Carpentry & Power Tools",
        "Saw blade guards, dust extraction, eye/ear safety & cord inspection",
        "OSHA 1926 Subpart I (Tools)"
    ),
    ELECTRICAL_LOTO(
        "Electrical & Lockout/Tagout (LOTO)",
        "Zero-energy verification, Arc Flash PPE, insulated 1000V tools & breakers",
        "OSHA 1910.147 / 1926 Subpart K"
    ),
    WORKING_AT_HEIGHTS(
        "Working at Heights & Fall Protection",
        "Harnesses, 5000 lb anchor points, guardrails, 4:1 ladder rule & toe boards",
        "OSHA 1926.501 (Fall Protection)"
    ),
    HOT_WORK_WELDING(
        "Hot Work, Torch & Arc Welding",
        "35-ft combustible perimeter, 30-min fire watch, ABC extinguishers & flash shades",
        "OSHA 1926.352 (Fire Prevention)"
    ),
    TRENCHING_EXCAVATION(
        "Trenching, Shoring & Excavation",
        "Call 811 locates, soil class benching/shields >5ft, egress ladders every 25ft",
        "OSHA 1926 Subpart P (Excavations)"
    ),
    CONFINED_SPACE(
        "Confined Space Entry",
        "Multi-gas test (O2/LEL/H2S/CO), entry permits, tripod winch & dedicated attendant",
        "OSHA 1910.146 / 1926 Subpart AA"
    ),
    CHEMICAL_COATING(
        "Chemical Handling & Spray Painting",
        "Organic vapor cartridges, eye wash access, explosion-proof fans & nitrile/butyl",
        "OSHA 1910.1200 (Hazard Comm)"
    ),
    DEMOLITION_STRUCTURAL(
        "Demolition & Structural Removal",
        "Load-bearing shoring, hazardous lead/asbestos survey & drop-zone barriers",
        "OSHA 1926 Subpart T (Demolition)"
    )
}

data class PpeItem(
    val id: String,
    val name: String,
    val standard: String,
    val isRequired: Boolean = true,
    val isChecked: Boolean = false
)

data class SafetyCheckpoint(
    val id: String,
    val category: String,
    val prompt: String,
    val hazardDesc: String,
    val controlMeasure: String,
    val isChecked: Boolean = false,
    val isCritical: Boolean = false
)

enum class RiskSeverity(val label: String, val score: Int) {
    MINOR("Minor / First Aid", 1),
    MODERATE("Moderate / Medical Aid", 2),
    CRITICAL("Critical / Lost Time", 3),
    CATASTROPHIC("Catastrophic / Fatality", 4)
}

enum class RiskLikelihood(val label: String, val score: Int) {
    RARE("Rare / Unlikely", 1),
    POSSIBLE("Possible / Occasional", 2),
    LIKELY("Likely / Frequent", 3),
    ALMOST_CERTAIN("Almost Certain / Continuous", 4)
}

enum class RiskLevel(val label: String, val colorHex: Long) {
    LOW("LOW RISK - Standard Controls", 0xFF15803D),
    MEDIUM("MEDIUM RISK - Engineering Controls Required", 0xFFD97706),
    HIGH("HIGH RISK - Formal Permit & Safety Briefing", 0xFFEA580C),
    EXTREME("EXTREME RISK - STOP WORK / Rethink Plan", 0xFFDC2626)
}

data class SafetyChecklistUiState(
    val selectedTemplate: JsaTemplateType = JsaTemplateType.GENERAL_CONSTRUCTION,
    val jobSiteName: String = "Main Workshop / Project Alpha",
    val supervisorName: String = "Lead Contractor",
    val customNotes: String = "",

    // Risk Matrix Assessment
    val riskSeverity: RiskSeverity = RiskSeverity.MODERATE,
    val riskLikelihood: RiskLikelihood = RiskLikelihood.POSSIBLE,

    // Items
    val ppeList: List<PpeItem> = emptyList(),
    val checkpoints: List<SafetyCheckpoint> = emptyList(),

    // Computed Audit Metrics
    val ppeCompletionPercent: Int = 0,
    val checkpointCompletionPercent: Int = 0,
    val criticalCheckpointsAllPassed: Boolean = false,
    val isReadyToWork: Boolean = false
)

class SafetyChecklistViewModel(
    private val toolLogRepository: ToolLogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafetyChecklistUiState())
    val uiState: StateFlow<SafetyChecklistUiState> = _uiState.asStateFlow()

    init {
        loadTemplate(JsaTemplateType.GENERAL_CONSTRUCTION)
    }

    fun selectTemplate(template: JsaTemplateType) {
        if (_uiState.value.selectedTemplate != template) {
            loadTemplate(template)
        }
    }

    fun setJobSiteName(name: String) {
        _uiState.value = _uiState.value.copy(jobSiteName = name)
    }

    fun setSupervisorName(name: String) {
        _uiState.value = _uiState.value.copy(supervisorName = name)
    }

    fun setCustomNotes(notes: String) {
        _uiState.value = _uiState.value.copy(customNotes = notes)
    }

    fun setRiskAssessment(severity: RiskSeverity? = null, likelihood: RiskLikelihood? = null) {
        _uiState.value = _uiState.value.copy(
            riskSeverity = severity ?: _uiState.value.riskSeverity,
            riskLikelihood = likelihood ?: _uiState.value.riskLikelihood
        )
    }

    fun togglePpe(id: String) {
        val updated = _uiState.value.ppeList.map {
            if (it.id == id) it.copy(isChecked = !it.isChecked) else it
        }
        _uiState.value = _uiState.value.copy(ppeList = updated)
        recalculateMetrics()
    }

    fun toggleCheckpoint(id: String) {
        val updated = _uiState.value.checkpoints.map {
            if (it.id == id) it.copy(isChecked = !it.isChecked) else it
        }
        _uiState.value = _uiState.value.copy(checkpoints = updated)
        recalculateMetrics()
    }

    fun checkAllPpe() {
        val updated = _uiState.value.ppeList.map { it.copy(isChecked = true) }
        _uiState.value = _uiState.value.copy(ppeList = updated)
        recalculateMetrics()
    }

    fun checkAllCheckpoints() {
        val updated = _uiState.value.checkpoints.map { it.copy(isChecked = true) }
        _uiState.value = _uiState.value.copy(checkpoints = updated)
        recalculateMetrics()
    }

    fun resetAll() {
        loadTemplate(_uiState.value.selectedTemplate)
    }

    private fun loadTemplate(template: JsaTemplateType) {
        val ppe = getDefaultPpeForTemplate(template)
        val checks = getDefaultCheckpointsForTemplate(template)
        _uiState.value = _uiState.value.copy(
            selectedTemplate = template,
            ppeList = ppe,
            checkpoints = checks
        )
        recalculateMetrics()
    }

    private fun recalculateMetrics() {
        val s = _uiState.value
        val requiredPpe = s.ppeList.filter { it.isRequired }
        val ppeChecked = requiredPpe.count { it.isChecked }
        val ppePct = if (requiredPpe.isNotEmpty()) (ppeChecked * 100) / requiredPpe.size else 100

        val totalChecks = s.checkpoints.size
        val checksPassed = s.checkpoints.count { it.isChecked }
        val checkPct = if (totalChecks > 0) (checksPassed * 100) / totalChecks else 100

        val criticals = s.checkpoints.filter { it.isCritical }
        val criticalPassed = criticals.all { it.isChecked }

        val ready = (ppePct == 100) && criticalPassed && (checkPct >= 80)

        _uiState.value = s.copy(
            ppeCompletionPercent = ppePct,
            checkpointCompletionPercent = checkPct,
            criticalCheckpointsAllPassed = criticalPassed,
            isReadyToWork = ready
        )
    }

    fun getCalculatedRiskLevel(): RiskLevel {
        val score = _uiState.value.riskSeverity.score * _uiState.value.riskLikelihood.score
        return when {
            score >= 12 -> RiskLevel.EXTREME
            score >= 8 -> RiskLevel.HIGH
            score >= 4 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
    }

    fun saveJsaAuditLog() {
        val s = _uiState.value
        val risk = getCalculatedRiskLevel()
        val statusStr = if (s.isReadyToWork) "APPROVED / SAFE" else "PENDING ACTIONS"
        val summary = "JSA [${s.selectedTemplate.title}] @ ${s.jobSiteName}: Status: $statusStr (PPE ${s.ppeCompletionPercent}%, Checks ${s.checkpointCompletionPercent}%, Risk: ${risk.label}). Lead: ${s.supervisorName}"

        viewModelScope.launch {
            toolLogRepository.logToolActivity(
                toolType = "SAFETY",
                title = "OSHA JSA: ${s.selectedTemplate.title}",
                summary = summary,
                value = s.checkpointCompletionPercent.toDouble()
            )
        }
    }

    private fun getDefaultPpeForTemplate(template: JsaTemplateType): List<PpeItem> {
        return when (template) {
            JsaTemplateType.GENERAL_CONSTRUCTION -> listOf(
                PpeItem("ppe_eye", "Safety Glasses with Side Shields", "ANSI Z87.1+", true, true),
                PpeItem("ppe_ear", "Hearing Protection (Plugs/Muffs >25dB NRR)", "ANSI S3.19", true, true),
                PpeItem("ppe_feet", "Steel / Composite Toe Work Boots", "ASTM F2413", true, true),
                PpeItem("ppe_head", "Hard Hat (Type 1 Class E/G)", "ANSI Z89.1", false, false),
                PpeItem("ppe_hand", "Cut-Resistant Grip Gloves (Level A2-A4)", "ANSI/ISEA 105", true, false),
                PpeItem("ppe_dust", "Particulate Dust Mask (N95 / FFP2)", "NIOSH N95", true, false)
            )
            JsaTemplateType.ELECTRICAL_LOTO -> listOf(
                PpeItem("ppe_volt_gloves", "Insulated Rubber Gloves with Leather Protectors", "ASTM D120 Class 0 (1000V)", true, false),
                PpeItem("ppe_arc_face", "Arc Flash Face Shield & Balaclava", "NFPA 70E (8-40 cal/cm²)", true, false),
                PpeItem("ppe_fr_cloth", "Flame Resistant (FR) Rated Clothing", "NFPA 2112 / 70E Cat 2", true, false),
                PpeItem("ppe_eye", "Non-Conductive Safety Glasses", "ANSI Z87.1 Dielectric", true, true),
                PpeItem("ppe_feet", "Electrical Hazard (EH) Rated Boots", "ASTM F2413 EH", true, true),
                PpeItem("ppe_ear", "Hearing Protection", "NRR > 25dB", true, false)
            )
            JsaTemplateType.WORKING_AT_HEIGHTS -> listOf(
                PpeItem("ppe_harness", "Full Body Fall Arrest Harness", "ANSI Z359.11", true, false),
                PpeItem("ppe_lanyard", "Shock-Absorbing Lanyard / Self-Retracting Lifeline (SRL)", "ANSI Z359.14", true, false),
                PpeItem("ppe_head_strap", "Hard Hat with Chin Strap (Height Rated)", "EN 12492 / ANSI Z89.1", true, false),
                PpeItem("ppe_feet", "Slip-Resistant Steel Toe Boots", "ASTM F2413 Slip Rating", true, true),
                PpeItem("ppe_eye", "Safety Glasses with Retention Cord", "ANSI Z87.1+", true, true),
                PpeItem("ppe_hand", "Rigger / Climbing Work Gloves", "High Dexterity", false, false)
            )
            JsaTemplateType.HOT_WORK_WELDING -> listOf(
                PpeItem("ppe_weld_helmet", "Auto-Darkening Welding Helmet (Shade 9-13)", "ANSI Z87.1 / CSA W117.2", true, false),
                PpeItem("ppe_weld_gloves", "Heavy Split Cowhide Welding Gauntlets", "ANSI Cut & Heat Level 4", true, false),
                PpeItem("ppe_leather_jacket", "Leather Welding Jacket / Spats / Apron", "Flame & Spatter Proof", true, false),
                PpeItem("ppe_resp_weld", "Welding Fume Respirator (P100 / Ozone Filter)", "NIOSH P100 / 2097", true, false),
                PpeItem("ppe_feet", "Heat-Resistant Leather Work Boots (No synthetic mesh)", "ASTM F2413", true, true),
                PpeItem("ppe_ear", "Flame-Resistant Ear Plugs", "Spatter Protection", true, false)
            )
            JsaTemplateType.TRENCHING_EXCAVATION -> listOf(
                PpeItem("ppe_head", "Hard Hat (Overhead Spoil Hazard)", "ANSI Z89.1", true, true),
                PpeItem("ppe_hivis", "High-Visibility Safety Vest (Class 2 / 3)", "ANSI/ISEA 107", true, true),
                PpeItem("ppe_feet", "Puncture-Resistant Steel Shank Work Boots", "ASTM F2413 PR", true, true),
                PpeItem("ppe_eye", "Sealed Safety Goggles (Dust & Dirt)", "ANSI Z87.1+", true, true),
                PpeItem("ppe_ear", "Hearing Protection (Heavy Equipment Noise)", "NRR > 28dB", true, false),
                PpeItem("ppe_gas", "4-Gas Atmospheric Sniffer Badge", "O2, LEL, H2S, CO", false, false)
            )
            JsaTemplateType.CONFINED_SPACE -> listOf(
                PpeItem("ppe_gas_monitor", "Calibrated 4-Gas Detector (Continuous Active)", "Bump Tested Today", true, false),
                PpeItem("ppe_rescue_harness", "Class III Full Body Rescue Harness with D-Rings", "ANSI Z359.1", true, false),
                PpeItem("ppe_supplied_air", "Supplied Air (SCBA) or Half-Mask Vapor Respirator", "NIOSH Certified", true, false),
                PpeItem("ppe_head", "Hard Hat with Intrinsically Safe Headlamp", "Class 1 Div 1 Certified", true, true),
                PpeItem("ppe_hand", "Chemical / Abrasion Resistant Gloves", "Material Specific", true, false),
                PpeItem("ppe_radio", "Intrinsically Safe Two-Way Radio", "FM Approved", true, false)
            )
            JsaTemplateType.CHEMICAL_COATING -> listOf(
                PpeItem("ppe_resp_ov", "Dual-Cartridge Organic Vapor Respirator (OV/P100)", "NIOSH OV/AG/P100", true, false),
                PpeItem("ppe_chem_gloves", "Heavy Nitrile / Butyl Rubber Chemical Gloves", "EN 374 Type A", true, false),
                PpeItem("ppe_chem_goggles", "Indirect-Vent Chemical Splash Goggles", "ANSI Z87.1 D3", true, true),
                PpeItem("ppe_tyvek", "Chemical Impermeable Coveralls (Tyvek / Microporous)", "Type 5/6 Barrier", true, false),
                PpeItem("ppe_feet", "Chemical Resistant Rubber / Neoprene Boots", "ASTM F2413", false, false),
                PpeItem("ppe_eyewash", "15-Minute Eyewash Station Verified Accessible", "ANSI Z358.1", true, true)
            )
            JsaTemplateType.DEMOLITION_STRUCTURAL -> listOf(
                PpeItem("ppe_resp_hepa", "Half-Mask P100 HEPA Respirator (Silica & Lead)", "NIOSH P100", true, false),
                PpeItem("ppe_head", "Heavy Duty Hard Hat (Class E)", "ANSI Z89.1", true, true),
                PpeItem("ppe_feet", "Steel Shank & Metatarsal Guard Safety Boots", "ASTM F2413 Mt/PR", true, true),
                PpeItem("ppe_hand", "Heavy Leather Anti-Vibration Demolition Gloves", "EN 388 Level 4", true, false),
                PpeItem("ppe_eye", "Full Face Shield over Safety Glasses", "ANSI Z87.1+", true, false),
                PpeItem("ppe_ear", "High NRR Earmuffs (Pneumatic hammer noise)", "NRR > 30dB", true, false)
            )
        }
    }

    private fun getDefaultCheckpointsForTemplate(template: JsaTemplateType): List<SafetyCheckpoint> {
        return when (template) {
            JsaTemplateType.GENERAL_CONSTRUCTION -> listOf(
                SafetyCheckpoint("c_1", "Tool Inspection", "Inspect power cords, plugs & extension cords for fraying or missing ground pins", "Electric shock, arc fires", "Remove damaged tools with Out-of-Service tag", false, true),
                SafetyCheckpoint("c_2", "Tool Inspection", "Ensure all blade guards, splitters & riving knives are active and freely moving", "Severe laceration, amputation", "Never wedge open or remove manufacturer guards", false, true),
                SafetyCheckpoint("c_3", "Housekeeping", "Work area swept free of sawdust mounds, trip hazards & loose offcuts", "Slips, trips, combustibility", "Maintain 36\" clear pathways around stationary saws", false, false),
                SafetyCheckpoint("c_4", "Stationary Tools", "Table saw kickback zone clear; push sticks, featherboards & feather clamps ready", "Violent workpiece kickback", "Stand out of direct line of rotation; use push sticks <6\"", false, true),
                SafetyCheckpoint("c_5", "Dust & Air", "Dust collection or shop vac connected to high-dust generating sanders & routers", "Airborne respirable wood dust", "Run active exhaust / HEPA vacuum filtration", false, false),
                SafetyCheckpoint("c_6", "Fire & First Aid", "First aid kit stocked and ABC fire extinguisher within 25 ft reach", "Emergency delay", "Verify green pressure gauge and inspection tag", false, true)
            )
            JsaTemplateType.ELECTRICAL_LOTO -> listOf(
                SafetyCheckpoint("e_1", "Isolation & Lockout", "All energy sources identified, switched OFF, tagged and padlocked with personal key", "Unintended re-energization / electrocution", "Follow strict 1-person 1-lock procedure", false, true),
                SafetyCheckpoint("e_2", "Zero Energy Verification", "Test multimeter on known live source, verify dead on circuit, re-test on live source (Live-Dead-Live)", "Hidden back-feed, false zero reading", "Use CAT III/IV 600V+ meter with intact leads", false, true),
                SafetyCheckpoint("e_3", "Stored Energy Dissipation", "Bleed down high-voltage capacitors, inductors, UPS backups and springs", "Residual high-voltage stored shock", "Ground terminals with rated discharge probe", false, true),
                SafetyCheckpoint("e_4", "Tools & Equipment", "Inspect insulated hand tools for cracks or nicks in 1000V rated insulation", "Flashover to grounded chassis", "Use certified VDE/IEC 60900 1000V tools only", false, true),
                SafetyCheckpoint("e_5", "Arc Flash Perimeter", "Establish Flash Protection Boundary & barricade zone to unqualified personnel", "Arc blast thermal injury to bystanders", "Place hazard tape & warning signage", false, false)
            )
            JsaTemplateType.WORKING_AT_HEIGHTS -> listOf(
                SafetyCheckpoint("h_1", "Anchor Point Certification", "Identify rated anchor point capable of supporting 5,000 lbs (22.2 kN) per worker", "Anchor point failure under shock load", "Attach to approved structural beams or engineered D-rings", false, true),
                SafetyCheckpoint("h_2", "Harness & Lanyard Check", "Inspect harness webbing, stitch patterns, grommets and snap-hook gates for cuts/burns", "Harness rupture during fall", "Remove harness immediately if involved in a prior fall", false, true),
                SafetyCheckpoint("h_3", "Ladder 4:1 Ratio", "Extension ladder placed at 4:1 slope angle, tied off at top and extends 3 ft above landing", "Ladder kick-out or tip over", "Maintain 3 points of contact on ladder rungs at all times", false, true),
                SafetyCheckpoint("h_4", "Scaffold Guardrails", "Scaffolding equipped with top rails (42\"), midrails (21\"), toe boards (3.5\") & full planking", "Worker fall or falling tools hitting below", "Install mudsills, base plates and diagonal cross braces", false, true),
                SafetyCheckpoint("h_5", "Drop Zone Exclusion", "Barricade ground perimeter directly beneath elevated work platform with danger tape", "Falling objects striking ground crew", "Use tool tethers on all hand tools >1 lb", false, false)
            )
            JsaTemplateType.HOT_WORK_WELDING -> listOf(
                SafetyCheckpoint("w_1", "35-Foot Flammable Radius", "Remove all sawdust, solvents, cardboard, fuels and combustibles within 35 feet of work", "Ignition of shop materials from flying sparks", "Cover non-movable items with certified fireproof blankets", false, true),
                SafetyCheckpoint("w_2", "Dedicated Fire Watch", "Designate a dedicated trained fire watch with ready ABC extinguisher during & 30 min post work", "Smoldering hidden fires inside cavities", "Maintain uninterrupted visual scan of work zone", false, true),
                SafetyCheckpoint("w_3", "Cylinder Storage & Chains", "Oxygen and Acetylene gas cylinders secured upright with steel chains and caps in place", "Cylinder valve sheer, projectile explosion", "Store oxygen 20ft away from fuel gases or separated by 5ft fire wall", false, true),
                SafetyCheckpoint("w_4", "Ventilation & Fume Extraction", "Position local exhaust snorkel or portable blower directly at welding arc plume", "Metal fume fever, ozone, toxic oxides", "Ensure cross-flow air movement away from welder's breathing zone", false, false),
                SafetyCheckpoint("w_5", "Ground Clamp Proximity", "Attach work clamp as close to weld joint as possible to avoid stray return currents", "Arc strikes through bearings, conduit or gas pipes", "Never ground through piping carrying flammable fluids", false, true)
            )
            JsaTemplateType.TRENCHING_EXCAVATION -> listOf(
                SafetyCheckpoint("t_1", "811 Underground Utility Locate", "Confirm 811 'Call Before You Dig' ticket is active and utility paint/flags verified on site", "Striking high-pressure gas or high-voltage lines", "Hand-dig/pothole within 24\" tolerance zone", false, true),
                SafetyCheckpoint("t_2", "Competent Person Inspection", "OSHA competent person daily soil classification (Type A/B/C) and fissure/water inspection", "Sudden trench wall cave-in", "Inspect before entry and after rain events", false, true),
                SafetyCheckpoint("t_3", "Protective Shoring / Sloping", "Trench deeper than 5 feet protected by trench box, hydraulic shoring or OSHA benching", "Crushing entrapment from soil weight", "Shield must extend at least 18\" above trench lip", false, true),
                SafetyCheckpoint("t_4", "Egress Ladders", "Ladders or ramps placed inside trench every 25 feet of lateral travel, extending 3 ft past top", "Trapped in rapid water rise or collapse", "Never travel more than 25 feet to reach an exit point", false, true),
                SafetyCheckpoint("t_5", "Spoil Pile Distance", "Excavated spoil dirt and heavy equipment kept back at least 2 feet (0.6 m) from trench edge", "Spoil surcharge sliding into trench", "Install stop logs if trucks back up near edge", false, false)
            )
            JsaTemplateType.CONFINED_SPACE -> listOf(
                SafetyCheckpoint("cs_1", "4-Gas Atmospheric Sniffing", "Test top, middle, and bottom: Oxygen (19.5-23.5%), LEL (<10%), H2S (<10ppm), CO (<35ppm)", "Asphyxiation, toxic poisoning, flash fire", "Continuous gas monitoring with audible/visual alarms active", false, true),
                SafetyCheckpoint("cs_2", "Entry Permit & Attendant", "Signed OSHA permit posted at hatch; dedicated outside attendant in constant communication", "Unmonitored incapacitation inside vessel", "Attendant must NEVER enter space without backup rescue team", false, true),
                SafetyCheckpoint("cs_3", "Mechanical Rescue Tripod", "Egress tripod, retrieval winch, and self-retracting lifeline hooked to entrant's harness", "Inability to extract unconscious worker", "Verify hoist mechanism operates freely before entry", false, true),
                SafetyCheckpoint("cs_4", "Forced Air Positive Purge", "Continuous mechanical air blower ducted to bottom of space running for 15+ min prior to entry", "Accumulation of heavy vapors (CO2/Propane/Solvents)", "Ensure intake hose is placed in clean outdoor air", false, true)
            )
            JsaTemplateType.CHEMICAL_COATING -> listOf(
                SafetyCheckpoint("cc_1", "Safety Data Sheet (SDS) Available", "SDS for all resins, solvents, paints and catalysts printed and reviewed by crew", "Mishandling chemical exposure", "Know exact flash point, reactivity and PPE needs", false, true),
                SafetyCheckpoint("cc_2", "Active Cross Ventilation", "Explosion-proof intake and exhaust fans generating negative room pressure for fumes", "Vapor cloud ignition, respiratory toxicity", "No open flames, pilot lights or unshielded motors within 20ft", false, true),
                SafetyCheckpoint("cc_3", "Emergency Eyewash & Drench", "Tested emergency eyewash station located within 10 seconds unobstructed walking path", "Permanent caustic or solvent eye damage", "Flush eyes continuously for full 15 minutes if exposed", false, true),
                SafetyCheckpoint("cc_4", "Flammable Rag Storage", "Oily/solvent-soaked rags placed in UL-listed self-closing airtight metal disposal cans", "Spontaneous combustion fires (Linseed oil/Stains)", "Never wad up solvent rags in open trash bins", false, true)
            )
            JsaTemplateType.DEMOLITION_STRUCTURAL -> listOf(
                SafetyCheckpoint("d_1", "Utility Line Isolation", "Gas, electric, water and sewer services locked out, capped and verified dead at meter", "Explosion, flooding, live wire contact", "Obtain written utility confirmation before knocking down walls", false, true),
                SafetyCheckpoint("d_2", "Hazardous Material Survey", "Structure surveyed for Asbestos-Containing Materials (ACM) and Lead-Based Paint (LBP)", "Inhaling toxic asbestos fibers or lead dust", "Abatement by licensed contractor if materials are friable", false, true),
                SafetyCheckpoint("d_3", "Temporary Shoring Posts", "Screw jacks and temporary structural shoring installed before removing load-bearing studs", "Catastrophic ceiling/roof collapse", "Verify transfer of load to foundation below", false, true),
                SafetyCheckpoint("d_4", "Drop Chutes & Debris Netting", "Enclosed debris chutes for drops >20 ft; barricaded danger perimeter at ground level", "Debris striking workers or public below", "Never drop materials through floor holes without guards", false, false)
            )
        }
    }
}
