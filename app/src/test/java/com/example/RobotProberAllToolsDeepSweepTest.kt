package com.example

import android.content.Context
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.domain.model.ToolDefinition
import com.example.ui.ViewModelFactory
import com.example.ui.navigation.NavGraph
import com.example.ui.navigation.ScreenRoutes
import com.example.ui.theme.BrillianToolsTheme
import org.junit.AfterClass
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RobotProberAllToolsDeepSweepTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("app_user_settings", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .putBoolean("is_first_launch", false)
            .commit()
    }

    private fun resolveRouteMatchingNavGraph(toolId: String): String {
        return when (toolId) {
            "widget_usb_pro_camera", "tool_usb_pro_camera" -> ScreenRoutes.UsbProCamera.route
            "widget_qr_code_scanner" -> ScreenRoutes.QrCodeScanner.route
            "widget_psychrometric" -> ScreenRoutes.Psychrometric.route
            "widget_refrigerant" -> ScreenRoutes.Refrigerant.route
            "widget_duct_sizer" -> ScreenRoutes.DuctSizer.route
            "widget_expansion_tank" -> ScreenRoutes.ExpansionTank.route
            "widget_sling_angle" -> ScreenRoutes.SlingAngle.route
            "widget_kerf_bending" -> ScreenRoutes.KerfBending.route
            "widget_cutlist_optimizer" -> ScreenRoutes.CutlistOptimizer.route
            "widget_dado_step_over" -> ScreenRoutes.DadoStepOver.route
            "widget_board_footage" -> ScreenRoutes.BoardFootage.route
            "widget_stair_layout" -> ScreenRoutes.StairLayout.route
            "widget_rafter_calculator" -> ScreenRoutes.RafterCalculator.route
            "widget_compound_miter" -> ScreenRoutes.CompoundMiter.route
            "widget_wood_moisture" -> ScreenRoutes.WoodMoisture.route
            "widget_joinery_spacing" -> ScreenRoutes.JoinerySpacing.route
            "widget_segmented_turning" -> ScreenRoutes.SegmentedTurning.route
            "widget_sagulator" -> ScreenRoutes.Sagulator.route
            "widget_grain_matching" -> ScreenRoutes.GrainMatching.route
            "widget_drill_tap_chart" -> ScreenRoutes.DrillTapChart.route
            "widget_blade_speed" -> ScreenRoutes.BladeSpeed.route
            "widget_ohms_law" -> ScreenRoutes.OhmsLaw.route
            "widget_voltage_drop" -> ScreenRoutes.VoltageDrop.route
            "widget_conduit_fill" -> ScreenRoutes.ConduitFill.route
            "widget_conduit_bender" -> ScreenRoutes.ConduitBender.route
            "widget_resistor_color_code" -> ScreenRoutes.ResistorColorCode.route
            "widget_breaker_panel" -> ScreenRoutes.BreakerPanel.route
            "widget_led_driver" -> ScreenRoutes.LedDriver.route
            "widget_box_fill_capacity" -> ScreenRoutes.BoxFillCapacity.route
            "widget_industrial_motor_fla" -> ScreenRoutes.IndustrialMotorFla.route
            "widget_solar_battery_sizer" -> ScreenRoutes.SolarBatterySizer.route
            "widget_current_loop_scaling" -> ScreenRoutes.CurrentLoopScaling.route
            "widget_power_factor_correction" -> ScreenRoutes.PowerFactorCorrection.route
            "widget_pipe_sizing" -> ScreenRoutes.PipeSizing.route
            "widget_hvac_load" -> ScreenRoutes.HvacLoad.route
            "widget_tile_grout" -> ScreenRoutes.TileGrout.route
            "widget_paint_coverage" -> ScreenRoutes.PaintCoverage.route
            "widget_drywall_stud" -> ScreenRoutes.DrywallStud.route
            "widget_safety_checklist" -> ScreenRoutes.SafetyChecklist.route
            "widget_chemical_msds" -> ScreenRoutes.ChemicalMsds.route
            "widget_concrete_volume" -> ScreenRoutes.ConcreteVolume.route
            "widget_rebar_estimator" -> ScreenRoutes.RebarEstimator.route
            "widget_cut_fill_earthwork" -> ScreenRoutes.CutFillEarthwork.route
            "widget_masonry_mortar" -> ScreenRoutes.MasonryMortar.route
            "widget_slope_drainage" -> ScreenRoutes.SlopeDrainage.route
            "widget_soil_asphalt" -> ScreenRoutes.SoilAsphalt.route
            "widget_beam_deflection" -> ScreenRoutes.BeamDeflection.route
            "widget_earthwork_grade" -> ScreenRoutes.EarthworkGrade.route
            "widget_framing_roofing" -> ScreenRoutes.FramingRoofing.route
            "widget_meteorology" -> ScreenRoutes.Meteorology.route
            "widget_parabolic_focus" -> ScreenRoutes.ParabolicFocus.route
            "widget_stationing_cogo" -> ScreenRoutes.StationingCogo.route
            "widget_retaining_wall_sizer" -> ScreenRoutes.RetainingWallSizer.route
            "widget_aggregate_sieve" -> ScreenRoutes.AggregateSieve.route
            "widget_equipment_hauling" -> ScreenRoutes.EquipmentHauling.route
            "widget_stormwater_rational" -> ScreenRoutes.StormwaterRational.route
            "widget_digital_level" -> ScreenRoutes.DigitalLevel.route
            "widget_compass" -> ScreenRoutes.Compass.route
            "widget_decibel_meter" -> ScreenRoutes.DecibelMeter.route
            "widget_strobe_tachometer" -> ScreenRoutes.StrobeTachometer.route
            "widget_thermal_camera" -> ScreenRoutes.ThermalCamera.route
            "widget_usb_endoscope" -> ScreenRoutes.UsbEndoscope.route
            "widget_vibration_analyzer" -> ScreenRoutes.VibrationAnalyzer.route
            "widget_ble_multimeter" -> ScreenRoutes.BleMultimeter.route
            "widget_sun_path_tracker" -> ScreenRoutes.SunPathTracker.route
            "widget_barometric_altimeter" -> ScreenRoutes.BarometricAltimeter.route
            "widget_surface_lux_meter" -> ScreenRoutes.LuxMeter.route
            "widget_fractional_calc" -> ScreenRoutes.FractionalCalculator.route
            "widget_ar_measurement" -> ScreenRoutes.ArMeasurement.route
            "widget_ar_area_calculator" -> ScreenRoutes.ArAreaCalculator.route
            "widget_plumb_bob" -> ScreenRoutes.PlumbBob.route
            "widget_stud_detector" -> ScreenRoutes.StudDetector.route
            "widget_laser_measure" -> ScreenRoutes.LaserMeasure.route
            "widget_jobsite_ir_remote", "widget_ir_remote" -> ScreenRoutes.JobsiteIrRemote.route
            "widget_outdoor_activities" -> ScreenRoutes.OutdoorActivities.route
            "widget_material_inventory" -> ScreenRoutes.MaterialInventory.route
            "widget_tasks" -> ScreenRoutes.TaskChecklist.route
            "widget_timer" -> ScreenRoutes.FocusTimer.route
            "widget_unit_converter" -> ScreenRoutes.UnitConverter.route
            "widget_calculator" -> ScreenRoutes.Calculator.route
            "widget_wood_species_studio" -> ScreenRoutes.WoodSpeciesStudio.route
            "widget_painting_coating_studio" -> ScreenRoutes.PaintingCoatingStudio.route
            "widget_weld_heat_input", "widget_weld_carbon_equivalent", "widget_weld_electrode_selector",
            "widget_weld_deposition_estimator", "widget_weld_shielding_gas", "widget_metal_k_factor",
            "widget_metal_bend_deduction", "widget_metal_press_brake_tonnage", "widget_metal_cone_unfolder",
            "widget_metal_square_to_round", "widget_pipe_miter_saddle", "widget_pipe_rolling_offset",
            "widget_pipe_flange_pcd", "widget_pipe_orange_peel", "widget_metal_thermal_distortion",
            "widget_metal_structural_profiles", "widget_metal_plasma_cutting", "widget_metal_flame_straightening",
            "widget_weld_fillet_throat", "widget_weld_defects", "widget_weld_symbol_decoder",
            "widget_weld_schaeffler", "widget_metal_surface_flatness", "widget_weld_tungsten_grind",
            "widget_pipe_hydro_test" -> ScreenRoutes.MetalworksStudio.createRoute(toolId)
            "widget_color_palette", "widget_color_tools" -> ScreenRoutes.ColorDevTools.route
            "widget_notes" -> ScreenRoutes.QuickNotes.route
            "widget_sensor_detector" -> ScreenRoutes.SensorDetector.route
            "widget_settings" -> ScreenRoutes.Settings.route
            else -> ScreenRoutes.ToolCatalog.route
        }
    }

    private fun runToolBatch(startInclusive: Int, endExclusive: Int) {
        val factory = ViewModelFactory(context)
        var navController: NavHostController? = null
        var density = 1f

        composeRule.setContent {
            density = LocalDensity.current.density
            val nc = rememberNavController()
            navController = nc
            BrillianToolsTheme(darkTheme = false) {
                NavGraph(navController = nc, factory = factory)
            }
        }
        composeRule.waitForIdle()

        val allTools = ToolDefinition.ALL_TOOLS
        val actualEnd = minOf(endExclusive, allTools.size)

        for (i in startInclusive until actualEnd) {
            val tool = allTools[i]
            val route = resolveRouteMatchingNavGraph(tool.id)
            var mounted = false
            var returnedBack = false
            var titleMatched = true
            var clickableFound = 0
            var clickedOk = 0
            var editableFound = 0
            var editedOk = 0
            var smallTargetsCount = 0
            val notes = mutableListOf<String>()
            var errorMsg: String? = null

            try {
                composeRule.runOnUiThread {
                    navController?.navigate(route)
                }
                composeRule.waitForIdle()

                val activeRoute = navController?.currentBackStackEntry?.destination?.route ?: ""
                mounted = activeRoute != ScreenRoutes.Dashboard.route && activeRoute.isNotBlank()
                if (!mounted) {
                    errorMsg = "Navigation to '$route' stayed on '$activeRoute'"
                    RobotAuditReport.addFinding("CRITICAL", tool.id, "${tool.title} failed to mount", errorMsg)
                } else {
                    // Check TopBar title fallback ("Brillian Tools") on screens that use NavGraph TopAppBar
                    val isFullscreen = route == ScreenRoutes.UsbProCamera.route || route.startsWith("tool_metalworks_studio")
                    val visibleTexts = RobotSemanticsCrawler.dumpVisibleTexts(composeRule)
                    if (!isFullscreen && visibleTexts.firstOrNull() == "Brillian Tools") {
                        titleMatched = false
                        notes.add("Generic topBarTitle fallback")
                        RobotAuditReport.addFinding(
                            "LOW",
                            tool.id,
                            "Missing dedicated TopBar title for '${tool.title}'",
                            "NavGraph.kt topBarTitle `when(currentRoute)` falls back to generic 'Brillian Tools' on route '$activeRoute'."
                        )
                    }

                    // Check OutdoorActivities has no duplicate TopAppBar (btn_outdoor_back must not coexist with top_app_bar_back_button)
                    if (route == ScreenRoutes.OutdoorActivities.route &&
                        composeRule.onAllNodesWithTag("top_app_bar_back_button").fetchSemanticsNodes().isNotEmpty() &&
                        composeRule.onAllNodesWithTag("btn_outdoor_back").fetchSemanticsNodes().isNotEmpty()
                    ) {
                        notes.add("Double TopAppBar detected")
                        RobotAuditReport.addFinding(
                            "MEDIUM",
                            "OutdoorActivitiesScreen",
                            "Duplicate nested TopAppBar rendered inside NavGraph Scaffold",
                            "Both NavGraph's BrillianTopAppBar and OutdoorActivitiesScreen's internal TopAppBar render simultaneously with two back buttons."
                        )
                    }

                    val stats = RobotSemanticsCrawler.probeCurrentScreen(
                        rule = composeRule,
                        density = density,
                        areaLabel = tool.id,
                        maxInputs = 3,
                        maxClicks = 8
                    )
                    clickableFound = stats.clickableFound
                    clickedOk = stats.clickedSuccess
                    editableFound = stats.editableFound
                    editedOk = stats.editedSuccess
                    smallTargetsCount = stats.smallTouchTargets.size

                    if (stats.errors.isNotEmpty()) {
                        errorMsg = stats.errors.joinToString("; ")
                        RobotAuditReport.addFinding("HIGH", tool.id, "${tool.title}: interaction error(s)", errorMsg)
                    }
                    if (stats.smallTouchTargets.isNotEmpty()) {
                        notes.add("${smallTargetsCount} <48dp targets")
                    }

                    // Navigate back to Dashboard
                    when {
                        composeRule.onAllNodesWithTag("metalworks_back_button").fetchSemanticsNodes().isNotEmpty() -> {
                            composeRule.onNodeWithTag("metalworks_back_button").performClick()
                        }
                        composeRule.onAllNodesWithTag("btn_camera_close").fetchSemanticsNodes().isNotEmpty() -> {
                            composeRule.onNodeWithTag("btn_camera_close").performClick()
                        }
                        composeRule.onAllNodesWithTag("top_app_bar_back_button").fetchSemanticsNodes().isNotEmpty() -> {
                            composeRule.onNodeWithTag("top_app_bar_back_button").performClick()
                        }
                        else -> {
                            composeRule.runOnUiThread {
                                navController?.popBackStack(ScreenRoutes.Dashboard.route, inclusive = false)
                            }
                        }
                    }
                    composeRule.waitForIdle()

                    val afterBackRoute = navController?.currentBackStackEntry?.destination?.route
                    if (afterBackRoute != ScreenRoutes.Dashboard.route) {
                        composeRule.runOnUiThread {
                            navController?.popBackStack(ScreenRoutes.Dashboard.route, inclusive = false)
                        }
                        composeRule.waitForIdle()
                        returnedBack = false
                        notes.add("Back nav left route=$afterBackRoute")
                        RobotAuditReport.addFinding(
                            "HIGH",
                            tool.id,
                            "${tool.title}: Back button did not return cleanly to Dashboard",
                            "After pressing back on '$route', backstack remained on '$afterBackRoute'."
                        )
                    } else {
                        returnedBack = true
                    }
                }
            } catch (t: Throwable) {
                errorMsg = "${t.javaClass.simpleName}: ${t.message}"
                RobotAuditReport.addFinding("CRITICAL", tool.id, "${tool.title} crashed during Robolectric probe", errorMsg)
                // Attempt recovery back to dashboard
                try {
                    composeRule.runOnUiThread {
                        navController?.popBackStack(ScreenRoutes.Dashboard.route, inclusive = false)
                    }
                    composeRule.waitForIdle()
                } catch (_: Throwable) {}
            }

            RobotAuditReport.toolRecords.add(
                RobotAuditReport.ToolProbeRecord(
                    index = i + 1,
                    id = tool.id,
                    title = tool.title,
                    category = tool.category,
                    mounted = mounted,
                    titleMatched = titleMatched,
                    clickableCount = clickableFound,
                    clickedCount = clickedOk,
                    editableCount = editableFound,
                    editedCount = editedOk,
                    smallTargetsCount = smallTargetsCount,
                    returnedBackCleanly = returnedBack,
                    notes = if (notes.isEmpty()) "OK" else notes.joinToString(", "),
                    error = errorMsg
                )
            )
        }
    }

    @Test fun sweep_batch_01_tools_001_to_020() = runToolBatch(0, 20)
    @Test fun sweep_batch_02_tools_021_to_040() = runToolBatch(20, 40)
    @Test fun sweep_batch_03_tools_041_to_060() = runToolBatch(40, 60)
    @Test fun sweep_batch_04_tools_061_to_080() = runToolBatch(60, 80)
    @Test fun sweep_batch_05_tools_081_to_100() = runToolBatch(80, 100)
    @Test fun sweep_batch_06_tools_101_to_end() = runToolBatch(100, ToolDefinition.ALL_TOOLS.size)

    @Test
    fun sweep_batch_07_painting_studio_all_9_tabs_and_secondary_screens() {
        val factory = ViewModelFactory(context)
        var navController: NavHostController? = null
        var density = 1f

        composeRule.setContent {
            density = LocalDensity.current.density
            val nc = rememberNavController()
            navController = nc
            BrillianToolsTheme(darkTheme = false) {
                NavGraph(navController = nc, factory = factory)
            }
        }
        composeRule.waitForIdle()

        // 1. Painting & Coating Studio — crawl all 9 tabs (25 tools)
        composeRule.runOnUiThread {
            navController?.navigate(ScreenRoutes.PaintingCoatingStudio.route)
        }
        composeRule.waitForIdle()

        val paintingTabs = listOf(
            "Coverage & Sizing",
            "Mixing & Viscosity",
            "Equipment",
            "Climate Curing",
            "Wood Finish",
            "Metal Rust",
            "Wall & Plaster",
            "Laminates & HPL",
            "Color Match & Brands"
        )
        var paintingTabsPassed = 0
        for (tabName in paintingTabs) {
            try {
                composeRule.onAllNodesWithText(tabName)[0].performScrollTo().performClick()
                composeRule.waitForIdle()
                val stats = RobotSemanticsCrawler.probeCurrentScreen(
                    composeRule, density, "PaintingTab:$tabName", maxInputs = 3, maxClicks = 6
                )
                if (stats.errors.isNotEmpty()) {
                    RobotAuditReport.addFinding("HIGH", "PaintingCoatingStudio", "Tab '$tabName' errors", stats.errors.joinToString("; "))
                } else {
                    paintingTabsPassed++
                }
            } catch (t: Throwable) {
                RobotAuditReport.addFinding("HIGH", "PaintingCoatingStudio", "Tab '$tabName' crashed/unreachable", "${t.javaClass.simpleName}: ${t.message}")
            }
        }
        RobotAuditReport.coreFlowResults.add("Painting & Coating Studio: $paintingTabsPassed/${paintingTabs.size} tabs probed cleanly")

        // 2. Probe CustomizeDashboardScreen directly via NavController
        for ((name, route) in listOf(
            "CustomizeDashboard" to ScreenRoutes.CustomizeDashboard.route
        )) {
            try {
                composeRule.runOnUiThread { navController?.navigate(route) }
                composeRule.waitForIdle()
                val stats = RobotSemanticsCrawler.probeCurrentScreen(composeRule, density, name, maxInputs = 1, maxClicks = 6)
                RobotAuditReport.coreFlowResults.add("$name ($route): clicked ${stats.clickedSuccess}/${stats.clickableFound}, errors=${stats.errors.size}")
                composeRule.runOnUiThread { navController?.popBackStack(ScreenRoutes.Dashboard.route, inclusive = false) }
                composeRule.waitForIdle()
            } catch (t: Throwable) {
                RobotAuditReport.addFinding("HIGH", name, "$name failed probe", "${t.javaClass.simpleName}: ${t.message}")
            }
        }

        // 3. Probe Metalworks Studio dropdown selector switching across all 25 tools
        try {
            composeRule.runOnUiThread { navController?.navigate(ScreenRoutes.MetalworksStudio.createRoute("widget_weld_heat_input")) }
            composeRule.waitForIdle()
            val metalTools = ToolDefinition.ALL_TOOLS.filter { it.category == "Metalworks" }
            var selectorSwitchCount = 0
            // Sample dropdown selector interaction cleanly
            composeRule.onNodeWithTag("metalworks_tool_selector_button").performClick()
            composeRule.waitForIdle()
            if (composeRule.onAllNodesWithText(metalTools.last().title, substring = true).fetchSemanticsNodes().isNotEmpty()) {
                composeRule.onAllNodesWithText(metalTools.last().title, substring = true)[0].performClick()
                composeRule.waitForIdle()
                selectorSwitchCount++
            }
            RobotAuditReport.coreFlowResults.add("Metalworks Studio: ${metalTools.size} registered tools in catalog, dropdown selector switch tested=$selectorSwitchCount")
        } catch (t: Throwable) {
            RobotAuditReport.addFinding("HIGH", "MetalworksStudio", "Dropdown selector probe failed", "${t.javaClass.simpleName}: ${t.message}")
        }
    }

    companion object {
        @JvmStatic
        @AfterClass
        fun flushReport() {
            RobotAuditReport.writeReportFile()
        }
    }
}
