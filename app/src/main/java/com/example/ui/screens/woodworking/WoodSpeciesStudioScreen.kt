package com.example.ui.screens.woodworking

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun WoodSpeciesStudioScreen(
    viewModel: WoodSpeciesStudioViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var activeSubTab by remember { mutableStateOf(0) } // 0: Catalog, 1: Drying & Preservation, 2: Infographics & Hacks

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row to navigate the Studio segments
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Species Database") },
                icon = { Icon(Icons.Default.MenuBook, contentDescription = null) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Drying & Care") },
                icon = { Icon(Icons.Default.Opacity, contentDescription = null) }
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { activeSubTab = 2 },
                text = { Text("Workshop Hacks") },
                icon = { Icon(Icons.Default.Build, contentDescription = null) }
            )
        }

        when (activeSubTab) {
            0 -> SpeciesCatalogSection(viewModel = viewModel, uiState = uiState)
            1 -> DryingPreservationSection(viewModel = viewModel, uiState = uiState)
            2 -> WorkshopHacksSection(viewModel = viewModel, uiState = uiState)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeciesCatalogSection(
    viewModel: WoodSpeciesStudioViewModel,
    uiState: WoodSpeciesStudioUiState
) {
    val filteredSpecies = remember(uiState.searchQuery, uiState.selectedRegion, viewModel.allSpecies) {
        viewModel.allSpecies.filter {
            (uiState.selectedRegion == "ALL" || it.region == uiState.selectedRegion) &&
                    (it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                            it.botanicalName.contains(uiState.searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Region Filters
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                label = { Text("Search Timber Species") },
                placeholder = { Text("e.g. Walnut, White Oak, Teak...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("wood_species_search"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val regions = listOf("ALL", "North American", "European", "Asian & Southeast Asian")
                regions.forEach { region ->
                    FilterChip(
                        selected = uiState.selectedRegion == region,
                        onClick = { viewModel.setSelectedRegion(region) },
                        label = { Text(region) }
                    )
                }
            }
        }

        // Selected Species Detail Display Card
        uiState.selectedSpecies?.let { species ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = species.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = species.botanicalName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Light,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = species.region.take(3).uppercase(Locale.ROOT),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = species.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hardness & Density Matrix Indicators
                        Text(
                            text = "Technical Characteristic Matrix",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Janka Slider Indicator
                        Text(
                            text = "Janka Hardness: ${species.jankaHardness} lbf",
                            style = MaterialTheme.typography.labelMedium
                        )
                        LinearProgressIndicator(
                            progress = { (species.jankaHardness.toFloat() / 4000f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Softwood (300 lbf)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text("Ultra Hard (4000 lbf)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Density Meter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Density / Specific Gravity:", style = MaterialTheme.typography.bodyMedium)
                            Text("${species.density} kg/m³", fontWeight = FontWeight.Bold)
                        }

                        // T/R Ratio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Volumetric T/R Ratio:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "${species.trRatio} (${if (species.trRatio <= 1.5) "Ultra Stable" else if (species.trRatio <= 1.9) "Good Stability" else "High Movement Warning"})",
                                fontWeight = FontWeight.Bold,
                                color = if (species.trRatio > 2.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Workability Ratings (1-5 Star display)
                        Text(
                            text = "Workability Ratings",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                RatingRow("Hand Planing", species.handPlaning)
                                RatingRow("Machine Route", species.machineRouting)
                                RatingRow("Nail/Screw", species.nailScrewHolding)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                RatingRow("Glue Adhesion", species.glueAdhesion)
                                RatingRow("Steam Bending", species.steamBending)
                            }
                        }

                        // Allergy / Toxicity alert
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Text(
                                    text = species.toxicityWarning,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // List Header
        item {
            Text(
                text = "All Available Species Catalog",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // Species List Item Cards
        items(filteredSpecies) { species ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectSpecies(species) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.selectedSpecies?.name == species.name) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = species.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = species.botanicalName,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${species.jankaHardness} lbf",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun RatingRow(label: String, score: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Row {
            for (i in 1..5) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = if (i <= score) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun DryingPreservationSection(
    viewModel: WoodSpeciesStudioViewModel,
    uiState: WoodSpeciesStudioUiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // EMC Calculator card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Equilibrium Moisture Content (EMC)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Target moisture level for wood to prevent joint cracking or cupping when matching indoor environments.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.emcTemp.toString(),
                            onValueChange = {
                                it.toDoubleOrNull()?.let { v -> viewModel.updateEmcInputs(v, uiState.emcHumidity) }
                            },
                            label = { Text("Temp (°F)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = uiState.emcHumidity.toString(),
                            onValueChange = {
                                it.toDoubleOrNull()?.let { v -> viewModel.updateEmcInputs(uiState.emcTemp, v) }
                            },
                            label = { Text("Humidity (%)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "CALCULATED TARGET EMC",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.1f", uiState.calculatedEmc)}%",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Equilibrium Wood Moisture Percentage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Drying Schedules
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Air Drying Schedule & Kiln Guidelines",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Rule of thumb: 1 year per inch of thickness for green logs to air-dry properly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.boardThicknessInches.toString(),
                            onValueChange = {
                                it.toDoubleOrNull()?.let { v -> viewModel.updateDryingInputs(v, uiState.initialMoisture, uiState.targetMoisture) }
                            },
                            label = { Text("Thickness (in)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = uiState.initialMoisture.toString(),
                            onValueChange = {
                                it.toDoubleOrNull()?.let { v -> viewModel.updateDryingInputs(uiState.boardThicknessInches, v, uiState.targetMoisture) }
                            },
                            label = { Text("Initial MC (%)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = uiState.targetMoisture.toString(),
                            onValueChange = {
                                it.toDoubleOrNull()?.let { v -> viewModel.updateDryingInputs(uiState.boardThicknessInches, uiState.initialMoisture, v) }
                            },
                            label = { Text("Target MC (%)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Est. Air Drying Duration:", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${String.format("%.1f", uiState.estAirDryingMonths)} Months",
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Typical Step-by-Step Kiln Schedule Guide:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Pre-heating Stage: 120°F DB, 115°F WB for 24 hours to open pores safely.\n" +
                                "2. Initial Drying Step: 130°F DB, 118°F WB until moisture drops to 25%.\n" +
                                "3. Intermediate Stage: 145°F DB, 120°F WB until moisture drops to 15%.\n" +
                                "4. Equalizing Stage: 160°F DB, 125°F WB to relieve internal stresses and case hardening.",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // End-grain and Care Guides
        item {
            Text(
                text = "Preservation & Protection Guides",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. End-Grain Sealing (Prevent Splitting)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Freshly felled logs lose water from ends 10x faster than sides. Apply Anchorseal, thick latex paint, or paraffin wax within 24 hours of cut to block radial checking.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Borate Termite Protection recipe",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Dissolve 1 cup of Disodium Octaborate Tetrahydrate (or pure Borax) with 1 gallon of hot water. Spray or brush generously on interior framing. Permanently deters powderpost beetles (bubuk kayu) and dry-wood termites.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Shou Sugi Ban (Yakisugi) Guide",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Char: Flame-torch softwood (Pine, Cedar) surfaces until they carbonize (gator skin texture).\n" +
                                "2. Brush: Rub with steel wire brush to remove soft soot.\n" +
                                "3. Wash: Clean with water.\n" +
                                "4. Oil: Finish with Tung Oil. Results in natural weather/bug proofing.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun WorkshopHacksSection(
    viewModel: WoodSpeciesStudioViewModel,
    uiState: WoodSpeciesStudioUiState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Springback Compensator tool
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Bent Lamination Springback Compensator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calculates the adjusted form radius needed because laminated wood springs outward slightly after removing from clamps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = uiState.targetRadius.toString(),
                            onValueChange = {
                                it.toDoubleOrNull()?.let { v -> viewModel.updateSpringbackInputs(v, uiState.plyThickness, uiState.plyCount) }
                            },
                            label = { Text("Target Rad (in)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = uiState.plyCount.toString(),
                            onValueChange = {
                                it.toIntOrNull()?.let { v -> viewModel.updateSpringbackInputs(uiState.targetRadius, uiState.plyThickness, v) }
                            },
                            label = { Text("Ply Count") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "BUILD FORM RADIUS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${String.format("%.2f", uiState.computedSpringbackRadius)}\"",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Build mold with this tighter radius to hit target radius.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Joinery selection Strength card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Joinery Selection & Strength Matrix",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Mortise & Tenon: High tension, High shear, Max ranking. Ideal for frames.\n" +
                                "• Half Lap: Excellent racking/shear strength. Great for cross-stretcher joints.\n" +
                                "• Dowels: Good shear alignment. Medium tension capacity.\n" +
                                "• Domino / Biscuit: Outstanding alignment, low lateral racking protection.\n" +
                                "• Pocket Holes: Quick, moderate tension, poor sheer resistance (needs backing).",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Wood Movement Cheat sheet
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Wood Movement Cheat Sheet",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Flatsawn boards cup away from the heartwood as they dry.\n" +
                                "2. Quartersawn boards shrink only in thickness, not width (very stable).\n" +
                                "3. Secure tabletops using figure-8 clips or Z-clips in slots. Never pocket screw solid wood tops tightly to frames, as seasonal expansion will crack the wood.",
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Hacks and tricks
        item {
            Text(
                text = "Rapid Workshop Hacks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        // 1. Router Sled Flattening
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Flattening Twisted Slabs Without a Planer",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Build a router sled over rails. Use a wide 2\" surfacing/mortising bit. Maintain a 30% bit step-over rate on each pass to prevent gouging or burn marks.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // 2. Fast Grain Filler paste
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "The Instant Grain Filler Trick",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Collect fine sawdust (180 or 220 grit) from the sanding bag. Mix thoroughly with PVA glue or clear sanding sealer to form a thick paste. Fills gaps with an exact color-match.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // 3. Sanding Checklist
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Sanding Progression Checklist",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Course flat sanding: 80 -> 120 grit.\n" +
                                "2. Medium: 150 grit.\n" +
                                "3. Raise the grain: spray lightly with water, let dry, then sand with 220 grit. This prevents water-based finishes from raising whiskers.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
