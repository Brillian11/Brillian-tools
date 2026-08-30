package com.example.ui.navigation

sealed class ScreenRoutes(val route: String) {
    object Dashboard : ScreenRoutes("dashboard")
    object CustomizeDashboard : ScreenRoutes("customize_dashboard")
    object ToolCatalog : ScreenRoutes("tool_catalog")
    object SyncQueue : ScreenRoutes("sync_queue")
    object Settings : ScreenRoutes("app_settings")
    
    // Woodworking Suite Routes
    object KerfBending : ScreenRoutes("tool_kerf_bending")
    object CutlistOptimizer : ScreenRoutes("tool_cutlist_optimizer")
    object DadoStepOver : ScreenRoutes("tool_dado_step_over")
    object BoardFootage : ScreenRoutes("tool_board_footage")
    object StairLayout : ScreenRoutes("tool_stair_layout")
    object RafterCalculator : ScreenRoutes("tool_rafter_calculator")
    object CompoundMiter : ScreenRoutes("tool_compound_miter")
    object WoodMoisture : ScreenRoutes("tool_wood_moisture")
    object JoinerySpacing : ScreenRoutes("tool_joinery_spacing")
    object SegmentedTurning : ScreenRoutes("tool_segmented_turning")
    object Sagulator : ScreenRoutes("tool_sagulator")
    object GrainMatching : ScreenRoutes("tool_grain_matching")
    object DrillTapChart : ScreenRoutes("tool_drill_tap_chart")
    object BladeSpeed : ScreenRoutes("tool_blade_speed")

    // Electrical & Electronics Suite Routes
    object OhmsLaw : ScreenRoutes("tool_ohms_law")
    object VoltageDrop : ScreenRoutes("tool_voltage_drop")
    object ConduitFill : ScreenRoutes("tool_conduit_fill")
    object ConduitBender : ScreenRoutes("tool_conduit_bender")
    object ResistorColorCode : ScreenRoutes("tool_resistor_color_code")
    object BreakerPanel : ScreenRoutes("tool_breaker_panel")
    object LedDriver : ScreenRoutes("tool_led_driver")
    object BoxFillCapacity : ScreenRoutes("tool_box_fill_capacity")
    object IndustrialMotorFla : ScreenRoutes("tool_industrial_motor_fla")
    object SolarBatterySizer : ScreenRoutes("tool_solar_battery_sizer")
    object CurrentLoopScaling : ScreenRoutes("tool_current_loop_scaling")
    object PowerFactorCorrection : ScreenRoutes("tool_power_factor_correction")

    // Maintenance, Plumbing & Housework Suite Routes
    object PipeSizing : ScreenRoutes("tool_pipe_sizing")
    object HvacLoad : ScreenRoutes("tool_hvac_load")
    object TileGrout : ScreenRoutes("tool_tile_grout")
    object PaintCoverage : ScreenRoutes("tool_paint_coverage")
    object DrywallStud : ScreenRoutes("tool_drywall_stud")

    // Safety & Compliance Suite Routes
    object SafetyChecklist : ScreenRoutes("tool_safety_checklist")
    object ChemicalMsds : ScreenRoutes("tool_chemical_msds")

    // Civil Engineering, Earthwork & Masonry Suite Routes
    object ConcreteVolume : ScreenRoutes("tool_concrete_volume")
    object RebarEstimator : ScreenRoutes("tool_rebar_estimator")
    object CutFillEarthwork : ScreenRoutes("tool_cut_fill_earthwork")
    object MasonryMortar : ScreenRoutes("tool_masonry_mortar")
    object SlopeDrainage : ScreenRoutes("tool_slope_drainage")
    object SoilAsphalt : ScreenRoutes("tool_soil_asphalt")
    object BeamDeflection : ScreenRoutes("tool_beam_deflection")
    object EarthworkGrade : ScreenRoutes("tool_earthwork_grade")
    object FramingRoofing : ScreenRoutes("tool_framing_roofing")
    object Meteorology : ScreenRoutes("tool_meteorology")
    object ParabolicFocus : ScreenRoutes("tool_parabolic_focus")
    object StationingCogo : ScreenRoutes("tool_stationing_cogo")
    object RetainingWallSizer : ScreenRoutes("tool_retaining_wall_sizer")
    object AggregateSieve : ScreenRoutes("tool_aggregate_sieve")
    object EquipmentHauling : ScreenRoutes("tool_equipment_hauling")
    object StormwaterRational : ScreenRoutes("tool_stormwater_rational")

    // Sensor & Hardware Suite Routes
    object DigitalLevel : ScreenRoutes("tool_digital_level")
    object Compass : ScreenRoutes("tool_compass")
    object DecibelMeter : ScreenRoutes("tool_decibel_meter")
    object StrobeTachometer : ScreenRoutes("tool_strobe_tachometer")
    object ThermalCamera : ScreenRoutes("tool_thermal_camera")
    object UsbEndoscope : ScreenRoutes("tool_usb_endoscope")
    object VibrationAnalyzer : ScreenRoutes("tool_vibration_analyzer")
    object BleMultimeter : ScreenRoutes("tool_ble_multimeter")
    object SunPathTracker : ScreenRoutes("tool_sun_path_tracker")
    object BarometricAltimeter : ScreenRoutes("tool_barometric_altimeter")
    object LuxMeter : ScreenRoutes("tool_lux_meter")
    object FractionalCalculator : ScreenRoutes("tool_fractional_calc")
    object ArMeasurement : ScreenRoutes("tool_ar_measurement")
    object ArAreaCalculator : ScreenRoutes("tool_ar_area_calculator")
    object PlumbBob : ScreenRoutes("tool_plumb_bob")
    object StudDetector : ScreenRoutes("tool_stud_detector")
    object LaserMeasure : ScreenRoutes("tool_laser_measure")
    object JobsiteIrRemote : ScreenRoutes("tool_jobsite_ir_remote")

    // Inventory Route
    object MaterialInventory : ScreenRoutes("material_inventory")

    // Legacy Utility Tool Routes
    object FocusTimer : ScreenRoutes("tool_focus_timer")
    object UnitConverter : ScreenRoutes("tool_unit_converter")
    object ColorDevTools : ScreenRoutes("tool_color_dev_tools")
    object QuickNotes : ScreenRoutes("tool_quick_notes")
    object TaskChecklist : ScreenRoutes("tool_task_checklist")
    
    // Welcome & Smart Calculator
    object Psychrometric : ScreenRoutes("widget_psychrometric")
    object Refrigerant : ScreenRoutes("widget_refrigerant")
    object DuctSizer : ScreenRoutes("widget_duct_sizer")
    object ExpansionTank : ScreenRoutes("widget_expansion_tank")
    object SlingAngle : ScreenRoutes("widget_sling_angle")

    object Welcome : ScreenRoutes("welcome")
    object Calculator : ScreenRoutes("tool_calculator")
    object Library : ScreenRoutes("library")
    object PaintingCoatingStudio : ScreenRoutes("tool_painting_coating_studio")
    object WoodSpeciesStudio : ScreenRoutes("tool_wood_species_studio")
    object MetalworksStudio : ScreenRoutes("tool_metalworks_studio?toolId={toolId}") {
        fun createRoute(toolId: String = "widget_weld_heat_input") = "tool_metalworks_studio?toolId=$toolId"
    }
    object About : ScreenRoutes("tool_about")
    object OutdoorActivities : ScreenRoutes("tool_outdoor_activities")

    // New requested tools
    object UsbProCamera : ScreenRoutes("tool_usb_pro_camera")
    object QrCodeScanner : ScreenRoutes("tool_qr_code_scanner")
}
