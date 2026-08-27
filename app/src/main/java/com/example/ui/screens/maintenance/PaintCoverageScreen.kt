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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
fun PaintCoverageScreen(
    viewModel: PaintCoverageViewModel,
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
                                .background(Color(0xFFFCE7F3)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatPaint,
                                contentDescription = null,
                                tint = Color(0xFFDB2777)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Wall Area & Paint Coverage",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Net wall, primer, trim & ceiling cans",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.isMetric) "Metric (m/L)" else "US (ft/Gal)",
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

        // Section 1: Room Dimensions
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
                        text = "1. Room Geometry & Wall Height",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Net: ${String.format("%.1f", if (state.isMetric) state.netWallAreaSqM else state.netWallAreaSqFt)} $areaUnit",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFDB2777),
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
                        value = state.ceilingHeight.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(height = v) } },
                        label = { Text("Height ($dimUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Section 2: Openings Deductions (Doors, Windows)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Openings Deductions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Standard Doors (-21 sq ft / 2 m² each)", style = MaterialTheme.typography.bodyMedium)
                        Text("Includes frame trim subtraction", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (state.doorCount > 0) viewModel.updateDimensions(doors = state.doorCount - 1) }) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("${state.doorCount}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { viewModel.updateDimensions(doors = state.doorCount + 1) }) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Standard Windows (-15 sq ft / 1.4 m² each)", style = MaterialTheme.typography.bodyMedium)
                        Text("Subtracted from paintable surface", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (state.windowCount > 0) viewModel.updateDimensions(windows = state.windowCount - 1) }) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("${state.windowCount}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { viewModel.updateDimensions(windows = state.windowCount + 1) }) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Surface Porosity & Coats
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Wall Surface Porosity & Absorption",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                SurfacePorosity.values().forEach { por ->
                    FilterChip(
                        selected = state.surfacePorosity == por,
                        onClick = { viewModel.setSurfacePorosity(por) },
                        label = {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(por.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${por.desc} (~${por.coverageSqFtPerGal.toInt()} sq ft/gal)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        leadingIcon = if (state.surfacePorosity == por) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Coats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Wall Paint Coats", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(1, 2, 3).forEach { c ->
                            FilterChip(
                                selected = state.paintCoats == c,
                                onClick = { viewModel.updateCoats(paint = c) },
                                label = { Text("$c ${if (c == 1) "Coat" else "Coats"}") }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Primer / Sealer Coats", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0, 1, 2).forEach { c ->
                            FilterChip(
                                selected = state.primerCoats == c,
                                onClick = { viewModel.updateCoats(primer = c) },
                                label = { Text(if (c == 0) "None" else "$c ${if (c == 1) "Coat" else "Coats"}") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Paint Ceiling (${String.format("%.0f", if (state.isMetric) state.ceilingAreaSqM else state.ceilingAreaSqFt)} $areaUnit)", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = state.includeCeiling, onCheckedChange = { viewModel.toggleCeiling() })
                }
            }
        }

        // Section 4: Paint Can Takeoff Results
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Paint & Supply Order Takeoff",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFFCE7F3),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Wall Topcoat Paint", style = MaterialTheme.typography.labelSmall, color = Color(0xFFDB2777))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isMetric) "${state.wallPaintLitersBuy} Liters" else "${state.wallPaintGallonCans} Gal ${if (state.wallPaintQuartCans > 0) "+ ${state.wallPaintQuartCans} Qt" else ""}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFFDB2777))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${state.paintCoats} Coats on ${String.format("%.0f", if (state.isMetric) state.netWallAreaSqM else state.netWallAreaSqFt)} $areaUnit",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFDB2777).copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Primer / Sealer", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1D4ED8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.primerCoats == 0) "None Needed" else if (state.isMetric) "${state.primerLitersBuy} Liters" else "${state.primerGallonCans} Gal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D4ED8))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (state.primerCoats == 0) "Pre-primed walls" else "${state.primerCoats} coat foundation",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1D4ED8).copy(alpha = 0.8f)
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
                        color = Color(0xFFFEF3C7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Ceiling Flat White", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB45309))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (!state.includeCeiling) "Excluded" else if (state.isMetric) "${state.ceilingPaintLitersBuy} Liters" else "${state.ceilingPaintGallonCans} Gal",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (state.includeCeiling) "2 coats ceiling" else "Walls only",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB45309).copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF3E8FF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Tape & Sundries", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7E22CE))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.painterTapeRolls} Tape Rolls",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF7E22CE))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${state.rollerCovers} Rollers, ${state.dropCloths} Drop Cloths",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF7E22CE).copy(alpha = 0.8f)
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDB2777))
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Paint Takeoff to Job Log", fontWeight = FontWeight.Bold)
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
                        text = "Paint takeoff saved to offline project log!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
