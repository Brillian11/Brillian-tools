package com.example.ui.screens.library

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LibraryItem(
    val id: String,
    val title: String,
    val category: String, // "3D Model", "2D Floorplan", "Document", "Guidance"
    val format: String, // ".obj", ".skp", ".skb", ".pdf", ".dwg"
    val fileSize: String,
    val description: String,
    val author: String,
    val dateAdded: String,
    val uriString: String? = null
)

data class SavedMeasurement(
    val id: String,
    val itemId: String,
    val itemTitle: String,
    val title: String,
    val distanceMeters: Float,
    val distanceFeet: Float,
    val pointA: String,
    val pointB: String,
    val mode: String, // "2D Blueprint", "3D Model", "Document Spec"
    val timestamp: String,
    val note: String = ""
)

class LibraryViewModel : ViewModel() {

    private val _items = MutableStateFlow(
        listOf(
            LibraryItem(
                id = "m0",
                title = "Parametric Timber Pavilion & Pergola Frame",
                category = "3D Model",
                format = ".obj / .glb",
                fileSize = "16.2 MB",
                description = "Architectural 3D CAD model featuring heavy timber joinery, mortise-and-tenon rafters, reinforced footing brackets, and open-slat roof shading.",
                author = "Brillian 3D Design Labs",
                dateAdded = "2026-08-28"
            ),
            LibraryItem(
                id = "m1",
                title = "Concrete Bridge Span & Pier Assembly",
                category = "3D Model",
                format = ".obj",
                fileSize = "18.4 MB",
                description = "High-detail civil engineering bridge span with reinforced concrete abutments and pier foundations.",
                author = "Civil StructEng Group",
                dateAdded = "2026-08-20"
            ),
            LibraryItem(
                id = "m_excavator",
                title = "Hydraulic Boom & Articulated Joint 3D",
                category = "3D Model",
                format = ".gltf",
                fileSize = "22.5 MB",
                description = "Heavy machinery kinematic assembly with hydraulic cylinders, high-pressure hose clearances, and articulated pivot pins.",
                author = "Industrial Mechanical CAD",
                dateAdded = "2026-08-27"
            ),
            LibraryItem(
                id = "m2",
                title = "Modern Commercial Office Floorplan",
                category = "3D Model",
                format = ".skp",
                fileSize = "24.1 MB",
                description = "SketchUp interior design model featuring open-plan workstations, conference pods, and HVAC ducting routing.",
                author = "Apex Architecture",
                dateAdded = "2026-08-22"
            ),
            LibraryItem(
                id = "m3",
                title = "Structural Steel Truss Frame",
                category = "3D Model",
                format = ".skb",
                fileSize = "12.8 MB",
                description = "Warehouse structural steel roof truss with gusset plates and moment connections.",
                author = "SteelCraft Engineering",
                dateAdded = "2026-08-15"
            ),
            LibraryItem(
                id = "m4",
                title = "Level 1 Architectural Floor Plan",
                category = "2D Floorplan",
                format = ".dwg",
                fileSize = "4.2 MB",
                description = "Detailed architectural floor plan showing partition walls, electrical drop locations, room dimensions, and plumbing risers.",
                author = "Drafting Dept A",
                dateAdded = "2026-08-24"
            ),
            LibraryItem(
                id = "fp_villa",
                title = "Modern 2-Story Villa Blueprint (Ground Floor)",
                category = "2D Floorplan",
                format = ".dwg",
                fileSize = "6.8 MB",
                description = "Architectural CAD floorplan featuring living room, master suite, open kitchen, patio, and garage layout with dimension strings.",
                author = "ArchStudio Design",
                dateAdded = "2026-08-27"
            ),
            LibraryItem(
                id = "fp_commercial",
                title = "Commercial Cafe & Retail Space Layout",
                category = "2D Floorplan",
                format = ".dxf",
                fileSize = "3.9 MB",
                description = "Retail shop floorplan with guest seating zone, barista counter, dry storage, ADA restroom, and egress paths.",
                author = "Interior CAD Consultants",
                dateAdded = "2026-08-25"
            ),
            LibraryItem(
                id = "m5",
                title = "HVAC & Electrical MEP Master Schematic",
                category = "Document",
                format = ".pdf",
                fileSize = "8.9 MB",
                description = "Mechanical, Electrical, and Plumbing (MEP) specification and riser diagram for commercial retrofit.",
                author = "MEP Engineering",
                dateAdded = "2026-08-25"
            ),
            LibraryItem(
                id = "g1",
                title = "Indonesian Local Woods & Timber Grading Handbook",
                category = "Guidance",
                format = "Handbook",
                fileSize = "2.8 MB",
                description = "Comprehensive guide on Janka hardness, density, shrinkage, and workability for Jati, Mahoni, Albasia, Waru, Keruing, Nangka, Cengkih, Bangkirai, Ulin, and Merbau.",
                author = "Indonesian Forestry & Woodworking Board",
                dateAdded = "2026-08-26"
            ),
            LibraryItem(
                id = "g2",
                title = "Chemical & Solvent MSDS Safety Guide",
                category = "Guidance",
                format = "MSDS",
                fileSize = "1.2 MB",
                description = "Safety Data Sheets and handling precautions for SikaTop Seal 107, Aquaproof, PVC/CPVC Solvent Cement, Contact Cement, and Portland Cement dust.",
                author = "K3 Construction Safety",
                dateAdded = "2026-08-26"
            ),
            LibraryItem(
                id = "g3",
                title = "Construction Safety Standards & K3 Guidelines",
                category = "Guidance",
                format = "Standard",
                fileSize = "1.9 MB",
                description = "SNI compliance rules, personal protective equipment (PPE), scaffolding safety, and excavation shoring checklists for Indonesian projects.",
                author = "Ministry of Public Works",
                dateAdded = "2026-08-26"
            ),
            LibraryItem(
                id = "g4",
                title = "Concrete & Masonry Mix Standards (SNI / ACI)",
                category = "Guidance",
                format = "Standard",
                fileSize = "2.1 MB",
                description = "Site mix proportions (1:2:3, 1:3:5), water-cement ratios for Semen Gresik, Tiga Roda, Holcim, and Padang, slump test guidelines, and curing timeframes.",
                author = "SNI Technical Committee",
                dateAdded = "2026-08-26"
            ),
            LibraryItem(
                id = "g5",
                title = "Paint & Protective Coatings Technical Guide",
                category = "Guidance",
                format = "Manual",
                fileSize = "1.5 MB",
                description = "Application specifications and color schemes for Mowilex woodstain, Dulux exterior paints, Jotun epoxy primers, Avitex, and Catylac wall paints.",
                author = "Architectural Finishes Group",
                dateAdded = "2026-08-26"
            )
        )
    )
    val items: StateFlow<List<LibraryItem>> = _items.asStateFlow()

    private val _selectedItem = MutableStateFlow<LibraryItem?>(null)
    val selectedItem: StateFlow<LibraryItem?> = _selectedItem.asStateFlow()

    private val _isArMode = MutableStateFlow(false)
    val isArMode: StateFlow<Boolean> = _isArMode.asStateFlow()

    private val _savedMeasurements = MutableStateFlow<List<SavedMeasurement>>(
        listOf(
            SavedMeasurement(
                id = "sm_sample_1",
                itemId = "m0",
                itemTitle = "Parametric Timber Pavilion",
                title = "Column Post Clear Span",
                distanceMeters = 3.60f,
                distanceFeet = 11.81f,
                pointA = "Column SW Base",
                pointB = "Column SE Base",
                mode = "3D Model",
                timestamp = "2026-08-28 10:15",
                note = "Verified minimum 3.5m clearance for outdoor dining table"
            ),
            SavedMeasurement(
                id = "sm_sample_2",
                itemId = "fp_villa",
                itemTitle = "Modern 2-Story Villa Blueprint",
                title = "Living Room Width",
                distanceMeters = 5.20f,
                distanceFeet = 17.06f,
                pointA = "West Wall Column",
                pointB = "East Wall Column",
                mode = "2D Blueprint",
                timestamp = "2026-08-28 09:40",
                note = "Dimension verified against architectural CAD drawing"
            )
        )
    )
    val savedMeasurements: StateFlow<List<SavedMeasurement>> = _savedMeasurements.asStateFlow()

    private val _isLocalDatabaseActive = MutableStateFlow(false)
    val isLocalDatabaseActive: StateFlow<Boolean> = _isLocalDatabaseActive.asStateFlow()

    private val _lastDatabaseMessage = MutableStateFlow<String?>(null)
    val lastDatabaseMessage: StateFlow<String?> = _lastDatabaseMessage.asStateFlow()

    fun loadLocalLibraryDatabase() {
        _isLocalDatabaseActive.value = true
        val localSupportedDatabase = listOf(
            LibraryItem(
                id = "loc_stl_01",
                title = "CNC Precision Aluminum Bracket Assembly",
                category = "3D Model",
                format = ".stl",
                fileSize = "14.5 MB",
                description = "High-precision 3D STL mesh for CNC milling and 3D printing, featuring countersunk screw holes and chamfered structural ribs.",
                author = "Local CAD Storage",
                dateAdded = "2026-08-29"
            ),
            LibraryItem(
                id = "loc_ifc_01",
                title = "Multi-Story Commercial Building BIM IFC Structural Model",
                category = "3D Model",
                format = ".ifc",
                fileSize = "32.0 MB",
                description = "Industry Foundation Classes (IFC) BIM model containing structural steel columns, rebar grids, floor slabs, and curtain walls.",
                author = "BIM Master Database",
                dateAdded = "2026-08-29"
            ),
            LibraryItem(
                id = "loc_step_01",
                title = "Industrial Gearbox & Bevel Pinion Assembly",
                category = "3D Model",
                format = ".step / .stp",
                fileSize = "19.8 MB",
                description = "Standard STEP exchange CAD model for mechanical engineering, gear teeth profiles, and shaft couplings.",
                author = "Mechanical Engineering Dept",
                dateAdded = "2026-08-29"
            ),
            LibraryItem(
                id = "loc_fbx_01",
                title = "Modular Scaffolding & Safety Tower Mesh",
                category = "3D Model",
                format = ".fbx",
                fileSize = "11.2 MB",
                description = "Textured 3D FBX asset for jobsite visualization, steel tube clamps, and plank decking.",
                author = "Site Safety Visuals",
                dateAdded = "2026-08-29"
            ),
            LibraryItem(
                id = "loc_dxf_01",
                title = "Laser Cut Metal Gate & Decorative Panel Vector",
                category = "2D Floorplan",
                format = ".dxf",
                fileSize = "3.1 MB",
                description = "DXF 2D vector path for CNC laser cutting, plasma tables, and architectural metalwork.",
                author = "Fabrication Studio",
                dateAdded = "2026-08-29"
            ),
            LibraryItem(
                id = "loc_csv_01",
                title = "Field Topography & Total Station Coordinate Points",
                category = "Document",
                format = ".csv",
                fileSize = "0.8 MB",
                description = "Northing, Easting, Elevation (NEZ) survey point data collected via GPS and optical total station.",
                author = "Geospatial Survey Team",
                dateAdded = "2026-08-29"
            )
        )
        val combined = (localSupportedDatabase + _items.value).distinctBy { it.id }
        _items.value = combined
        _lastDatabaseMessage.value = "Local Library Database initialized! ${combined.size} supported files (.obj, .glb, .skp, .dwg, .dxf, .stl, .ifc, .step, .pdf, .csv) loaded."
    }

    fun clearDatabaseMessage() {
        _lastDatabaseMessage.value = null
    }

    fun selectItem(item: LibraryItem?) {
        _selectedItem.value = item
    }

    fun setArMode(enabled: Boolean) {
        _isArMode.value = enabled
    }

    fun saveMeasurement(
        itemId: String,
        itemTitle: String,
        title: String,
        distanceMeters: Float,
        pointA: String,
        pointB: String,
        mode: String,
        note: String = ""
    ) {
        val feet = distanceMeters * 3.28084f
        val newMeas = SavedMeasurement(
            id = "meas_${System.currentTimeMillis()}",
            itemId = itemId,
            itemTitle = itemTitle,
            title = title.ifBlank { "Tape Measure (${"%.2f".format(distanceMeters)}m)" },
            distanceMeters = distanceMeters,
            distanceFeet = feet,
            pointA = pointA,
            pointB = pointB,
            mode = mode,
            timestamp = "2026-08-28 10:45",
            note = note
        )
        _savedMeasurements.value = listOf(newMeas) + _savedMeasurements.value
    }

    fun deleteMeasurement(measurementId: String) {
        _savedMeasurements.value = _savedMeasurements.value.filterNot { it.id == measurementId }
    }

    fun addCustomItem(title: String, category: String, format: String, size: String, description: String, uriString: String? = null) {
        val newItem = LibraryItem(
            id = "custom_${System.currentTimeMillis()}",
            title = title,
            category = category,
            format = format,
            fileSize = size,
            description = description,
            author = "Device Storage",
            dateAdded = "Today",
            uriString = uriString
        )
        _items.value = listOf(newItem) + _items.value
        selectItem(newItem)
    }
}
