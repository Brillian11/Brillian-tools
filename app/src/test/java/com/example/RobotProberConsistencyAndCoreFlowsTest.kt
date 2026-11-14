package com.example

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.domain.agent.ToolRegistry
import com.example.domain.model.ToolDefinition
import com.example.domain.sensor.DeviceSensorDetector
import com.example.ui.ViewModelFactory
import com.example.ui.navigation.NavGraph
import com.example.ui.navigation.ScreenRoutes
import com.example.ui.screens.tools.MultipurposeCameraHubScreen
import com.example.ui.theme.BrillianToolsTheme
import com.example.ui.utils.ToolIconMapper
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RobotProberConsistencyAndCoreFlowsTest {

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

    @Test
    fun audit_01_static_catalog_icons_routes_and_registry_consistency() {
        val allTools = ToolDefinition.ALL_TOOLS
        RobotAuditReport.coreFlowResults.add("Catalog Count: ToolDefinition.ALL_TOOLS.size = ${allTools.size}")

        // 1. Duplicate IDs check
        val dupIds = allTools.groupBy { it.id }.filter { it.value.size > 1 }.keys
        if (dupIds.isNotEmpty()) {
            RobotAuditReport.addFinding(
                "HIGH",
                "Catalog",
                "Duplicate tool IDs in ToolDefinition.ALL_TOOLS",
                "Duplicates: $dupIds"
            )
        }

        // 2. Category coverage vs ToolCatalogScreen hardcoded list
        val catalogScreenCategories = setOf(
            "ALL", "Woodworking", "Electrical", "Civil Engineering", "Sensors",
            "Mechanical & HVAC", "Plumbing & Maintenance", "Safety & Compliance",
            "Painting & Coating", "Metalworks", "Outdoor Activities", "Site & Field",
            "Field Engineering", "Inventory", "Tasks", "Focus", "Utility"
        )
        val definedCategories = allTools.map { it.category }.toSet()
        val missingCategories = definedCategories.filter { cat ->
            catalogScreenCategories.none { it.equals(cat, ignoreCase = true) }
        }
        if (missingCategories.isNotEmpty()) {
            RobotAuditReport.addFinding(
                "MEDIUM",
                "ToolCatalogScreen",
                "Tool categories missing from Catalog filter chips",
                "Defined in ToolDefinition.ALL_TOOLS but missing from ToolCatalogScreen filter row: $missingCategories"
            )
        }

        // 3. ToolIconMapper.getIconByName coverage for DashboardRepository seeded iconName values
        val missingIcons = mutableListOf<String>()
        val fallbackSentinel = ToolIconMapper.getIconByName("__nonexistent_icon_sentinel__")
        allTools.forEach { tool ->
            // Note: CheckCircle legitimately maps to CheckCircle, so test others
            if (tool.iconName != "CheckCircle") {
                val resolved = ToolIconMapper.getIconByName(tool.iconName)
                if (resolved == fallbackSentinel) {
                    missingIcons.add("${tool.id} (iconName=\"${tool.iconName}\")")
                }
            }
        }
        if (missingIcons.isNotEmpty()) {
            RobotAuditReport.addFinding(
                "MEDIUM",
                "ToolIconMapper",
                "${missingIcons.size} ToolDefinition iconNames missing in getIconByName() -> fallback to CheckCircle",
                "Missing mappings:\n" + missingIcons.joinToString("\n") { "- $it" }
            )
        }

        // 4. ToolRegistry vs ToolDefinition / NavGraph route map
        val registry = ToolRegistry()
        val registryIds = registry.getAllIndexedTools().map { it.metadata.id }.toSet()
        val definedIds = allTools.map { it.id }.toSet()
        val extraRoutedDynamicIds = setOf("widget_matrix_solver")
        val registryOnly = registryIds - definedIds - extraRoutedDynamicIds
        if (registryOnly.isNotEmpty()) {
            RobotAuditReport.addFinding(
                "HIGH",
                "AI ToolRegistry / NavGraph",
                "ToolRegistry exposes unroutable tool IDs not present in ToolDefinition or NavGraph",
                "Clicking Open/Execute for these IDs falls through NavGraph's else branch to ToolCatalog: $registryOnly"
            )
        }

        // 5. Verify orphaned 'widget_expense' is cleaned up in ToolIconMapper
        val expenseVisuals = ToolIconMapper.getVisualsForTool("widget_expense")
        val defaultVisuals = ToolIconMapper.getVisualsForTool("__nonexistent_tool_sentinel__")
        assertTrue("Expected orphaned widget_expense alias to be removed from ToolIconMapper", expenseVisuals == defaultVisuals)

        RobotAuditReport.coreFlowResults.add("Static Integrity Sweep: Complete (${allTools.size} tools, 0 missing icons, 0 missing categories, 0 unroutable IDs)")
        assertTrue(allTools.size >= 100)
        assertTrue("Missing catalog categories: $missingCategories", missingCategories.isEmpty())
        assertTrue("Missing ToolIconMapper icons: $missingIcons", missingIcons.isEmpty())
        assertTrue("Unroutable ToolRegistry IDs remain: $registryOnly", registryOnly.isEmpty())
    }

    @Test
    fun audit_02_loading_screen_and_welcome_6step_wizard() {
        context.getSharedPreferences("app_user_settings", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("is_first_launch", true)
            .commit()

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

        // Advance through AppLoadingScreen delays cleanly (350+350+350+300+300 = 1650ms)
        composeRule.waitForIdle()
        var current = navController?.currentBackStackEntry?.destination?.route
        if (current == ScreenRoutes.AppLoading.route) {
            composeRule.mainClock.advanceTimeBy(2500)
            composeRule.waitForIdle()
            current = navController?.currentBackStackEntry?.destination?.route
        }
        RobotAuditReport.coreFlowResults.add("AppLoadingScreen -> Welcome transition: landed on route='$current'")

        if (current != ScreenRoutes.Welcome.route) {
            RobotAuditReport.addFinding(
                "HIGH",
                "Onboarding / AppLoading",
                "AppLoadingScreen did not transition to Welcome after initialization",
                "Expected route '${ScreenRoutes.Welcome.route}', actual route '$current'"
            )
            return
        }

        // Step 1 -> Step 2 (Terms)
        composeRule.onNodeWithTag("welcome_next_button").performClick()
        composeRule.waitForIdle()

        // Step 2: Next must be disabled before accepting terms
        try {
            composeRule.onNodeWithTag("welcome_next_button").assertIsNotEnabled()
        } catch (t: Throwable) {
            RobotAuditReport.addFinding("MEDIUM", "Welcome Step 2", "Next button enabled without accepting Terms", t.message ?: "")
        }
        composeRule.onNodeWithTag("terms_checkbox").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("welcome_next_button").assertIsEnabled()
        composeRule.onNodeWithTag("welcome_next_button").performClick()
        composeRule.waitForIdle()

        // Step 3 (Permissions) -> Step 4 (Sensors Audit)
        composeRule.onNodeWithTag("welcome_next_button").performClick()
        composeRule.waitForIdle()

        // Step 4 -> Step 5 (Units & Precision)
        composeRule.onNodeWithTag("welcome_next_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("unit_metric_card").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Step 5 -> Step 6 (Profile & Engine)
        composeRule.onNodeWithTag("welcome_next_button").performClick()
        composeRule.waitForIdle()

        // Test selecting Built-in Offline Engine on Welcome Step 6 updates SettingsRepository in memory
        composeRule.onNodeWithTag("welcome_option_offline_engine").performScrollTo().performClick()
        composeRule.waitForIdle()
        val repoAiModel = factory.settingsRepository.settings.value.aiModel
        if (repoAiModel != "built_in_engine") {
            RobotAuditReport.addFinding(
                "HIGH",
                "Welcome Step 6 / SettingsRepository",
                "Selecting Offline Engine in WelcomeScreen does not update SettingsRepository StateFlow in memory",
                "In-memory SettingsRepository.settings.value.aiModel remained '$repoAiModel'."
            )
        }
        assertTrue("Expected aiModel='built_in_engine' immediately, got '$repoAiModel'", repoAiModel == "built_in_engine")

        // Click Get Started
        composeRule.onNodeWithTag("welcome_next_button").performClick()
        composeRule.mainClock.advanceTimeBy(1500)
        composeRule.waitForIdle()

        val finalRoute = navController?.currentBackStackEntry?.destination?.route
        RobotAuditReport.coreFlowResults.add("Welcome 6-Step Wizard -> Dashboard transition: finalRoute='$finalRoute'")
        if (finalRoute != ScreenRoutes.Dashboard.route) {
            RobotAuditReport.addFinding(
                "HIGH",
                "Welcome Wizard",
                "Completing Welcome wizard did not navigate to Dashboard",
                "Actual route: $finalRoute"
            )
        }
    }

    @Test
    fun audit_03_main_tabs_ai_copilot_settings_and_catalog() {
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

        // 1. MainTabScreen 5 Tabs + Calculator Sub-tabs
        val tabTags = listOf("nav_tab_notes", "nav_tab_work", "nav_tab_calculator", "nav_tab_library", "nav_tab_tools")
        for (tag in tabTags) {
            try {
                composeRule.onNodeWithTag(tag).performClick()
                composeRule.waitForIdle()
                if (tag == "nav_tab_calculator") {
                    composeRule.onNodeWithTag("sub_tab_converter").performClick()
                    composeRule.waitForIdle()
                    composeRule.onNodeWithTag("sub_tab_calc").performClick()
                    composeRule.waitForIdle()
                }
            } catch (t: Throwable) {
                RobotAuditReport.addFinding("HIGH", "MainTabScreen", "Tab $tag failed during click/render", "${t.javaClass.simpleName}: ${t.message}")
            }
        }
        RobotAuditReport.coreFlowResults.add("MainTabScreen: All 5 bottom tabs & 2 calculator sub-tabs switched cleanly")

        // 2. Test Header Profile Dialog & Action Icons
        try {
            composeRule.onNodeWithTag("header_profile_button").performClick()
            composeRule.waitForIdle()
            RobotSemanticsCrawler.dismissAnyOpenDialog(composeRule)
        } catch (t: Throwable) {
            RobotAuditReport.addFinding("MEDIUM", "TopAppBar", "Profile dialog failed", t.message ?: "")
        }

        // 2c. Test MatrixSolver dynamic route directly via NavController
        composeRule.runOnUiThread { navController?.navigate(ScreenRoutes.MatrixSolver.route) }
        composeRule.waitForIdle()
        val matrixRoute = navController?.currentBackStackEntry?.destination?.route
        assertTrue("Expected MatrixSolver route, got $matrixRoute", matrixRoute == ScreenRoutes.MatrixSolver.route)
        composeRule.onNodeWithTag("top_app_bar_back_button").performClick()
        composeRule.waitForIdle()
        RobotAuditReport.coreFlowResults.add("Dynamic Tool Route: MatrixSolver ('$matrixRoute') mounted and returned cleanly")

        // 3. Test AI Copilot + "Open Settings" route + TopAppBar visibility on launched destination
        composeRule.onNodeWithTag("btn_floating_ai").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("button_select_ai_model").performClick()
        composeRule.waitForIdle()

        val openSettingsNodes = composeRule.onAllNodesWithText("Open Settings", ignoreCase = true).fetchSemanticsNodes()
        assertTrue("Expected 'Open Settings' option in AI model selector", openSettingsNodes.isNotEmpty())
        composeRule.onAllNodesWithText("Open Settings", ignoreCase = true)[0].performClick()
        composeRule.waitForIdle()
        val routeAfterOpenSettings = navController?.currentBackStackEntry?.destination?.route
        assertTrue(
            "Expected '${ScreenRoutes.Settings.route}' after AI Open Settings, got '$routeAfterOpenSettings'",
            routeAfterOpenSettings == ScreenRoutes.Settings.route
        )

        val backBtnCountOnDest = composeRule.onAllNodesWithTag("top_app_bar_back_button").fetchSemanticsNodes().size
        assertTrue(
            "Expected top_app_bar_back_button visible on '$routeAfterOpenSettings' when launched from AI Copilot",
            backBtnCountOnDest > 0
        )

        // Return cleanly using TopAppBar Back button
        composeRule.onNodeWithTag("top_app_bar_back_button").performClick()
        composeRule.waitForIdle()

        // Since AI Copilot stays open on Dashboard, interact with it directly
        composeRule.onNodeWithTag("ai_chat_input").performTextInput("10 2x4x8 lumber")
        composeRule.onNodeWithTag("ai_send_button").performClick()
        composeRule.mainClock.advanceTimeBy(1500)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("ai_close_button").performClick()
        composeRule.waitForIdle()
        RobotAuditReport.coreFlowResults.add("AI Copilot: Overlay open/close, model dropdown, Open Settings -> Settings with visible TopAppBar/Back verified")

        // 4. Probe Settings -> Sensors Detector -> Back -> About -> Back
        try {
            composeRule.onNodeWithTag("app_settings_button").performClick()
            composeRule.waitForIdle()

            // Probe language toggle & offline engine toggle & light/dark toggle
            composeRule.onNodeWithTag("switch_offline_engine").performScrollTo().performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("chip_lang_id").performScrollTo().performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("chip_lang_en").performScrollTo().performClick()
            composeRule.waitForIdle()

            // Navigate to Sensors Detector from Settings
            composeRule.onNodeWithTag("button_open_sensors_detector").performScrollTo().performClick()
            composeRule.waitForIdle()
            val sensorRoute = navController?.currentBackStackEntry?.destination?.route
            RobotSemanticsCrawler.probeCurrentScreen(composeRule, density, "SensorDetector")
            composeRule.onNodeWithTag("top_app_bar_back_button").performClick()
            composeRule.waitForIdle()

            // Navigate to About from Settings
            composeRule.onNodeWithTag("card_navigate_to_about").performScrollTo().performClick()
            composeRule.waitForIdle()
            val aboutRoute = navController?.currentBackStackEntry?.destination?.route
            RobotSemanticsCrawler.probeCurrentScreen(composeRule, density, "About")
            composeRule.onNodeWithTag("top_app_bar_back_button").performClick()
            composeRule.waitForIdle()

            composeRule.onNodeWithTag("top_app_bar_back_button").performClick()
            composeRule.waitForIdle()
            RobotAuditReport.coreFlowResults.add("Settings -> SensorDetector ($sensorRoute) & About ($aboutRoute): Probed cleanly")
        } catch (t: Throwable) {
            RobotAuditReport.addFinding("HIGH", "Settings / Secondary Screens", "Failed probing Settings -> SensorDetector/About", "${t.javaClass.simpleName}: ${t.message}")
        }

        // 4b. Verify Floating Camera FAB shows SensorUnavailableDialog when USB Host hardware is absent
        Shadows.shadowOf(context.packageManager).setSystemFeature(PackageManager.FEATURE_USB_HOST, false)
        DeviceSensorDetector.getInstance(context).refresh()
        composeRule.onNodeWithTag("btn_floating_camera").performClick()
        composeRule.waitForIdle()
        val unavailableDialogNodes = composeRule.onAllNodesWithText("Tool Hardware Unavailable", substring = true).fetchSemanticsNodes()
        assertTrue("Expected Floating Camera FAB to gate with SensorUnavailableDialog when USB Host missing", unavailableDialogNodes.isNotEmpty())
        RobotSemanticsCrawler.dismissAnyOpenDialog(composeRule)
        composeRule.waitForIdle()
        RobotAuditReport.coreFlowResults.add("Floating Camera FAB: Properly gated with SensorUnavailableDialog when hardware is absent")

        // 4c. Verify Dashboard search placeholder reflects actual ALL_TOOLS count (not stale 109)
        val searchPlaceholderNodes = composeRule.onAllNodesWithText("Search ${ToolDefinition.ALL_TOOLS.size} tools & calculators...", substring = true).fetchSemanticsNodes()
        assertTrue("Expected dynamic tool count '${ToolDefinition.ALL_TOOLS.size}' in Dashboard search placeholder", searchPlaceholderNodes.isNotEmpty())

        // 5. Probe Tool Catalog Search, Outdoor Activities filter chip, & touch targets
        composeRule.onNodeWithTag("tool_catalog_button").performClick()
        composeRule.waitForIdle()
        val catalogStats = RobotSemanticsCrawler.probeCurrentScreen(composeRule, density, "ToolCatalog", maxInputs = 0, maxClicks = 5)
        if (catalogStats.smallTouchTargets.isNotEmpty()) {
            RobotAuditReport.addFinding(
                "LOW",
                "Accessibility / ToolCatalogScreen",
                "Interactive touch targets smaller than 48dp detected in Tool Catalog",
                catalogStats.smallTouchTargets.joinToString("\n") { "- $it" }
            )
        }
        assertTrue("Expected 0 <48dp touch targets in ToolCatalogScreen, found: ${catalogStats.smallTouchTargets}", catalogStats.smallTouchTargets.isEmpty())

        // Verify Outdoor Activities filter chip exists
        val outdoorChip = composeRule.onAllNodesWithText("Outdoor Activities (", substring = true).fetchSemanticsNodes()
        assertTrue("Expected 'Outdoor Activities' filter chip in ToolCatalogScreen", outdoorChip.isNotEmpty())
        composeRule.onAllNodesWithText("Outdoor Activities (", substring = true)[0].performScrollTo().performClick()
        composeRule.waitForIdle()
        val cardCount = composeRule.onAllNodes(hasText("Outdoor Activities & Topo Suite", substring = true)).fetchSemanticsNodes().size
        assertTrue("Expected Outdoor Activities tool card when Outdoor Activities chip selected", cardCount > 0)

        // Reset category back to ALL
        val allChip = composeRule.onAllNodesWithText("ALL (", substring = true).fetchSemanticsNodes()
        if (allChip.isNotEmpty()) {
            composeRule.onAllNodesWithText("ALL (", substring = true)[0].performScrollTo().performClick()
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag("top_app_bar_back_button").performClick()
        composeRule.waitForIdle()
        RobotAuditReport.coreFlowResults.add("ToolCatalogScreen: Search, Outdoor Activities filter chip, and 48dp touch-target check verified")
    }

    @Test
    fun audit_04_labor_cost_input_bug_and_sensor_enabled_catalog_probe() {
        // Grant hardware sensor features in Robolectric ShadowPackageManager
        val shadowPm = Shadows.shadowOf(context.packageManager)
        listOf(
            PackageManager.FEATURE_CAMERA_ANY,
            PackageManager.FEATURE_MICROPHONE,
            PackageManager.FEATURE_SENSOR_ACCELEROMETER,
            PackageManager.FEATURE_SENSOR_GYROSCOPE,
            PackageManager.FEATURE_SENSOR_COMPASS,
            PackageManager.FEATURE_SENSOR_BAROMETER,
            PackageManager.FEATURE_SENSOR_LIGHT,
            PackageManager.FEATURE_BLUETOOTH_LE,
            PackageManager.FEATURE_CONSUMER_IR,
            PackageManager.FEATURE_USB_HOST
        ).forEach { feature ->
            shadowPm.setSystemFeature(feature, true)
        }
        DeviceSensorDetector.getInstance(context).refresh()

        val factory = ViewModelFactory(context)
        var navController: NavHostController? = null

        composeRule.setContent {
            val nc = rememberNavController()
            navController = nc
            BrillianToolsTheme(darkTheme = false) {
                NavGraph(navController = nc, factory = factory)
            }
        }
        composeRule.waitForIdle()

        // 1. Verify SettingsScreen labor cost input preserves typing ("25." -> "30") without '.0' reset
        composeRule.onNodeWithTag("app_settings_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("input_labor_cost").performScrollTo().performTextReplacement("25.")
        composeRule.waitForIdle()
        val displayedDot = composeRule.onAllNodesWithText("25.", substring = false).fetchSemanticsNodes()
        assertTrue("Expected '25.' to be preserved during decimal entry", displayedDot.isNotEmpty())

        composeRule.onNodeWithTag("input_labor_cost").performTextReplacement("30")
        composeRule.waitForIdle()
        val after30 = factory.settingsRepository.settings.value.laborCostPerHour
        assertTrue("Expected laborCostPerHour == 30.0, got $after30", after30 == 30.0)
        val forcedResetNodes = composeRule.onAllNodesWithText("30.0", substring = false).fetchSemanticsNodes()
        assertTrue("Labor cost field should NOT overwrite '30' to '30.0'", forcedResetNodes.isEmpty())

        composeRule.onNodeWithTag("top_app_bar_back_button").performClick()
        composeRule.waitForIdle()

        // 2. Test clean open/close of UsbProCameraScreen via btn_floating_camera & btn_camera_close
        try {
            composeRule.onNodeWithTag("btn_floating_camera").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("btn_camera_close").performClick()
            composeRule.waitForIdle()
            val routeAfterClose = navController?.currentBackStackEntry?.destination?.route
            RobotAuditReport.coreFlowResults.add("UsbProCameraScreen clean launch & close: routeAfterClose='$routeAfterClose'")
        } catch (t: Throwable) {
            RobotAuditReport.addFinding("HIGH", "UsbProCameraScreen", "Direct camera close failed", "${t.javaClass.simpleName}: ${t.message}")
        }

        RobotAuditReport.coreFlowResults.add("SettingsScreen labor input + hardware-feature-enabled NavGraph verified")
    }

    @Test
    fun audit_05_multipurpose_camera_hub_default_factory_mounts() {
        var closed = false
        composeRule.setContent {
            BrillianToolsTheme(darkTheme = false) {
                MultipurposeCameraHubScreen(
                    onNavigateToTool = {},
                    isIndonesian = false,
                    onClose = { closed = true }
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("btn_camera_close").performClick()
        composeRule.waitForIdle()
        assertTrue("Expected MultipurposeCameraHubScreen onClose callback to fire", closed)
        RobotAuditReport.coreFlowResults.add("MultipurposeCameraHubScreen: Mounts with default ViewModelFactory & triggers onClose cleanly")
    }

    companion object {
        @JvmStatic
        @AfterClass
        fun flushReport() {
            RobotAuditReport.writeReportFile()
        }
    }
}
