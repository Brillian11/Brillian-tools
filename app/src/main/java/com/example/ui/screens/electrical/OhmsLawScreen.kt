package com.example.ui.screens.electrical

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricalServices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OhmsLawScreen(
    viewModel: OhmsLawViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var vInput by remember { mutableStateOf(state.voltageV.toString()) }
    var iInput by remember { mutableStateOf(state.currentA.toString()) }
    var rInput by remember { mutableStateOf(state.resistanceOhm.toString()) }
    var pInput by remember { mutableStateOf(state.powerWatts.toString()) }

    fun syncInputs(v: Double, i: Double, r: Double, p: Double, pf: Double) {
        viewModel.updateInputs(v, i, r, p, pf)
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ohm's Law & Power Triangle",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Voltage (V), Current (I), Resistance (R), Real & Reactive Power",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Circuit Type Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "CIRCUIT SYSTEM TYPE",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.circuitType == CircuitType.DC,
                            onClick = { viewModel.setCircuitType(CircuitType.DC) },
                            label = { Text("DC Circuit") }
                        )
                        FilterChip(
                            selected = state.circuitType == CircuitType.AC_SINGLE_PHASE,
                            onClick = { viewModel.setCircuitType(CircuitType.AC_SINGLE_PHASE) },
                            label = { Text("1-Phase AC") }
                        )
                        FilterChip(
                            selected = state.circuitType == CircuitType.AC_THREE_PHASE,
                            onClick = { viewModel.setCircuitType(CircuitType.AC_THREE_PHASE) },
                            label = { Text("3-Phase AC") }
                        )
                    }
                }
            }

            // Calculation Mode (Known Variables)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "CHOOSE KNOWN INPUT VALUES",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = state.inputMode == CalculationInputMode.V_AND_I,
                            onClick = { viewModel.setInputMode(CalculationInputMode.V_AND_I) },
                            label = { Text("V & I") }
                        )
                        FilterChip(
                            selected = state.inputMode == CalculationInputMode.V_AND_R,
                            onClick = { viewModel.setInputMode(CalculationInputMode.V_AND_R) },
                            label = { Text("V & R") }
                        )
                        FilterChip(
                            selected = state.inputMode == CalculationInputMode.V_AND_P,
                            onClick = { viewModel.setInputMode(CalculationInputMode.V_AND_P) },
                            label = { Text("V & P") }
                        )
                        FilterChip(
                            selected = state.inputMode == CalculationInputMode.I_AND_R,
                            onClick = { viewModel.setInputMode(CalculationInputMode.I_AND_R) },
                            label = { Text("I & R") }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = state.inputMode == CalculationInputMode.I_AND_P,
                            onClick = { viewModel.setInputMode(CalculationInputMode.I_AND_P) },
                            label = { Text("I & P") }
                        )
                        FilterChip(
                            selected = state.inputMode == CalculationInputMode.R_AND_P,
                            onClick = { viewModel.setInputMode(CalculationInputMode.R_AND_P) },
                            label = { Text("R & P") }
                        )
                    }
                }
            }

            // Input Fields
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "ENTER CIRCUIT PARAMETERS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    val isVActive = state.inputMode in listOf(CalculationInputMode.V_AND_I, CalculationInputMode.V_AND_R, CalculationInputMode.V_AND_P)
                    val isIActive = state.inputMode in listOf(CalculationInputMode.V_AND_I, CalculationInputMode.I_AND_R, CalculationInputMode.I_AND_P)
                    val isRActive = state.inputMode in listOf(CalculationInputMode.V_AND_R, CalculationInputMode.I_AND_R, CalculationInputMode.R_AND_P)
                    val isPActive = state.inputMode in listOf(CalculationInputMode.V_AND_P, CalculationInputMode.I_AND_P, CalculationInputMode.R_AND_P)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isVActive) {
                            OutlinedTextField(
                                value = vInput,
                                onValueChange = {
                                    vInput = it
                                    it.toDoubleOrNull()?.let { v ->
                                        syncInputs(v, iInput.toDoubleOrNull() ?: 10.0, rInput.toDoubleOrNull() ?: 12.0, pInput.toDoubleOrNull() ?: 1200.0, state.powerFactor)
                                    }
                                },
                                label = { Text("Voltage (V)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (isIActive) {
                            OutlinedTextField(
                                value = iInput,
                                onValueChange = {
                                    iInput = it
                                    it.toDoubleOrNull()?.let { i ->
                                        syncInputs(vInput.toDoubleOrNull() ?: 120.0, i, rInput.toDoubleOrNull() ?: 12.0, pInput.toDoubleOrNull() ?: 1200.0, state.powerFactor)
                                    }
                                },
                                label = { Text("Current (Amps)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (isRActive) {
                            OutlinedTextField(
                                value = rInput,
                                onValueChange = {
                                    rInput = it
                                    it.toDoubleOrNull()?.let { r ->
                                        syncInputs(vInput.toDoubleOrNull() ?: 120.0, iInput.toDoubleOrNull() ?: 10.0, r, pInput.toDoubleOrNull() ?: 1200.0, state.powerFactor)
                                    }
                                },
                                label = { Text("Resistance (Ω)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (isPActive) {
                            OutlinedTextField(
                                value = pInput,
                                onValueChange = {
                                    pInput = it
                                    it.toDoubleOrNull()?.let { p ->
                                        syncInputs(vInput.toDoubleOrNull() ?: 120.0, iInput.toDoubleOrNull() ?: 10.0, rInput.toDoubleOrNull() ?: 12.0, p, state.powerFactor)
                                    }
                                },
                                label = { Text("Real Power (Watts)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (state.circuitType != CircuitType.DC) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Power Factor (PF = cos θ):", style = MaterialTheme.typography.labelSmall)
                                Text(String.format("%.2f (θ = %.1f°)", state.powerFactor, state.phaseAngleDegrees), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                            Slider(
                                value = state.powerFactor.toFloat(),
                                onValueChange = { pf ->
                                    syncInputs(
                                        vInput.toDoubleOrNull() ?: 120.0,
                                        iInput.toDoubleOrNull() ?: 10.0,
                                        rInput.toDoubleOrNull() ?: 12.0,
                                        pInput.toDoubleOrNull() ?: 1200.0,
                                        pf.toDouble()
                                    )
                                },
                                valueRange = 0.5f..1.0f
                            )
                        }
                    }
                }
            }

            // Power Triangle Canvas (For AC and DC visual)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (state.circuitType == CircuitType.DC) "DC POWER VECTOR" else "AC POWER TRIANGLE (P, Q, S)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    ) {
                        val primary = MaterialTheme.colorScheme.primary
                        val secondary = MaterialTheme.colorScheme.secondary
                        val tertiary = MaterialTheme.colorScheme.tertiary

                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            val w = size.width
                            val h = size.height

                            val originX = 40f
                            val originY = h - 20f

                            val pRatio = 0.7f
                            val pX = originX + (w - 80f) * pRatio
                            val pY = originY

                            val qRatio = if (state.circuitType == CircuitType.DC) 0f else (state.calculatedReactivePowerVAR / (state.calculatedApparentPowerVA + 0.001)).toFloat().coerceIn(0f, 0.8f)
                            val qY = originY - (h - 40f) * qRatio

                            // Draw Real Power (P) Line (Horizontal base)
                            drawLine(
                                color = primary,
                                start = Offset(originX, originY),
                                end = Offset(pX, pY),
                                strokeWidth = 5f
                            )

                            if (state.circuitType != CircuitType.DC && qRatio > 0.05f) {
                                // Draw Reactive Power (Q) Line (Vertical leg)
                                drawLine(
                                    color = tertiary,
                                    start = Offset(pX, originY),
                                    end = Offset(pX, qY),
                                    strokeWidth = 4f
                                )

                                // Draw Apparent Power (S) Line (Hypotenuse)
                                drawLine(
                                    color = secondary,
                                    start = Offset(originX, originY),
                                    end = Offset(pX, qY),
                                    strokeWidth = 4f
                                )

                                // Triangle fill
                                val path = Path().apply {
                                    moveTo(originX, originY)
                                    lineTo(pX, pY)
                                    lineTo(pX, qY)
                                    close()
                                }
                                drawPath(path, color = primary.copy(alpha = 0.15f))
                            }
                        }
                    }
                }
            }

            // Results Output Cards Grid
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SOLVED ELECTRICAL VALUES",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        IconButton(onClick = {
                            val info = "Ohm's Law Calculation (${state.circuitType.name}):\n" +
                                    "Voltage: ${String.format("%.2f", state.calculatedVoltage)} V\n" +
                                    "Current: ${String.format("%.2f", state.calculatedCurrent)} A\n" +
                                    "Resistance: ${String.format("%.3f", state.calculatedResistance)} Ω\n" +
                                    "Real Power (P): ${String.format("%.1f", state.calculatedRealPowerW)} W\n" +
                                    "Apparent Power (S): ${String.format("%.1f", state.calculatedApparentPowerVA)} VA\n" +
                                    "Reactive Power (Q): ${String.format("%.1f", state.calculatedReactivePowerVAR)} VAR\n" +
                                    "Power Factor: ${String.format("%.2f", state.powerFactor)}"
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logCalculation()
                            Toast.makeText(context, "Copied Ohm's Law Results!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Voltage (V)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.2f", state.calculatedVoltage)} V",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Column {
                            Text("Current (I)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.2f", state.calculatedCurrent)} A",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Resistance (R)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.3f", state.calculatedResistance)} Ω",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Real Power (P)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.1f", state.calculatedRealPowerW)} W",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Column {
                            Text("Apparent (S)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.1f", state.calculatedApparentPowerVA)} VA",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Reactive (Q)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "${String.format("%.1f", state.calculatedReactivePowerVAR)} VAR",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }

                    // Energy & Cost estimate banner
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Daily Consumption (8h/day)", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format("%.2f", state.dailyKwh)} kWh/day",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Est. Monthly Cost (@$0.15/kWh)", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "$${String.format("%.2f", state.monthlyCostUsd)} / mo",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
