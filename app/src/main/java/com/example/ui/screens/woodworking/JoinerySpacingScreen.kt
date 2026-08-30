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
fun JoinerySpacingScreen(
    viewModel: JoinerySpacingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    var widthInput by remember { mutableStateOf(if (state.isMetric) "450" else "18.0") }
    var thicknessInput by remember { mutableStateOf(if (state.isMetric) "19" else "0.75") }
    var countInput by remember { mutableStateOf(state.jointCount.toString()) }
    var sizeInput by remember { mutableStateOf(if (state.isMetric) "38" else "1.5") }
    var edgeInput by remember { mutableStateOf(if (state.isMetric) "38" else "1.5") }

    androidx.compose.runtime.LaunchedEffect(state.isMetric) {
        widthInput = if (state.isMetric) "450" else "18.0"
        thicknessInput = if (state.isMetric) "19" else "0.75"
        sizeInput = if (state.isMetric) "38" else "1.5"
        edgeInput = if (state.isMetric) "38" else "1.5"
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
                    label = { Text("Imperial (in)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = state.isMetric,
                    onClick = { viewModel.setUnitSystem(true) },
                    label = { Text("Metric (mm)") },
                    modifier = Modifier.weight(1f)
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("WORKPIECE & JOINERY PARAMETERS", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = widthInput,
                            onValueChange = {
                                widthInput = it
                                viewModel.updateInputs(
                                    it.toDoubleOrNull() ?: 18.0,
                                    thicknessInput.toDoubleOrNull() ?: 0.75,
                                    countInput.toIntOrNull() ?: 3,
                                    sizeInput.toDoubleOrNull() ?: 1.5,
                                    edgeInput.toDoubleOrNull() ?: 1.5
                                )
                            },
                            label = { Text(if (state.isMetric) "Width (mm)" else "Width (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = countInput,
                            onValueChange = {
                                countInput = it
                                viewModel.updateInputs(
                                    widthInput.toDoubleOrNull() ?: 18.0,
                                    thicknessInput.toDoubleOrNull() ?: 0.75,
                                    it.toIntOrNull() ?: 3,
                                    sizeInput.toDoubleOrNull() ?: 1.5,
                                    edgeInput.toDoubleOrNull() ?: 1.5
                                )
                            },
                            label = { Text("Joint Count") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("CENTERLINE LAYOUT COORDINATES", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Center-to-Center Spacing", style = MaterialTheme.typography.labelSmall)
                            Text(state.c2cDisplay, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Edge Offset", style = MaterialTheme.typography.labelSmall)
                            Text(state.edgeDisplay, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }
    }
}
