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
import androidx.compose.material.icons.filled.Dashboard
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
fun DrywallStudScreen(
    viewModel: DrywallStudViewModel,
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
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = null,
                                tint = Color(0xFFB45309)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Drywall & Framing Studs",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Sheets, 16/24\" OC studs, screws & mud",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.isMetric) "Metric (m)" else "US (ft/in)",
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

        // Section 1: Wall & Ceiling Dimensions
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
                        text = "1. Wall Run Length & Height",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Total: ${String.format("%.1f", if (state.isMetric) state.combinedDrywallAreaSqM else state.combinedDrywallAreaSqFt)} $areaUnit",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFB45309),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.wallRunLength.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(wallRun = v) } },
                        label = { Text("Wall Run ($dimUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.wallHeight.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(height = v) } },
                        label = { Text("Wall Height ($dimUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Include Ceiling Drywall", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = state.includeCeiling, onCheckedChange = { viewModel.toggleCeiling() })
                }

                if (state.includeCeiling) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.roomWidthForCeiling.toString(),
                        onValueChange = { it.toDoubleOrNull()?.let { v -> viewModel.updateDimensions(roomWidth = v) } },
                        label = { Text("Room Width for Ceiling ($dimUnit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Waste slider
                Text(
                    text = "Cutting Waste Margin: ${state.wastePercent.toInt()}%",
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

        // Section 2: Drywall Sheet Size Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "2. Drywall Sheet Dimensions",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                DrywallSheetPreset.values().forEach { preset ->
                    FilterChip(
                        selected = state.sheetPreset == preset,
                        onClick = { viewModel.setSheetPreset(preset) },
                        label = { Text(preset.label, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        leadingIcon = if (state.sheetPreset == preset) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }

        // Section 3: Framing Stud Spacing & Openings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "3. Wall Stud Spacing (On-Center)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(10.dp))
                StudSpacing.values().forEach { spacing ->
                    FilterChip(
                        selected = state.studSpacing == spacing,
                        onClick = { viewModel.setStudSpacing(spacing) },
                        label = { Text(spacing.label, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        leadingIcon = if (state.studSpacing == spacing) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text("Corners & Opening Framing Additions", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("90° Corners (3 studs / corner)", style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (state.cornersCount > 0) viewModel.updateOpenings(corners = state.cornersCount - 1) }) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("${state.cornersCount}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { viewModel.updateOpenings(corners = state.cornersCount + 1) }) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("T-Junction Partitions (2 studs)", style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (state.tJunctionsCount > 0) viewModel.updateOpenings(tJunctions = state.tJunctionsCount - 1) }) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("${state.tJunctionsCount}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { viewModel.updateOpenings(tJunctions = state.tJunctionsCount + 1) }) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Door & Window Openings (King+Jack studs)", style = MaterialTheme.typography.bodySmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (state.doorOpenings > 0) viewModel.updateOpenings(doors = state.doorOpenings - 1) }) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("${state.doorOpenings + state.windowOpenings}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { viewModel.updateOpenings(doors = state.doorOpenings + 1) }) {
                            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Takeoff Bill of Materials
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Drywall & Framing Material Takeoff",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
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
                            Text("Drywall Sheets", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB45309))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.totalSheetsWithWaste} Sheets",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFFB45309))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${state.sheetPreset.label} (+${state.wastePercent.toInt()}% waste)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFB45309).copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFECFDF5),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Framing 2x4/2x6 Studs", style = MaterialTheme.typography.labelSmall, color = Color(0xFF047857))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.totalFramingStudsNeeded} Studs",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF047857))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Field + Corners + Openings",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF047857).copy(alpha = 0.8f)
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
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Joint Mud / Compound", style = MaterialTheme.typography.labelSmall, color = Color(0xFF1D4ED8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.jointCompound4_5GalBuckets} Buckets",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF1D4ED8))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "4.5 Gallon ready-mix pails",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF1D4ED8).copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFF3E8FF),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Screws & Joint Tape", style = MaterialTheme.typography.labelSmall, color = Color(0xFF7E22CE))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${state.screwsCount} Screws",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = Color(0xFF7E22CE))
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "1-1/4\" (~${String.format("%.1f", state.screwsLbs)} lbs) + ${state.jointTapeRolls500Ft} Tape roll",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF7E22CE).copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFFFFBEB),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Top & Sole Plate Lumber", style = MaterialTheme.typography.labelSmall, color = Color(0xFFB45309))
                            Text(
                                text = if (state.isMetric) "${String.format("%.1f", state.plateLumberTotalLinearM)} m Total" else "${String.format("%.1f", state.plateLumberTotalLinearFt)} linear ft",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            )
                        }
                        Surface(
                            color = Color(0xFFD97706),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${state.plateBoards10FtCount} Boards (10ft)",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309))
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Drywall & Framing Takeoff to Job Log", fontWeight = FontWeight.Bold)
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
                        text = "Drywall and framing takeoff saved to offline project log!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF15803D), fontWeight = FontWeight.Medium)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
