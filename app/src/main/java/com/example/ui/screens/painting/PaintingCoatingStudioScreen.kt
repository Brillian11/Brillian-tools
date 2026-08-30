package com.example.ui.screens.painting

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaintingCoatingStudioScreen(
    viewModel: PaintingCoatingStudioViewModel,
    modifier: Modifier = Modifier
) {
    val isImperial by viewModel.isImperial.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    // Screen level scroll state
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Painting Studio",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "Painting & Industrial Coating Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "25 advanced sizing, mixing, environmental & preparation tools",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Unit toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isImperial) "Imperial Units (gal, mils, °F)" else "Metric Units (L, microns, °C)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Switch(
                            checked = isImperial,
                            onCheckedChange = { viewModel.toggleUnits() },
                            modifier = Modifier.testTag("unit_toggle")
                        )
                    }
                }
            }
        }

        // 9 Category Tabs (Scrollable)
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            edgePadding = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            val tabs = listOf(
                "Coverage & Sizing",
                "Mixing & Viscosity",
                "Equipment",
                "Climate Curing",
                "Wood Finish",
                "Metal Rust",
                "Wall & Plaster",
                "Laminates & HPL",
                "Color Match & Brands"
            )
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { viewModel.setActiveTab(index) },
                    text = { Text(title, style = MaterialTheme.typography.bodyMedium) }
                )
            }
        }

        // Render Active Tab Columns
        when (activeTab) {
            0 -> TabCoverageAndSizing(viewModel, isImperial)
            1 -> TabMixingAndViscosity(viewModel, isImperial)
            2 -> TabEquipmentAndTips(viewModel, isImperial)
            3 -> TabEnvironmentalCuring(viewModel, isImperial)
            4 -> TabWoodFinishing(viewModel, isImperial)
            5 -> TabMetalAndRust(viewModel, isImperial)
            6 -> TabWallAndMasonry(viewModel, isImperial)
            7 -> TabLaminatesAndFilms(viewModel, isImperial)
            8 -> TabColorRecommendations(viewModel)
        }
    }
}

// ==================== TAB 1: COVERAGE AND SIZING ====================
@Composable
fun TabCoverageAndSizing(viewModel: PaintingCoatingStudioViewModel, isImperial: Boolean) {
    val areaStr by viewModel.areaInput.collectAsState()
    val substrate by viewModel.substrateType.collectAsState()
    val primerReq by viewModel.primerRequired.collectAsState()
    val coats by viewModel.numCoats.collectAsState()

    val dftStr by viewModel.targetDft.collectAsState()
    val solidsStr by viewModel.volumeSolids.collectAsState()
    val thinnerStr by viewModel.thinnerPercent.collectAsState()

    val trimLenStr by viewModel.trimLength.collectAsState()
    val trimWidthStr by viewModel.trimWidth.collectAsState()
    val sidingLenStr by viewModel.sidingLength.collectAsState()
    val sidingHeightStr by viewModel.sidingHeight.collectAsState()
    val lapOverlapStr by viewModel.lapOverlap.collectAsState()

    val steelProf by viewModel.steelProfile.collectAsState()
    val steelLenStr by viewModel.steelLength.collectAsState()
    val steelDimStr by viewModel.steelDiameterOrWidth.collectAsState()

    val area = areaStr.toDoubleOrNull() ?: 0.0
    val dft = dftStr.toDoubleOrNull() ?: 0.0
    val solids = solidsStr.toDoubleOrNull() ?: 0.0
    val thinner = thinnerStr.toDoubleOrNull() ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Feature 1: Substrate-Specific Paint Volume Calculator
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("1. Substrate-Specific Paint Volume", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = areaStr,
                    onValueChange = { viewModel.areaInput.value = it },
                    label = { Text(if (isImperial) "Surface Area (sq ft)" else "Surface Area (sq m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Select Substrate Porosity Profile:", style = MaterialTheme.typography.bodySmall)
                val substrates = listOf("Smooth Timber", "Porous Brick", "Non-Porous Iron", "Drywall")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    substrates.forEach { s ->
                        FilterChip(
                            selected = substrate == s,
                            onClick = { viewModel.substrateType.value = s },
                            label = { Text(s) }
                        )
                    }
                }

                val factor = when (substrate) {
                    "Smooth Timber" -> if (isImperial) 350.0 else 32.5
                    "Porous Brick" -> if (isImperial) 150.0 else 14.0
                    "Non-Porous Iron" -> if (isImperial) 450.0 else 42.0
                    else -> if (isImperial) 400.0 else 37.0
                }

                val volumeNeeded = if (factor > 0) area / factor else 0.0
                val unitLabel = if (isImperial) "Gallons" else "Liters"

                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)).padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Coverage Spread Rate:")
                    Text(String.format("%.1f %s", factor, if (isImperial) "sq ft/gal" else "sq m/L"), fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Volume Needed:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(String.format("%.2f %s", volumeNeeded, unitLabel), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Button(
                    onClick = { viewModel.logCalculation("Substrate paint sizer", "Needed %.2f %s for %s substrate".format(volumeNeeded, unitLabel, substrate)) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Log to Field Notes")
                }
            }
        }

        // Feature 2: Multi-Coat Build & Primer Sizer
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("2. Multi-Coat Build & Primer Sizer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = primerReq, onCheckedChange = { viewModel.primerRequired.value = it })
                    Text("Include Primer / Sealer Base Coat")
                }

                Text("Number of Finish / Top Coats: $coats")
                Slider(
                    value = coats.toFloat(),
                    onValueChange = { viewModel.numCoats.value = it.roundToInt() },
                    valueRange = 1f..4f,
                    steps = 2
                )

                val primSpread = if (isImperial) 300.0 else 28.0
                val topSpread = if (isImperial) 400.0 else 37.0
                val unit = if (isImperial) "Gal" else "Liters"

                val primerVolume = if (primerReq) area / primSpread else 0.0
                val topcoatVolume = (area / topSpread) * coats

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Primer Volume:")
                    Text("%.2f %s".format(primerVolume, unit), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Finish Topcoats:")
                    Text("%.2f %s (%d coats)".format(topcoatVolume, unit, coats), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cumulative Combined Volume:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("%.2f %s".format(primerVolume + topcoatVolume, unit), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Feature 3: DFT to WFT Converter
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("3. Dry Film Thickness (DFT) to Wet Film Thickness (WFT)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = dftStr,
                    onValueChange = { viewModel.targetDft.value = it },
                    label = { Text(if (isImperial) "Target DFT (mils)" else "Target DFT (microns)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = solidsStr,
                    onValueChange = { viewModel.volumeSolids.value = it },
                    label = { Text("Volume Solids % (e.g. 50)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = thinnerStr,
                    onValueChange = { viewModel.thinnerPercent.value = it },
                    label = { Text("Thinner/Reducer Addition % (optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                val solidsDecimal = if (solids > 0) solids / 100.0 else 1.0
                val wftBase = if (solidsDecimal > 0) dft / solidsDecimal else 0.0
                val wftThinned = wftBase * (1.0 + (thinner / 100.0))
                val lengthUnit = if (isImperial) "mils" else "microns"

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Required Base Wet Paint (WFT):")
                    Text("%.2f %s".format(wftBase, lengthUnit), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Target Wet Thickness with Reducer:")
                    Text("%.2f %s".format(wftThinned, lengthUnit), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Feature 4: Architectural Trim, Siding & Board Sizer
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("4. Trim, Siding & Board Meter Sizer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = trimLenStr,
                        onValueChange = { viewModel.trimLength.value = it },
                        label = { Text(if (isImperial) "Trim Length (ft)" else "Trim Length (m)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = trimWidthStr,
                        onValueChange = { viewModel.trimWidth.value = it },
                        label = { Text(if (isImperial) "Width (inches)" else "Width (cm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sidingLenStr,
                        onValueChange = { viewModel.sidingLength.value = it },
                        label = { Text("Siding Length") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = sidingHeightStr,
                        onValueChange = { viewModel.sidingHeight.value = it },
                        label = { Text("Siding Height") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                val trimLen = trimLenStr.toDoubleOrNull() ?: 0.0
                val trimWidth = trimWidthStr.toDoubleOrNull() ?: 0.0
                val sidLen = sidingLenStr.toDoubleOrNull() ?: 0.0
                val sidHt = sidingHeightStr.toDoubleOrNull() ?: 0.0

                val trimArea = if (isImperial) (trimLen * (trimWidth / 12.0)) else (trimLen * (trimWidth / 100.0))
                val sidingArea = sidLen * sidHt
                val finalUnit = if (isImperial) "sq ft" else "sq m"

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Trim Surface Area:")
                    Text("%.2f %s".format(trimArea, finalUnit), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Siding Surface Area:")
                    Text("%.2f %s".format(sidingArea, finalUnit), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Feature 5: Irregular Metal & Structural Steel Sizer
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("5. Irregular Metal & Structural Steel Area", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("I-Beam", "H-Section", "Round Pipe", "Corrugated").forEach { profile ->
                        FilterChip(
                            selected = steelProf == profile,
                            onClick = { viewModel.steelProfile.value = profile },
                            label = { Text(profile) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = steelLenStr,
                        onValueChange = { viewModel.steelLength.value = it },
                        label = { Text(if (isImperial) "Section Length (ft)" else "Section Length (m)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = steelDimStr,
                        onValueChange = { viewModel.steelDiameterOrWidth.value = it },
                        label = { Text(if (steelProf == "Round Pipe") "Diameter" else "Flange Width") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                val stLen = steelLenStr.toDoubleOrNull() ?: 0.0
                val stDim = steelDimStr.toDoubleOrNull() ?: 0.0

                val steelArea = when (steelProf) {
                    "Round Pipe" -> {
                        val d = if (isImperial) stDim / 12.0 else stDim / 100.0
                        PI * d * stLen
                    }
                    "I-Beam" -> {
                        val w = if (isImperial) stDim / 12.0 else stDim / 100.0
                        // Surface area estimation (roughly 2.5 times width per length)
                        w * 2.5 * stLen
                    }
                    "H-Section" -> {
                        val w = if (isImperial) stDim / 12.0 else stDim / 100.0
                        w * 3.2 * stLen
                    }
                    else -> {
                        stLen * (if (isImperial) stDim else stDim / 100.0) * 1.2
                    }
                }
                val areaUnit = if (isImperial) "sq ft" else "sq m"

                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Steel Area:")
                    Text("%.2f %s".format(steelArea, areaUnit), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ==================== TAB 2: MIXING AND VISCOSITY ====================
@Composable
fun TabMixingAndViscosity(viewModel: PaintingCoatingStudioViewModel, isImperial: Boolean) {
    val baseRatioStr by viewModel.mixBaseRatio.collectAsState()
    val actRatioStr by viewModel.mixActivatorRatio.collectAsState()
    val thinPctStr by viewModel.mixThinnerPercent.collectAsState()
    val volStr by viewModel.mixTargetVolume.collectAsState()

    val cupType by viewModel.viscosityCupType.collectAsState()
    val secsStr by viewModel.viscositySeconds.collectAsState()

    val tempStr by viewModel.ambientTemp.collectAsState()
    val method by viewModel.applicationMethod.collectAsState()

    val baseR = baseRatioStr.toDoubleOrNull() ?: 4.0
    val actR = actRatioStr.toDoubleOrNull() ?: 1.0
    val thinP = thinPctStr.toDoubleOrNull() ?: 0.0
    val targetVol = volStr.toDoubleOrNull() ?: 1000.0

    val secs = secsStr.toDoubleOrNull() ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Feature 6: 2K Paint & Epoxy Mixing Ratio Engine
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("6. 2-Part (2K) Paint & Epoxy Mixing Ratio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = baseRatioStr,
                        onValueChange = { viewModel.mixBaseRatio.value = it },
                        label = { Text("Base Ratio") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = actRatioStr,
                        onValueChange = { viewModel.mixActivatorRatio.value = it },
                        label = { Text("Activator") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = thinPctStr,
                        onValueChange = { viewModel.mixThinnerPercent.value = it },
                        label = { Text("Thinner %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = volStr,
                    onValueChange = { viewModel.mixTargetVolume.value = it },
                    label = { Text(if (isImperial) "Target Fluid Volume (fl oz)" else "Target Fluid Volume (mL)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // Calculations
                val totalParts = baseR + actR
                val thinVolume = targetVol * (thinP / 100.0)
                val remainingVol = targetVol - thinVolume
                val partAVol = remainingVol * (baseR / totalParts)
                val partBVol = remainingVol * (actR / totalParts)

                val volUnit = if (isImperial) "fl oz" else "mL"

                Column(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("Mix Recipe Breakdown:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Part A (Base Coat):", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("%.1f %s".format(partAVol, volUnit), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Part B (Hardener/Activator):", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("%.1f %s".format(partBVol, volUnit), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Part C (Thinner/Reducer):", color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("%.1f %s".format(thinVolume, volUnit), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                Button(
                    onClick = { viewModel.logCalculation("2K Epoxy Mix", "Part A: %.1f%s, Part B: %.1f%s".format(partAVol, volUnit, partBVol, volUnit)) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Log Mix Recipe")
                }
            }
        }

        // Feature 7: Viscosity Cup Flow-Time Converter
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("7. Viscosity Cup Flow-Time Converter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Ford #4", "Zahn #2", "DIN 4").forEach { cup ->
                        FilterChip(
                            selected = cupType == cup,
                            onClick = { viewModel.viscosityCupType.value = cup },
                            label = { Text(cup) }
                        )
                    }
                }

                OutlinedTextField(
                    value = secsStr,
                    onValueChange = { viewModel.viscositySeconds.value = it },
                    label = { Text("Drain Time (seconds)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                val cSt = when (cupType) {
                    "Ford #4" -> (3.85 * secs) - 15.9
                    "Zahn #2" -> (2.9 * secs) - 5.0
                    else -> (4.57 * secs) - 32.5
                }
                val computedCSt = max(0.0, cSt)

                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Computed Kinematic Viscosity:")
                    Text("%.1f cSt".format(computedCSt), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Feature 8: Thinner & Reducer Percentage Optimizer
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("8. Thinner & Reducer % Optimizer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = tempStr,
                        onValueChange = { viewModel.ambientTemp.value = it },
                        label = { Text(if (isImperial) "Ambient Temp (°F)" else "Ambient Temp (°C)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    var expanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(method)
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            listOf("Brush/Roller", "Conventional Spray", "HVLP Spray", "Airless Spray").forEach { m ->
                                DropdownMenuItem(text = { Text(m) }, onClick = {
                                    viewModel.applicationMethod.value = m
                                    expanded = false
                                })
                            }
                        }
                    }
                }

                val temp = tempStr.toDoubleOrNull() ?: 77.0
                val tempF = if (isImperial) temp else (temp * 1.8) + 32.0

                val suggestedReduction = when {
                    tempF > 90 -> if (method.contains("Spray")) "15% - 20% (Use slow reducer to prevent dry spray)" else "10%"
                    tempF < 60 -> if (method.contains("Spray")) "5% - 10% (Use fast reducer)" else "2%"
                    else -> if (method.contains("Spray")) "10%" else "5%"
                }

                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Optimized Solvent Reduction:")
                    Text(suggestedReduction, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Feature 9: Pot Life & Induction Timer
        var countdownTimer by remember { mutableStateOf(0) }
        var isTimerRunning by remember { mutableStateOf(false) }

        LaunchedEffect(isTimerRunning, countdownTimer) {
            if (isTimerRunning && countdownTimer > 0) {
                delay(1000L)
                countdownTimer -= 1
                if (countdownTimer == 0) {
                    isTimerRunning = false
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("9. Pot Life & Induction countdown timer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = {
                        countdownTimer = 15 * 60 // 15 mins for sweat-in
                        isTimerRunning = true
                    }) {
                        Text("Start 15m Induction")
                    }

                    Button(onClick = {
                        countdownTimer = 120 * 60 // 2 hours for Pot Life
                        isTimerRunning = true
                    }) {
                        Text("Start 2hr Pot Life")
                    }
                }

                if (countdownTimer > 0) {
                    val minutes = countdownTimer / 60
                    val seconds = countdownTimer % 60
                    Text(
                        text = "Time Remaining: %02d:%02d".format(minutes, seconds),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (countdownTimer < 300) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    )
                }
            }
        }
    }
}

// ==================== TAB 3: SPRAY EQUIPMENT AND TIPS ====================
@Composable
fun TabEquipmentAndTips(viewModel: PaintingCoatingStudioViewModel, isImperial: Boolean) {
    val coatingType by viewModel.coatingTypeForTip.collectAsState()
    val complexity by viewModel.structureComplexity.collectAsState()
    val nozzleSize by viewModel.nozzleSizeMm.collectAsState()
    val paintViscosity by viewModel.paintViscosityClass.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Feature 10: Airless Spray Tip Selector
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("10. Airless Spray Tip Selector", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Coating: $coatingType")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Stain/Varnish", "Lacquer/Enamel", "Latex Paint", "Heavy Elastomeric").forEach { c ->
                            DropdownMenuItem(text = { Text(c) }, onClick = {
                                viewModel.coatingTypeForTip.value = c
                                expanded = false
                            })
                        }
                    }
                }

                val recommendedTip = when (coatingType) {
                    "Stain/Varnish" -> "0.011\" to 0.013\" (e.g., 211, 313)"
                    "Lacquer/Enamel" -> "0.013\" to 0.015\" (e.g., 413, 515)"
                    "Latex Paint" -> "0.015\" to 0.019\" (e.g., 517, 519)"
                    else -> "0.021\" or larger (e.g., 521, 625)"
                }

                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Suggested Orifice:")
                    Text(recommendedTip, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Feature 11: Spray Transfer Efficiency & Overspray Sizer
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("11. Transfer Efficiency & Overspray Loss Estimator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Text("Structure Complexity Profile:")
                val complexProfiles = listOf("Flat Wall", "Open Steel Frame", "Lattice / Railing")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    complexProfiles.forEach { cp ->
                        FilterChip(
                            selected = complexity == cp,
                            onClick = { viewModel.structureComplexity.value = cp },
                            label = { Text(cp) }
                        )
                    }
                }

                val efficiency = when (complexity) {
                    "Flat Wall" -> 65
                    "Open Steel Frame" -> 40
                    else -> 20
                }

                val lossFactorMultiplier = 100.0 / efficiency

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Estimated Transfer Efficiency:")
                    Text("$efficiency %", fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Material Purchase Multiplier:")
                    Text("%.2fx".format(lossFactorMultiplier), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Feature 12: Compressor CFM & Fluid Nozzle Sizer
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("12. Compressor CFM & Fluid Nozzle Sizer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = nozzleSize,
                    onValueChange = { viewModel.nozzleSizeMm.value = it },
                    label = { Text("Nozzle Size (mm, e.g. 1.4)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text("Paint Viscosity Profile:")
                val viscosities = listOf("Low (Stain)", "Medium (Latex)", "High (Gelcoat)")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    viscosities.forEach { v ->
                        FilterChip(
                            selected = paintViscosity == v,
                            onClick = { viewModel.paintViscosityClass.value = v },
                            label = { Text(v) }
                        )
                    }
                }

                val nz = nozzleSize.toDoubleOrNull() ?: 1.4
                val isCompatible = when (paintViscosity) {
                    "Low (Stain)" -> nz in 1.0..1.4
                    "Medium (Latex)" -> nz in 1.5..2.0
                    else -> nz >= 2.1
                }

                val minCfm = if (nz >= 1.8) "9.0 to 12.0 CFM @ 40 PSI" else "6.0 to 8.5 CFM @ 40 PSI"

                Column(
                    modifier = Modifier.fillMaxWidth().background(
                        if (isCompatible) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nozzle Compatibility:")
                        Text(if (isCompatible) "Optimal Match" else "Warning: Mismatch", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Required Compressor CFM:")
                        Text(minCfm, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== TAB 4: ENVIRONMENTAL CURING ====================
@Composable
fun TabEnvironmentalCuring(viewModel: PaintingCoatingStudioViewModel, isImperial: Boolean) {
    val tempStr by viewModel.ambientTemp.collectAsState()
    val humStr by viewModel.envRelativeHumidity.collectAsState()
    val surfTempStr by viewModel.envSurfaceTemp.collectAsState()
    val chemistry by viewModel.envCoatingChemistry.collectAsState()
    val airflow by viewModel.envAirflow.collectAsState()

    val temp = tempStr.toDoubleOrNull() ?: 77.0
    val rh = humStr.toDoubleOrNull() ?: 65.0
    val surfTemp = surfTempStr.toDoubleOrNull() ?: 72.0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Feature 13: Dew Point & Condensation Engine
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("13. Dew Point & Condensation Warning", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = humStr,
                        onValueChange = { viewModel.envRelativeHumidity.value = it },
                        label = { Text("Relative Humidity %") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = surfTempStr,
                        onValueChange = { viewModel.envSurfaceTemp.value = it },
                        label = { Text(if (isImperial) "Steel Surface Temp (°F)" else "Steel Surface Temp (°C)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                // Dew point calculation (mils / Magnus-Tetens formula)
                val tempC = if (isImperial) (temp - 32.0) / 1.8 else temp
                val surfC = if (isImperial) (surfTemp - 32.0) / 1.8 else surfTemp

                // Dew point approx
                val a = 17.27
                val b = 237.7
                val alpha = ((a * tempC) / (b + tempC)) + ln(rh / 100.0)
                val dewPointC = (b * alpha) / (a - alpha)
                val dewPointDisplay = if (isImperial) (dewPointC * 1.8) + 32.0 else dewPointC

                // Danger check if substrate is within 3C (5F) of dew point
                val delta = surfC - dewPointC
                val isSafe = delta > 3.0

                Column(
                    modifier = Modifier.fillMaxWidth().background(
                        if (isSafe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ).padding(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Calculated Dew Point:")
                        Text("%.1f %s".format(dewPointDisplay, if (isImperial) "°F" else "°C"), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Substrate Delta to Dew Point:")
                        Text("%.1f %s".format(if (isImperial) delta * 1.8 else delta, if (isImperial) "°F" else "°C"), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Condensation Safety:")
                        Text(if (isSafe) "SAFE TO APPLY" else "WARNING: MOISTURE RISK", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Feature 14: Recoat & Overcoat Window Matrix
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("14. Recoat & Overcoat Window Matrix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                var expanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Chemistry: $chemistry")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Alkyd Enamel", "2K Epoxy", "Waterborne Acrylic", "Polyurethane").forEach { chem ->
                            DropdownMenuItem(text = { Text(chem) }, onClick = {
                                viewModel.envCoatingChemistry.value = chem
                                expanded = false
                            })
                        }
                    }
                }

                // Window mappings based on temperature (hotter = faster)
                val tempC = if (isImperial) (temp - 32.0) / 1.8 else temp
                val speedMultiplier = if (tempC > 30.0) 0.6 else if (tempC < 15.0) 2.0 else 1.0

                val touch = when (chemistry) {
                    "Alkyd Enamel" -> 4.0 * speedMultiplier
                    "2K Epoxy" -> 2.0 * speedMultiplier
                    "Waterborne Acrylic" -> 1.0 * speedMultiplier
                    else -> 1.5 * speedMultiplier
                }

                val minRecoat = touch * 3.0
                val maxRecoat = when (chemistry) {
                    "2K Epoxy" -> 72.0 // After 72 hours requires sanding
                    else -> 120.0
                }

                Column(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dry to Touch:")
                        Text("%.1f hours".format(touch), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Min Recoat Window:")
                        Text("%.1f hours".format(minRecoat), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Max Overcoat Window:")
                        Text("%.0f hours (scuff-sand after)".format(maxRecoat), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Feature 15: Blushing & Solvent Pop Risk Indicator
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("15. Blushing & Solvent Pop Risk Indicator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Text("Select Drying Airflow Level:")
                val airflows = listOf("Stagnant", "Medium / Normal", "Strong / Outdoor")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    airflows.forEach { af ->
                        FilterChip(
                            selected = airflow == af,
                            onClick = { viewModel.envAirflow.value = af },
                            label = { Text(af) }
                        )
                    }
                }

                val blushRisk = if (rh > 80.0) "HIGH RISK (Hazing/Moisture trapping likely)" else "Low Risk"
                val tempC = if (isImperial) (temp - 32.0) / 1.8 else temp
                val popRisk = if (tempC > 32.0 && airflow == "Stagnant") "HIGH RISK (Rapid surface curing + skinning)" else "Low Risk"

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Clear Coat Blushing Risk:")
                    Text(blushRisk, fontWeight = FontWeight.Bold, color = if (blushRisk.contains("HIGH")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Solvent Pop Risk:")
                    Text(popRisk, fontWeight = FontWeight.Bold, color = if (popRisk.contains("HIGH")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ==================== TAB 5: WOOD FINISHING ====================
@Composable
fun TabWoodFinishing(viewModel: PaintingCoatingStudioViewModel, isImperial: Boolean) {
    val surfaceType by viewModel.woodSurfaceType.collectAsState()
    val species by viewModel.woodSpecies.collectAsState()
    val conditioner by viewModel.woodConditionerApplied.collectAsState()
    val grainType by viewModel.woodGrainType.collectAsState()
    val areaStr by viewModel.areaInput.collectAsState()

    val area = areaStr.toDoubleOrNull() ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Feature 16: Grit Progression Guide
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("16. Sanding Grit Progression Roadmap", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                val surfaceProfiles = listOf("Softwood", "Hardwood", "MDF/Veneer", "Inter-coat scuff")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    surfaceProfiles.forEach { sp ->
                        FilterChip(
                            selected = surfaceType == sp,
                            onClick = { viewModel.woodSurfaceType.value = sp },
                            label = { Text(sp) }
                        )
                    }
                }

                val roadmap = when (surfaceType) {
                    "Softwood" -> "80 -> 120 -> 150 -> 180 Grit (Avoid over-sanding to preserve absorption)"
                    "Hardwood" -> "80 -> 120 -> 180 -> 220 Grit (Sanding with the grain is critical)"
                    "MDF/Veneer" -> "150 -> 180 -> 220 Grit (Light pressure, thin veneer risk)"
                    else -> "320 to 400 Grit (Very light scuff to remove dust nibs)"
                }

                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Recommended Progression:")
                    Text(roadmap, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End, modifier = Modifier.fillMaxWidth(0.6f))
                }
            }
        }

        // Feature 17: Wood Stain & Conditioner Estimator
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("17. Wood Stain Intensity & Blotching Risk", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Text("Select Timber Wood Species:")
                val speciesList = listOf("Pine", "Birch", "Cherry", "Red Oak", "Maple")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    speciesList.forEach { sp ->
                        FilterChip(
                            selected = species == sp,
                            onClick = { viewModel.woodSpecies.value = sp },
                            label = { Text(sp) }
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = conditioner, onCheckedChange = { viewModel.woodConditionerApplied.value = it })
                    Text("Pre-stain Conditioner / Washcoat Applied")
                }

                val blotchRisk = when (species) {
                    "Pine", "Birch", "Cherry" -> if (conditioner) "Low (Controlled)" else "EXTREMELY HIGH BLOTCH RISK"
                    else -> "Low (Uniform Open Grain)"
                }

                Row(
                    modifier = Modifier.fillMaxWidth().background(
                        if (blotchRisk.contains("EXTREMELY")) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Conditioner Recommendation:")
                    Text(if (blotchRisk.contains("EXTREMELY")) "Pre-condition Required!" else "Washcoat Optional", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Feature 18: Wood Grain Filler & Sealer
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("18. Wood Grain Filler & Sealer Sizer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Open Pore", "Closed Pore").forEach { gp ->
                        FilterChip(
                            selected = grainType == gp,
                            onClick = { viewModel.woodGrainType.value = gp },
                            label = { Text(gp) }
                        )
                    }
                }

                // Filler calculation (Pounds or kg per area)
                val fillerRate = if (grainType == "Open Pore") 0.15 else 0.05 // lbs per sq ft
                val totalFiller = area * fillerRate
                val fillerUnit = if (isImperial) "lbs" else "kg"

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Sanding Sealer Needed:")
                    Text("%.1f fl oz".format(area * 0.25), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Estimated Paste Wood Filler:")
                    Text("%.2f %s".format(totalFiller, fillerUnit), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== TAB 6: METAL AND RUST ====================
@Composable
fun TabMetalAndRust(viewModel: PaintingCoatingStudioViewModel, isImperial: Boolean) {
    val rustGrade by viewModel.steelRustGrade.collectAsState()
    val standard by viewModel.blastStandard.collectAsState()
    val rustSev by viewModel.rustSeverity.collectAsState()
    val areaStr by viewModel.areaInput.collectAsState()

    val metal by viewModel.primerSubstrateMetal.collectAsState()
    val paintType by viewModel.primerPaintType.collectAsState()

    val area = areaStr.toDoubleOrNull() ?: 0.0

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Feature 19: ISO/SSPC Prep Reference
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("19. ISO/SSPC Surface Prep Guide", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Text("Rust Condition Grade:")
                var rExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { rExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(rustGrade)
                    }
                    DropdownMenu(expanded = rExpanded, onDismissRequest = { rExpanded = false }) {
                        listOf("A - Light Rust", "B - Rusted", "C - Heavy Pit", "D - Scale/Spall").forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = {
                                viewModel.steelRustGrade.value = r
                                rExpanded = false
                            })
                        }
                    }
                }

                Text("Required Blast Standard:")
                var bExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { bExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(standard)
                    }
                    DropdownMenu(expanded = bExpanded, onDismissRequest = { bExpanded = false }) {
                        listOf("SSPC-SP 2 Hand", "SSPC-SP 3 Power", "SSPC-SP 6 Commercial", "SSPC-SP 10 Near-White").forEach { b ->
                            DropdownMenuItem(text = { Text(b) }, onClick = {
                                viewModel.blastStandard.value = b
                                bExpanded = false
                            })
                        }
                    }
                }

                val anchorProfile = when (standard) {
                    "SSPC-SP 2 Hand" -> "No profile (scuff only)"
                    "SSPC-SP 3 Power" -> "1.0 mils profile"
                    "SSPC-SP 6 Commercial" -> "1.5 to 2.5 mils profile"
                    else -> "2.0 to 3.5 mils profile (Optimal adhesion)"
                }

                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Target Blast Adhesion Profile:")
                    Text(anchorProfile, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Feature 20: Rust Converter Sizer
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("20. Rust Converter & Acid Etch Sizer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Text("Rust Severity:")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Light", "Moderate", "Heavy Scale").forEach { r ->
                        FilterChip(
                            selected = rustSev == r,
                            onClick = { viewModel.rustSeverity.value = r },
                            label = { Text(r) }
                        )
                    }
                }

                val convertRate = when (rustSev) {
                    "Light" -> 0.1
                    "Moderate" -> 0.2
                    else -> 0.4
                } // gal per 100 sq ft

                val fluidNeeded = (area / 100.0) * convertRate
                val fluidUnit = if (isImperial) "Gallons" else "Liters"

                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rust Converter Fluid Required:")
                    Text("%.2f %s".format(if (isImperial) fluidNeeded else fluidNeeded * 3.785, fluidUnit), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Feature 21: Galvanic & Primer Compatibility
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("21. Galvanic Corrosion & Primer Compatibility Matrix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var mExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { mExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(metal)
                        }
                        DropdownMenu(expanded = mExpanded, onDismissRequest = { mExpanded = false }) {
                            listOf("Galvanized Zinc", "Carbon Steel", "Aluminum", "Stainless").forEach { m ->
                                DropdownMenuItem(text = { Text(m) }, onClick = {
                                    viewModel.primerSubstrateMetal.value = m
                                    mExpanded = false
                                })
                            }
                        }
                    }

                    var pExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(onClick = { pExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(paintType)
                        }
                        DropdownMenu(expanded = pExpanded, onDismissRequest = { pExpanded = false }) {
                            listOf("Alkyd / Oil-based", "Epoxy Primer", "Zinc-Rich Epoxy", "Acrylic").forEach { p ->
                                DropdownMenuItem(text = { Text(p) }, onClick = {
                                    viewModel.primerPaintType.value = p
                                    pExpanded = false
                                })
                            }
                        }
                    }
                }

                val safetyVerdict = when {
                    metal == "Galvanized Zinc" && paintType == "Alkyd / Oil-based" -> "DANGER: Saponification risk (paint will peel off soon)"
                    metal == "Carbon Steel" && paintType == "Zinc-Rich Epoxy" -> "OPTIMAL: Dynamic cathodic / galvanic protection"
                    metal == "Aluminum" && paintType == "Acrylic" -> "Compatible: Excellent waterborne adhesion"
                    else -> "Compatible: General preparation standards apply"
                }

                Row(
                    modifier = Modifier.fillMaxWidth().background(
                        if (safetyVerdict.contains("DANGER")) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ).padding(10.dp)
                ) {
                    Text(safetyVerdict, fontWeight = FontWeight.Bold, color = if (safetyVerdict.contains("DANGER")) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

// ==================== TAB 7: WALL AND MASONRY ====================
@Composable
fun TabWallAndMasonry(viewModel: PaintingCoatingStudioViewModel, isImperial: Boolean) {
    val thickness by viewModel.puttyThicknessMm.collectAsState()
    val areaStr by viewModel.areaInput.collectAsState()
    val moistureStr by viewModel.moistureReadingPercent.collectAsState()
    val phStr by viewModel.concretePhLevel.collectAsState()
    val ageStr by viewModel.concreteAgeWeeks.collectAsState()
    val widthStr by viewModel.jointWidthMm.collectAsState()
    val depthStr by viewModel.jointDepthMm.collectAsState()
    val lengthStr by viewModel.jointLengthFt.collectAsState()
    val cartSize by viewModel.cartridgeSizeMl.collectAsState()

    val area = areaStr.toDoubleOrNull() ?: 0.0
    val moisture = moistureStr.toDoubleOrNull() ?: 10.0
    val ph = phStr.toDoubleOrNull() ?: 11.0
    val age = ageStr.toIntOrNull() ?: 2

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Feature 22: Putty & Plaster Estimator
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("22. Wall Skim Coat & Putty Estimator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Text("Plaster Thickness (mm): $thickness")
                Slider(
                    value = thickness.toFloat(),
                    onValueChange = { viewModel.puttyThicknessMm.value = "%.1f".format(it) },
                    valueRange = 1.0f..3.0f,
                    steps = 3
                )

                val tMm = thickness.toDoubleOrNull() ?: 2.0
                // Estimating 1.2 kg dry putty per sq meter per mm thickness
                val areaSqM = if (isImperial) area * 0.0929 else area
                val totalPuttyKg = areaSqM * 1.2 * tMm
                val bagsNeeded = ceil(totalPuttyKg / 20.0).toInt() // standard 20kg dry putty bag

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Putty Weight Required:")
                    Text("%.1f kg".format(totalPuttyKg), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Required Bags (20kg standard):")
                    Text("$bagsNeeded Bags", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Feature 23: Moisture & Efflorescence Checker
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("23. Masonry Efflorescence & Moisture", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                OutlinedTextField(
                    value = moistureStr,
                    onValueChange = { viewModel.moistureReadingPercent.value = it },
                    label = { Text("Masonry Moisture Meter Reading %") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                val isSafe = moisture < 12.0

                Row(
                    modifier = Modifier.fillMaxWidth().background(
                        if (isSafe) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ).padding(10.dp)
                ) {
                    Text(
                        if (isSafe) "SAFE: Moisture within limits" else "DANGER: Extreme peeling/blistering/efflorescence risk",
                        fontWeight = FontWeight.Bold,
                        color = if (isSafe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Feature 24: Alkali Burn Risk & pH Sizer
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("24. Alkali Burn Risk & pH Neutralization", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phStr,
                        onValueChange = { viewModel.concretePhLevel.value = it },
                        label = { Text("Plaster pH level (e.g. 11)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = ageStr,
                        onValueChange = { viewModel.concreteAgeWeeks.value = it },
                        label = { Text("Concrete Age (weeks)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                val risk = when {
                    ph > 9.0 && age < 4 -> "CRITICAL RISK: Direct alkyd application will saponify. Use acrylic alkali-resistant primer!"
                    ph > 9.0 -> "Moderate Risk: High plaster alkalinity"
                    else -> "No Risk"
                }

                Row(
                    modifier = Modifier.fillMaxWidth().background(
                        if (risk.contains("CRITICAL")) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ).padding(10.dp)
                ) {
                    Text(risk, fontWeight = FontWeight.Bold, color = if (risk.contains("CRITICAL")) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Feature 25: Caulk & Sealant Yield Calculator
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("25. Caulk & Sealant Yield Calculator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = widthStr,
                        onValueChange = { viewModel.jointWidthMm.value = it },
                        label = { Text("Joint Width (mm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = depthStr,
                        onValueChange = { viewModel.jointDepthMm.value = it },
                        label = { Text("Joint Depth (mm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                OutlinedTextField(
                    value = lengthStr,
                    onValueChange = { viewModel.jointLengthFt.value = it },
                    label = { Text(if (isImperial) "Joint Length (ft)" else "Joint Length (m)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Text("Cartridge Size:")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("300", "600").forEach { cs ->
                        FilterChip(
                            selected = cartSize == cs,
                            onClick = { viewModel.cartridgeSizeMl.value = cs },
                            label = { Text("$cs mL") }
                        )
                    }
                }

                val w = widthStr.toDoubleOrNull() ?: 10.0
                val d = depthStr.toDoubleOrNull() ?: 8.0
                val l = lengthStr.toDoubleOrNull() ?: 50.0

                val lMeters = if (isImperial) l * 0.3048 else l
                val totalVolumeMl = (w * d * lMeters * 1000.0) / 1000.0 // volume in cm3/mL approx
                val cSizeVal = cartSize.toDoubleOrNull() ?: 300.0
                val cartridgesNeeded = ceil(totalVolumeMl / cSizeVal).toInt()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Volume Required:")
                    Text("%.1f mL".format(totalVolumeMl), fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Cartridges / Sausages Needed:")
                    Text("$cartridgesNeeded Tubes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== TAB 8: LAMINATES, HPL & ARCHITECTURAL INTERIOR FILMS ====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabLaminatesAndFilms(viewModel: PaintingCoatingStudioViewModel, isImperial: Boolean) {
    val materialCat by viewModel.laminateMaterialCategory.collectAsState()
    val pattern by viewModel.laminateTexturePattern.collectAsState()
    val lengthStr by viewModel.laminateSurfaceLength.collectAsState()
    val widthStr by viewModel.laminateSurfaceWidth.collectAsState()
    val qtyStr by viewModel.laminateQuantity.collectAsState()
    val wasteStr by viewModel.laminateWastePercent.collectAsState()
    val edgeLenStr by viewModel.edgeBandingLength.collectAsState()
    val edgeWidStr by viewModel.edgeBandingWidth.collectAsState()
    val edgeThickStr by viewModel.edgeBandingThickness.collectAsState()
    val adhesiveType by viewModel.adhesiveGlueType.collectAsState()

    val categories = listOf(
        "HPL (High Pressure Laminate - Taco Brand)",
        "Kertasive Architectural Interior Film",
        "Vinyl Wrap / Laminated Sticker",
        "Melamine / PVC Film",
        "Acrylic High Gloss Sheet"
    )

    val patterns = when {
        materialCat.contains("Taco") || materialCat.contains("HPL") -> listOf(
            "Taco Woodgrain Natural Oak",
            "Taco Warm Teak & Walnut",
            "Taco Solid Silk Super Matte",
            "Taco High Gloss Pure White",
            "Taco Concrete & Industrial Stone",
            "Taco Fabric & Linen Texture"
        )
        materialCat.contains("Kertasive") -> listOf(
            "Kertasive Solid Warm Off-White",
            "Kertasive Scandinavian Oak Film",
            "Kertasive Calacatta Gold Marble",
            "Kertasive Rust Industrial Cement",
            "Kertasive Leather & Suede Film"
        )
        else -> listOf(
            "Architectural Matte Decal",
            "High Gloss Vehicle/Cabinetry Vinyl",
            "Brushed Metallic Silver / Gold",
            "Carbon Fiber Textured Vinyl"
        )
    }

    val adhesives = listOf(
        "Contact Cement / Yellow Glue (Fox / Aibon)",
        "Water-based Contact Adhesive",
        "Hot Melt EVA Glue (Edgebander)",
        "3M Primer 94 (Kertasive/Vinyl)"
    )

    val length = lengthStr.toDoubleOrNull() ?: 2400.0
    val width = widthStr.toDoubleOrNull() ?: 600.0
    val qty = qtyStr.toIntOrNull() ?: 1
    val waste = (wasteStr.toDoubleOrNull() ?: 15.0) / 100.0
    val edgeLen = edgeLenStr.toDoubleOrNull() ?: 0.0

    // Standard Sheet Size: 1220 x 2440 mm (4 x 8 ft) = 2.9768 m²
    val singleSurfaceAreaM2 = (length * width) / 1000000.0
    val totalSurfaceAreaM2 = singleSurfaceAreaM2 * qty
    val totalAreaWithWasteM2 = totalSurfaceAreaM2 * (1.0 + waste)

    val standardSheetAreaM2 = 1.22 * 2.44 // ~2.977 m2
    val sheetsNeeded = ceil(totalAreaWithWasteM2 / standardSheetAreaM2).toInt().coerceAtLeast(1)
    val yieldPercentage = ((totalSurfaceAreaM2 / (sheetsNeeded * standardSheetAreaM2)) * 100.0).coerceIn(0.0, 100.0)

    // Adhesive Coverage: Contact cement requires ~0.20 kg / m² on both surface + substrate (double-sided application)
    val adhesiveKgNeeded = totalSurfaceAreaM2 * 2.0 * 0.12 // 0.24 kg/m2 total
    val adhesiveLitersNeeded = adhesiveKgNeeded / 0.88 // Density of contact cement ~0.88 kg/L

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Card 1: Material & Finish Brand Selection
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("1. Laminated Material & Brand Selector", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Text("Finish Category:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = materialCat == cat,
                            onClick = { 
                                viewModel.laminateMaterialCategory.value = cat 
                                viewModel.laminateTexturePattern.value = if (cat.contains("Kertasive")) "Kertasive Scandinavian Oak Film" else "Taco Woodgrain Natural Oak"
                            },
                            label = { Text(cat) }
                        )
                    }
                }

                Text("Texture & Pattern Variety:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    patterns.forEach { pat ->
                        FilterChip(
                            selected = pattern == pat,
                            onClick = { viewModel.laminateTexturePattern.value = pat },
                            label = { Text(pat) }
                        )
                    }
                }
            }
        }

        // Card 2: Surface Dimensions & Sheet Yield Sizer
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("2. Surface Area & Sheet Sizing (1220 × 2440 mm)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = lengthStr,
                        onValueChange = { viewModel.laminateSurfaceLength.value = it },
                        label = { Text("Length (mm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = widthStr,
                        onValueChange = { viewModel.laminateSurfaceWidth.value = it },
                        label = { Text("Width (mm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { viewModel.laminateQuantity.value = it },
                        label = { Text("Panel Qty") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = wasteStr,
                        onValueChange = { viewModel.laminateWastePercent.value = it },
                        label = { Text("Waste Margin (%)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Surface Area:")
                        Text(String.format(Locale.US, "%.2f m² (%.1f sq ft)", totalSurfaceAreaM2, totalSurfaceAreaM2 * 10.7639), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Standard 4×8 ft Sheets Required:")
                        Text("$sheetsNeeded Sheet${if (sheetsNeeded > 1) "s" else ""}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Material Yield / Utilization:")
                        Text(String.format(Locale.US, "%.1f%%", yieldPercentage), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Card 3: Edge Banding & Adhesive Requirement
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FormatPaint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("3. Edge Banding & Contact Adhesive", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = edgeLenStr,
                        onValueChange = { viewModel.edgeBandingLength.value = it },
                        label = { Text("Edge Perimeter (m)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = edgeWidStr,
                        onValueChange = { viewModel.edgeBandingWidth.value = it },
                        label = { Text("Width (mm)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Text("Adhesive / Bonding Compound:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    adhesives.forEach { adh ->
                        FilterChip(
                            selected = adhesiveType == adh,
                            onClick = { viewModel.adhesiveGlueType.value = adh },
                            label = { Text(adh) }
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Contact Adhesive (Fox / Aibon):")
                        Text(String.format(Locale.US, "%.2f kg (%.2f L)", adhesiveKgNeeded, adhesiveLitersNeeded), fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Edge Banding Roll Needed:")
                        Text("${ceil(edgeLen * 1.1).toInt()} meters (incl. 10% trim)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Card 4: Pro Craft Application Best Practices Guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Professional Installation Steps for $materialCat:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1. Substrate Prep: Sand MDF / Plywood with 180-grit to eliminate dust and burrs.\n" +
                           "2. Glue Application: Spread contact cement evenly on both plywood and HPL using a notched spreader.\n" +
                           "3. Tack Time: Allow 10–15 minutes until tacky (dry to touch, no strings on finger).\n" +
                           "4. Alignment: Use timber dowels as spacers, then press from center outward using a J-roller to remove trapped air bubbles.\n" +
                           "5. Trimming: Flush trim edges using a carbide-tipped laminate trimmer router bit (bevel 15°–22°).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// ==================== TAB 9: COLOR RECOMMENDATIONS & COMBINATION SCORE ====================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabColorRecommendations(viewModel: PaintingCoatingStudioViewModel) {
    val selectedVibe by viewModel.selectedCozyVibe.collectAsState()
    val selectedBrand by viewModel.selectedCommercialBrand.collectAsState()

    val vibes = listOf(
        "Warm Hygge",
        "Rustic Cabin",
        "Classic Elegance",
        "Coastal Calm",
        "Forest Retreat",
        "Modern Industrial",
        "High Gloss Luxury"
    )
    val brands = listOf(
        "Sherwin-Williams",
        "Benjamin Moore",
        "Jotun",
        "Nippon Paint",
        "Duco / Danagloss",
        "Propan",
        "Avian Brands",
        "Dulux"
    )

    fun parseColorHex(hex: String): Color {
        return try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            Color.LightGray
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Vibe Picker Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "1. Select Desired Ambience / Vibe",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    vibes.forEach { vibe ->
                        FilterChip(
                            selected = selectedVibe == vibe,
                            onClick = { viewModel.selectedCozyVibe.value = vibe },
                            label = { Text(vibe) }
                        )
                    }
                }
            }
        }

        // Brand Picker Card
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "2. Select Commercial Brand",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    brands.forEach { brand ->
                        FilterChip(
                            selected = selectedBrand == brand,
                            onClick = { viewModel.selectedCommercialBrand.value = brand },
                            label = { Text(brand) }
                        )
                    }
                }
            }
        }

        // Filter and display matching palettes
        val matchedPalettes = viewModel.commercialPalettes.filter {
            it.category == selectedVibe && it.brand == selectedBrand
        }

        if (matchedPalettes.isEmpty()) {
            // General matching fallback from selected brand
            val brandFallback = viewModel.commercialPalettes.filter { it.brand == selectedBrand }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No direct match for '$selectedVibe' in '$selectedBrand'. Showing alternative ideal option below:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    brandFallback.forEach { palette ->
                        PaletteCard(palette = palette, onParse = { parseColorHex(it) })
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        } else {
            matchedPalettes.forEach { palette ->
                PaletteCard(palette = palette, onParse = { parseColorHex(it) })
            }
        }
    }
}

@Composable
fun PaletteCard(
    palette: PaintingCoatingStudioViewModel.ColorRecommendation,
    onParse: (String) -> Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Title & Brand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = palette.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${palette.brand} • ${palette.category}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Combination Score Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (palette.score >= 95) Color(0xFFDCFCE7) else Color(0xFFFEF9C3),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Match Score: ${palette.score}%",
                        color = if (palette.score >= 95) Color(0xFF15803D) else Color(0xFF854D0E),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Swatches Rendering
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Base Color Swatch
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(onParse(palette.baseHex), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Base: " + palette.baseName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = palette.baseHex,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Accent Color Swatch
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(onParse(palette.accentHex), shape = RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Accent: " + palette.accentName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = palette.accentHex,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Description
            Text(
                text = palette.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            // Ideal Formula Metrics Breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Contrast Ratio", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("4.8:1 (Ideal)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Warmth Level", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Comfort Warm", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Cozy Index", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("★ ★ ★ ★ ★", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
