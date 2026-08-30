package com.example.ui.screens.woodworking

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RafterCalculatorScreen(
    viewModel: RafterCalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var spanInput by remember { mutableStateOf(if (state.isMetric) "7.2" else "24.0") }
    var pitchInput by remember { mutableStateOf("6.0") }
    var ridgeInput by remember { mutableStateOf(if (state.isMetric) "38" else "1.5") }
    var overhangInput by remember { mutableStateOf(if (state.isMetric) "400" else "16.0") }
    var seatCutInput by remember { mutableStateOf(if (state.isMetric) "89" else "3.5") }

    androidx.compose.runtime.LaunchedEffect(state.isMetric) {
        spanInput = if (state.isMetric) "7.2" else "24.0"
        ridgeInput = if (state.isMetric) "38" else "1.5"
        overhangInput = if (state.isMetric) "400" else "16.0"
        seatCutInput = if (state.isMetric) "89" else "3.5"
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
            // Header
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
                            imageVector = Icons.Default.Architecture,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Roof Rafter & Framing Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (state.isMetric) "Metric roof span (m), overhang (mm), birdsmouth & rafter lengths" else "Common, Hip & Jack rafter lengths, birdsmouth cuts & plumb angles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Unit Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !state.isMetric,
                    onClick = { viewModel.setUnitSystem(false) },
                    label = { Text("Imperial (in / ft / pitch)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.isMetric,
                    onClick = { viewModel.setUnitSystem(true) },
                    label = { Text("Metric (mm / m / deg)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Description Info Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (state.isMetric)
                            "Metric Framing Mode: Roof span in meters (m), timber dimensions in millimeters (mm), roof slope pitch ratio or angle (°)."
                        else
                            "Imperial Framing Mode: Building span in feet, pitch expressed as rise in inches over 12\" run.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Input Form Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "ROOF GEOMETRY PARAMETERS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = spanInput,
                            onValueChange = {
                                spanInput = it
                                viewModel.updateInputs(
                                    it.toDoubleOrNull() ?: 24.0,
                                    pitchInput.toDoubleOrNull() ?: 6.0,
                                    ridgeInput.toDoubleOrNull() ?: 1.5,
                                    overhangInput.toDoubleOrNull() ?: 16.0,
                                    state.rafterLumberNominal,
                                    seatCutInput.toDoubleOrNull() ?: 3.5
                                )
                            },
                            label = { Text(if (state.isMetric) "Building Span (m)" else "Building Span (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = pitchInput,
                            onValueChange = {
                                pitchInput = it
                                viewModel.updateInputs(
                                    spanInput.toDoubleOrNull() ?: 24.0,
                                    it.toDoubleOrNull() ?: 6.0,
                                    ridgeInput.toDoubleOrNull() ?: 1.5,
                                    overhangInput.toDoubleOrNull() ?: 16.0,
                                    state.rafterLumberNominal,
                                    seatCutInput.toDoubleOrNull() ?: 3.5
                                )
                            },
                            label = { Text(if (state.isMetric) "Slope Pitch Ratio" else "Pitch (in/12)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ridgeInput,
                            onValueChange = {
                                ridgeInput = it
                                viewModel.updateInputs(
                                    spanInput.toDoubleOrNull() ?: 24.0,
                                    pitchInput.toDoubleOrNull() ?: 6.0,
                                    it.toDoubleOrNull() ?: 1.5,
                                    overhangInput.toDoubleOrNull() ?: 16.0,
                                    state.rafterLumberNominal,
                                    seatCutInput.toDoubleOrNull() ?: 3.5
                                )
                            },
                            label = { Text(if (state.isMetric) "Ridge Board (mm)" else "Ridge Board (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = overhangInput,
                            onValueChange = {
                                overhangInput = it
                                viewModel.updateInputs(
                                    spanInput.toDoubleOrNull() ?: 24.0,
                                    pitchInput.toDoubleOrNull() ?: 6.0,
                                    ridgeInput.toDoubleOrNull() ?: 1.5,
                                    it.toDoubleOrNull() ?: 16.0,
                                    state.rafterLumberNominal,
                                    seatCutInput.toDoubleOrNull() ?: 3.5
                                )
                            },
                            label = { Text(if (state.isMetric) "Overhang (mm)" else "Overhang (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Rafter Timber Stock:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    val stockChips = if (state.isMetric) {
                        listOf("38x140 mm (2x6)", "38x184 mm (2x8)", "38x235 mm (2x10)", "38x286 mm (2x12)")
                    } else {
                        listOf("2x6 (5.5\")", "2x8 (7.25\")", "2x10 (9.25\")", "2x12 (11.25\")")
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        stockChips.forEach { stock ->
                            FilterChip(
                                selected = state.rafterLumberNominal == stock,
                                onClick = {
                                    viewModel.updateInputs(
                                        spanInput.toDoubleOrNull() ?: 24.0,
                                        pitchInput.toDoubleOrNull() ?: 6.0,
                                        ridgeInput.toDoubleOrNull() ?: 1.5,
                                        overhangInput.toDoubleOrNull() ?: 16.0,
                                        stock,
                                        seatCutInput.toDoubleOrNull() ?: 3.5
                                    )
                                },
                                label = { Text(stock, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            }

            // Calculations Summary Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "CALCULATED RAFTER LENGTHS & CUT ANGLES",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Common Rafter Length", style = MaterialTheme.typography.labelSmall)
                            Text(state.commonRafterFtIn, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            Text("Total w/ Eave Tail: ${state.totalCommonLengthWithTailFtIn}", style = MaterialTheme.typography.bodySmall)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Plumb / Seat Cut Angle", style = MaterialTheme.typography.labelSmall)
                            Text("${String.format("%.1f", state.plumbCutAngleDeg)}° / ${String.format("%.1f", state.seatCutAngleDeg)}°", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Roof Incline: ${String.format("%.1f", state.pitchAngleDeg)}°", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Hip / Valley Rafter", style = MaterialTheme.typography.labelSmall)
                            Text(state.hipRafterFtIn, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Roof Area", style = MaterialTheme.typography.labelSmall)
                            Text(state.totalRoofAreaDisplay, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32)))
                        }
                    }
                }
            }
        }
    }
}
