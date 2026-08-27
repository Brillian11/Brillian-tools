package com.example.ui.screens.electrical

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolarBatterySizerScreen(
    viewModel: SolarBatterySizerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Solar PV & Battery Bank Sizer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "MPPT String Limits & Off-Grid Autonomy Storage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(state.calculationSummary))
                        Toast.makeText(context, "Solar system spec copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Copy Summary")
                    }
                    IconButton(onClick = {
                        viewModel.saveToLogs()
                        Toast.makeText(context, "Saved to project log", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.BookmarkBorder, contentDescription = "Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. Dual Overview Cards (Array Power & Battery Bank)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Solar Array Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SOLAR PV ARRAY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${String.format("%.2f", state.actualInstalledPvWatts / 1000.0)} kWp",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${state.totalPanelsCount}x Panels (${state.parallelStringsCount} strings of ${state.panelsInSeriesPerString})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Battery Bank Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("BATTERY BANK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${String.format("%.1f", state.totalBatteryKwh)} kWh",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "${String.format("%.0f", state.batteryAmpHoursAtDcVoltage)} Ah @ ${state.systemDcVoltage}V (${state.standardServerRackBattCount}x Modules)",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // 2. Interactive Solar String & Battery Diagram
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "String & MPPT Connection Architecture",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SolarSystemVisualCanvas(
                            state = state,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )
                    }
                }
            }

            // 3. Load Demand & Environmental Parameters
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Energy Consumption & Site Conditions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Daily kWh Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Daily Consumption:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${String.format("%.1f", state.dailyKwh)} kWh / day",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = state.dailyKwh.toFloat(),
                            onValueChange = { viewModel.setDailyKwh(it.toDouble()) },
                            valueRange = 1f..60f,
                            steps = 59
                        )

                        // Peak Sun Hours Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Peak Sun Hours (PSH):", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${String.format("%.1f", state.peakSunHours)} hrs / day",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = state.peakSunHours.toFloat(),
                            onValueChange = { viewModel.setPeakSunHours(it.toDouble()) },
                            valueRange = 2f..7.5f,
                            steps = 11
                        )

                        // Days of Autonomy
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Days of Autonomy (No Sun):", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${String.format("%.1f", state.daysOfAutonomy)} days",
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Slider(
                            value = state.daysOfAutonomy.toFloat(),
                            onValueChange = { viewModel.setDaysOfAutonomy(it.toDouble()) },
                            valueRange = 1f..5f,
                            steps = 8
                        )

                        // System DC Voltage
                        Text("System DC Bus Voltage:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(12, 24, 48).forEach { v ->
                                FilterChip(
                                    selected = state.systemDcVoltage == v,
                                    onClick = { viewModel.setSystemVoltage(v) },
                                    label = { Text("${v}V DC") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Battery Chemistry & Panel Hardware Specs
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Hardware & Component Specifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Battery Storage Chemistry:", style = MaterialTheme.typography.labelSmall)
                        BatteryChemistry.values().forEach { chem ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = state.batteryChemistry == chem,
                                    onClick = { viewModel.setBatteryChemistry(chem) },
                                    label = { Text(chem.label, fontSize = 12.sp) }
                                )
                                Text(
                                    text = "DoD: ${(chem.dod * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("Solar Panel Size:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                Triple(400, 41.2, 34.5),
                                Triple(450, 49.8, 41.5),
                                Triple(550, 49.9, 41.9),
                                Triple(650, 55.4, 46.2)
                            ).forEach { (watts, voc, vmp) ->
                                FilterChip(
                                    selected = state.panelWatts == watts,
                                    onClick = { viewModel.setPanelSpecs(watts, voc, vmp, watts / vmp) },
                                    label = { Text("${watts}W") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // MPPT Max Input Voltage
                        Text("MPPT Max Input Voltage (Voc Max):", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(150.0, 250.0, 450.0, 600.0).forEach { vMax ->
                                FilterChip(
                                    selected = state.mpptMaxVoc == vMax,
                                    onClick = { viewModel.setMpptMaxVoc(vMax) },
                                    label = { Text("${vMax.toInt()}V") }
                                )
                            }
                        }
                    }
                }
            }

            // 5. Engineering Sizing Summary Table
            item {
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Detailed Engineering Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        SolarSummaryRow("String Max Voc (Cold -10°C)", "${String.format("%.1f", state.maxStringVocCold)} V (Limit ${state.mpptMaxVoc.toInt()}V)")
                        SolarSummaryRow("String Operating Vmp", "${String.format("%.1f", state.stringVmpOperating)} V")
                        SolarSummaryRow("Total Array Max Current", "${String.format("%.1f", state.totalArrayIscAmps)} A (${state.parallelStringsCount} strings)")
                        SolarSummaryRow("MPPT Controllers Required", "${state.mpptControllersCount} unit(s) @ ${state.mpptMaxAmps}A")
                        SolarSummaryRow("Required Installation Area", "${String.format("%.1f", state.requiredRoofAreaM2)} m² (~${String.format("%.0f", state.requiredRoofAreaSqFt)} sq ft)")
                        SolarSummaryRow("Usable Storage Energy", "${String.format("%.1f", state.usableBatteryKwh)} kWh")
                        SolarSummaryRow("Total Nominal Storage", "${String.format("%.1f", state.totalBatteryKwh)} kWh")
                        SolarSummaryRow("Recommended Inverter Size", "${String.format("%.1f", state.recommendedInverterContinuousKw)} kW continuous")
                    }
                }
            }
        }
    }
}

@Composable
private fun SolarSummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}

@Composable
private fun SolarSystemVisualCanvas(
    state: SolarBatteryUiState,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(Color(0xFF181B20), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        val w = size.width
        val h = size.height

        val colorSolar = Color(0xFFFFB300)
        val colorMppt = Color(0xFF00E676)
        val colorBatt = Color(0xFF42A5F5)
        val colorInverter = Color(0xFFAB47BC)

        // 1. Solar Panels on left (grid of blue/yellow cells)
        val panelW = 34f
        val panelH = 22f
        val numShowPanels = (state.totalPanelsCount).coerceIn(2, 8)
        for (i in 0 until numShowPanels) {
            val px = 14f + (i % 2) * (panelW + 6f)
            val py = 16f + (i / 2) * (panelH + 6f)
            drawRoundRect(
                color = colorSolar,
                topLeft = Offset(px, py),
                size = Size(panelW, panelH),
                cornerRadius = CornerRadius(2f, 2f),
                style = Stroke(width = 2f)
            )
            // grid lines inside panel
            drawLine(Color(0xFF555555), Offset(px + panelW / 2, py), Offset(px + panelW / 2, py + panelH), strokeWidth = 1f)
        }

        // DC Wire to MPPT
        val mpptX = w * 0.40f
        val mpptY = h * 0.5f
        drawLine(Color(0xFFFF7043), Offset(95f, h * 0.5f), Offset(mpptX - 25f, mpptY), strokeWidth = 3f)

        // 2. MPPT Charge Controller Box
        drawRoundRect(
            color = colorMppt,
            topLeft = Offset(mpptX - 24f, mpptY - 24f),
            size = Size(48f, 48f),
            cornerRadius = CornerRadius(6f, 6f),
            style = Stroke(width = 2.5f)
        )

        // Wire to Battery (downwards/right)
        val battX = w * 0.70f
        val battY = h * 0.75f
        drawLine(Color(0xFF29B6F6), Offset(mpptX + 24f, mpptY), Offset(battX - 28f, battY), strokeWidth = 3f)

        // 3. Battery Rack
        drawRoundRect(
            color = colorBatt,
            topLeft = Offset(battX - 28f, battY - 22f),
            size = Size(56f, 44f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = 2.5f)
        )

        // Wire to Inverter (upwards/right)
        val invX = w * 0.72f
        val invY = h * 0.25f
        drawLine(Color(0xFFBA68C8), Offset(mpptX + 24f, mpptY), Offset(invX - 26f, invY), strokeWidth = 3f)

        // 4. Hybrid Inverter Box
        drawRoundRect(
            color = colorInverter,
            topLeft = Offset(invX - 24f, invY - 22f),
            size = Size(52f, 44f),
            cornerRadius = CornerRadius(4f, 4f),
            style = Stroke(width = 2.5f)
        )
    }
}
