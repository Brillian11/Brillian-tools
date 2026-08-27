package com.example.ui.screens.civil

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Layers
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MasonryMortarScreen(
    viewModel: MasonryMortarViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

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
                            imageVector = Icons.Default.HomeWork,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Brick, Block & Mortar Calculator",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Quantities for standard bricks, CMU concrete blocks, mortar mix volume & core grout filling",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Masonry Type & Units Selector
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
                            text = "MASONRY UNIT TYPE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = !state.isMetric,
                                onClick = { viewModel.setUnitSystem(false) },
                                label = { Text("US (sq.ft/in)") }
                            )
                            FilterChip(
                                selected = state.isMetric,
                                onClick = { viewModel.setUnitSystem(true) },
                                label = { Text("Metric (m²/mm)") }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MasonryType.values().forEach { type ->
                            FilterChip(
                                selected = state.masonryType == type,
                                onClick = { viewModel.setMasonryType(type) },
                                label = { Text(type.label) }
                            )
                        }
                    }

                    // Grouting Option if CMU
                    if (state.masonryType.isCmuBlock) {
                        Text("CMU CORE REINFORCING GROUT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            GroutFillOption.values().forEach { opt ->
                                FilterChip(
                                    selected = state.groutOption == opt,
                                    onClick = { viewModel.setGroutOption(opt) },
                                    label = { Text(opt.label) }
                                )
                            }
                        }
                    }
                }
            }

            // Wall Dimension Inputs
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "WALL DIMENSIONS & OPENINGS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.wallLength.toString(),
                            onValueChange = { viewModel.updateInputs(wLength = it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(if (state.isMetric) "Wall Length (m)" else "Wall Length (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.wallHeight.toString(),
                            onValueChange = { viewModel.updateInputs(wHeight = it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(if (state.isMetric) "Wall Height (m)" else "Wall Height (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = state.openingsAreaSqFtOrM2.toString(),
                            onValueChange = { viewModel.updateInputs(openingsArea = it.toDoubleOrNull() ?: 0.0) },
                            label = { Text(if (state.isMetric) "Doors/Windows Deduct (m²)" else "Doors/Windows Deduct (sq.ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = state.mortarJointInOrMm.toString(),
                            onValueChange = { viewModel.updateInputs(joint = it.toDoubleOrNull() ?: 0.375) },
                            label = { Text(if (state.isMetric) "Mortar Joint (mm)" else "Mortar Joint (in, 3/8\")") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Double Wythe toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Wall Construction:", style = MaterialTheme.typography.bodyMedium)
                        FilterChip(
                            selected = !state.isDoubleWythe,
                            onClick = { if (state.isDoubleWythe) viewModel.toggleDoubleWythe() },
                            label = { Text("Single Wythe") }
                        )
                        FilterChip(
                            selected = state.isDoubleWythe,
                            onClick = { if (!state.isDoubleWythe) viewModel.toggleDoubleWythe() },
                            label = { Text("Double Wythe (2-layer)") }
                        )
                    }

                    // Waste Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Waste & Cut Allowance:", style = MaterialTheme.typography.labelSmall)
                            Text("+${state.wastePercent.toInt()}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Slider(
                            value = state.wastePercent.toFloat(),
                            onValueChange = { viewModel.updateInputs(waste = it.toDouble()) },
                            valueRange = 0f..20f,
                            steps = 3
                        )
                    }
                }
            }

            // Results Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL MATERIAL QUANTITIES",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        IconButton(onClick = {
                            val info = "Masonry Estimate (${state.masonryType.label}):\n" +
                                    "Net Wall Area: ${String.format("%.1f", state.netWallAreaSqFt)} sq.ft (${String.format("%.1f", state.netWallAreaSqM)} m²)\n" +
                                    "Total Units Needed (+${state.wastePercent.toInt()}% waste): ${state.totalUnitsNeeded} units (Raw: ${state.rawUnitsCount})\n" +
                                    "Mortar Volume: ${String.format("%.2f", state.mortarCuFtNeeded)} cu.ft (${String.format("%.2f", state.mortarCuMNeeded)} m³)\n" +
                                    "Pre-Mix Mortar Bags (80 lb): ${state.preMixMortar80lbBags} bags\n" +
                                    "Masonry Sand: ~${String.format("%.2f", state.masonrySandTons)} tons\n" +
                                    "Portland Cement (Site Mix 1:3): ~${state.cementBagsForSiteMix} bags\n" +
                                    (if (state.masonryType.isCmuBlock && state.groutOption.fractionCoreFilled > 0)
                                        "Core Grout (${state.groutOption.label}): ${String.format("%.2f", state.groutVolumeCuYds)} yd³ (${state.groutPreMixBags} bags)\n" else "")
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logCalculation()
                            Toast.makeText(context, "Copied Masonry Quantities!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total Masonry Units", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${state.totalUnitsNeeded} Units",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pre-Mix Mortar Bags", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${state.preMixMortar80lbBags} Bags (80 lb)",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Net Wall Area", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.1f", state.netWallAreaSqFt)} sq.ft (${String.format("%.1f", state.netWallAreaSqM)} m²)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Mortar Volume", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = "${String.format("%.2f", state.mortarCuFtNeeded)} ft³ (${String.format("%.2f", state.mortarCuMNeeded)} m³)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            )
                        }
                    }

                    // Site Mix or Core Grout Details
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Site-Mix Option (1:3 Mortar):", style = MaterialTheme.typography.labelSmall)
                                Text("${state.cementBagsForSiteMix} Cement Bags + ${String.format("%.2f", state.masonrySandTons)} Tons Sand", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }

                            if (state.masonryType.isCmuBlock && state.groutOption.fractionCoreFilled > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Core Grout Filling (${state.groutOption.label}):", style = MaterialTheme.typography.labelSmall)
                                    Text("${String.format("%.2f", state.groutVolumeCuYds)} yd³ (${state.groutPreMixBags} bags)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
