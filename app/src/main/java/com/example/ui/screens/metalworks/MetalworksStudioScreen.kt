package com.example.ui.screens.metalworks

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.ToolDefinition
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetalworksStudioScreen(
    viewModel: MetalworksStudioViewModel,
    onNavigateBack: () -> Unit = {},
    initialToolId: String = "widget_weld_heat_input"
) {
    LaunchedEffect(initialToolId) {
        if (initialToolId.isNotBlank()) {
            viewModel.selectTool(initialToolId)
        }
    }

    val selectedToolId by viewModel.selectedToolId.collectAsState()
    val isImperial by viewModel.isImperial.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val metalTools = remember {
        ToolDefinition.ALL_TOOLS.filter { it.category == "Metalworks" }
    }

    var dropdownExpanded by remember { mutableStateOf(false) }

    val currentToolDef = remember(selectedToolId) {
        metalTools.find { it.id == selectedToolId } ?: metalTools.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Metalworks & Welding Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentToolDef?.title ?: "Select Tool",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("metalworks_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Unit Toggle
                    FilterChip(
                        selected = !isImperial,
                        onClick = { viewModel.toggleUnits() },
                        label = { Text(if (isImperial) "Imperial" else "Metric") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Units",
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tool Selector Bar
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("metalworks_tool_selector_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Construction,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = currentToolDef?.title ?: "Select Metalworks Tool",
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 450.dp)
                    ) {
                        metalTools.forEachIndexed { index, tool ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = "${index + 1}. ${tool.title}",
                                            fontWeight = if (tool.id == selectedToolId) FontWeight.Bold else FontWeight.Normal,
                                            color = if (tool.id == selectedToolId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = tool.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.selectTool(tool.id)
                                    dropdownExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (tool.id == selectedToolId) Icons.Default.CheckCircle else Icons.Default.Build,
                                        contentDescription = null,
                                        tint = if (tool.id == selectedToolId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }

            // Main Active Tool Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                when (selectedToolId) {
                    "widget_weld_heat_input" -> ToolHeatInput(viewModel, isImperial)
                    "widget_weld_carbon_equivalent" -> ToolCarbonEquivalent(viewModel, isImperial)
                    "widget_weld_electrode_selector" -> ToolElectrodeSelector(viewModel, isImperial)
                    "widget_weld_deposition_estimator" -> ToolDepositionEstimator(viewModel, isImperial)
                    "widget_weld_shielding_gas" -> ToolShieldingGas(viewModel, isImperial)
                    "widget_metal_k_factor" -> ToolKFactor(viewModel, isImperial)
                    "widget_metal_bend_deduction" -> ToolBendDeduction(viewModel, isImperial)
                    "widget_metal_press_brake_tonnage" -> ToolPressBrakeTonnage(viewModel, isImperial)
                    "widget_metal_cone_unfolder" -> ToolConeUnfolder(viewModel, isImperial)
                    "widget_metal_square_to_round" -> ToolSquareToRound(viewModel, isImperial)
                    "widget_pipe_miter_saddle" -> ToolPipeMiter(viewModel, isImperial)
                    "widget_pipe_rolling_offset" -> ToolRollingOffset(viewModel, isImperial)
                    "widget_pipe_flange_pcd" -> ToolFlangePcd(viewModel, isImperial)
                    "widget_pipe_orange_peel" -> ToolOrangePeel(viewModel, isImperial)
                    "widget_metal_thermal_distortion" -> ToolThermalDistortion(viewModel, isImperial)
                    "widget_metal_structural_profiles" -> ToolStructuralProfiles(viewModel, isImperial)
                    "widget_metal_plasma_cutting" -> ToolPlasmaCutting(viewModel, isImperial)
                    "widget_metal_flame_straightening" -> ToolFlameStraightening(viewModel, isImperial)
                    "widget_weld_fillet_throat" -> ToolFilletThroat(viewModel, isImperial)
                    "widget_weld_defects" -> ToolWeldDefects(viewModel, isImperial)
                    "widget_weld_symbol_decoder" -> ToolWeldSymbolDecoder(viewModel, isImperial)
                    "widget_weld_schaeffler" -> ToolSchaefflerDiagram(viewModel, isImperial)
                    "widget_metal_surface_flatness" -> ToolSurfaceFlatness(viewModel, isImperial)
                    "widget_weld_tungsten_grind" -> ToolTungstenGrind(viewModel, isImperial)
                    "widget_pipe_hydro_test" -> ToolHydroTest(viewModel, isImperial)
                    else -> ToolHeatInput(viewModel, isImperial)
                }
            }
        }
    }
}

// ==========================================
// INDIVIDUAL METALWORKS TOOLS IMPLEMENTATION
// ==========================================

@Composable
private fun ToolHeatInput(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val voltage by viewModel.heatVoltage.collectAsState()
    val current by viewModel.heatCurrent.collectAsState()
    val travelSpeed by viewModel.heatTravelSpeed.collectAsState()
    val efficiency by viewModel.heatEfficiency.collectAsState()

    val v = voltage.toDoubleOrNull() ?: 24.0
    val i = current.toDoubleOrNull() ?: 180.0
    val s = travelSpeed.toDoubleOrNull() ?: 250.0
    val eff = efficiency.toDoubleOrNull() ?: 0.8

    // Heat input Q = (V * I * 60 * eff) / (Speed * 1000)
    val heatKjMm = if (s > 0) (v * i * 60.0 * eff) / (s * 1000.0) else 0.0
    val heatKjIn = heatKjMm * 25.4

    val displayVal = if (isImperial) "%.2f kJ/in".format(heatKjIn) else "%.2f kJ/mm".format(heatKjMm)

    val coolingCategory = when {
        heatKjMm < 0.8 -> "Fast Cooling (Risk of Martensite / Cracking)"
        heatKjMm <= 2.2 -> "Optimal Cooling Rate (Ductile Grain Structure)"
        else -> "Slow Cooling (High Heat Input - Grain Coarsening Risk)"
    }

    MetalToolContainer(
        title = "Welding Parameter & Heat Input Calculator",
        description = "Calculates arc energy and thermal heat input (kJ/mm or kJ/in) based on arc efficiency",
        resultDisplay = displayVal,
        resultSubtitle = coolingCategory
    ) {
        OutlinedTextField(
            value = voltage,
            onValueChange = { viewModel.heatVoltage.value = it },
            label = { Text("Arc Voltage (V)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = current,
            onValueChange = { viewModel.heatCurrent.value = it },
            label = { Text("Welding Current (A)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = travelSpeed,
            onValueChange = { viewModel.heatTravelSpeed.value = it },
            label = { Text(if (isImperial) "Travel Speed (in/min)" else "Travel Speed (mm/min)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Process Thermal Efficiency Factor (η):", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = efficiency == "0.8",
                onClick = { viewModel.heatEfficiency.value = "0.8" },
                label = { Text("GMAW/FCAW (0.8)") }
            )
            FilterChip(
                selected = efficiency == "0.6",
                onClick = { viewModel.heatEfficiency.value = "0.6" },
                label = { Text("GTAW (0.6)") }
            )
            FilterChip(
                selected = efficiency == "1.0",
                onClick = { viewModel.heatEfficiency.value = "1.0" },
                label = { Text("SAW (1.0)") }
            )
        }
    }
}

@Composable
private fun ToolCarbonEquivalent(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val c by viewModel.ceCarbon.collectAsState()
    val mn by viewModel.ceManganese.collectAsState()
    val cr by viewModel.ceChromium.collectAsState()
    val mo by viewModel.ceMolybdenum.collectAsState()
    val v by viewModel.ceVanadium.collectAsState()
    val ni by viewModel.ceNickel.collectAsState()
    val cu by viewModel.ceCopper.collectAsState()
    val si by viewModel.ceSilicon.collectAsState()

    val cVal = c.toDoubleOrNull() ?: 0.18
    val mnVal = mn.toDoubleOrNull() ?: 1.20
    val crVal = cr.toDoubleOrNull() ?: 0.15
    val moVal = mo.toDoubleOrNull() ?: 0.05
    val vVal = v.toDoubleOrNull() ?: 0.02
    val niVal = ni.toDoubleOrNull() ?: 0.10
    val cuVal = cu.toDoubleOrNull() ?: 0.05
    val siVal = si.toDoubleOrNull() ?: 0.25

    // CE(IIW) = C + Mn/6 + (Cr+Mo+V)/5 + (Ni+Cu)/15
    val ceIIW = cVal + (mnVal / 6.0) + ((crVal + moVal + vVal) / 5.0) + ((niVal + cuVal) / 15.0)
    // Pcm = C + Si/30 + (Mn+Cu+Cr)/20 + Ni/60 + Mo/15 + V/10
    val pcm = cVal + (siVal / 30.0) + ((mnVal + cuVal + crVal) / 20.0) + (niVal / 60.0) + (moVal / 15.0) + (vVal / 10.0)

    val preheatAdvice = when {
        ceIIW < 0.40 -> if (isImperial) "Preheat: Optional (Room Temp 70°F)" else "Preheat: Optional (Room Temp 20°C)"
        ceIIW in 0.40..0.45 -> if (isImperial) "Preheat Required: 200°F - 300°F" else "Preheat Required: 100°C - 150°C"
        else -> if (isImperial) "Preheat Critical: 350°F - 450°F (Low Hydrogen Electrodes)" else "Preheat Critical: 180°C - 230°C (Low Hydrogen Electrodes)"
    }

    MetalToolContainer(
        title = "Carbon Equivalent & Pre-Heat Sizer",
        description = "Evaluates steel weldability (CE IIW & Pcm) to prevent cold cracking and hydrogen cracking",
        resultDisplay = "CE(IIW): %.3f | Pcm: %.3f".format(ceIIW, pcm),
        resultSubtitle = preheatAdvice
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = c, onValueChange = { viewModel.ceCarbon.value = it },
                label = { Text("Carbon %") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = mn, onValueChange = { viewModel.ceManganese.value = it },
                label = { Text("Mn %") }, modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cr, onValueChange = { viewModel.ceChromium.value = it },
                label = { Text("Cr %") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = mo, onValueChange = { viewModel.ceMolybdenum.value = it },
                label = { Text("Mo %") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = v, onValueChange = { viewModel.ceVanadium.value = it },
                label = { Text("V %") }, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToolElectrodeSelector(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val baseMetal by viewModel.baseMetal.collectAsState()

    val options = listOf(
        "Carbon Steel (A36/1020)",
        "High Strength Steel (A572/1045)",
        "Stainless 304 / 316",
        "4130 Chromoly Alloy",
        "Aluminum 6061 / 5052",
        "Cast Iron"
    )

    val spec = when (baseMetal) {
        "Carbon Steel (A36/1020)" -> "Filler: ER70S-6 (GMAW) or E7018 (SMAW)\nGas: 75% Ar / 25% CO2 or 100% CO2\nPolarity: DCEP"
        "High Strength Steel (A572/1045)" -> "Filler: E8018-C3 or ER80S-D2\nGas: 90% Ar / 10% CO2\nPolarity: DCEP (Low Hydrogen)"
        "Stainless 304 / 316" -> "Filler: ER308L / ER316L\nGas: 98% Ar / 2% O2 or 100% Argon (TIG)\nPolarity: DCEP (MIG) / DCEN (TIG)"
        "4130 Chromoly Alloy" -> "Filler: ER70S-2 (TIG for stress relief) or ER80S-D2\nGas: 100% Argon\nPolarity: DCEN"
        "Aluminum 6061 / 5052" -> "Filler: ER4043 (general) or ER5356 (high strength)\nGas: 100% Argon or Ar/He Mix\nPolarity: AC High Frequency"
        else -> "Filler: Ni-CI (Pure Nickel) or NiFe-CI\nGas: 100% Argon\nPolarity: DCEP / Peening Required"
    }

    MetalToolContainer(
        title = "Electrode & Filler Metal Selector",
        description = "Matches base metal alloys to correct filler wire, shielding gas, and polarity specs",
        resultDisplay = baseMetal,
        resultSubtitle = spec
    ) {
        Text("Select Base Metal Alloy:", style = MaterialTheme.typography.labelMedium)
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.baseMetal.value = option }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = baseMetal == option,
                    onClick = { viewModel.baseMetal.value = option }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(option, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ToolDepositionEstimator(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val thick by viewModel.depThickness.collectAsState()
    val gap by viewModel.depRootGap.collectAsState()
    val bevel by viewModel.depBevelAngle.collectAsState()
    val len by viewModel.depWeldLength.collectAsState()
    val eff by viewModel.depEfficiency.collectAsState()

    val t = thick.toDoubleOrNull() ?: 10.0
    val g = gap.toDoubleOrNull() ?: 2.0
    val bDeg = bevel.toDoubleOrNull() ?: 60.0
    val l = len.toDoubleOrNull() ?: 1000.0
    val efficiency = (eff.toDoubleOrNull() ?: 85.0) / 100.0

    val bRad = Math.toRadians(bDeg / 2.0)
    // Area = t*g + t^2 * tan(bRad)
    val areaMm2 = (t * g) + (t * t * tan(bRad))
    val volCm3 = (areaMm2 * l) / 1000.0
    val steelDensityGcm3 = 7.85
    val netWeightKg = (volCm3 * steelDensityGcm3) / 1000.0
    val requiredWireKg = if (efficiency > 0) netWeightKg / efficiency else netWeightKg

    val displayVal = if (isImperial) {
        "%.2f lbs filler wire".format(requiredWireKg * 2.20462)
    } else {
        "%.2f kg filler wire".format(requiredWireKg)
    }

    MetalToolContainer(
        title = "Weld Deposition & Consumable Estimator",
        description = "Computes weight of filler wire or electrodes required including deposition loss",
        resultDisplay = displayVal,
        resultSubtitle = "Net Deposited Weight: %.2f kg (Vol: %.1f cm³)".format(netWeightKg, volCm3)
    ) {
        OutlinedTextField(
            value = thick, onValueChange = { viewModel.depThickness.value = it },
            label = { Text(if (isImperial) "Plate Thickness (in)" else "Plate Thickness (mm)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = gap, onValueChange = { viewModel.depRootGap.value = it },
                label = { Text("Root Gap") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = bevel, onValueChange = { viewModel.depBevelAngle.value = it },
                label = { Text("Bevel Angle (°)") }, modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = len, onValueChange = { viewModel.depWeldLength.value = it },
            label = { Text(if (isImperial) "Total Weld Length (in)" else "Total Weld Length (mm)") },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToolShieldingGas(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val press by viewModel.gasPressureBar.collectAsState()
    val vol by viewModel.gasCylinderVolumeL.collectAsState()
    val flow by viewModel.gasFlowRate.collectAsState()

    val p = press.toDoubleOrNull() ?: 150.0 // Bar
    val v = vol.toDoubleOrNull() ?: 50.0 // Liters
    val f = flow.toDoubleOrNull() ?: 12.0 // L/min

    val totalGasLiters = p * v
    val totalMinutes = if (f > 0) totalGasLiters / f else 0.0
    val hours = (totalMinutes / 60.0).toInt()
    val mins = (totalMinutes % 60.0).toInt()

    MetalToolContainer(
        title = "Shielding Gas Flow & Bottle Runtime Estimator",
        description = "Calculates cylinder volume remaining and active arc burning time",
        resultDisplay = "${hours}h ${mins}m Arc Time",
        resultSubtitle = "Total Remaining Gas Volume: %.0f Liters (~%.1f cu ft)".format(totalGasLiters, totalGasLiters / 28.3168)
    ) {
        OutlinedTextField(
            value = press, onValueChange = { viewModel.gasPressureBar.value = it },
            label = { Text("Cylinder Gauge Pressure (Bar / ~15x PSI)") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = vol, onValueChange = { viewModel.gasCylinderVolumeL.value = it },
                label = { Text("Bottle Size (Liters)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = flow, onValueChange = { viewModel.gasFlowRate.value = it },
                label = { Text("Flow Rate (L/min)") }, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToolKFactor(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val thick by viewModel.kThickness.collectAsState()
    val radius by viewModel.kRadius.collectAsState()
    val angle by viewModel.kAngle.collectAsState()
    val kFactor by viewModel.kFactor.collectAsState()

    val t = thick.toDoubleOrNull() ?: 2.0
    val r = radius.toDoubleOrNull() ?: 3.0
    val a = angle.toDoubleOrNull() ?: 90.0
    val k = kFactor.toDoubleOrNull() ?: 0.38

    // BA = (pi * A / 180) * (R + K * T)
    val ba = (Math.PI * a / 180.0) * (r + k * t)

    val unit = if (isImperial) "in" else "mm"

    MetalToolContainer(
        title = "Sheet Metal K-Factor & Bend Allowance",
        description = "Calculates exact flat pattern layout lengths using material neutral axis displacement",
        resultDisplay = "Bend Allowance: %.3f %s".format(ba, unit),
        resultSubtitle = "Neutral Axis Offset: %.3f %s from inside bend".format(k * t, unit)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = thick, onValueChange = { viewModel.kThickness.value = it },
                label = { Text("Thickness ($unit)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = radius, onValueChange = { viewModel.kRadius.value = it },
                label = { Text("Inside Radius ($unit)") }, modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = angle, onValueChange = { viewModel.kAngle.value = it },
                label = { Text("Bend Angle (°)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = kFactor, onValueChange = { viewModel.kFactor.value = it },
                label = { Text("K-Factor (0.33-0.50)") }, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToolBendDeduction(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val thick by viewModel.bdThickness.collectAsState()
    val radius by viewModel.bdRadius.collectAsState()
    val angle by viewModel.bdAngle.collectAsState()
    val kFactor by viewModel.bdKFactor.collectAsState()

    val t = thick.toDoubleOrNull() ?: 2.0
    val r = radius.toDoubleOrNull() ?: 3.0
    val a = angle.toDoubleOrNull() ?: 90.0
    val k = kFactor.toDoubleOrNull() ?: 0.38

    val aRad = Math.toRadians(a)
    val ossb = tan(aRad / 2.0) * (r + t)
    val ba = (Math.PI * a / 180.0) * (r + k * t)
    val bd = 2.0 * ossb - ba

    val unit = if (isImperial) "in" else "mm"

    MetalToolContainer(
        title = "Bend Deduction & Setback Sizer",
        description = "Computes outside setback (OSSB) and bend deduction (BD) for press brake backgauges",
        resultDisplay = "Bend Deduction (BD): %.3f %s".format(bd, unit),
        resultSubtitle = "Outside Setback (OSSB): %.3f %s".format(ossb, unit)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = thick, onValueChange = { viewModel.bdThickness.value = it },
                label = { Text("Sheet Thickness ($unit)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = radius, onValueChange = { viewModel.bdRadius.value = it },
                label = { Text("Inside Radius ($unit)") }, modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = angle, onValueChange = { viewModel.bdAngle.value = it },
                label = { Text("Bend Angle (°)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = kFactor, onValueChange = { viewModel.bdKFactor.value = it },
                label = { Text("K-Factor") }, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToolPressBrakeTonnage(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val tens by viewModel.pbTensile.collectAsState()
    val thick by viewModel.pbThickness.collectAsState()
    val len by viewModel.pbLength.collectAsState()
    val die by viewModel.pbDieOpening.collectAsState()

    val sigma = tens.toDoubleOrNull() ?: 450.0 // MPa
    val t = thick.toDoubleOrNull() ?: 3.0 // mm
    val l = len.toDoubleOrNull() ?: 1000.0 // mm
    val v = die.toDoubleOrNull() ?: 24.0 // mm

    // P (metric tonnes) = (1.33 * L * t^2 * sigma) / (V * 10000) approx
    val tonnageMetric = if (v > 0) (1.33 * l * t * t * (sigma / 450.0) * 60) / (v * 10.0) else 0.0
    val tonnageUS = tonnageMetric * 1.10231

    MetalToolContainer(
        title = "Press Brake Tonnage Estimator",
        description = "Calculates required air bending force based on sheet thickness, length, and die opening",
        resultDisplay = if (isImperial) "%.1f US Tons".format(tonnageUS) else "%.1f Metric Tonnes".format(tonnageMetric),
        resultSubtitle = "Recommended V-Die Opening: %.1f mm (8x Thickness)".format(t * 8.0)
    ) {
        OutlinedTextField(
            value = tens, onValueChange = { viewModel.pbTensile.value = it },
            label = { Text("Material Tensile Strength (MPa / ~145x PSI)") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = thick, onValueChange = { viewModel.pbThickness.value = it },
                label = { Text("Thickness (mm)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = len, onValueChange = { viewModel.pbLength.value = it },
                label = { Text("Bend Length (mm)") }, modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = die, onValueChange = { viewModel.pbDieOpening.value = it },
            label = { Text("V-Die Opening (mm)") }, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToolConeUnfolder(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val topD by viewModel.coneTopDia.collectAsState()
    val botD by viewModel.coneBottomDia.collectAsState()
    val h by viewModel.coneHeight.collectAsState()

    val d1 = topD.toDoubleOrNull() ?: 100.0
    val d2 = botD.toDoubleOrNull() ?: 250.0
    val height = h.toDoubleOrNull() ?: 200.0

    val slant = sqrt(height * height + ((d2 - d1) / 2.0).pow(2))
    val rInner = if (d2 > d1) (d1 * slant) / (d2 - d1) else 0.0
    val rOuter = rInner + slant
    val thetaDeg = if (slant > 0) (360.0 * (d2 - d1) / 2.0) / slant else 0.0

    val unit = if (isImperial) "in" else "mm"

    MetalToolContainer(
        title = "Cone, Frustum & Transition Hopper Unfolder",
        description = "Generates 2D flat cutting layout arc radii and included layout angle",
        resultDisplay = "Layout Angle: %.1f°".format(thetaDeg),
        resultSubtitle = "Outer Radius: %.1f %s | Inner Radius: %.1f %s | Slant: %.1f %s".format(rOuter, unit, rInner, unit, slant, unit)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = topD, onValueChange = { viewModel.coneTopDia.value = it },
                label = { Text("Top Dia ($unit)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = botD, onValueChange = { viewModel.coneBottomDia.value = it },
                label = { Text("Bottom Dia ($unit)") }, modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = h, onValueChange = { viewModel.coneHeight.value = it },
            label = { Text("Vertical Height ($unit)") }, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToolSquareToRound(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val len by viewModel.sqBaseLen.collectAsState()
    val wid by viewModel.sqBaseWidth.collectAsState()
    val dia by viewModel.sqTopDia.collectAsState()
    val h by viewModel.sqHeight.collectAsState()

    val a = len.toDoubleOrNull() ?: 200.0
    val b = wid.toDoubleOrNull() ?: 200.0
    val d = dia.toDoubleOrNull() ?: 150.0
    val height = h.toDoubleOrNull() ?: 250.0

    val cornerX = a / 2.0
    val cornerY = b / 2.0
    val trueCornerLen = sqrt(cornerX * cornerX + cornerY * cornerY + height * height)

    val unit = if (isImperial) "in" else "mm"

    MetalToolContainer(
        title = "Square-to-Round Transition Layout Engine",
        description = "Triangulation engine for sheet metal duct transitions and exhaust hoppers",
        resultDisplay = "Corner True Length: %.1f %s".format(trueCornerLen, unit),
        resultSubtitle = "Base: %.0fx%.0f %s -> Top Dia: %.0f %s (H: %.0f %s)".format(a, b, unit, d, unit, height, unit)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = len, onValueChange = { viewModel.sqBaseLen.value = it },
                label = { Text("Base Length ($unit)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = wid, onValueChange = { viewModel.sqBaseWidth.value = it },
                label = { Text("Base Width ($unit)") }, modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = dia, onValueChange = { viewModel.sqTopDia.value = it },
                label = { Text("Top Round Dia ($unit)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = h, onValueChange = { viewModel.sqHeight.value = it },
                label = { Text("Transition Height ($unit)") }, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToolPipeMiter(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val mainD by viewModel.miterMainOd.collectAsState()
    val branchD by viewModel.miterBranchOd.collectAsState()
    val angle by viewModel.miterAngle.collectAsState()

    val dMain = mainD.toDoubleOrNull() ?: 114.3
    val dBranch = branchD.toDoubleOrNull() ?: 114.3
    val alpha = angle.toDoubleOrNull() ?: 90.0

    val aRad = Math.toRadians(alpha)
    val maxCutback = if (sin(aRad) > 0) (dBranch / (2.0 * tan(aRad))) else 0.0

    val unit = if (isImperial) "in" else "mm"

    MetalToolContainer(
        title = "Pipe Miter & Saddle Cut Template Generator",
        description = "Generates 2D wrap-around template ordinates for pipe tee and saddle joints",
        resultDisplay = "Max Cutback Offset: %.1f %s".format(maxCutback, unit),
        resultSubtitle = "Branch OD: %.1f %s @ %.0f° Angle".format(dBranch, unit, alpha)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = mainD, onValueChange = { viewModel.miterMainOd.value = it },
                label = { Text("Header Pipe OD ($unit)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = branchD, onValueChange = { viewModel.miterBranchOd.value = it },
                label = { Text("Branch Pipe OD ($unit)") }, modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = angle, onValueChange = { viewModel.miterAngle.value = it },
            label = { Text("Intersection Angle (°)") }, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToolRollingOffset(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val rise by viewModel.rollRise.collectAsState()
    val roll by viewModel.rollRoll.collectAsState()
    val run by viewModel.rollRun.collectAsState()

    val h = rise.toDoubleOrNull() ?: 300.0
    val w = roll.toDoubleOrNull() ?: 400.0
    val l = run.toDoubleOrNull() ?: 600.0

    val offset = sqrt(h * h + w * w)
    val travel = sqrt(offset * offset + l * l)
    val rollAngle = Math.toDegrees(atan2(w, h))
    val fittingAngle = Math.toDegrees(atan2(offset, l))

    val unit = if (isImperial) "in" else "mm"

    MetalToolContainer(
        title = "Rolling Offset & 3D Pipe Travel Calculator",
        description = "Computes 3D compound travel length and roll fitting orientation angle",
        resultDisplay = "True Travel Length: %.1f %s".format(travel, unit),
        resultSubtitle = "Roll Angle: %.1f° | Fitting Pitch Angle: %.1f°".format(rollAngle, fittingAngle)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = rise, onValueChange = { viewModel.rollRise.value = it },
                label = { Text("Vertical Rise ($unit)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = roll, onValueChange = { viewModel.rollRoll.value = it },
                label = { Text("Horizontal Roll ($unit)") }, modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = run, onValueChange = { viewModel.rollRun.value = it },
            label = { Text("Horizontal Run ($unit)") }, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToolFlangePcd(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val dia by viewModel.pcdDia.collectAsState()
    val holes by viewModel.pcdHoles.collectAsState()

    val pcd = dia.toDoubleOrNull() ?: 180.0
    val n = holes.toIntOrNull() ?: 8

    val stepAngle = if (n > 0) 360.0 / n else 0.0
    val chordDist = if (n > 0) pcd * sin(Math.toRadians(180.0 / n)) else 0.0

    val unit = if (isImperial) "in" else "mm"

    MetalToolContainer(
        title = "Pipe Flange Bolt Hole Circle (PCD) Generator",
        description = "Calculates exact chord spacing distances and angular steps for drilling bolt patterns",
        resultDisplay = "Chord Distance: %.2f %s".format(chordDist, unit),
        resultSubtitle = "Hole Step Angle: %.1f° across %d holes on %.1f %s PCD".format(stepAngle, n, pcd, unit)
    ) {
        OutlinedTextField(
            value = dia, onValueChange = { viewModel.pcdDia.value = it },
            label = { Text("Pitch Circle Diameter PCD ($unit)") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = holes, onValueChange = { viewModel.pcdHoles.value = it },
            label = { Text("Number of Bolt Holes") }, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToolOrangePeel(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val pipeD by viewModel.peelPipeOd.collectAsState()
    val petals by viewModel.peelPetals.collectAsState()

    val d = pipeD.toDoubleOrNull() ?: 168.3
    val n = petals.toIntOrNull() ?: 6

    val circum = Math.PI * d
    val petalWidth = if (n > 0) circum / n else 0.0
    val domeCapHeight = d / 2.0

    val unit = if (isImperial) "in" else "mm"

    MetalToolContainer(
        title = "Orange Peel / Bullnose Pipe Cap Layout",
        description = "Flat layout pattern calculator for cutting wedge petals to form welded domed end-caps",
        resultDisplay = "Petal Base Width: %.1f %s".format(petalWidth, unit),
        resultSubtitle = "Petals: %d | Dome Cap Radius: %.1f %s".format(n, domeCapHeight, unit)
    ) {
        OutlinedTextField(
            value = pipeD, onValueChange = { viewModel.peelPipeOd.value = it },
            label = { Text("Pipe Outside Diameter ($unit)") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = petals, onValueChange = { viewModel.peelPetals.value = it },
            label = { Text("Number of Petals (4, 6, 8)") }, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToolThermalDistortion(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val thick by viewModel.distThickness.collectAsState()
    val passes by viewModel.distPasses.collectAsState()

    val t = thick.toDoubleOrNull() ?: 12.0
    val p = passes.toIntOrNull() ?: 4

    val approxAngleDeg = p * 0.8
    val presetTiltDeg = approxAngleDeg * 0.9

    val unit = if (isImperial) "in" else "mm"

    MetalToolContainer(
        title = "Thermal Shrinkage & Distortion Compensator",
        description = "Predicts angular distortion on single-V butt welds to establish proper pre-setting angles",
        resultDisplay = "Reverse Preset Angle: %.1f°".format(presetTiltDeg),
        resultSubtitle = "Estimated Angular Distortion: ~%.1f° across %d weld passes".format(approxAngleDeg, p)
    ) {
        OutlinedTextField(
            value = thick, onValueChange = { viewModel.distThickness.value = it },
            label = { Text("Plate Thickness ($unit)") }, modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = passes, onValueChange = { viewModel.distPasses.value = it },
            label = { Text("Number of Weld Passes") }, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToolStructuralProfiles(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val selProfile by viewModel.selectedProfile.collectAsState()

    val profiles = listOf(
        "W8x31 / UB 203x133x30",
        "W10x49 / UB 254x146x73",
        "W12x65 / UB 305x165x97",
        "HSS 4x4x1/4 / SHS 100x100x6",
        "HSS 6x6x3/8 / SHS 150x150x10",
        "L3x3x1/4 Angle",
        "C6x8.2 Channel"
    )

    val details = when (selProfile) {
        "W8x31 / UB 203x133x30" -> "Depth: 8.0 in (203 mm) | Flange W: 8.0 in | Weight: 31 lbs/ft (46.1 kg/m) | Ix: 110 in⁴ | Sx: 27.5 in³"
        "W10x49 / UB 254x146x73" -> "Depth: 10.0 in (254 mm) | Flange W: 10.0 in | Weight: 49 lbs/ft (72.9 kg/m) | Ix: 272 in⁴ | Sx: 54.6 in³"
        "W12x65 / UB 305x165x97" -> "Depth: 12.1 in (307 mm) | Flange W: 12.0 in | Weight: 65 lbs/ft (96.7 kg/m) | Ix: 533 in⁴ | Sx: 87.9 in³"
        "HSS 4x4x1/4 / SHS 100x100x6" -> "Size: 4.0x4.0 in (101.6 mm) | Wall: 0.233 in (5.9 mm) | Weight: 12.2 lbs/ft | Ix: 7.1 in⁴"
        "HSS 6x6x3/8 / SHS 150x150x10" -> "Size: 6.0x6.0 in (152.4 mm) | Wall: 0.349 in (8.9 mm) | Weight: 27.5 lbs/ft | Ix: 38.8 in⁴"
        "L3x3x1/4 Angle" -> "Legs: 3.0x3.0 in (76.2 mm) | Wall: 0.25 in | Weight: 4.9 lbs/ft | Area: 1.44 in²"
        else -> "Depth: 6.0 in | Flange W: 1.92 in | Weight: 8.2 lbs/ft | Ix: 13.1 in⁴"
    }

    MetalToolContainer(
        title = "Structural Steel Profile Section Lookup",
        description = "Section modulus (Sx/Z), weight, and moment of inertia (Ix) table for UB/UC, HSS, and channels",
        resultDisplay = selProfile,
        resultSubtitle = details
    ) {
        Text("Select Structural Steel Section:", style = MaterialTheme.typography.labelMedium)
        profiles.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectedProfile.value = item }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = selProfile == item,
                    onClick = { viewModel.selectedProfile.value = item }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(item, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ToolPlasmaCutting(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val mat by viewModel.cutMaterial.collectAsState()
    val thick by viewModel.cutThickness.collectAsState()

    val t = thick.toDoubleOrNull() ?: 6.0 // mm

    val specs = when {
        t <= 3.0 -> "Power: 45A | Speed: 150 in/min (3800 mm/min) | Gas Press: 75 PSI | Piercing Delay: 0.1s | Standoff: 1.5 mm"
        t <= 6.0 -> "Power: 65A | Speed: 80 in/min (2000 mm/min) | Gas Press: 85 PSI | Piercing Delay: 0.4s | Standoff: 1.5 mm"
        t <= 12.0 -> "Power: 85A | Speed: 40 in/min (1000 mm/min) | Gas Press: 90 PSI | Piercing Delay: 0.8s | Standoff: 2.0 mm"
        else -> "Power: 105A / Oxy-Acetylene | Speed: 20 in/min (500 mm/min) | Piercing Delay: 1.5s | Pre-heat Torch Required"
    }

    MetalToolContainer(
        title = "Plasma & Oxy-Fuel Cutting Chart",
        description = "Recommends cutting power, travel speed, gas pressure, and pierce delays",
        resultDisplay = "$mat - %.1f mm".format(t),
        resultSubtitle = specs
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mat == "Mild Steel",
                onClick = { viewModel.cutMaterial.value = "Mild Steel" },
                label = { Text("Mild Steel") }
            )
            FilterChip(
                selected = mat == "Stainless Steel",
                onClick = { viewModel.cutMaterial.value = "Stainless Steel" },
                label = { Text("Stainless Steel") }
            )
            FilterChip(
                selected = mat == "Aluminum",
                onClick = { viewModel.cutMaterial.value = "Aluminum" },
                label = { Text("Aluminum") }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = thick, onValueChange = { viewModel.cutThickness.value = it },
            label = { Text("Plate Thickness (mm)") }, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToolFlameStraightening(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val pattern by viewModel.heatPatternType.collectAsState()

    val guide = when (pattern) {
        "Triangular Heat (Beam Flange)" -> "Place sharp point of heat triangle toward web, broad base at outer flange edge.\nHeat to Cherry Red (600°C - 650°C).\nAllows base shrinkage to pull beam straight."
        "Line Heat (Plates/Pipes)" -> "Pass heating torch at steady speed along line where camber is needed.\nDo not overheat past 700°C to preserve yield strength."
        else -> "Apply circular spot heat to center of pucker, then hammer lightly around circumference while cooling."
    }

    MetalToolContainer(
        title = "Flame Straightening & Spot Heating Guide",
        description = "Visual procedure for heating triangular zones and line heats to pull warped beams true",
        resultDisplay = pattern,
        resultSubtitle = guide
    ) {
        listOf("Triangular Heat (Beam Flange)", "Line Heat (Plates/Pipes)", "Spot Heat (Sheet Puckers)").forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.heatPatternType.value = item }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = pattern == item,
                    onClick = { viewModel.heatPatternType.value = item }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(item, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ToolFilletThroat(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val leg by viewModel.filletLeg.collectAsState()

    val a = leg.toDoubleOrNull() ?: 8.0
    val throatTheoretical = a * 0.7071
    val throatEffective = throatTheoretical * 0.9 // conservatively

    val unit = if (isImperial) "in" else "mm"

    MetalToolContainer(
        title = "Fillet Weld Leg to Throat Sizer",
        description = "Converts between leg length and effective throat thickness for AWS inspection",
        resultDisplay = "Theoretical Throat: %.2f %s".format(throatTheoretical, unit),
        resultSubtitle = "Leg Size: %.1f %s | Effective Throat: %.2f %s".format(a, unit, throatEffective, unit)
    ) {
        OutlinedTextField(
            value = leg, onValueChange = { viewModel.filletLeg.value = it },
            label = { Text("Fillet Weld Leg Length ($unit)") }, modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ToolWeldDefects(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val search by viewModel.defectSearchQuery.collectAsState()

    val defects = listOf(
        "Porosity" to "Gas entrapment in weld metal. Fix: Clean oil/rust, increase shielding gas, reduce draughts.",
        "Undercut" to "Groove melted into base metal at toe. Fix: Lower voltage/current, decrease travel speed, adjust torch angle.",
        "Lack of Fusion" to "Incomplete melting between passes or base metal. Fix: Increase current, ensure proper bevel angle and cleanliness.",
        "Hydrogen Cold Cracks" to "Cracks in HAZ after cooling. Fix: Use low-hydrogen electrodes (E7018), increase preheat & interpass temp.",
        "Centerline Hot Cracking" to "Solidification crack along weld center. Fix: Lower depth-to-width ratio, decrease current, change filler alloy."
    )

    MetalToolContainer(
        title = "Weld Defect & Acceptance Guide (AWS D1.1)",
        description = "Reference catalog for diagnosing porosity, undercut, lack of fusion, and cracking",
        resultDisplay = "AWS D1.1 Inspection Reference",
        resultSubtitle = "Select defect below to review cause & remedy"
    ) {
        defects.forEach { (name, desc) ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(desc, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ToolWeldSymbolDecoder(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val symType by viewModel.symbolType.collectAsState()
    val symSide by viewModel.symbolSide.collectAsState()
    val symSize by viewModel.symbolSize.collectAsState()

    val unit = if (isImperial) "in" else "mm"

    val description = "Blueprint Symbol: $symType on $symSide with size $symSize $unit.\nReference Arrow points to joint. Symbol placed below reference line = Arrow Side."

    MetalToolContainer(
        title = "Welding Symbol Blueprint Decoder",
        description = "Interactive symbol builder and reference guide explaining blueprint callouts",
        resultDisplay = "$symType ($symSide)",
        resultSubtitle = description
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = symType == "Fillet Weld",
                onClick = { viewModel.symbolType.value = "Fillet Weld" },
                label = { Text("Fillet") }
            )
            FilterChip(
                selected = symType == "V-Groove",
                onClick = { viewModel.symbolType.value = "V-Groove" },
                label = { Text("V-Groove") }
            )
            FilterChip(
                selected = symType == "Bevel-Groove",
                onClick = { viewModel.symbolType.value = "Bevel-Groove" },
                label = { Text("Bevel") }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = symSide == "Arrow Side",
                onClick = { viewModel.symbolSide.value = "Arrow Side" },
                label = { Text("Arrow Side") }
            )
            FilterChip(
                selected = symSide == "Other Side",
                onClick = { viewModel.symbolSide.value = "Other Side" },
                label = { Text("Other Side") }
            )
            FilterChip(
                selected = symSide == "Both Sides",
                onClick = { viewModel.symbolSide.value = "Both Sides" },
                label = { Text("Both Sides") }
            )
        }
    }
}

@Composable
private fun ToolSchaefflerDiagram(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val cr by viewModel.schaefflerCr.collectAsState()
    val ni by viewModel.schaefflerNi.collectAsState()

    val crEq = cr.toDoubleOrNull() ?: 19.5
    val niEq = ni.toDoubleOrNull() ?: 10.2

    // Simple ferrite number FN estimation
    val fn = max(0.0, 3.0 * (crEq - 0.93 * niEq - 6.7))

    val structure = when {
        fn < 3 -> "Fully Austenitic (Hot Cracking Sensitivity)"
        fn in 3.0..12.0 -> "Austenite + Ferrite (Optimal Structure - High Resistance to Cracking)"
        else -> "Duplex / High Ferrite (Embrittlement Risk at Elevated Temp)"
    }

    MetalToolContainer(
        title = "Ferrite Number & Schaeffler Diagram Tool",
        description = "Calculates stainless steel weld ferrite content from Ni and Cr equivalents",
        resultDisplay = "Ferrite Number: %.1f FN".format(fn),
        resultSubtitle = "Microstructure Prediction: $structure"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = cr, onValueChange = { viewModel.schaefflerCr.value = it },
                label = { Text("Chromium Equivalent (Creq)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = ni, onValueChange = { viewModel.schaefflerNi.value = it },
                label = { Text("Nickel Equivalent (Nieq)") }, modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ToolSurfaceFlatness(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val p1 by viewModel.flatP1.collectAsState()
    val p2 by viewModel.flatP2.collectAsState()
    val p3 by viewModel.flatP3.collectAsState()

    val v1 = p1.toDoubleOrNull() ?: 0.2
    val v2 = p2.toDoubleOrNull() ?: -0.1
    val v3 = p3.toDoubleOrNull() ?: 0.4

    val maxDev = max(abs(v1), max(abs(v2), abs(v3)))
    val unit = if (isImperial) "mils" else "μm"

    MetalToolContainer(
        title = "Surface Plate Flatness Multi-Point Map",
        description = "Maps workshop welding table warpage and surface plate deviation grid",
        resultDisplay = "Max Peak-to-Valley Deviation: %.2f %s".format(maxDev, unit),
        resultSubtitle = "Tolerance Grade: Grade B Workshop Floor Table"
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = p1, onValueChange = { viewModel.flatP1.value = it }, label = { Text("Point A ($unit)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = p2, onValueChange = { viewModel.flatP2.value = it }, label = { Text("Point B ($unit)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = p3, onValueChange = { viewModel.flatP3.value = it }, label = { Text("Point C ($unit)") }, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun ToolTungstenGrind(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val alloy by viewModel.tungstenAlloy.collectAsState()

    val alloys = listOf(
        "2% Thoriated (Red - EWTh-2)",
        "2% Ceriated (Orange - EWCe-2)",
        "1.5% Lanthanated (Gold - EWLa-1.5)",
        "Pure Tungsten (Green - EWP)"
    )

    val guide = when (alloy) {
        "2% Thoriated (Red - EWTh-2)" -> "Current: DCEN | Best for Carbon & Stainless Steel. High arc stability. Radioactive (use HEPA dust mask when grinding)."
        "2% Ceriated (Orange - EWCe-2)" -> "Current: DCEN / Low AC | Excellent low-current arc starting for thin sheet metal & tubing."
        "1.5% Lanthanated (Gold - EWLa-1.5)" -> "Current: DCEN & AC | Universal best choice for all metals (Aluminum, Stainless, Titanium). Non-radioactive."
        else -> "Current: AC (Inverter) | Used for AC aluminum welding. Forms balled tip under heat."
    }

    MetalToolContainer(
        title = "Tungsten Electrode Grind & Color Table",
        description = "Guide for selecting GTAW electrode alloys, grinding taper angle, and current balance",
        resultDisplay = alloy,
        resultSubtitle = guide
    ) {
        alloys.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.tungstenAlloy.value = item }
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(selected = alloy == item, onClick = { viewModel.tungstenAlloy.value = item })
                Spacer(modifier = Modifier.width(8.dp))
                Text(item, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun ToolHydroTest(viewModel: MetalworksStudioViewModel, isImperial: Boolean) {
    val pipeD by viewModel.hydroPipeOd.collectAsState()
    val wallT by viewModel.hydroWallThick.collectAsState()
    val yieldS by viewModel.hydroYield.collectAsState()

    val d = pipeD.toDoubleOrNull() ?: 219.1
    val t = wallT.toDoubleOrNull() ?: 8.18
    val y = yieldS.toDoubleOrNull() ?: 241.0 // MPa

    // Barlow's Burst Pressure P = (2 * S * t) / D
    val burstMpa = if (d > 0) (2.0 * y * t) / d else 0.0
    val maxAllowableMpa = burstMpa * 0.72 // 72% SMYS
    val hydroTestMpa = maxAllowableMpa * 1.5

    MetalToolContainer(
        title = "Hydrostatic Test & Wall Hoop Stress Sizer",
        description = "Computes safe hydro-test pressures for fabricated tanks and pipes (ASME Section VIII)",
        resultDisplay = if (isImperial) "Test Pressure: %.0f PSI".format(hydroTestMpa * 145.038) else "Test Pressure: %.1f MPa".format(hydroTestMpa),
        resultSubtitle = "Theoretical Burst Pressure: %.1f MPa (~%.0f PSI)".format(burstMpa, burstMpa * 145.038)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = pipeD, onValueChange = { viewModel.hydroPipeOd.value = it },
                label = { Text("Pipe OD (mm)") }, modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = wallT, onValueChange = { viewModel.hydroWallThick.value = it },
                label = { Text("Wall Thickness (mm)") }, modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = yieldS, onValueChange = { viewModel.hydroYield.value = it },
            label = { Text("Yield Strength SMYS (MPa)") }, modifier = Modifier.fillMaxWidth()
        )
    }
}

// ==========================================
// REUSABLE CONTAINER FOR METALWORKS TOOLS
// ==========================================

@Composable
private fun MetalToolContainer(
    title: String,
    description: String,
    resultDisplay: String,
    resultSubtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val resolvedIcon = when {
        title.contains("Weld", ignoreCase = true) || title.contains("Electrode", ignoreCase = true) -> Icons.Default.Science
        title.contains("Bend", ignoreCase = true) || title.contains("K-Factor", ignoreCase = true) || title.contains("Tonnage", ignoreCase = true) || title.contains("Cone", ignoreCase = true) || title.contains("Square", ignoreCase = true) || title.contains("Miter", ignoreCase = true) -> Icons.Default.Architecture
        title.contains("Pipe", ignoreCase = true) || title.contains("Flange", ignoreCase = true) || title.contains("Hydro", ignoreCase = true) -> Icons.Default.Build
        title.contains("Plasma", ignoreCase = true) || title.contains("Flame", ignoreCase = true) || title.contains("Thermal", ignoreCase = true) -> Icons.Default.FlashOn
        title.contains("Profile", ignoreCase = true) || title.contains("Flatness", ignoreCase = true) -> Icons.Default.GridOn
        else -> Icons.Default.Construction
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Info Box
        com.example.ui.components.ToolInfoBox(
            icon = resolvedIcon,
            title = title,
            description = description
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Primary Output Banner
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = resultDisplay,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontFamily = FontFamily.Monospace
                        )
                        if (resultSubtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = resultSubtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Inputs Form
                content()

                Spacer(modifier = Modifier.height(20.dp))

                // Copy & Action Button
                Button(
                    onClick = {
                        val textToCopy = "$title Output:\n$resultDisplay\n$resultSubtitle"
                        clipboardManager.setText(AnnotatedString(textToCopy))
                        Toast.makeText(context, "Results copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Calculation Summary")
                }
            }
        }
    }
}
