package com.example.ui.screens.sensors

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RefrigerantScreen(
    viewModel: RefrigerantViewModel,
    modifier: Modifier = Modifier
) {
    val selectedRefrigerant by viewModel.selectedRefrigerant.collectAsState()
    val pressureInput by viewModel.pressureInput.collectAsState()
    val isPressureImperial by viewModel.isPressureImperial.collectAsState()
    val lineTempInput by viewModel.lineTempInput.collectAsState()
    val isTempImperial by viewModel.isTempImperial.collectAsState()
    val mode by viewModel.mode.collectAsState()

    val saturationTemp by viewModel.saturationTemp.collectAsState()
    val targetValue by viewModel.targetValue.collectAsState()

    var showHint by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Hint Box
        AnimatedVisibility(
            visible = showHint,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Refrigerant Saturation & Charge Sizer",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Calculates superheat and subcooling live from saturation pressure. Target superheat should typically range from 8°F to 15°F depending on the expansion valve.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(
                        onClick = { showHint = false },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Active Calculation Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Live Saturation Calculator",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                // Refrigerant Selector
                Text(
                    text = "Select Refrigerant",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val refrigerants = listOf("R410A", "R134a", "R32", "R22")
                    refrigerants.forEach { ref ->
                        val isSelected = selectedRefrigerant == ref
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectRefrigerant(ref) },
                            label = { Text(ref) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chip_refrigerant_$ref")
                        )
                    }
                }

                // Mode Selector (Superheat or Subcooling)
                Text(
                    text = "Select Calculation Mode",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = listOf("Superheat", "Subcooling")
                    modes.forEach { modeName ->
                        val isSelected = mode == modeName
                        ElevatedButton(
                            onClick = { viewModel.setMode(modeName) },
                            colors = ButtonDefaults.elevatedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("mode_button_$modeName")
                        ) {
                            Text(modeName)
                        }
                    }
                }

                // Pressure Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pressureInput,
                        onValueChange = { viewModel.setPressureInput(it) },
                        label = { Text("Measured Pressure") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pressure_input"),
                        singleLine = true
                    )

                    Button(
                        onClick = { viewModel.togglePressureUnits() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("toggle_pressure_units_button")
                    ) {
                        Text(if (isPressureImperial) "psig" else "kPa")
                    }
                }

                // Line Temperature Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = lineTempInput,
                        onValueChange = { viewModel.setLineTempInput(it) },
                        label = { Text(if (mode == "Superheat") "Suction Line Temp" else "Liquid Line Temp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("line_temp_input"),
                        singleLine = true
                    )

                    Button(
                        onClick = { viewModel.toggleTempUnits() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("toggle_temp_units_button")
                    ) {
                        Text(if (isTempImperial) "°F" else "°C")
                    }
                }
            }
        }

        // Live Diagnostic Analysis Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Thermodynamic Saturation Output",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Saturation Temperature",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%.1f %s".format(saturationTemp, if (isTempImperial) "°F" else "°C"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Calculated $mode",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "%.1f Δ%s".format(targetValue, if (isTempImperial) "°F" else "°C"),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Dynamic HVAC Diagnostic Safety Status Bar
                val sh = if (isTempImperial) targetValue else targetValue * 1.8
                val (diagnosticText, diagnosticColor) = when {
                    sh < 5.0 -> "EXTREMELY LOW! Risk of liquid slugging into the compressor." to Color(0xFFDC2626) // Red
                    sh in 5.0..8.0 -> "Acceptably low. Flooded coil configuration." to Color(0xFFD97706) // Orange
                    sh in 8.0..15.0 -> "Optimal charge state. Safe HVAC operation." to Color(0xFF16A34A) // Green
                    sh in 15.0..25.0 -> "High. Risk of starved evaporator or poor efficiency." to Color(0xFFD97706) // Orange
                    else -> "CRITICAL HIGH! Evaporator starved, compressor overheating." to Color(0xFFDC2626) // Red
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(diagnosticColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (diagnosticColor == Color(0xFF16A34A)) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = diagnosticColor
                        )
                        Text(
                            text = diagnosticText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = diagnosticColor
                        )
                    }
                }

                Button(
                    onClick = { viewModel.logActivity() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_refrigerant_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Log",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Write to Field Notes Log")
                }
            }
        }

        // P/T Lookup Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Refrigerant P/T Chart (Saturation psig / kPa)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Compare refrigerant pressures at standard temperatures. Pressures listed are gauge (psig).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider()

                // Table Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TableHeaderItem("Temp", Modifier.weight(1f))
                    TableHeaderItem("R410A", Modifier.weight(1.2f))
                    TableHeaderItem("R134a", Modifier.weight(1.2f))
                    TableHeaderItem("R32", Modifier.weight(1.2f))
                    TableHeaderItem("R22", Modifier.weight(1.2f))
                }

                Divider()

                // Static P/T Rows
                val tempPoints = listOf(32, 40, 50, 70, 90, 110)
                tempPoints.forEach { fTemp ->
                    val cTemp = (fTemp - 32.0) / 1.8

                    // Saturation pressures in psig
                    val r410aPsig = getPsigForTemp("R410A", cTemp)
                    val r134aPsig = getPsigForTemp("R134a", cTemp)
                    val r32Psig = getPsigForTemp("R32", cTemp)
                    val r22Psig = getPsigForTemp("R22", cTemp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$fTemp°F",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "%.1f".format(r410aPsig), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                        Text(text = "%.1f".format(r134aPsig), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                        Text(text = "%.1f".format(r32Psig), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                        Text(text = "%.1f".format(r22Psig), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.TableHeaderItem(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

// Quick analytical psig solver for display chart
private fun getPsigForTemp(refrigerant: String, tempC: Double): Double {
    val tK = tempC + 273.15
    val lnP = when (refrigerant) {
        "R134a" -> 14.3686 - (2430.2 / (tK - 47.15))
        "R22" -> 14.186 - (2173.5 / (tK - 34.1))
        "R410A" -> 14.453 - (2127.3 / (tK - 45.4))
        "R32" -> 14.281 - (2084.1 / (tK - 49.3))
        else -> 14.3686 - (2430.2 / (tK - 47.15))
    }
    val pKpaAbs = Math.exp(lnP)
    val pKpaGauge = pKpaAbs - 101.325
    return pKpaGauge / 6.89476
}
