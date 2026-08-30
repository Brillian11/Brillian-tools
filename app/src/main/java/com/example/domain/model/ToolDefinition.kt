package com.example.domain.model

import java.util.Locale

data class ToolDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val iconName: String,
    val defaultSpan: Int = 1,
    val keywords: List<String> = emptyList()
) {
    fun matchesSearch(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase(Locale.ROOT)
        val tokens = q.split("\\s+".toRegex())
        return tokens.all { token ->
            title.lowercase(Locale.ROOT).contains(token) ||
            description.lowercase(Locale.ROOT).contains(token) ||
            category.lowercase(Locale.ROOT).contains(token) ||
            keywords.any { it.lowercase(Locale.ROOT).contains(token) }
        }
    }

    companion object {
        val ALL_TOOLS = listOf(
            // Woodworking Suite
            ToolDefinition(
                id = "widget_board_footage",
                title = "Board Footage & Lumber Estimator",
                description = "Calculate board feet volume ((T x W x L)/12), species density & lumber pricing",
                category = "Woodworking",
                iconName = "Calculate",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_cutlist_optimizer",
                title = "Cut List Optimizer (1D & 2D)",
                description = "Bin packing cut optimizer for stock boards and 4x8 plywood nesting",
                category = "Woodworking",
                iconName = "ContentCut",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_stair_layout",
                title = "Stair Layout & Stringer Calculator",
                description = "Stringer rise, run, step count, throat thickness & IRC headroom compliance",
                category = "Woodworking",
                iconName = "Stairs",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_rafter_calculator",
                title = "Rafter & Roof Pitch Calculator",
                description = "Common, hip, valley, and jack rafter lengths with birdsmouth seat cuts",
                category = "Woodworking",
                iconName = "Roofing",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_compound_miter",
                title = "Compound Miter & Bevel Calculator",
                description = "Miter and bevel saw angles for crown moldings & multi-sided polyhedral frames",
                category = "Woodworking",
                iconName = "CropRotate",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_wood_moisture",
                title = "Wood Moisture & Shrinkage Estimator",
                description = "Tangential & radial wood shrinkage forecasting based on species & target EMC",
                category = "Woodworking",
                iconName = "WaterDrop",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_joinery_spacing",
                title = "Joinery & Tenon Spacing Calculator",
                description = "Equal spacing distributions for mortise/tenons, dowels & pocket hole screws",
                category = "Woodworking",
                iconName = "Splitscreen",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_kerf_bending",
                title = "Kerf Bending",
                description = "Kerf cut spacing, pitch, and depth for bending solid timber",
                category = "Woodworking",
                iconName = "Architecture",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_dado_step_over",
                title = "Dado & Lap Joint Planner",
                description = "Single blade hogging fence offsets for wide lap joints",
                category = "Woodworking",
                iconName = "TableChart",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_segmented_turning",
                title = "Segmented Woodturning & Bowls",
                description = "Stave miter angles, segment edge lengths, and ring stack dimensions for multi-sided lathe turnings",
                category = "Woodworking",
                iconName = "RotateRight",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_sagulator",
                title = "Lumber Sagulator (Shelf Deflection)",
                description = "Shelf deflection analysis under point and uniform loads with species E-modulus & span limits",
                category = "Woodworking",
                iconName = "Straighten",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_grain_matching",
                title = "Grain Matching & Board Layout",
                description = "Digital canvas for previewing and arranging timber grain orientations, slip-matches & bookmatches before glue-up",
                category = "Woodworking",
                iconName = "Texture",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_drill_tap_chart",
                title = "Drill Bit / Tap & Die Reference",
                description = "Tap drill sizes, thread clearance holes, and drill bit conversions across Imperial, Metric, and Number/Letter gauges",
                category = "Woodworking",
                iconName = "Construction",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_blade_speed",
                title = "Blade Surface Speed (SFPM)",
                description = "Surface Feet per Minute (SFPM), arbor RPM, and blade tip velocity based on motor and pulley ratios",
                category = "Woodworking",
                iconName = "Speed",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_wood_species_studio",
                title = "Wood Species & Preservation Studio",
                description = "Interactive global wood database, EMC calculator, and step-by-step drying & preservation guides",
                category = "Woodworking",
                iconName = "MenuBook",
                defaultSpan = 2
            ),

            // Electrical & Electronics Suite
            ToolDefinition(
                id = "widget_ohms_law",
                title = "Ohm's Law & Power Triangle",
                description = "Instant voltage (V), current (I), resistance (R), real power (W) & reactive power (VAR)",
                category = "Electrical",
                iconName = "Bolt",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_voltage_drop",
                title = "Voltage Drop Calculator",
                description = "NEC 3% branch & 5% line degradation analyzer for Copper & Aluminum runs",
                category = "Electrical",
                iconName = "ElectricMeter",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_conduit_fill",
                title = "Wire Gauge & Conduit Fill",
                description = "NEC Chapter 9 Table 1 & 4 fill percentages and Table 310.16 wire ampacity",
                category = "Electrical",
                iconName = "Cable",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_conduit_bender",
                title = "Conduit Bender Angles & Offsets",
                description = "Offsets, 3/4-bend saddles, 90° stub-up take-up & mark multipliers",
                category = "Electrical",
                iconName = "Straighten",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_resistor_color_code",
                title = "Resistor Color Code Decoder",
                description = "4-Band, 5-Band, 6-Band, Tolerance, TCR PPM & SMD code decoder",
                category = "Electrical",
                iconName = "Palette",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_breaker_panel",
                title = "Breaker Panel Load Calculator",
                description = "Single/Three-Phase load balancing, neutral unbalance & busbar limits",
                category = "Electrical",
                iconName = "FlashOn",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_led_driver",
                title = "LED Driver & Transformer Sizer",
                description = "Total wattage load with 80% safety headroom & Class 2 power supply sizing",
                category = "Electrical",
                iconName = "LightMode",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_box_fill_capacity",
                title = "Box Fill Capacity (NEC 314.16)",
                description = "Required junction box volume deductions for conductors, internal clamps, device yokes, and grounds",
                category = "Electrical",
                iconName = "Inbox",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_industrial_motor_fla",
                title = "Industrial Motor FLA Sizer (NEC 430)",
                description = "Wire ampacity, inverse-time breaker, time-delay fuse, and overload protection for 1-phase & 3-phase AC motors",
                category = "Electrical",
                iconName = "ElectricMeter",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_solar_battery_sizer",
                title = "Solar PV & Battery Bank Sizer",
                description = "Array wattage, MPPT cold Voc limits, days of autonomy, and LiFePO4 / Lead-Acid battery bank capacity",
                category = "Electrical",
                iconName = "WbSunny",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_current_loop_scaling",
                title = "4–20 mA Current Loop & Scaling",
                description = "Process variable scaling, NAMUR NE43 fault states, loop voltage compliance, and PLC integer counts",
                category = "Electrical",
                iconName = "Tune",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_power_factor_correction",
                title = "Harmonics & Power Factor Correction",
                description = "Required capacitor bank kVAR, detuned anti-resonance reactors, line current reduction, and utility ROI",
                category = "Electrical",
                iconName = "TrendingDown",
                defaultSpan = 2
            ),

            // Maintenance, Plumbing & Housework Suite
            ToolDefinition(
                id = "widget_pipe_sizing",
                title = "Pipe Sizing & Friction Loss Calculator",
                description = "Water flow rates (GPM / L/min), velocity limits, pressure drops & pipe sizing for Copper, PEX, PVC",
                category = "Plumbing & Maintenance",
                iconName = "Water",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_hvac_load",
                title = "HVAC BTU & Room Load Estimator",
                description = "Mini-splits, AC tonnage, heat pumps & radiator sizing based on volume, climate & insulation",
                category = "Plumbing & Maintenance",
                iconName = "WbSunny",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_tile_grout",
                title = "Tile, Grout & Flooring Estimator",
                description = "Tile counts, carton boxes, 10-15% waste factor, ANSI grout weight & thin-set mortar bags",
                category = "Plumbing & Maintenance",
                iconName = "GridOn",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_paint_coverage",
                title = "Wall Area & Paint/Primer Coverage Estimator",
                description = "Net wall & ceiling area, door/window subtractions, primer & paint cans required",
                category = "Plumbing & Maintenance",
                iconName = "Palette",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_drywall_stud",
                title = "Drywall & Framing Stud Calculator",
                description = "4x8/4x12 drywall sheets, 16/24\" OC studs, plates, corner posts, screws & joint compound",
                category = "Plumbing & Maintenance",
                iconName = "HomeWork",
                defaultSpan = 2
            ),

            // Safety & Compliance Suite
            ToolDefinition(
                id = "widget_safety_checklist",
                title = "OSHA Safety Checklist & PPE Auditor",
                description = "Pre-job safety analysis (JSA) templates, hazard assessments, PPE auditor & OSHA site inspection forms",
                category = "Safety & Compliance",
                iconName = "HealthAndSafety",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_chemical_msds",
                title = "Chemical & Material Safety (MSDS/SDS)",
                description = "Searchable hazard reference, GHS pictograms, PPE requirements, first aid, storage & disposal for workshop chemicals",
                category = "Safety & Compliance",
                iconName = "Science",
                defaultSpan = 2
            ),

            // Civil Engineering, Earthwork & Masonry Suite
            ToolDefinition(
                id = "widget_concrete_volume",
                title = "Concrete Volume & Bag Mix Sizer",
                description = "Computes cubic yards/meters for slabs, footings, post holes & sonotubes with dry bag mix counts",
                category = "Civil Engineering",
                iconName = "Calculate",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_rebar_estimator",
                title = "Rebar Spacing & Weight Estimator",
                description = "Linear weight, #3-#11 & 6-32mm bar grids, lap splices, chairs & tie wire schedules",
                category = "Civil Engineering",
                iconName = "GridOn",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_cut_fill_earthwork",
                title = "Cut & Fill Earthwork Volume",
                description = "Excavation trenches, grading cut/fill, basement pits, swell/shrinkage & dump trucks",
                category = "Civil Engineering",
                iconName = "Terrain",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_masonry_mortar",
                title = "Brick, Block & Mortar Calculator",
                description = "Standard bricks, CMU concrete blocks, mortar volume & core grout filling",
                category = "Civil Engineering",
                iconName = "HomeWork",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_slope_drainage",
                title = "Slope, Drainage & Culvert Gradient Sizer",
                description = "Manning's pipe flow capacity, Rational runoff (Q=CIA), trench drop & grade deltas",
                category = "Civil Engineering",
                iconName = "Water",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_soil_asphalt",
                title = "Soil Compaction & Asphalt Tonnage Estimator",
                description = "Paving area, lift depth, roll-down compaction factor, US short tons & metric tonnes",
                category = "Civil Engineering",
                iconName = "PrecisionManufacturing",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_beam_deflection",
                title = "Beam Deflection & Moment",
                description = "Shear, bending moment (Mmax), and L/360 deflection limits",
                category = "Civil Engineering",
                iconName = "Engineering",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_earthwork_grade",
                title = "Earthwork Cut/Fill & Grade",
                description = "Average end area soil volume & % grade to angle converter",
                category = "Civil Engineering",
                iconName = "Terrain",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_framing_roofing",
                title = "Framing, Drywall & Roofing",
                description = "16\"/24\" O.C. studs, 4x8 drywall, and rafter pitch calculator",
                category = "Civil Engineering",
                iconName = "HomeWork",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_stationing_cogo",
                title = "Stationing & Offset COGO",
                description = "Roadway alignments, centerline coordinate geometry (X,Y,Z), inverse points & curve geometry",
                category = "Civil Engineering",
                iconName = "Navigation",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_retaining_wall_sizer",
                title = "Retaining Wall Soil Pressure Sizer",
                description = "Rankine lateral earth pressure, overturning/sliding safety factors, geogrid tiers & drainage gravel",
                category = "Civil Engineering",
                iconName = "Foundation",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_aggregate_sieve",
                title = "Aggregate Sieve Analysis & Fineness Modulus",
                description = "ASTM C33 gradation curves, Fineness Modulus (FM), Cu/Cc uniformity & gravel batching",
                category = "Civil Engineering",
                iconName = "Grain",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_equipment_hauling",
                title = "Heavy Equipment Hauling & Axle Load",
                description = "Weight distribution, Federal Bridge Formula B, steer/drive/trailer loading & lowboy CG balance",
                category = "Civil Engineering",
                iconName = "LocalShipping",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_stormwater_rational",
                title = "Stormwater Runoff (Rational Method)",
                description = "Peak discharge rate (Q = CIA), Manning culvert pipe diameter & detention basin retention volume",
                category = "Civil Engineering",
                iconName = "WaterDrop",
                defaultSpan = 2
            ),

            // Sensor & Hardware Suite
            ToolDefinition(
                id = "widget_digital_level",
                title = "Water Level & Inclinometer",
                description = "Water level bubble vial & IMU pitch/roll inclinometer",
                category = "Sensors",
                iconName = "ScreenRotation",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_compass",
                title = "Digital Compass & Bearing",
                description = "360° cardinal compass, azimuth bearing, level & magnetic declination",
                category = "Sensors",
                iconName = "Explore",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_decibel_meter",
                title = "Decibel Sound Meter",
                description = "RMS sound pressure level meter with OSHA 85dB noise warning",
                category = "Sensors",
                iconName = "GraphicEq",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_strobe_tachometer",
                title = "Camera Optical & Strobe Tachometer",
                description = "Optical LED strobe & camera frame analysis to freeze blades and measure RPM",
                category = "Sensors",
                iconName = "FlashOn",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_thermal_camera",
                title = "Thermal Camera (FLIR / Seek USB-C)",
                description = "Radiometric false-color thermal viewer for heat leaks & overloaded breakers",
                category = "Sensors",
                iconName = "DeviceThermostat",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_usb_endoscope",
                title = "USB Borescope & Endoscope Stream",
                description = "Direct USB-OTG camera viewer for wall cavities, pipes, ductwork & engine bays",
                category = "Sensors",
                iconName = "Videocam",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_vibration_analyzer",
                title = "Vibration Spectrum Analyzer",
                description = "Accelerometer FFT spectral analyzer & ISO 10816-3 machinery vibration diagnostics",
                category = "Sensors",
                iconName = "GraphicEq",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_ble_multimeter",
                title = "BLE Smart Multimeter & Clamp Sync",
                description = "Bluetooth multimeter telemetry streamer & NFPA 70E remote arc-flash safety",
                category = "Sensors",
                iconName = "ElectricMeter",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_sun_path_tracker",
                title = "Sun Path & Shadow Tracker",
                description = "Solar trajectory astronomy for passive solar glazing, eave overhang & PV tilt",
                category = "Sensors",
                iconName = "WbSunny",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_barometric_altimeter",
                title = "Barometric Altimeter & Elevation Delta",
                description = "Sub-decimeter hypsometric elevation surveyor, benchmark zero & weather trend",
                category = "Sensors",
                iconName = "Landscape",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_surface_lux_meter",
                title = "Surface Lux & Foot-Candle Meter",
                description = "Ambient light sensor photometry & IESNA/OSHA task lighting compliance auditor",
                category = "Sensors",
                iconName = "Lightbulb",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_fractional_calc",
                title = "Fractional Calculator",
                description = "Tape measure fraction arithmetic with live metric conversions",
                category = "Sensors",
                iconName = "Calculate",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_ar_measurement",
                title = "AR Camera Length & Height",
                description = "Camera-based length, distance & height measurement with viewfinder",
                category = "Sensors",
                iconName = "Camera",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_ar_area_calculator",
                title = "AR Area Calculator",
                description = "Camera viewfinder AR multi-point surface area & perimeter solver",
                category = "Sensors",
                iconName = "SquareFoot",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_plumb_bob",
                title = "Plumb Bob & Wall Squareness",
                description = "Sensor-assisted vertical plumb line & 90° corner squareness checker",
                category = "Sensors",
                iconName = "Architecture",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_stud_detector",
                title = "Stud & Metal Detector",
                description = "Magnetometer metal detector for drywall screws, rebar & iron pipes",
                category = "Sensors",
                iconName = "Sensors",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_laser_measure",
                title = "Laser Measure & Bluetooth Log",
                description = "Bluetooth LE laser distance sync (Bosch/Leica) & room area log",
                category = "Sensors",
                iconName = "Bluetooth",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_jobsite_ir_remote",
                title = "Jobsite IR Remote & Commissioning",
                description = "Native Consumer IR offline 38kHz remote for HVAC split/cassettes, shop air scrubbers, lighting sensors, radiant heaters & AV displays",
                category = "Sensors",
                iconName = "SettingsRemote",
                defaultSpan = 2
            ),

            // Field Engineering & Site Suite
            ToolDefinition(
                id = "widget_meteorology",
                title = "Meteorology & Site Weather",
                description = "Jobsite weather, humidity, AQI, barometric pressure & curing advisory",
                category = "Site & Field",
                iconName = "WbSunny",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_parabolic_focus",
                title = "Parabolic Dish Calculator",
                description = "Parabolic dish focal point (f = d²/16c), f/d ratio & feed horn layout",
                category = "Field Engineering",
                iconName = "CellTower",
                defaultSpan = 2
            ),

            // Material Inventory
            ToolDefinition(
                id = "widget_material_inventory",
                title = "Material Inventory",
                description = "Local Room material stock tracker & timber density database",
                category = "Inventory",
                iconName = "Category",
                defaultSpan = 2
            ),

            // General Utilities & Logs
            ToolDefinition(
                id = "widget_tasks",
                title = "Quick Tasks",
                description = "Checklist for daily priorities with sync status",
                category = "Tasks",
                iconName = "CheckCircle",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_notes",
                title = "Quick Field Notes",
                description = "Jobsite memo pad & field log entries with offline sync",
                category = "Tasks",
                iconName = "StickyNote2",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_timer",
                title = "Focus Timer",
                description = "Pomodoro & interval timer for deep work sessions",
                category = "Focus",
                iconName = "Timer",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_unit_converter",
                title = "Unit Converter",
                description = "Instant length, mass, area, pressure, and temp converter",
                category = "Utility",
                iconName = "SwapHoriz",
                defaultSpan = 1
            ),
            ToolDefinition(
                id = "widget_color_tools",
                title = "Trade Color & Paint Studio",
                description = "Indonesian paint codes (Nippon, Mowilex, Avitex, Propan), trade palettes & screen calibration",
                category = "Utility",
                iconName = "Palette",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_calculator",
                title = "Scientific Calculator",
                description = "Basic and Scientific calculator with trigonometry, logs, exponents & constants",
                category = "Utility",
                iconName = "Calculate",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_psychrometric",
                title = "Psychrometric Air Calculator",
                description = "Computes dew point, wet bulb, RH & enthalpy from dry bulb and pressure readings",
                category = "Mechanical & HVAC",
                iconName = "DeviceThermostat",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_refrigerant",
                title = "Refrigerant P/T Chart",
                description = "Saturation pressure-temperature curves for R410A, R134a, R32 & R22 to set superheat/subcooling",
                category = "Mechanical & HVAC",
                iconName = "DeviceThermostat",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_duct_sizer",
                title = "Duct Velocity & Static Sizer",
                description = "Duct velocity, CFM airflow, and round-to-rectangular equivalent sizing",
                category = "Mechanical & HVAC",
                iconName = "Grain",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_expansion_tank",
                title = "Hydronic Expansion Tank Sizer",
                description = "Computes required expansion tank volume for boilers and hydronic heating loops",
                category = "Mechanical & HVAC",
                iconName = "WaterDrop",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_sling_angle",
                title = "Rigging & Sling Tension Sizer",
                description = "Calculates tension spikes on multi-leg lifting bridles at acute angles (30°, 45°, 60°)",
                category = "Mechanical & HVAC",
                iconName = "Architecture",
                defaultSpan = 2
            ),
            ToolDefinition(
                id = "widget_painting_coating_studio",
                title = "Painting & Coating Studio",
                description = "25 advanced tools for substrate coverage, 2K mixing ratios, spray tip selection, curing climate analysis, and prep matrices",
                category = "Painting & Coating",
                iconName = "Palette",
                defaultSpan = 2
            ),

            // Metalworks & Welding Suite (25 Essential Features)
            ToolDefinition(
                id = "widget_weld_heat_input",
                title = "Welding Parameter & Heat Input Calculator",
                description = "Calculates arc energy and heat input (kJ/mm or kJ/in) based on voltage, current, travel speed & efficiency factor",
                category = "Metalworks",
                iconName = "Science",
                defaultSpan = 2,
                keywords = listOf("heat input", "arc energy", "voltage", "current", "travel speed", "SMAW", "GMAW", "GTAW", "SAW", "efficiency", "KJ/mm", "KJ/in", "welding parameters")
            ),
            ToolDefinition(
                id = "widget_weld_carbon_equivalent",
                title = "Carbon Equivalent & Pre-Heat Sizer",
                description = "Computes CE(IIW) and Pcm from steel alloy percentages to determine required preheat and interpass temperatures",
                category = "Metalworks",
                iconName = "Science",
                defaultSpan = 2,
                keywords = listOf("carbon equivalent", "CEIIW", "Pcm", "preheat", "interpass", "cold cracking", "hydrogen", "alloy", "steel metallurgy")
            ),
            ToolDefinition(
                id = "widget_weld_electrode_selector",
                title = "Electrode & Filler Metal Selector",
                description = "Cross-reference matrix matching base metals to correct filler alloys (ER70S-6, E7018, ER308L, ER4043) and shielding gases",
                category = "Metalworks",
                iconName = "Construction",
                defaultSpan = 2,
                keywords = listOf("filler metal", "electrode", "ER70S-6", "E7018", "ER308L", "ER4043", "shielding gas", "stainless", "aluminum", "chromoly")
            ),
            ToolDefinition(
                id = "widget_weld_deposition_estimator",
                title = "Weld Deposition & Consumable Estimator",
                description = "Computes required filler wire or stick electrode weight (kg/lb) based on joint design, plate thickness, and root gap",
                category = "Metalworks",
                iconName = "Calculate",
                defaultSpan = 2,
                keywords = listOf("deposition", "consumable", "wire weight", "stick electrode", "V-groove", "root gap", "reinforcement", "kilogram", "pound")
            ),
            ToolDefinition(
                id = "widget_weld_shielding_gas",
                title = "Shielding Gas Flow & Bottle Runtime Estimator",
                description = "Calculates cylinder volume remaining from pressure gauge (Bar/PSI) and outputs active arc time at target flow rates",
                category = "Metalworks",
                iconName = "Air",
                defaultSpan = 2,
                keywords = listOf("shielding gas", "gas flow", "cylinder volume", "pressure gauge", "Bar", "PSI", "CFH", "arc time", "argon", "CO2")
            ),
            ToolDefinition(
                id = "widget_metal_k_factor",
                title = "Sheet Metal K-Factor & Bend Allowance",
                description = "Calculates exact flat pattern layout lengths using material thickness, inside bend radius, bend angle, and K-factor",
                category = "Metalworks",
                iconName = "Straighten",
                defaultSpan = 2,
                keywords = listOf("K-factor", "bend allowance", "BA", "sheet metal", "bend radius", "thickness", "flat pattern", "bend physics")
            ),
            ToolDefinition(
                id = "widget_metal_bend_deduction",
                title = "Bend Deduction & Setback Sizer",
                description = "Computes outside setback (OSSB) and bend deduction (BD) to set backgauges accurately on press brakes",
                category = "Metalworks",
                iconName = "Architecture",
                defaultSpan = 2,
                keywords = listOf("bend deduction", "BD", "setback", "OSSB", "press brake", "backgauge")
            ),
            ToolDefinition(
                id = "widget_metal_press_brake_tonnage",
                title = "Press Brake Tonnage Estimator",
                description = "Calculates required bending tonnage based on material tensile strength, sheet thickness, bend length, and V-die opening",
                category = "Metalworks",
                iconName = "Speed",
                defaultSpan = 2,
                keywords = listOf("press brake", "tonnage", "bending force", "tensile strength", "V-die", "die opening")
            ),
            ToolDefinition(
                id = "widget_metal_cone_unfolder",
                title = "Cone, Frustum & Transition Hopper Unfolder",
                description = "Generates 2D flat layout cutting arcs and chord lengths for round-to-round cones and eccentric reducers",
                category = "Metalworks",
                iconName = "AutoAwesome",
                defaultSpan = 2,
                keywords = listOf("cone", "frustum", "hopper", "unfolder", "flat pattern", "chord length", "eccentric reducer")
            ),
            ToolDefinition(
                id = "widget_metal_square_to_round",
                title = "Square-to-Round Transition Layout Engine",
                description = "Computes coordinate points and true-length triangulation lines for building ventilation and exhaust duct transitions",
                category = "Metalworks",
                iconName = "Grain",
                defaultSpan = 2,
                keywords = listOf("square to round", "transition", "duct", "triangulation", "coordinates", "exhaust")
            ),
            ToolDefinition(
                id = "widget_pipe_miter_saddle",
                title = "Pipe Miter & Saddle Cut Template Generator",
                description = "Generates 2D wrap-around templates for pipe-to-pipe tee joints, 90° saddles, and angular branch intersections",
                category = "Metalworks",
                iconName = "Architecture",
                defaultSpan = 2,
                keywords = listOf("pipe miter", "saddle cut", "wrap-around template", "tee joint", "branch intersection", "layout")
            ),
            ToolDefinition(
                id = "widget_pipe_rolling_offset",
                title = "Rolling Offset & 3D Pipe Travel Calculator",
                description = "Computes true travel length and compound fitting angles for 3D rolling offsets across vertical rise and horizontal roll",
                category = "Metalworks",
                iconName = "Navigation",
                defaultSpan = 2,
                keywords = listOf("rolling offset", "3D pipe", "travel length", "compound fitting", "vertical rise", "horizontal roll")
            ),
            ToolDefinition(
                id = "widget_pipe_flange_pcd",
                title = "Pipe Flange Bolt Hole Circle (PCD) Generator",
                description = "Calculates exact (X, Y) chord distances and coordinates for drilling evenly spaced bolt patterns on flanges and end-caps",
                category = "Metalworks",
                iconName = "GridOn",
                defaultSpan = 2,
                keywords = listOf("PCD", "bolt circle", "flange", "XY coordinates", "chord distance", "bolt pattern")
            ),
            ToolDefinition(
                id = "widget_pipe_orange_peel",
                title = "Orange Peel / Bullnose Pipe Cap Layout",
                description = "Flat pattern calculator for cutting wedge petals on pipe ends to form welded domed end-caps",
                category = "Metalworks",
                iconName = "CheckCircle",
                defaultSpan = 2,
                keywords = listOf("orange peel", "bullnose", "pipe cap", "wedge petals", "domed end")
            ),
            ToolDefinition(
                id = "widget_metal_thermal_distortion",
                title = "Thermal Shrinkage & Distortion Compensator",
                description = "Predicts angular distortion on single-V and double-V butt welds to establish proper pre-setting or clamping angles",
                category = "Metalworks",
                iconName = "Science",
                defaultSpan = 2,
                keywords = listOf("thermal shrinkage", "distortion", "angular distortion", "butt weld", "pre-setting", "clamping angle")
            ),
            ToolDefinition(
                id = "widget_metal_structural_profiles",
                title = "Structural Steel Profile Section Lookup",
                description = "Dimensional, weight, and section modulus (Z) table for universal beams (UB/UC), HSS/RHS/SHS, angle iron, and channels",
                category = "Metalworks",
                iconName = "HomeWork",
                defaultSpan = 2,
                keywords = listOf("structural steel", "universal beam", "UB", "UC", "HSS", "RHS", "SHS", "angle iron", "channel", "section modulus", "Z")
            ),
            ToolDefinition(
                id = "widget_metal_plasma_cutting",
                title = "Plasma & Oxy-Fuel Cutting Chart",
                description = "Recommends cutting tip orifice sizes, oxygen/acetylene pressures, standoff heights, and cutting travel speeds",
                category = "Metalworks",
                iconName = "FlashOn",
                defaultSpan = 2,
                keywords = listOf("plasma cutting", "oxy-fuel", "cutting tip", "orifice", "acetylene pressure", "standoff", "travel speed")
            ),
            ToolDefinition(
                id = "widget_metal_flame_straightening",
                title = "Flame Straightening & Spot Heating Guide",
                description = "Visual instructional guide on placing heat triangles, line heats, and wedge heats to pull warped structural beams true",
                category = "Metalworks",
                iconName = "Engineering",
                defaultSpan = 2,
                keywords = listOf("flame straightening", "spot heating", "heat triangle", "line heat", "wedge heat", "warp correction")
            ),
            ToolDefinition(
                id = "widget_weld_fillet_throat",
                title = "Fillet Weld Leg to Throat Sizer",
                description = "Converts between theoretical throat thickness, effective throat, and leg length for quality inspection",
                category = "Metalworks",
                iconName = "Straighten",
                defaultSpan = 2,
                keywords = listOf("fillet weld", "throat thickness", "effective throat", "leg length", "AWS", "inspection")
            ),
            ToolDefinition(
                id = "widget_weld_defects",
                title = "Weld Defect & Acceptance Guide (AWS D1.1)",
                description = "Photographic reference for diagnosing porosity, undercut, lack of fusion, cold lap, and convexity against code tolerances",
                category = "Metalworks",
                iconName = "CheckCircle",
                defaultSpan = 2,
                keywords = listOf("weld defect", "AWS D1.1", "ISO 5817", "porosity", "undercut", "lack of fusion", "cold lap", "convexity")
            ),
            ToolDefinition(
                id = "widget_weld_symbol_decoder",
                title = "Welding Symbol Blueprint Decoder",
                description = "Interactive visual builder and reference guide explaining reference lines, arrows, weld types, pitch spacing, and NDE callouts",
                category = "Metalworks",
                iconName = "Architecture",
                defaultSpan = 2,
                keywords = listOf("welding symbol", "blueprint decoder", "reference line", "arrow", "groove weld", "pitch spacing", "NDE")
            ),
            ToolDefinition(
                id = "widget_weld_schaeffler",
                title = "Ferrite Number & Schaeffler Diagram Tool",
                description = "Calculates duplex and austenitic stainless steel weld ferrite content from nickel and chromium equivalents to avoid hot cracking",
                category = "Metalworks",
                iconName = "Science",
                defaultSpan = 2,
                keywords = listOf("ferrite number", "FN", "Schaeffler diagram", "duplex", "austenitic", "stainless steel", "hot cracking")
            ),
            ToolDefinition(
                id = "widget_metal_surface_flatness",
                title = "Surface Plate Flatness Multi-Point Map",
                description = "Allows fabricators to input dial indicator readings across a grid to map workshop table warpage and anvil flatness",
                category = "Metalworks",
                iconName = "GridOn",
                defaultSpan = 2,
                keywords = listOf("surface plate", "anvil", "flatness map", "dial indicator", "grid warpage", "table calibration")
            ),
            ToolDefinition(
                id = "widget_weld_tungsten_grind",
                title = "Tungsten Electrode Grind & Color Table",
                description = "Guide for selecting electrode alloy (2% Thoriated, Ceriated, Lanthanated, Pure) and grinding taper angle for GTAW",
                category = "Metalworks",
                iconName = "Palette",
                defaultSpan = 2,
                keywords = listOf("tungsten electrode", "grind geometry", "thoriated", "ceriated", "lanthanated", "GTAW", "DCEN", "AC")
            ),
            ToolDefinition(
                id = "widget_pipe_hydro_test",
                title = "Hydrostatic Test & Wall Hoop Stress Sizer",
                description = "Computes safe hydro-test pressures for fabricated tanks and pipes based on ASME Section VIII allowable stresses and joint efficiency",
                category = "Metalworks",
                iconName = "WaterDrop",
                defaultSpan = 2,
                keywords = listOf("hydrostatic test", "pressure", "hoop stress", "ASME", "allowable stress", "joint efficiency", "wall thickness")
            ),
            ToolDefinition(
                id = "widget_ir_remote",
                title = "Jobsite & Commercial IR Remote",
                description = "Hardware IR transmitter for 60+ brands of AC, TV, Projector, Satellite & AV receivers",
                category = "Sensors",
                iconName = "SettingsRemote",
                defaultSpan = 2,
                keywords = listOf("ir remote", "infrared", "ac", "tv", "projector", "satellite", "av", "dyson", "gree", "daikin", "samsung")
            ),
            ToolDefinition(
                id = "widget_outdoor_activities",
                title = "Outdoor Activities & Topo Suite",
                description = "Vector topo map engine (10m/50m contours), dual-dial compass, barometric altimeter, survival & WFA",
                category = "Outdoor Activities",
                iconName = "Terrain",
                defaultSpan = 2,
                keywords = listOf("outdoor", "topo", "compass", "contour", "mbtiles", "expedition", "survival", "altimeter", "avalanche", "sos")
            ),
            ToolDefinition(
                id = "widget_usb_pro_camera",
                title = "USB Full-Screen Pro Camera",
                description = "Full-screen high-definition USB camera viewer supporting snapshot capture, video recording, clip playback, and wired/wireless connection modes.",
                category = "Sensors",
                iconName = "Camera",
                defaultSpan = 2,
                keywords = listOf("usb camera", "borescope", "endoscope", "full screen", "screenshot", "record video", "playback", "wireless", "wired")
            ),
            ToolDefinition(
                id = "widget_qr_code_scanner",
                title = "Instant QR Code Scanner",
                description = "Instantly scan QR codes to translate text, load web links, or open dedicated on-device apps with trade routing suggestions.",
                category = "Sensors",
                iconName = "Explore",
                defaultSpan = 2,
                keywords = listOf("qr code", "barcode", "scan", "translate", "url", "open apps", "instant scanner")
            ),
            ToolDefinition(
                id = "widget_settings",
                title = "App Settings",
                description = "Units, light/dark theme, precision, weather provider & sensors",
                category = "Utility",
                iconName = "Settings",
                defaultSpan = 2
            )
        )
    }
}
