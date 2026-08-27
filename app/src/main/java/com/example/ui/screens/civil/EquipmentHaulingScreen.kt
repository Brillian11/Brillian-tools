package com.example.ui.screens.civil

import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Composable
fun EquipmentHaulingScreen(
    viewModel: EquipmentHaulingViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val uWeight = if (state.isMetric) "kg" else "lbs"
    val uDist = if (state.isMetric) "m" else "ft"

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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalShipping,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column {
                            Text(
                                text = "Equipment Hauling & Axle Load",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Bridge Formula B, Axle Weights & CG Balance",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                viewModel.saveToLog()
                                Toast.makeText(context, "Hauling configuration logged", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = "Save to Log",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(
                            onClick = {
                                val text = buildString {
                                    appendLine("=== HEAVY HAULING & AXLE LOAD REPORT ===")
                                    appendLine("Rig: ${state.rigType.label}")
                                    appendLine("Equipment: ${state.equipmentPreset.label}")
                                    appendLine("Payload Weight: ${String.format("%.0f", state.payloadWeight)} $uWeight")
                                    appendLine("Dimensions: ${state.machineLength}x${state.machineWidth}x${state.machineHeight} $uDist")
                                    appendLine("Total Travel Height: ${String.format("%.1f", state.totalTravelHeight)} $uDist")
                                    appendLine("--------------------------------------")
                                    appendLine("Steer Axle: ${String.format("%.0f", state.steerAxleLoad)} / ${String.format("%.0f", state.steerLegalLimit)} $uWeight (${if (state.isSteerOverloaded) "OVERLOAD" else "LEGAL"})")
                                    appendLine("Drive Tandem: ${String.format("%.0f", state.driveAxleGroupLoad)} / ${String.format("%.0f", state.driveLegalLimit)} $uWeight (${if (state.isDriveOverloaded) "OVERLOAD" else "LEGAL"})")
                                    appendLine("Trailer Bogies: ${String.format("%.0f", state.trailerAxleGroupLoad)} / ${String.format("%.0f", state.trailerLegalLimit)} $uWeight (${if (state.isTrailerOverloaded) "OVERLOAD" else "LEGAL"})")
                                    appendLine("Gross Vehicle Weight (GVW): ${String.format("%.0f", state.grossVehicleWeight)} / ${String.format("%.0f", state.gvwLegalLimit)} $uWeight")
                                    appendLine("Bridge Formula Limit: ${String.format("%.0f", state.federalBridgeFormulaLimit)} $uWeight")
                                    appendLine("--------------------------------------")
                                    appendLine("Permit Requirements:")
                                    appendLine("• Overweight Permit: ${if (state.requiresOverweightPermit) "YES" else "NO"}")
                                    appendLine("• Oversize Width Permit: ${if (state.requiresOversizeWidthPermit) "YES (>8.5 ft)" else "NO"}")
                                    appendLine("• Oversize Height Permit: ${if (state.requiresOversizeHeightPermit) "YES (>13.5 ft)" else "NO"}")
                                    appendLine("• 'OVERSIZE LOAD' Banners: ${if (state.requiresOversizeSignsBanners) "REQUIRED" else "NOT REQUIRED"}")
                                    appendLine("• Pilot Escort Vehicle: ${if (state.requiresPilotEscortCar) "REQUIRED (>12 ft width / >14.5 ft height)" else "NOT REQUIRED"}")
                                }
                                clipboardManager.setText(AnnotatedString(text))
                                Toast.makeText(context, "Hauling report copied to clipboard", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Report",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Unit and Rig Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !state.isMetric,
                    onClick = { viewModel.setMetric(false) },
                    label = { Text("US Customary (lbs/ft)") }
                )
                FilterChip(
                    selected = state.isMetric,
                    onClick = { viewModel.setMetric(true) },
                    label = { Text("Metric (kg/m)") }
                )
            }

            // Rig Type Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HaulerRigType.values().forEach { r ->
                    FilterChip(
                        selected = state.rigType == r,
                        onClick = { viewModel.setRigType(r) },
                        label = { Text(r.label) }
                    )
                }
            }

            // Interactive Truck Rig Diagram Canvas
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1E293B)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Rig Axle Statics & CG Placement",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        val legal = !state.isSteerOverloaded && !state.isDriveOverloaded && !state.isTrailerOverloaded && !state.isGvwOverloaded
                        Surface(
                            color = if (legal) Color(0xFF059669) else Color(0xFFDC2626),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (legal) "LEGAL AXLE WEIGHTS" else "PERMIT REQUIRED / OVERLOAD",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Real-time load shift along lowboy deck with axle reaction calculations",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    ) {
                        val width = size.width
                        val height = size.height

                        val groundY = height * 0.82f

                        // 1. Tractor Cab
                        val cabStartX = width * 0.05f
                        val cabWidth = width * 0.22f
                        val cabTopY = groundY - 60f

                        drawRect(
                            color = Color(0xFF475569),
                            topLeft = Offset(cabStartX, cabTopY),
                            size = Size(cabWidth, 40f)
                        )
                        // Windshield
                        drawRect(
                            color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                            topLeft = Offset(cabStartX + 5f, cabTopY + 5f),
                            size = Size(20f, 18f)
                        )

                        // 2. Tractor Steer Wheel
                        val steerAxleX = cabStartX + 18f
                        drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(steerAxleX, groundY))
                        drawCircle(color = Color(0xFF94A3B8), radius = 6f, center = Offset(steerAxleX, groundY))

                        // Tractor Drive Tandem Wheels
                        val driveAxle1X = cabStartX + cabWidth - 30f
                        val driveAxle2X = cabStartX + cabWidth - 10f
                        drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(driveAxle1X, groundY))
                        drawCircle(color = Color(0xFF94A3B8), radius = 6f, center = Offset(driveAxle1X, groundY))
                        drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(driveAxle2X, groundY))
                        drawCircle(color = Color(0xFF94A3B8), radius = 6f, center = Offset(driveAxle2X, groundY))

                        // 3. Gooseneck & Lowboy Deck
                        val gooseneckStartX = cabStartX + cabWidth - 20f
                        val deckStartX = gooseneckStartX + 35f
                        val deckEndX = width * 0.78f
                        val deckWidth = deckEndX - deckStartX
                        val deckY = groundY - 18f

                        // Gooseneck beam
                        val gooseneckPath = Path().apply {
                            moveTo(gooseneckStartX, groundY - 35f)
                            lineTo(deckStartX, deckY)
                            lineTo(deckStartX, deckY + 8f)
                            lineTo(gooseneckStartX, groundY - 28f)
                            close()
                        }
                        drawPath(path = gooseneckPath, color = Color(0xFFD97706))

                        // Lowboy Drop Deck Well
                        drawRect(
                            color = Color(0xFFD97706),
                            topLeft = Offset(deckStartX, deckY),
                            size = Size(deckWidth, 8f)
                        )

                        // 4. Equipment Machinery Silhouette on Deck
                        val deckLen = state.rigType.deckLengthFt
                        val cgFrac = (state.loadCgPlacementOnDeck / deckLen).coerceIn(0.1, 0.9).toFloat()
                        val machCenterX = deckStartX + deckWidth * cgFrac
                        val machWidth = deckWidth * 0.65f
                        val machStartX = machCenterX - machWidth / 2f
                        val machTopY = deckY - 45f

                        // Machine body (Excavator / Bulldozer yellow)
                        drawRect(
                            color = Color(0xFFFBBF24),
                            topLeft = Offset(machStartX, machTopY),
                            size = Size(machWidth, 42f)
                        )
                        // Machine tracks
                        drawRect(
                            color = Color(0xFF1E293B),
                            topLeft = Offset(machStartX + 5f, deckY - 10f),
                            size = Size(machWidth - 10f, 10f)
                        )
                        // Boom arm
                        drawLine(
                            color = Color(0xFFF59E0B),
                            start = Offset(machStartX + 10f, machTopY + 10f),
                            end = Offset(machStartX - 25f, machTopY - 15f),
                            strokeWidth = 6f
                        )

                        // Center of Gravity Marker (Red target)
                        drawCircle(color = Color(0xFFEF4444), radius = 7f, center = Offset(machCenterX, machTopY + 20f))
                        drawLine(
                            color = Color(0xFFEF4444),
                            start = Offset(machCenterX, machTopY),
                            end = Offset(machCenterX, groundY + 12f),
                            strokeWidth = 2f
                        )

                        // 5. Trailer Bogie Wheels
                        val trailerBogieStartX = deckEndX + 10f
                        val numTrailerAxles = if (state.rigType == HaulerRigType.COMBO_6_AXLE_RGN) 3 else 2
                        for (a in 0 until numTrailerAxles) {
                            val wheelX = trailerBogieStartX + a * 20f
                            drawCircle(color = Color(0xFF0F172A), radius = 12f, center = Offset(wheelX, groundY))
                            drawCircle(color = Color(0xFF94A3B8), radius = 6f, center = Offset(wheelX, groundY))
                        }
                    }

                    // CG Slide Adjustment Slider
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Machine Position on Deck (${String.format("%.1f", state.loadCgPlacementOnDeck)} $uDist from front)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        if (abs(state.recommendedCgShiftInches) > 1.0) {
                            Text(
                                text = if (state.recommendedCgShiftInches > 0) "Slide ${String.format("%.0f", state.recommendedCgShiftInches)}\" BACK" else "Slide ${String.format("%.0f", abs(state.recommendedCgShiftInches))}\" FORWARD",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFBBF24),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Slider(
                        value = state.loadCgPlacementOnDeck.toFloat(),
                        onValueChange = { viewModel.updateInputs(loadCgPlacementOnDeck = it.toDouble()) },
                        valueRange = 2.0f..(state.rigType.deckLengthFt.toFloat() - 2.0f)
                    )
                }
            }

            // Machinery Preset Selection
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Heavy Machinery Payload Specs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HeavyEquipmentPreset.values().forEach { eq ->
                            FilterChip(
                                selected = state.equipmentPreset == eq,
                                onClick = { viewModel.setEquipmentPreset(eq) },
                                label = { Text(eq.label) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.payloadWeight.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(payloadWeight = it) } },
                            label = { Text("Operating Weight ($uWeight)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.machineWidth.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(machineWidth = it) } },
                            label = { Text("Width ($uDist)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = state.machineHeight.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(machineHeight = it) } },
                            label = { Text("Height ($uDist)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = state.machineLength.toString(),
                            onValueChange = { v -> v.toDoubleOrNull()?.let { viewModel.updateInputs(machineLength = it) } },
                            label = { Text("Length ($uDist)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            }

            // Axle Load Gauges & Bridge Weight Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Axle Group Loading & Legal Limits",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Steer Axle Bar
                    AxleWeightBar(
                        title = "Steer Axle",
                        actualWeight = state.steerAxleLoad,
                        legalLimit = state.steerLegalLimit,
                        uWeight = uWeight,
                        isOver = state.isSteerOverloaded
                    )

                    // Drive Tandem Bar
                    AxleWeightBar(
                        title = "Tractor Drive Group",
                        actualWeight = state.driveAxleGroupLoad,
                        legalLimit = state.driveLegalLimit,
                        uWeight = uWeight,
                        isOver = state.isDriveOverloaded
                    )

                    // Trailer Bogie Bar
                    AxleWeightBar(
                        title = "Trailer Axle Group",
                        actualWeight = state.trailerAxleGroupLoad,
                        legalLimit = state.trailerLegalLimit,
                        uWeight = uWeight,
                        isOver = state.isTrailerOverloaded
                    )

                    // Gross Vehicle Weight (GVW) Bar
                    AxleWeightBar(
                        title = "Gross Vehicle Weight (GVW)",
                        actualWeight = state.grossVehicleWeight,
                        legalLimit = state.gvwLegalLimit,
                        uWeight = uWeight,
                        isOver = state.isGvwOverloaded
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DOT Oversize / Overweight Permit Advisory",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermitBadge(
                            title = "Overweight",
                            active = state.requiresOverweightPermit,
                            desc = if (state.requiresOverweightPermit) "GVW > 80k lbs" else "Legal GVW",
                            modifier = Modifier.weight(1f)
                        )
                        PermitBadge(
                            title = "Oversize Width",
                            active = state.requiresOversizeWidthPermit,
                            desc = if (state.requiresOversizeWidthPermit) "Width > 8.5 ft" else "≤ 8'6\" Legal",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PermitBadge(
                            title = "Height Permit",
                            active = state.requiresOversizeHeightPermit,
                            desc = "${String.format("%.1f", state.totalTravelHeight)}' Travel",
                            modifier = Modifier.weight(1f)
                        )
                        PermitBadge(
                            title = "Pilot Escort Car",
                            active = state.requiresPilotEscortCar,
                            desc = if (state.requiresPilotEscortCar) "REQUIRED (>12' W)" else "Not Required",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AxleWeightBar(
    title: String,
    actualWeight: Double,
    legalLimit: Double,
    uWeight: String,
    isOver: Boolean
) {
    val fraction = if (legalLimit > 0) (actualWeight / legalLimit).toFloat().coerceIn(0f, 1.3f) else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${String.format("%.0f", actualWeight)} / ${String.format("%.0f", legalLimit)} $uWeight",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = if (isOver) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
            )
        }
        LinearProgressIndicator(
            progress = { fraction.coerceAtMost(1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = when {
                isOver -> Color(0xFFDC2626)
                fraction > 0.90f -> Color(0xFFF59E0B)
                else -> Color(0xFF10B981)
            },
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun PermitBadge(
    title: String,
    active: Boolean,
    desc: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (active) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (active) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (active) Color(0xFFDC2626) else Color(0xFF16A34A),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Color(0xFF991B1B) else Color(0xFF166534)
                )
            }
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = if (active) Color(0xFFB91C1C) else Color(0xFF15803D)
            )
        }
    }
}
