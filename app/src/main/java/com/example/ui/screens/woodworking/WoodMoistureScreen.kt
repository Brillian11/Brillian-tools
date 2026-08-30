package com.example.ui.screens.woodworking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun WoodMoistureScreen(
    viewModel: WoodMoistureViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    var initialMcInput by remember { mutableStateOf(state.initialMoisturePct.toString()) }
    var targetMcInput by remember { mutableStateOf(state.targetMoisturePct.toString()) }
    var widthInput by remember { mutableStateOf(if (state.isMetric) "200" else "8.0") }
    var thicknessInput by remember { mutableStateOf(if (state.isMetric) "25" else "1.0") }

    androidx.compose.runtime.LaunchedEffect(state.isMetric) {
        widthInput = if (state.isMetric) "200" else "8.0"
        thicknessInput = if (state.isMetric) "25" else "1.0"
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
            // Unit Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !state.isMetric,
                    onClick = { viewModel.setUnitSystem(false) },
                    label = { Text("Imperial (in / %)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.isMetric,
                    onClick = { viewModel.setUnitSystem(true) },
                    label = { Text("Metric (mm / %)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Inputs Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("MOISTURE & BOARD DIMENSIONS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = initialMcInput,
                            onValueChange = {
                                initialMcInput = it
                                viewModel.updateInputs(
                                    it.toDoubleOrNull() ?: 14.0,
                                    targetMcInput.toDoubleOrNull() ?: 8.0,
                                    widthInput.toDoubleOrNull() ?: 8.0,
                                    thicknessInput.toDoubleOrNull() ?: 1.0
                                )
                            },
                            label = { Text("Initial MC (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = targetMcInput,
                            onValueChange = {
                                targetMcInput = it
                                viewModel.updateInputs(
                                    initialMcInput.toDoubleOrNull() ?: 14.0,
                                    it.toDoubleOrNull() ?: 8.0,
                                    widthInput.toDoubleOrNull() ?: 8.0,
                                    thicknessInput.toDoubleOrNull() ?: 1.0
                                )
                            },
                            label = { Text("Target MC (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = widthInput,
                            onValueChange = {
                                widthInput = it
                                viewModel.updateInputs(
                                    initialMcInput.toDoubleOrNull() ?: 14.0,
                                    targetMcInput.toDoubleOrNull() ?: 8.0,
                                    it.toDoubleOrNull() ?: 8.0,
                                    thicknessInput.toDoubleOrNull() ?: 1.0
                                )
                            },
                            label = { Text(if (state.isMetric) "Width (mm)" else "Width (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = thicknessInput,
                            onValueChange = {
                                thicknessInput = it
                                viewModel.updateInputs(
                                    initialMcInput.toDoubleOrNull() ?: 14.0,
                                    targetMcInput.toDoubleOrNull() ?: 8.0,
                                    widthInput.toDoubleOrNull() ?: 8.0,
                                    it.toDoubleOrNull() ?: 1.0
                                )
                            },
                            label = { Text(if (state.isMetric) "Thick (mm)" else "Thick (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Output Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SHRINKAGE / EXPANSION RESULTS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Dimensional Change", style = MaterialTheme.typography.labelSmall)
                            Text(state.widthChangeDisplay, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Final Dimension", style = MaterialTheme.typography.labelSmall)
                            Text(state.finalWidthDisplay, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
