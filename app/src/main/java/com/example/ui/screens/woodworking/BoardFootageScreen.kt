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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
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
fun BoardFootageScreen(
    viewModel: BoardFootageViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var itemLabel by remember { mutableStateOf("") }
    var thicknessInput by remember { mutableStateOf(if (state.isMetric) String.format("%.1f", state.thicknessInches * 25.4) else state.thicknessInches.toString()) }
    var widthInput by remember { mutableStateOf(if (state.isMetric) String.format("%.1f", state.widthInches * 25.4) else state.widthInches.toString()) }
    var lengthInput by remember { mutableStateOf(if (state.isMetric) String.format("%.2f", state.lengthFeet / 3.28084) else state.lengthFeet.toString()) }
    var quantityInput by remember { mutableStateOf(state.quantity.toString()) }
    var priceInput by remember { mutableStateOf(state.customPricePerBF.toString()) }

    androidx.compose.runtime.LaunchedEffect(state.isMetric) {
        thicknessInput = if (state.isMetric) String.format("%.1f", state.thicknessInches * 25.4) else state.thicknessInches.toString()
        widthInput = if (state.isMetric) String.format("%.1f", state.widthInches * 25.4) else state.widthInches.toString()
        lengthInput = if (state.isMetric) String.format("%.2f", state.lengthFeet / 3.28084) else state.lengthFeet.toString()
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
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lumber Board Feet & Metric Volume",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (state.isMetric) "Calculate wood volume in Cubic Meters (m³) & Metric Timber Mass (kg)" else "Calculate Board Feet (BF), lumber cost & dry weight (lbs)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

             // Info & Description Card
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
                            "Metric Lumber Rule: 1 m³ = 1,000,000 cm³ = ~423.7 Board Feet. Dimensions are entered in millimeters (mm) and length in meters (m)."
                        else
                            "Standard Lumber Rule: 1 Board Foot = 12\" x 12\" x 1\" (144 cu in). 4/4 = 1.0\", 8/4 = 2.0\".",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Inputs Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ADD LUMBER ITEM",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    OutlinedTextField(
                        value = itemLabel,
                        onValueChange = { itemLabel = it },
                        label = { Text("Description / Part Name (e.g. Table Top)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Quarter / Metric Thickness Presets
                    Text(
                        text = if (state.isMetric) "Standard Metric Rough Thickness (mm):" else "Standard Rough Quarter Thickness:",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )

                    val quarterChips = if (state.isMetric) {
                        listOf("25mm (4/4)", "32mm (5/4)", "38mm (6/4)", "50mm (8/4)", "63mm (10/4)", "75mm (12/4)", "100mm (16/4)")
                    } else {
                        listOf("4/4 (1.0\")", "5/4 (1.25\")", "6/4 (1.5\")", "8/4 (2.0\")", "10/4 (2.5\")", "12/4 (3.0\")", "16/4 (4.0\")")
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        quarterChips.chunked(4).forEach { rowChips ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                rowChips.forEach { chip ->
                                    FilterChip(
                                        selected = state.inputThicknessQuarter == chip,
                                        onClick = {
                                            viewModel.setThicknessQuarter(chip)
                                            thicknessInput = if (state.isMetric) String.format("%.1f", state.thicknessInches * 25.4) else state.thicknessInches.toString()
                                        },
                                        label = { Text(chip, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = thicknessInput,
                            onValueChange = {
                                thicknessInput = it
                                val t = it.toDoubleOrNull() ?: 1.0
                                val w = widthInput.toDoubleOrNull() ?: 6.0
                                val l = lengthInput.toDoubleOrNull() ?: 8.0
                                val q = quantityInput.toIntOrNull() ?: 1
                                val p = priceInput.toDoubleOrNull() ?: 12.50
                                viewModel.updateDimensions(t, w, l, q, p)
                            },
                            label = { Text(if (state.isMetric) "Thick (mm)" else "Thick (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = widthInput,
                            onValueChange = {
                                widthInput = it
                                val t = thicknessInput.toDoubleOrNull() ?: 1.0
                                val w = it.toDoubleOrNull() ?: 6.0
                                val l = lengthInput.toDoubleOrNull() ?: 8.0
                                val q = quantityInput.toIntOrNull() ?: 1
                                val p = priceInput.toDoubleOrNull() ?: 12.50
                                viewModel.updateDimensions(t, w, l, q, p)
                            },
                            label = { Text(if (state.isMetric) "Width (mm)" else "Width (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lengthInput,
                            onValueChange = {
                                lengthInput = it
                                val t = thicknessInput.toDoubleOrNull() ?: 1.0
                                val w = widthInput.toDoubleOrNull() ?: 6.0
                                val l = it.toDoubleOrNull() ?: 8.0
                                val q = quantityInput.toIntOrNull() ?: 1
                                val p = priceInput.toDoubleOrNull() ?: 12.50
                                viewModel.updateDimensions(t, w, l, q, p)
                            },
                            label = { Text(if (state.isMetric) "Length (m)" else "Length (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = {
                                quantityInput = it
                                val t = thicknessInput.toDoubleOrNull() ?: 1.0
                                val w = widthInput.toDoubleOrNull() ?: 6.0
                                val l = lengthInput.toDoubleOrNull() ?: 8.0
                                val q = it.toIntOrNull() ?: 1
                                val p = priceInput.toDoubleOrNull() ?: 12.50
                                viewModel.updateDimensions(t, w, l, q, p)
                            },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = {
                                priceInput = it
                                val t = thicknessInput.toDoubleOrNull() ?: 1.0
                                val w = widthInput.toDoubleOrNull() ?: 6.0
                                val l = lengthInput.toDoubleOrNull() ?: 8.0
                                val q = quantityInput.toIntOrNull() ?: 1
                                val p = it.toDoubleOrNull() ?: 12.50
                                viewModel.updateDimensions(t, w, l, q, p)
                            },
                            label = { Text(if (state.isMetric) "Price / m³ ($)" else "Price / BF ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.addLumberItem(itemLabel)
                            itemLabel = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Cut List")
                    }
                }
            }

            // Summary Banners
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "PROJECT LUMBER TOTALS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(if (state.isMetric) "Net Volume (m³)" else "Net Board Feet", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = if (state.isMetric) "${String.format("%.4f", state.totalBoardFeetNet * 0.00235974)} m³" else "${String.format("%.1f", state.totalBoardFeetNet)} BF",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column {
                            Text("Gross (+${state.wastePercentage.toInt()}% waste)", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = if (state.isMetric) "${String.format("%.4f", state.totalBoardFeetWithWaste * 0.00235974)} m³" else "${String.format("%.1f", state.totalBoardFeetWithWaste)} BF",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Estimated Cost", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "$${String.format("%.2f", state.totalLumberCost)}",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF2E7D32)
                                )
                            )
                        }
                    }
                    Text(
                        text = if (state.isMetric)
                            "Estimated Total Dry Weight: ~${String.format("%.1f", state.totalEstimatedWeightLbs * 0.453592)} kg"
                        else
                            "Estimated Total Dry Weight: ~${String.format("%.1f", state.totalEstimatedWeightLbs)} lbs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cut List Table
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "LUMBER CUT LIST (${state.lumberList.size} ITEMS)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    state.lumberList.forEach { item ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${item.quantity}x",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        text = if (state.isMetric)
                                            "${item.species} • ${String.format("%.0f", item.thicknessInches * 25.4)}mm x ${String.format("%.0f", item.widthInches * 25.4)}mm x ${String.format("%.2f", item.lengthFeet / 3.28084)}m"
                                        else
                                            "${item.species} • ${item.thicknessInches}\" x ${item.widthInches}\" x ${item.lengthFeet}'",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (state.isMetric)
                                            "${String.format("%.4f", item.boardFeet * 0.00235974)} m³ (${String.format("%.1f", item.weightLbs * 0.453592)} kg) • $${String.format("%.2f", item.itemCost)}"
                                        else
                                            "${String.format("%.1f", item.boardFeet)} BF (${String.format("%.1f", item.weightLbs)} lbs) • $${String.format("%.2f", item.itemCost)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { viewModel.removeLumberItem(item.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
