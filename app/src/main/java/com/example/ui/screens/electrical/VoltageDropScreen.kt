package com.example.ui.screens.electrical

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
fun VoltageDropScreen(
    viewModel: VoltageDropViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    var sourceVInput by remember { mutableStateOf(state.sourceVoltageV.toString()) }
    var loadAInput by remember { mutableStateOf(state.loadCurrentA.toString()) }
    var distInput by remember { mutableStateOf(if (state.isMetric) "30.0" else "100.0") }

    androidx.compose.runtime.LaunchedEffect(state.isMetric) {
        distInput = if (state.isMetric) "30.0" else "100.0"
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !state.isMetric,
                    onClick = { viewModel.setUnitSystem(false) },
                    label = { Text("Imperial (ft / AWG)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.isMetric,
                    onClick = { viewModel.setUnitSystem(true) },
                    label = { Text("Metric (m / mm²)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("ELECTRICAL PARAMETERS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sourceVInput,
                            onValueChange = {
                                sourceVInput = it
                                viewModel.updateInputs(
                                    it.toDoubleOrNull() ?: 120.0,
                                    loadAInput.toDoubleOrNull() ?: 15.0,
                                    distInput.toDoubleOrNull() ?: 100.0
                                )
                            },
                            label = { Text("Voltage (V)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = loadAInput,
                            onValueChange = {
                                loadAInput = it
                                viewModel.updateInputs(
                                    sourceVInput.toDoubleOrNull() ?: 120.0,
                                    it.toDoubleOrNull() ?: 15.0,
                                    distInput.toDoubleOrNull() ?: 100.0
                                )
                            },
                            label = { Text("Load Current (A)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = distInput,
                        onValueChange = {
                            distInput = it
                            viewModel.updateInputs(
                                sourceVInput.toDoubleOrNull() ?: 120.0,
                                loadAInput.toDoubleOrNull() ?: 15.0,
                                it.toDoubleOrNull() ?: 100.0
                            )
                        },
                        label = { Text(if (state.isMetric) "One-Way Distance (m)" else "One-Way Distance (ft)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("VOLTAGE DROP RESULTS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Voltage Drop", style = MaterialTheme.typography.labelSmall)
                            Text("${String.format("%.2f", state.voltageDropV)} V (${String.format("%.2f", state.percentageDrop)}%)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Voltage at Load", style = MaterialTheme.typography.labelSmall)
                            Text("${String.format("%.1f", state.voltageAtLoadV)} V", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
