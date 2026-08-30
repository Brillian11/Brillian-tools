package com.example.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Foundation
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.ViewModelFactory
import com.example.ui.components.BrillianTopAppBar
import com.example.ui.screens.catalog.ToolCatalogScreen
import com.example.ui.screens.civil.AggregateSieveScreen
import com.example.ui.screens.outdoor.OutdoorActivitiesScreen
import com.example.ui.screens.civil.AggregateSieveViewModel
import com.example.ui.screens.civil.BeamDeflectionScreen
import com.example.ui.screens.civil.BeamDeflectionViewModel
import com.example.ui.screens.civil.ConcreteVolumeScreen
import com.example.ui.screens.civil.ConcreteVolumeViewModel
import com.example.ui.screens.civil.CutFillEarthworkScreen
import com.example.ui.screens.civil.CutFillEarthworkViewModel
import com.example.ui.screens.civil.EarthworkGradeScreen
import com.example.ui.screens.civil.EarthworkGradeViewModel
import com.example.ui.screens.civil.EquipmentHaulingScreen
import com.example.ui.screens.civil.EquipmentHaulingViewModel
import com.example.ui.screens.civil.FramingRoofingScreen
import com.example.ui.screens.civil.FramingRoofingViewModel
import com.example.ui.screens.civil.MasonryMortarScreen
import com.example.ui.screens.civil.MasonryMortarViewModel
import com.example.ui.screens.civil.MeteorologyScreen
import com.example.ui.screens.civil.MeteorologyViewModel
import com.example.ui.screens.civil.ParabolicFocusScreen
import com.example.ui.screens.civil.ParabolicFocusViewModel
import com.example.ui.screens.civil.RebarEstimatorScreen
import com.example.ui.screens.civil.RebarEstimatorViewModel
import com.example.ui.screens.civil.RetainingWallSizerScreen
import com.example.ui.screens.civil.RetainingWallSizerViewModel
import com.example.ui.screens.civil.SlopeDrainageScreen
import com.example.ui.screens.civil.SlopeDrainageViewModel
import com.example.ui.screens.civil.SoilAsphaltScreen
import com.example.ui.screens.civil.SoilAsphaltViewModel
import com.example.ui.screens.civil.StationingCogoScreen
import com.example.ui.screens.civil.StationingCogoViewModel
import com.example.ui.screens.civil.StormwaterRationalScreen
import com.example.ui.screens.civil.StormwaterRationalViewModel
import com.example.ui.screens.welcome.WelcomeScreen
import com.example.ui.screens.painting.PaintingCoatingStudioScreen
import com.example.ui.screens.painting.PaintingCoatingStudioViewModel
import com.example.ui.screens.metalworks.MetalworksStudioScreen
import com.example.ui.screens.metalworks.MetalworksStudioViewModel
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.ui.screens.customize.CustomizeDashboardScreen
import com.example.ui.screens.customize.CustomizeDashboardViewModel
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.dashboard.MainTabScreen
import com.example.ui.screens.electrical.BoxFillCapacityScreen
import com.example.ui.screens.electrical.BoxFillCapacityViewModel
import com.example.ui.screens.electrical.BreakerPanelScreen
import com.example.ui.screens.electrical.BreakerPanelViewModel
import com.example.ui.screens.electrical.ConduitBenderScreen
import com.example.ui.screens.electrical.ConduitBenderViewModel
import com.example.ui.screens.electrical.ConduitFillScreen
import com.example.ui.screens.electrical.ConduitFillViewModel
import com.example.ui.screens.electrical.CurrentLoopScalingScreen
import com.example.ui.screens.electrical.CurrentLoopScalingViewModel
import com.example.ui.screens.electrical.IndustrialMotorFlaScreen
import com.example.ui.screens.electrical.IndustrialMotorFlaViewModel
import com.example.ui.screens.electrical.LedDriverScreen
import com.example.ui.screens.electrical.LedDriverViewModel
import com.example.ui.screens.electrical.OhmsLawScreen
import com.example.ui.screens.electrical.OhmsLawViewModel
import com.example.ui.screens.electrical.PowerFactorCorrectionScreen
import com.example.ui.screens.electrical.PowerFactorCorrectionViewModel
import com.example.ui.screens.electrical.ResistorColorCodeScreen
import com.example.ui.screens.electrical.ResistorColorCodeViewModel
import com.example.ui.screens.electrical.SolarBatterySizerScreen
import com.example.ui.screens.electrical.SolarBatterySizerViewModel
import com.example.ui.screens.electrical.VoltageDropScreen
import com.example.ui.screens.electrical.VoltageDropViewModel
import com.example.ui.screens.inventory.MaterialInventoryScreen
import com.example.ui.screens.inventory.MaterialInventoryViewModel
import com.example.ui.screens.ir.JobsiteIrRemoteScreen
import com.example.ui.screens.ir.JobsiteIrRemoteViewModel
import com.example.ui.screens.maintenance.DrywallStudScreen
import com.example.ui.screens.maintenance.DrywallStudViewModel
import com.example.ui.screens.maintenance.HvacLoadScreen
import com.example.ui.screens.maintenance.HvacLoadViewModel
import com.example.ui.screens.maintenance.PaintCoverageScreen
import com.example.ui.screens.maintenance.PaintCoverageViewModel
import com.example.ui.screens.maintenance.PipeSizingScreen
import com.example.ui.screens.maintenance.PipeSizingViewModel
import com.example.ui.screens.maintenance.TileGroutScreen
import com.example.ui.screens.maintenance.TileGroutViewModel
import com.example.ui.screens.safety.ChemicalMsdsScreen
import com.example.ui.screens.safety.ChemicalMsdsViewModel
import com.example.ui.screens.safety.SafetyChecklistScreen
import com.example.ui.screens.safety.SafetyChecklistViewModel
import com.example.ui.screens.sensors.ArAreaCalculatorScreen
import com.example.ui.screens.sensors.ArAreaCalculatorViewModel
import com.example.ui.screens.sensors.ArMeasurementScreen
import com.example.ui.screens.sensors.ArMeasurementViewModel
import com.example.ui.screens.sensors.BarometricAltimeterScreen
import com.example.ui.screens.sensors.BarometricAltimeterViewModel
import com.example.ui.screens.sensors.BleMultimeterScreen
import com.example.ui.screens.sensors.BleMultimeterViewModel
import com.example.ui.screens.sensors.CompassScreen
import com.example.ui.screens.sensors.CompassViewModel
import com.example.ui.screens.sensors.DecibelMeterScreen
import com.example.ui.screens.sensors.DecibelMeterViewModel
import com.example.ui.screens.sensors.DigitalLevelScreen
import com.example.ui.screens.sensors.DigitalLevelViewModel
import com.example.ui.screens.sensors.CalculatorScreen
import com.example.ui.screens.sensors.CalculatorViewModel
import com.example.ui.screens.sensors.FractionalCalculatorScreen
import com.example.ui.screens.sensors.FractionalCalculatorViewModel
import com.example.ui.screens.sensors.PsychrometricScreen
import com.example.ui.screens.sensors.PsychrometricViewModel
import com.example.ui.screens.sensors.RefrigerantScreen
import com.example.ui.screens.sensors.RefrigerantViewModel
import com.example.ui.screens.sensors.DuctSizerScreen
import com.example.ui.screens.sensors.DuctSizerViewModel
import com.example.ui.screens.sensors.ExpansionTankScreen
import com.example.ui.screens.sensors.ExpansionTankViewModel
import com.example.ui.screens.sensors.SlingAngleScreen
import com.example.ui.screens.sensors.SlingAngleViewModel
import com.example.ui.screens.sensors.LaserMeasureScreen
import com.example.ui.screens.sensors.LaserMeasureViewModel
import com.example.ui.screens.sensors.LuxMeterScreen
import com.example.ui.screens.sensors.LuxMeterViewModel
import com.example.ui.screens.sensors.PlumbBobScreen
import com.example.ui.screens.sensors.PlumbBobViewModel
import com.example.ui.screens.sensors.StrobeTachometerScreen
import com.example.ui.screens.sensors.StrobeTachometerViewModel
import com.example.ui.screens.sensors.StudDetectorScreen
import com.example.ui.screens.sensors.StudDetectorViewModel
import com.example.ui.screens.sensors.SunPathTrackerScreen
import com.example.ui.screens.sensors.SunPathTrackerViewModel
import com.example.ui.screens.sensors.ThermalCameraScreen
import com.example.ui.screens.sensors.ThermalCameraViewModel
import com.example.ui.screens.sensors.UsbEndoscopeScreen
import com.example.ui.screens.sensors.UsbEndoscopeViewModel
import com.example.ui.screens.sensors.UsbProCameraScreen
import com.example.ui.screens.sensors.UsbProCameraViewModel
import com.example.ui.screens.sensors.QrCodeScannerScreen
import com.example.ui.screens.sensors.QrCodeScannerViewModel
import com.example.ui.screens.sensors.VibrationAnalyzerScreen
import com.example.ui.screens.sensors.VibrationAnalyzerViewModel
import com.example.ui.screens.settings.AboutScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.sync.SyncQueueScreen
import com.example.ui.screens.sync.SyncQueueViewModel
import com.example.ui.screens.tools.ColorDevToolsScreen
import com.example.ui.screens.tools.ColorDevToolsViewModel
import com.example.ui.screens.tools.FocusTimerScreen
import com.example.ui.screens.tools.FocusTimerViewModel
import com.example.ui.screens.tools.QuickNotesScreen
import com.example.ui.screens.tools.QuickNotesViewModel
import com.example.ui.screens.tools.TaskChecklistScreen
import com.example.ui.screens.tools.TaskChecklistViewModel
import com.example.ui.screens.tools.UnitConverterScreen
import com.example.ui.screens.tools.UnitConverterViewModel
import com.example.ui.screens.woodworking.BladeSpeedScreen
import com.example.ui.screens.woodworking.BladeSpeedViewModel
import com.example.ui.screens.woodworking.BoardFootageScreen
import com.example.ui.screens.woodworking.BoardFootageViewModel
import com.example.ui.screens.woodworking.CompoundMiterScreen
import com.example.ui.screens.woodworking.CompoundMiterViewModel
import com.example.ui.screens.woodworking.CutlistOptimizerScreen
import com.example.ui.screens.woodworking.CutlistOptimizerViewModel
import com.example.ui.screens.woodworking.DadoStepOverScreen
import com.example.ui.screens.woodworking.DadoStepOverViewModel
import com.example.ui.screens.woodworking.DrillTapChartScreen
import com.example.ui.screens.woodworking.DrillTapChartViewModel
import com.example.ui.screens.woodworking.GrainMatchingScreen
import com.example.ui.screens.woodworking.GrainMatchingViewModel
import com.example.ui.screens.woodworking.JoinerySpacingScreen
import com.example.ui.screens.woodworking.JoinerySpacingViewModel
import com.example.ui.screens.woodworking.KerfBendingScreen
import com.example.ui.screens.woodworking.KerfBendingViewModel
import com.example.ui.screens.woodworking.RafterCalculatorScreen
import com.example.ui.screens.woodworking.RafterCalculatorViewModel
import com.example.ui.screens.woodworking.SagulatorScreen
import com.example.ui.screens.woodworking.SagulatorViewModel
import com.example.ui.screens.woodworking.SegmentedTurningScreen
import com.example.ui.screens.woodworking.SegmentedTurningViewModel
import com.example.ui.screens.woodworking.StairLayoutScreen
import com.example.ui.screens.woodworking.StairLayoutViewModel
import com.example.ui.screens.woodworking.WoodMoistureScreen
import com.example.ui.screens.woodworking.WoodMoistureViewModel
import com.example.ui.screens.woodworking.WoodSpeciesStudioScreen
import com.example.ui.screens.woodworking.WoodSpeciesStudioViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    factory: ViewModelFactory? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val actualFactory = factory ?: remember { ViewModelFactory(context) }

    val dashboardViewModel: DashboardViewModel = viewModel(factory = actualFactory)

    val isOnline by dashboardViewModel.isOnlineMode.collectAsState()
    val pendingSyncCount by dashboardViewModel.pendingSyncCount.collectAsState()
    val isSyncing by dashboardViewModel.isSyncing.collectAsState()
    val isCopilotOpen by dashboardViewModel.isCopilotOpen.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ScreenRoutes.Dashboard.route

    val canNavigateBack = currentRoute != ScreenRoutes.Dashboard.route

    val topBarTitle = when (currentRoute) {
        ScreenRoutes.Dashboard.route -> "Brillian Tools"
        ScreenRoutes.CustomizeDashboard.route -> "Customize Dashboard"
        ScreenRoutes.ToolCatalog.route -> "Tool Catalog"
        ScreenRoutes.SyncQueue.route -> "Offline Sync Center"
        ScreenRoutes.Settings.route -> "App Settings"
        ScreenRoutes.KerfBending.route -> "Kerf Bending"
        ScreenRoutes.CutlistOptimizer.route -> "Cut List Optimizer (1D & 2D)"
        ScreenRoutes.DadoStepOver.route -> "Dado & Lap Joint Planner"
        ScreenRoutes.BoardFootage.route -> "Board Footage & Lumber Estimator"
        ScreenRoutes.StairLayout.route -> "Stair Layout Calculator"
        ScreenRoutes.RafterCalculator.route -> "Rafter & Roof Pitch Calculator"
        ScreenRoutes.CompoundMiter.route -> "Compound Miter & Bevel Calculator"
        ScreenRoutes.WoodMoisture.route -> "Wood Moisture & Shrinkage"
        ScreenRoutes.JoinerySpacing.route -> "Joinery & Tenon Spacing"
        ScreenRoutes.SegmentedTurning.route -> "Segmented Woodturning & Bowls"
        ScreenRoutes.Sagulator.route -> "Lumber Sagulator (Shelf Deflection)"
        ScreenRoutes.GrainMatching.route -> "Grain Matching & Board Layout"
        ScreenRoutes.DrillTapChart.route -> "Drill Bit / Tap & Die Reference"
        ScreenRoutes.BladeSpeed.route -> "Blade Surface Speed (SFPM)"
        ScreenRoutes.WoodSpeciesStudio.route -> "Wood Species & Preservation Studio"
        ScreenRoutes.About.route -> "About the App"
        ScreenRoutes.OhmsLaw.route -> "Ohm's Law & Power Triangle"
        ScreenRoutes.VoltageDrop.route -> "Voltage Drop Calculator"
        ScreenRoutes.ConduitFill.route -> "Wire Gauge & Conduit Fill"
        ScreenRoutes.ConduitBender.route -> "Conduit Bender Angles & Offsets"
        ScreenRoutes.ResistorColorCode.route -> "Resistor Color Code Decoder"
        ScreenRoutes.BreakerPanel.route -> "Breaker Panel Load Calculator"
        ScreenRoutes.LedDriver.route -> "LED Driver & Transformer Sizer"
        ScreenRoutes.BoxFillCapacity.route -> "Box Fill Capacity (NEC 314.16)"
        ScreenRoutes.IndustrialMotorFla.route -> "Industrial Motor FLA Sizer"
        ScreenRoutes.SolarBatterySizer.route -> "Solar Array & Battery Sizer"
        ScreenRoutes.CurrentLoopScaling.route -> "4–20 mA Current Loop & Scaling"
        ScreenRoutes.PowerFactorCorrection.route -> "Harmonics & Power Factor Correction"
        ScreenRoutes.PipeSizing.route -> "Pipe Sizing & Friction Loss"
        ScreenRoutes.HvacLoad.route -> "HVAC BTU & Room Load"
        ScreenRoutes.TileGrout.route -> "Tile, Grout & Flooring"
        ScreenRoutes.PaintCoverage.route -> "Paint & Primer Coverage"
        ScreenRoutes.DrywallStud.route -> "Drywall & Framing Studs"
        ScreenRoutes.SafetyChecklist.route -> "OSHA Safety Checklist & PPE Auditor"
        ScreenRoutes.ChemicalMsds.route -> "Chemical Safety (MSDS / SDS)"
        ScreenRoutes.ConcreteVolume.route -> "Concrete Volume & Bag Mix Sizer"
        ScreenRoutes.RebarEstimator.route -> "Rebar Spacing & Weight Estimator"
        ScreenRoutes.CutFillEarthwork.route -> "Cut & Fill Earthwork Volume"
        ScreenRoutes.MasonryMortar.route -> "Brick, Block & Mortar Calculator"
        ScreenRoutes.SlopeDrainage.route -> "Slope, Drainage & Culvert Sizer"
        ScreenRoutes.SoilAsphalt.route -> "Soil Compaction & Asphalt Tonnage"
        ScreenRoutes.BeamDeflection.route -> "Beam Deflection & Moment"
        ScreenRoutes.EarthworkGrade.route -> "Earthwork Cut/Fill & Grade"
        ScreenRoutes.FramingRoofing.route -> "Framing, Drywall & Roofing"
        ScreenRoutes.Meteorology.route -> "Meteorology & Site Weather"
        ScreenRoutes.ParabolicFocus.route -> "Parabolic Dish Focus"
        ScreenRoutes.StationingCogo.route -> "Stationing & Offset COGO"
        ScreenRoutes.RetainingWallSizer.route -> "Retaining Wall Soil Pressure Sizer"
        ScreenRoutes.AggregateSieve.route -> "Aggregate Sieve & Fineness Modulus"
        ScreenRoutes.EquipmentHauling.route -> "Equipment Hauling & Axle Load"
        ScreenRoutes.StormwaterRational.route -> "Stormwater Runoff (Rational Method)"
        ScreenRoutes.DigitalLevel.route -> "Digital Level & Inclinometer"
        ScreenRoutes.Compass.route -> "Digital Compass & Bearing"
        ScreenRoutes.DecibelMeter.route -> "Decibel Sound Meter"
        ScreenRoutes.StrobeTachometer.route -> "Optical Strobe Tachometer"
        ScreenRoutes.ThermalCamera.route -> "Thermal Camera (FLIR / Seek USB-C)"
        ScreenRoutes.UsbEndoscope.route -> "USB Borescope & Endoscope Stream"
        ScreenRoutes.VibrationAnalyzer.route -> "Vibration Spectrum Analyzer"
        ScreenRoutes.BleMultimeter.route -> "BLE Smart Multimeter & Clamp Sync"
        ScreenRoutes.SunPathTracker.route -> "Sun Path & Shadow Tracker"
        ScreenRoutes.BarometricAltimeter.route -> "Barometric Altimeter & Elevation Delta"
        ScreenRoutes.LuxMeter.route -> "Surface Lux & Foot-Candle Meter"
        ScreenRoutes.FractionalCalculator.route -> "Fractional Calculator"
        ScreenRoutes.ArMeasurement.route -> "AR Height & Distance"
        ScreenRoutes.ArAreaCalculator.route -> "AR Area Calculator"
        ScreenRoutes.PlumbBob.route -> "Plumb Bob & Wall Squareness"
        ScreenRoutes.StudDetector.route -> "Stud & Metal Detector"
        ScreenRoutes.LaserMeasure.route -> "Laser Measure & Bluetooth Log"
        ScreenRoutes.MaterialInventory.route -> "Material Inventory"
        ScreenRoutes.FocusTimer.route -> "Focus Timer"
        ScreenRoutes.UnitConverter.route -> "Unit Converter"
        ScreenRoutes.ColorDevTools.route -> "Trade Color & Paint Studio"
        ScreenRoutes.QuickNotes.route -> "Quick Notes"
        ScreenRoutes.TaskChecklist.route -> "Task Checklist"
        ScreenRoutes.Psychrometric.route -> "Psychrometric Air State"
        ScreenRoutes.Refrigerant.route -> "Refrigerant Pressure-Temp"
        ScreenRoutes.DuctSizer.route -> "Duct Sizer & Velocity"
        ScreenRoutes.ExpansionTank.route -> "Hydronic Expansion Tank"
        ScreenRoutes.SlingAngle.route -> "Rigging Sling Angle Sizer"
        else -> "Brillian Tools"
    }

    val userSettings by dashboardViewModel.userSettings.collectAsState()
    var showWorkspaceProfileDialog by remember { mutableStateOf(false) }

    val (profileTitle, profileIcon, profileDesc) = when (userSettings.workerProfile) {
        "Woodworker" -> Triple("Woodworking & Carpentry Workspace", Icons.Default.Engineering, "Optimized for carpenters, cabinetry & cabinet makers.")
        "Civil Engineer" -> Triple("Civil Engineering Workspace", Icons.Default.Foundation, "Optimized for concrete, grades, earthworks & drainage.")
        "Electrician" -> Triple("Electrician & Wiring Workspace", Icons.Default.FlashOn, "Optimized for Ohm's law, voltage drops, conduit filling.")
        "Mechanical" -> Triple("Mechanical, HVAC & Piping Workspace", Icons.Default.PrecisionManufacturing, "Optimized for Psychrometrics, refrigerant curves, duct sizers & piping.")
        "Painter" -> Triple("Painting, Coating & Surface Prep Workspace", Icons.Default.Palette, "Optimized for paint coverage, 2K mixing ratios, wet film thickness, dew points, and rust treatment.")
        else -> Triple("General Trade Workspace", Icons.Default.Build, "Standard tools for general contractors, handymen & maintenance.")
    }

    // Read directly from repository value synchronously to avoid any flow collection/recomposition lag
    val isFirstLaunch = remember { actualFactory.settingsRepository.settings.value.isFirstLaunch }
    val isFullscreenRoute = currentRoute == ScreenRoutes.UsbProCamera.route || currentRoute.startsWith("tool_metalworks_studio")
    val showTopBar = currentRoute != ScreenRoutes.Welcome.route && !isFullscreenRoute && !isCopilotOpen

    if (showWorkspaceProfileDialog) {
        AlertDialog(
            onDismissRequest = { showWorkspaceProfileDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Workspace & Quick Guide",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(
                        onClick = { showWorkspaceProfileDialog = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Active Workspace Profile Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = profileIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profileTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = profileDesc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    // New User Guide & Dashboard Hints Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Help,
                                contentDescription = "Help",
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(24.dp).padding(top = 2.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "New User Guide & Dashboard Hints",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "This dashboard shows your personalized trade tools. You can customize which tools are pinned by going to the 'Tool Catalog' (tap the grid icon in the top right corner).",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWorkspaceProfileDialog = false
                        navController.navigate(ScreenRoutes.Settings.route)
                    }
                ) {
                    Text("Change Profile in Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWorkspaceProfileDialog = false }) {
                    Text("Got it")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                BrillianTopAppBar(
                    title = topBarTitle,
                    canNavigateBack = canNavigateBack,
                    onNavigateBack = { navController.popBackStack() },
                    isOnline = isOnline,
                    pendingSyncCount = pendingSyncCount,
                    isSyncing = isSyncing,
                    onSyncBadgeClick = { navController.navigate(ScreenRoutes.SyncQueue.route) },
                    onProfileClick = if (currentRoute == ScreenRoutes.Dashboard.route) {
                        { showWorkspaceProfileDialog = true }
                    } else null,
                    profileIcon = profileIcon,
                    onCustomizeClick = if (currentRoute == ScreenRoutes.Dashboard.route) {
                        { navController.navigate(ScreenRoutes.CustomizeDashboard.route) }
                    } else null,
                    onCatalogClick = if (currentRoute == ScreenRoutes.Dashboard.route) {
                        { navController.navigate(ScreenRoutes.ToolCatalog.route) }
                    } else null,
                    onSettingsClick = { navController.navigate(ScreenRoutes.Settings.route) }
                )
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val isMetricSystem = userSettings.unitSystem == "Metric"
        val effectivePadding = if (isFullscreenRoute) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding
        Box(modifier = Modifier.fillMaxSize().padding(effectivePadding)) {
            NavHost(
                navController = navController,
                startDestination = if (isFirstLaunch) ScreenRoutes.Welcome.route else ScreenRoutes.Dashboard.route,
                modifier = Modifier.fillMaxSize()
            ) {
            composable(ScreenRoutes.Welcome.route) {
                WelcomeScreen(
                    viewModel = dashboardViewModel,
                    onFinished = {
                        navController.navigate(ScreenRoutes.Dashboard.route) {
                            popUpTo(ScreenRoutes.Welcome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(ScreenRoutes.Dashboard.route) {
                MainTabScreen(
                    dashboardViewModel = dashboardViewModel,
                    factory = actualFactory,
                    onNavigateToTool = { toolId ->
                        val route = when (toolId) {
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
                            "widget_settings" -> ScreenRoutes.Settings.route
                            else -> if (toolId.startsWith("tool_") || toolId.startsWith("screen_")) toolId else ScreenRoutes.ToolCatalog.route
                        }
                        navController.navigate(route)
                    },
                    onNavigateToCustomize = {
                        navController.navigate(ScreenRoutes.CustomizeDashboard.route)
                    }
                )
            }

            composable(ScreenRoutes.CustomizeDashboard.route) {
                val customizeViewModel: CustomizeDashboardViewModel = viewModel(factory = actualFactory)
                CustomizeDashboardScreen(viewModel = customizeViewModel)
            }

            composable(ScreenRoutes.ToolCatalog.route) {
                val catalogCustomizeViewModel: CustomizeDashboardViewModel = viewModel(factory = actualFactory)
                ToolCatalogScreen(
                    viewModel = catalogCustomizeViewModel,
                    onLaunchTool = { toolId ->
                        val route = when (toolId) {
                            "widget_usb_pro_camera" -> ScreenRoutes.UsbProCamera.route
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
                            "widget_settings" -> ScreenRoutes.Settings.route
                            else -> ScreenRoutes.Dashboard.route
                        }
                        navController.navigate(route)
                    }
                )
            }

            composable(ScreenRoutes.SyncQueue.route) {
                val syncViewModel: SyncQueueViewModel = viewModel(factory = actualFactory)
                SyncQueueScreen(viewModel = syncViewModel)
            }

            composable(ScreenRoutes.Settings.route) {
                val settingsViewModel: SettingsViewModel = viewModel(factory = actualFactory)
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToAbout = {
                        navController.navigate(ScreenRoutes.About.route)
                    }
                )
            }

            composable(ScreenRoutes.About.route) {
                AboutScreen()
            }

            // Woodworking Suite
            composable(ScreenRoutes.BoardFootage.route) {
                val vm: BoardFootageViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                BoardFootageScreen(viewModel = vm)
            }

            composable(ScreenRoutes.CutlistOptimizer.route) {
                val vm: CutlistOptimizerViewModel = viewModel(factory = actualFactory)
                CutlistOptimizerScreen(
                    viewModel = vm,
                    onNavigateToAi = { prompt ->
                        com.example.domain.agent.AiSessionBridge.startFreshSessionWithPrompt(
                            title = "Cutlist: ${vm.activeProjectName.value}",
                            prompt = prompt,
                            autoSend = false
                        )
                        dashboardViewModel.setCopilotOpen(true)
                        navController.navigate(ScreenRoutes.Dashboard.route) {
                            popUpTo(ScreenRoutes.Dashboard.route) { inclusive = false }
                        }
                    }
                )
            }

            composable(ScreenRoutes.StairLayout.route) {
                val vm: StairLayoutViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                StairLayoutScreen(viewModel = vm)
            }

            composable(ScreenRoutes.RafterCalculator.route) {
                val vm: RafterCalculatorViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                RafterCalculatorScreen(viewModel = vm)
            }

            composable(ScreenRoutes.CompoundMiter.route) {
                val vm: CompoundMiterViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                CompoundMiterScreen(viewModel = vm)
            }

            composable(ScreenRoutes.WoodMoisture.route) {
                val vm: WoodMoistureViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                WoodMoistureScreen(viewModel = vm)
            }

            composable(ScreenRoutes.JoinerySpacing.route) {
                val vm: JoinerySpacingViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                JoinerySpacingScreen(viewModel = vm)
            }

            composable(ScreenRoutes.SegmentedTurning.route) {
                val vm: SegmentedTurningViewModel = viewModel(factory = actualFactory)
                SegmentedTurningScreen(viewModel = vm)
            }

            composable(ScreenRoutes.Sagulator.route) {
                val vm: SagulatorViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                SagulatorScreen(viewModel = vm)
            }

            composable(ScreenRoutes.GrainMatching.route) {
                val vm: GrainMatchingViewModel = viewModel(factory = actualFactory)
                GrainMatchingScreen(viewModel = vm)
            }

            composable(ScreenRoutes.DrillTapChart.route) {
                val vm: DrillTapChartViewModel = viewModel(factory = actualFactory)
                DrillTapChartScreen(viewModel = vm)
            }

            composable(ScreenRoutes.BladeSpeed.route) {
                val vm: BladeSpeedViewModel = viewModel(factory = actualFactory)
                BladeSpeedScreen(viewModel = vm)
            }

            composable(ScreenRoutes.WoodSpeciesStudio.route) {
                val vm: WoodSpeciesStudioViewModel = viewModel(factory = actualFactory)
                WoodSpeciesStudioScreen(viewModel = vm)
            }

            composable(ScreenRoutes.KerfBending.route) {
                val vm: KerfBendingViewModel = viewModel(factory = actualFactory)
                KerfBendingScreen(viewModel = vm)
            }

            composable(ScreenRoutes.DadoStepOver.route) {
                val vm: DadoStepOverViewModel = viewModel(factory = actualFactory)
                DadoStepOverScreen(viewModel = vm)
            }

            // Electrical & Electronics Suite
            composable(ScreenRoutes.OhmsLaw.route) {
                val vm: OhmsLawViewModel = viewModel(factory = actualFactory)
                OhmsLawScreen(viewModel = vm)
            }

            composable(ScreenRoutes.VoltageDrop.route) {
                val vm: VoltageDropViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                VoltageDropScreen(viewModel = vm)
            }

            composable(ScreenRoutes.ConduitFill.route) {
                val vm: ConduitFillViewModel = viewModel(factory = actualFactory)
                ConduitFillScreen(viewModel = vm)
            }

            composable(ScreenRoutes.ConduitBender.route) {
                val vm: ConduitBenderViewModel = viewModel(factory = actualFactory)
                ConduitBenderScreen(viewModel = vm)
            }

            composable(ScreenRoutes.ResistorColorCode.route) {
                val vm: ResistorColorCodeViewModel = viewModel(factory = actualFactory)
                ResistorColorCodeScreen(viewModel = vm)
            }

            composable(ScreenRoutes.BreakerPanel.route) {
                val vm: BreakerPanelViewModel = viewModel(factory = actualFactory)
                BreakerPanelScreen(viewModel = vm)
            }

            composable(ScreenRoutes.LedDriver.route) {
                val vm: LedDriverViewModel = viewModel(factory = actualFactory)
                LedDriverScreen(viewModel = vm)
            }

            composable(ScreenRoutes.BoxFillCapacity.route) {
                val vm: BoxFillCapacityViewModel = viewModel(factory = actualFactory)
                BoxFillCapacityScreen(viewModel = vm)
            }

            composable(ScreenRoutes.IndustrialMotorFla.route) {
                val vm: IndustrialMotorFlaViewModel = viewModel(factory = actualFactory)
                IndustrialMotorFlaScreen(viewModel = vm)
            }

            composable(ScreenRoutes.SolarBatterySizer.route) {
                val vm: SolarBatterySizerViewModel = viewModel(factory = actualFactory)
                SolarBatterySizerScreen(viewModel = vm)
            }

            composable(ScreenRoutes.CurrentLoopScaling.route) {
                val vm: CurrentLoopScalingViewModel = viewModel(factory = actualFactory)
                CurrentLoopScalingScreen(viewModel = vm)
            }

            composable(ScreenRoutes.PowerFactorCorrection.route) {
                val vm: PowerFactorCorrectionViewModel = viewModel(factory = actualFactory)
                PowerFactorCorrectionScreen(viewModel = vm)
            }

            // Maintenance, Plumbing & Housework Suite
            composable(ScreenRoutes.PipeSizing.route) {
                val vm: PipeSizingViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                PipeSizingScreen(viewModel = vm)
            }

            composable(ScreenRoutes.HvacLoad.route) {
                val vm: HvacLoadViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                HvacLoadScreen(viewModel = vm)
            }

            composable(ScreenRoutes.TileGrout.route) {
                val vm: TileGroutViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                TileGroutScreen(viewModel = vm)
            }

            composable(ScreenRoutes.PaintCoverage.route) {
                val vm: PaintCoverageViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                PaintCoverageScreen(viewModel = vm)
            }

            composable(ScreenRoutes.DrywallStud.route) {
                val vm: DrywallStudViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                DrywallStudScreen(viewModel = vm)
            }

            // Safety & Compliance Suite
            composable(ScreenRoutes.SafetyChecklist.route) {
                val vm: SafetyChecklistViewModel = viewModel(factory = actualFactory)
                SafetyChecklistScreen(viewModel = vm)
            }

            composable(ScreenRoutes.ChemicalMsds.route) {
                val vm: ChemicalMsdsViewModel = viewModel(factory = actualFactory)
                ChemicalMsdsScreen(viewModel = vm)
            }

            // Civil Engineering, Earthwork & Masonry Suite
            composable(ScreenRoutes.ConcreteVolume.route) {
                val vm: ConcreteVolumeViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                ConcreteVolumeScreen(viewModel = vm)
            }

            composable(ScreenRoutes.RebarEstimator.route) {
                val vm: RebarEstimatorViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                RebarEstimatorScreen(viewModel = vm)
            }

            composable(ScreenRoutes.CutFillEarthwork.route) {
                val vm: CutFillEarthworkViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                CutFillEarthworkScreen(viewModel = vm)
            }

            composable(ScreenRoutes.MasonryMortar.route) {
                val vm: MasonryMortarViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                MasonryMortarScreen(viewModel = vm)
            }

            composable(ScreenRoutes.SlopeDrainage.route) {
                val vm: SlopeDrainageViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                SlopeDrainageScreen(viewModel = vm)
            }

            composable(ScreenRoutes.SoilAsphalt.route) {
                val vm: SoilAsphaltViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                SoilAsphaltScreen(viewModel = vm)
            }

            composable(ScreenRoutes.BeamDeflection.route) {
                val vm: BeamDeflectionViewModel = viewModel(factory = actualFactory)
                BeamDeflectionScreen(viewModel = vm)
            }

            composable(ScreenRoutes.EarthworkGrade.route) {
                val vm: EarthworkGradeViewModel = viewModel(factory = actualFactory)
                EarthworkGradeScreen(viewModel = vm)
            }

            composable(ScreenRoutes.FramingRoofing.route) {
                val vm: FramingRoofingViewModel = viewModel(factory = actualFactory)
                FramingRoofingScreen(viewModel = vm)
            }

            composable(ScreenRoutes.Meteorology.route) {
                val vm: MeteorologyViewModel = viewModel(factory = actualFactory)
                MeteorologyScreen(viewModel = vm)
            }

            composable(ScreenRoutes.ParabolicFocus.route) {
                val vm: ParabolicFocusViewModel = viewModel(factory = actualFactory)
                ParabolicFocusScreen(viewModel = vm)
            }

            composable(ScreenRoutes.StationingCogo.route) {
                val vm: StationingCogoViewModel = viewModel(factory = actualFactory)
                StationingCogoScreen(viewModel = vm)
            }

            composable(ScreenRoutes.RetainingWallSizer.route) {
                val vm: RetainingWallSizerViewModel = viewModel(factory = actualFactory)
                RetainingWallSizerScreen(viewModel = vm)
            }

            composable(ScreenRoutes.AggregateSieve.route) {
                val vm: AggregateSieveViewModel = viewModel(factory = actualFactory)
                AggregateSieveScreen(viewModel = vm)
            }

            composable(ScreenRoutes.EquipmentHauling.route) {
                val vm: EquipmentHaulingViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setMetric(isMetricSystem)
                }
                EquipmentHaulingScreen(viewModel = vm)
            }

            composable(ScreenRoutes.StormwaterRational.route) {
                val vm: StormwaterRationalViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setMetric(isMetricSystem)
                }
                StormwaterRationalScreen(viewModel = vm)
            }

            // Sensor Suite
            composable(ScreenRoutes.DigitalLevel.route) {
                val vm: DigitalLevelViewModel = viewModel(factory = actualFactory)
                DigitalLevelScreen(viewModel = vm)
            }

            composable(ScreenRoutes.Compass.route) {
                val vm: CompassViewModel = viewModel(factory = actualFactory)
                CompassScreen(viewModel = vm)
            }

            composable(ScreenRoutes.DecibelMeter.route) {
                val vm: DecibelMeterViewModel = viewModel(factory = actualFactory)
                DecibelMeterScreen(viewModel = vm)
            }

            composable(ScreenRoutes.StrobeTachometer.route) {
                val vm: StrobeTachometerViewModel = viewModel(factory = actualFactory)
                StrobeTachometerScreen(viewModel = vm)
            }

            composable(ScreenRoutes.ThermalCamera.route) {
                val vm: ThermalCameraViewModel = viewModel(factory = actualFactory)
                ThermalCameraScreen(viewModel = vm)
            }

            composable(ScreenRoutes.UsbEndoscope.route) {
                val vm: UsbEndoscopeViewModel = viewModel(factory = actualFactory)
                UsbEndoscopeScreen(viewModel = vm)
            }

            composable(ScreenRoutes.UsbProCamera.route) {
                val vm: UsbProCameraViewModel = viewModel(factory = actualFactory)
                UsbProCameraScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTool = { route -> navController.navigate(route) }
                )
            }

            composable(ScreenRoutes.QrCodeScanner.route) {
                val vm: QrCodeScannerViewModel = viewModel(factory = actualFactory)
                QrCodeScannerScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(ScreenRoutes.VibrationAnalyzer.route) {
                val vm: VibrationAnalyzerViewModel = viewModel(factory = actualFactory)
                VibrationAnalyzerScreen(viewModel = vm)
            }

            composable(ScreenRoutes.BleMultimeter.route) {
                val vm: BleMultimeterViewModel = viewModel(factory = actualFactory)
                BleMultimeterScreen(viewModel = vm)
            }

            composable(ScreenRoutes.SunPathTracker.route) {
                val vm: SunPathTrackerViewModel = viewModel(factory = actualFactory)
                SunPathTrackerScreen(viewModel = vm)
            }

            composable(ScreenRoutes.BarometricAltimeter.route) {
                val vm: BarometricAltimeterViewModel = viewModel(factory = actualFactory)
                BarometricAltimeterScreen(viewModel = vm)
            }

            composable(ScreenRoutes.LuxMeter.route) {
                val vm: LuxMeterViewModel = viewModel(factory = actualFactory)
                LuxMeterScreen(viewModel = vm)
            }

            composable(ScreenRoutes.FractionalCalculator.route) {
                val vm: FractionalCalculatorViewModel = viewModel(factory = actualFactory)
                FractionalCalculatorScreen(viewModel = vm)
            }

            composable(ScreenRoutes.ArMeasurement.route) {
                val vm: ArMeasurementViewModel = viewModel(factory = actualFactory)
                ArMeasurementScreen(viewModel = vm)
            }

            composable(ScreenRoutes.ArAreaCalculator.route) {
                val vm: ArAreaCalculatorViewModel = viewModel(factory = actualFactory)
                LaunchedEffect(isMetricSystem) {
                    vm.setUnitSystem(isMetricSystem)
                }
                ArAreaCalculatorScreen(viewModel = vm)
            }

            composable(ScreenRoutes.PlumbBob.route) {
                val vm: PlumbBobViewModel = viewModel(factory = actualFactory)
                PlumbBobScreen(viewModel = vm)
            }

            composable(ScreenRoutes.StudDetector.route) {
                val vm: StudDetectorViewModel = viewModel(factory = actualFactory)
                StudDetectorScreen(viewModel = vm)
            }

            composable(ScreenRoutes.LaserMeasure.route) {
                val vm: LaserMeasureViewModel = viewModel(factory = actualFactory)
                LaserMeasureScreen(viewModel = vm)
            }

            composable(ScreenRoutes.Psychrometric.route) {
                val vm: PsychrometricViewModel = viewModel(factory = actualFactory)
                PsychrometricScreen(viewModel = vm)
            }

            composable(ScreenRoutes.Refrigerant.route) {
                val vm: RefrigerantViewModel = viewModel(factory = actualFactory)
                RefrigerantScreen(viewModel = vm)
            }

            composable(ScreenRoutes.DuctSizer.route) {
                val vm: DuctSizerViewModel = viewModel(factory = actualFactory)
                DuctSizerScreen(viewModel = vm)
            }

            composable(ScreenRoutes.ExpansionTank.route) {
                val vm: ExpansionTankViewModel = viewModel(factory = actualFactory)
                ExpansionTankScreen(viewModel = vm)
            }

            composable(ScreenRoutes.SlingAngle.route) {
                val vm: SlingAngleViewModel = viewModel(factory = actualFactory)
                SlingAngleScreen(viewModel = vm)
            }

            composable(ScreenRoutes.JobsiteIrRemote.route) {
                val vm: JobsiteIrRemoteViewModel = viewModel(factory = actualFactory)
                JobsiteIrRemoteScreen(viewModel = vm)
            }

            composable(ScreenRoutes.OutdoorActivities.route) {
                OutdoorActivitiesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Inventory
            composable(ScreenRoutes.MaterialInventory.route) {
                val vm: MaterialInventoryViewModel = viewModel(factory = actualFactory)
                MaterialInventoryScreen(viewModel = vm)
            }

            // Painting & Coating Studio
            composable(ScreenRoutes.PaintingCoatingStudio.route) {
                val vm: PaintingCoatingStudioViewModel = viewModel(factory = actualFactory)
                PaintingCoatingStudioScreen(viewModel = vm)
            }

            // Metalworks & Welding Studio
            composable(
                route = ScreenRoutes.MetalworksStudio.route,
                arguments = listOf(navArgument("toolId") {
                    type = NavType.StringType
                    defaultValue = "widget_weld_heat_input"
                })
            ) { backStackEntry ->
                val toolIdArg = backStackEntry.arguments?.getString("toolId") ?: "widget_weld_heat_input"
                val vm: MetalworksStudioViewModel = viewModel(factory = actualFactory)
                MetalworksStudioScreen(
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() },
                    initialToolId = toolIdArg
                )
            }

            // Legacy Tools
            composable(ScreenRoutes.FocusTimer.route) {
                val timerViewModel: FocusTimerViewModel = viewModel(factory = actualFactory)
                FocusTimerScreen(viewModel = timerViewModel)
            }

            composable(ScreenRoutes.UnitConverter.route) {
                val converterViewModel: UnitConverterViewModel = viewModel(factory = actualFactory)
                UnitConverterScreen(viewModel = converterViewModel)
            }

            composable(ScreenRoutes.ColorDevTools.route) {
                val devToolsViewModel: ColorDevToolsViewModel = viewModel(factory = actualFactory)
                ColorDevToolsScreen(viewModel = devToolsViewModel)
            }

            composable(ScreenRoutes.QuickNotes.route) {
                val notesViewModel: QuickNotesViewModel = viewModel(factory = actualFactory)
                QuickNotesScreen(viewModel = notesViewModel)
            }

            composable(ScreenRoutes.TaskChecklist.route) {
                val checklistViewModel: TaskChecklistViewModel = viewModel(factory = actualFactory)
                TaskChecklistScreen(viewModel = checklistViewModel)
            }

            composable(ScreenRoutes.Calculator.route) {
                val calcViewModel: CalculatorViewModel = viewModel(factory = actualFactory)
                CalculatorScreen(viewModel = calcViewModel)
            }
        }


    }
}
}
