package com.example.ui.screens.maintenance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TileGroutScreen(
    viewModel: TileGroutViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showSavedMessage by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = null,
                                tint = Color(0xFF4338CA)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tile, Grout & Flooring",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Area, waste, tile cartons & grout weight",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.isMetric) "Metric (m/kg)" else "US (ft/lbs)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = state.isMetric,
                            onCheckedChange = { viewModel.setUnitSystem(it) }
                        )
                    }
                }
            }
        }

        // Section 1: Surface Area & Openings
        val dimUnit = if (state.isMetric) "m" else "ft"
        val areaUnit = if (state.isMetric) "m²" else "sq.ft"
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. Room Dimensions & Cutouts",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Net: ${String.format("%.1f", if (state.isMetric) state.netAreaSqM else state.netAreaSqFt)} $areaUnit",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF4338CA),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.roomLength.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(length = v) } },
                        label = { Text("Length ($dimUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.roomWidth.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(width = v) } },
                        label = { Text("Width ($dimUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.cutoutAreaSqFtOrM2.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(cutout = v) } },
                        label = { Text("Cutouts ($areaUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Waste percentage slider
                Text(
                    text = "Waste & Pattern Factor: ${state.wastePercent.toInt()}% (${if (state.wastePercent <= 10) "Straight Grid" else if (state.wastePercent <= 15) "Diagonal / Offset" else "Herringbone / Mosaic"})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
                Slider(
                    value = state.wastePercent.toFloat(),
                    onValueChange = { viewModel.updateDimensions(waste = it.toDouble()) },
                    valueRange = 5f..25f,
                    steps = 19
                )
            }
        }

        // Section 2: Tile Size Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Tile Format & Dimensions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                StandardTilePreset.values().forEach { preset ->
                    FilterChip(
                        selected = state.tilePreset == preset,
                        onClick = { viewModel.setTilePreset(preset) },
                        label = { Text(preset.label, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        leadingIcon = if (state.tilePreset == preset) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }

                if (state.tilePreset == StandardTilePreset.CUSTOM) {
                    Spacer(modifier = Modifier.height(10.dp))
                    val customDimUnit = if (state.isMetric) "cm" else "in"
                    val customThickUnit = if (state.isMetric) "mm" else "in"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = state.customTileLengthInOrCm.toString(),
                            onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(customL = v) } },
                            label = { Text("Length ($customDimUnit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.customTileWidthInOrCm.toString(),
                            onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(customW = v) } },
                            label = { Text("Width ($customDimUnit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.customTileThicknessInOrMm.toString(),
                            onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(customT = v) } },
                            label = { Text("Thick ($customThickUnit)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = state.piecesPerBox.toString(),
                    onValueChange = { it.toIntOrNull()?.let { v -> viewModel.updateDimensions(boxCount = v) } },
                    label = { Text("Tiles per Box / Carton") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Section 3: Grout Joint & Thin-Set Trowel Notch
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Grout Joint Width & Mortar Notch",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GroutJointWidth.values().forEach { joint ->
                        FilterChip(
                            selected = state.jointWidth == joint,
                            onClick = { viewModel.setJointWidth(joint) },
                            label = { Text(joint.label, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text("Thin-Set Trowel Notch Sizing", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(6.dp))
                MortarTrowelNotch.values().forEach { notch ->
                    FilterChip(
                        selected = state.trowelNotch == notch,
                        onClick = { viewModel.setTrowelNotch(notch) },
                        label = { Text(notch.label, fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }

        // Section 4: Takeoff Results
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Tile & Flooring Bill of Materials",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFE0E7FF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Tile Count", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4338CA))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.totalTilesWithWaste} Tiles",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF4338CA))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${state.totalBoxesNeeded} Boxes (${state.piecesPerBox} pcs/box)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4338CA).copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Grout Needed", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB45309))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isMetric) "${String.format("%.1f", state.groutWeightKg)} kg" else "${String.format("%.1f", state.groutWeightLbs)} lbs",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (state.isMetric) "${state.grout5kgBags} bags (5kg)" else "${state.grout25lbBags} bags (25lb)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB45309).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Thin-Set Mortar", style = MaterialTheme.typography.labelSmall, color = Color(0xFF15803D))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.thinset50lbBags} Bags",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF15803D))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "50 lb / 25 kg bags (+10% waste)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF15803D).copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF3E8FF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Perimeter Edge Trim", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7E22CE))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isMetric) "${String.format("%.1f", state.perimeterTrimM)} m" else "${String.format("%.1f", state.perimeterTrimFt)} ft",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF7E22CE))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Schluter/Jolly trim",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF7E22CE).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(14.dp))

                // Recommended Grout Type
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Recommended: ${state.recommendedGroutType.label}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            )
                            Text(
                                text = state.recommendedGroutType.recommendation,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }

        // Action Button: Save & Log Activity
        Button(
            onClick = {
                viewModel.logCalculation()
                showSavedMessage = true
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4338CA))
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Tile Takeoff to Job Log", fontWeight = FontWeight.Bold)
        }

        if (showSavedMessage) {
            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tile & grout takeoff saved to offline project log!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
