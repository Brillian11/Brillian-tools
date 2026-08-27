package com.example.ui.screens.electrical

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResistorColorCodeScreen(
    viewModel: ResistorColorCodeViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var smdInput by remember { mutableStateOf(state.smdInputCode) }

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
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Resistor Color Code Decoder",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "4-Band, 5-Band, 6-Band, Tolerance, PPM & SMD code decoder",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Band Mode Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "RESISTOR CONFIGURATION",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.bandMode == ResistorBandMode.BAND_4,
                            onClick = { viewModel.setBandMode(ResistorBandMode.BAND_4) },
                            label = { Text("4-Band (Standard)") }
                        )
                        FilterChip(
                            selected = state.bandMode == ResistorBandMode.BAND_5,
                            onClick = { viewModel.setBandMode(ResistorBandMode.BAND_5) },
                            label = { Text("5-Band (Precision)") }
                        )
                        FilterChip(
                            selected = state.bandMode == ResistorBandMode.BAND_6,
                            onClick = { viewModel.setBandMode(ResistorBandMode.BAND_6) },
                            label = { Text("6-Band (TCR)") }
                        )
                    }
                }
            }

            // Visual Graphical Resistor Canvas
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "VISUAL RESISTOR STRIPES",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            val w = size.width
                            val h = size.height
                            val midY = h / 2f

                            // Draw Wire Leads
                            drawLine(
                                color = Color(0xFF9E9E9E),
                                start = Offset(10f, midY),
                                end = Offset(w - 10f, midY),
                                strokeWidth = 8f
                            )

                            // Draw Resistor Ceramic Body
                            val bodyW = w * 0.7f
                            val bodyH = h * 0.55f
                            val bodyX = (w - bodyW) / 2f
                            val bodyY = (h - bodyH) / 2f

                            drawRoundRect(
                                color = Color(0xFFD7CCC8), // Tan / Beige ceramic body
                                topLeft = Offset(bodyX, bodyY),
                                size = Size(bodyW, bodyH),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                            drawRoundRect(
                                color = Color(0xFF8D6E63),
                                topLeft = Offset(bodyX, bodyY),
                                size = Size(bodyW, bodyH),
                                cornerRadius = CornerRadius(12f, 12f),
                                style = Stroke(width = 2f)
                            )

                            // Band stripe colors
                            val bandColors = mutableListOf<Color>()
                            bandColors.add(ResistorColorCodeUiState.DIGIT_COLORS[state.band1Index].color)
                            bandColors.add(ResistorColorCodeUiState.DIGIT_COLORS[state.band2Index].color)
                            if (state.bandMode != ResistorBandMode.BAND_4) {
                                bandColors.add(ResistorColorCodeUiState.DIGIT_COLORS[state.band3Index].color)
                            }
                            bandColors.add(ResistorColorCodeUiState.MULTIPLIER_COLORS[state.multiplierIndex].color)
                            bandColors.add(ResistorColorCodeUiState.TOLERANCE_COLORS[state.toleranceIndex].color)
                            if (state.bandMode == ResistorBandMode.BAND_6) {
                                bandColors.add(ResistorColorCodeUiState.TCR_COLORS[state.tcrIndex].color)
                            }

                            val stripeW = 16f
                            val totalSlots = bandColors.size
                            val spacing = (bodyW - 40f) / (totalSlots + 1)

                            bandColors.forEachIndexed { idx, col ->
                                val sx = bodyX + 20f + (idx * spacing)
                                drawRect(
                                    color = col,
                                    topLeft = Offset(sx, bodyY),
                                    size = Size(stripeW, bodyH)
                                )
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.2f),
                                    topLeft = Offset(sx, bodyY),
                                    size = Size(stripeW, bodyH),
                                    style = Stroke(width = 1f)
                                )
                            }
                        }
                    }
                }
            }

            // Results & Resistance Readout
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
                            text = "DECODED RESISTANCE VALUE",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        IconButton(onClick = {
                            val info = "Resistor Value (${state.bandMode.name}):\n" +
                                    "Nominal: ${state.formattedResistance}\n" +
                                    "Tolerance: ±${state.tolerancePct}%\n" +
                                    "Resistance Range: ${String.format("%.2f", state.minResistanceOhms)} Ω - ${String.format("%.2f", state.maxResistanceOhms)} Ω"
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logResistor()
                            Toast.makeText(context, "Copied Resistor Specs!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Nominal Value", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = state.formattedResistance,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Tolerance", style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = "±${state.tolerancePct}%",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    }

                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Min Resistance", style = MaterialTheme.typography.labelSmall)
                                Text("${String.format("%.2f", state.minResistanceOhms)} Ω", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Max Resistance", style = MaterialTheme.typography.labelSmall)
                                Text("${String.format("%.2f", state.maxResistanceOhms)} Ω", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // Band 1 Picker
            BandPickerSection(
                title = "BAND 1 (1ST DIGIT)",
                colors = ResistorColorCodeUiState.DIGIT_COLORS,
                selectedIndex = state.band1Index,
                onSelect = { viewModel.setBand1(it) }
            )

            // Band 2 Picker
            BandPickerSection(
                title = "BAND 2 (2ND DIGIT)",
                colors = ResistorColorCodeUiState.DIGIT_COLORS,
                selectedIndex = state.band2Index,
                onSelect = { viewModel.setBand2(it) }
            )

            // Band 3 Picker (If 5 or 6 Band)
            if (state.bandMode != ResistorBandMode.BAND_4) {
                BandPickerSection(
                    title = "BAND 3 (3RD DIGIT)",
                    colors = ResistorColorCodeUiState.DIGIT_COLORS,
                    selectedIndex = state.band3Index,
                    onSelect = { viewModel.setBand3(it) }
                )
            }

            // Multiplier Picker
            BandPickerSection(
                title = "MULTIPLIER (10^N)",
                colors = ResistorColorCodeUiState.MULTIPLIER_COLORS,
                selectedIndex = state.multiplierIndex,
                onSelect = { viewModel.setMultiplier(it) }
            )

            // Tolerance Picker
            BandPickerSection(
                title = "TOLERANCE (±%)",
                colors = ResistorColorCodeUiState.TOLERANCE_COLORS,
                selectedIndex = state.toleranceIndex,
                onSelect = { viewModel.setTolerance(it) }
            )

            // TCR Picker (If 6 Band)
            if (state.bandMode == ResistorBandMode.BAND_6) {
                BandPickerSection(
                    title = "TEMPERATURE COEFFICIENT (PPM/K)",
                    colors = ResistorColorCodeUiState.TCR_COLORS,
                    selectedIndex = state.tcrIndex,
                    onSelect = { viewModel.setTcr(it) }
                )
            }

            // SMD Resistor Code Decoder Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SMD CHIP RESISTOR DECODER",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = smdInput,
                            onValueChange = {
                                smdInput = it
                                viewModel.decodeSmd(it)
                            },
                            label = { Text("SMD Code (e.g. 472, 1001, 4R7)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Decoded SMD Value:", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = state.smdDecodedResistance,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BandPickerSection(
    title: String,
    colors: List<com.example.ui.screens.electrical.ResistorColor>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(colors) { idx, item ->
                    val isSelected = selectedIndex == idx
                    Surface(
                        color = item.color,
                        shape = RoundedCornerShape(8.dp),
                        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(idx) }
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val textColor = if (item.name in listOf("White", "Yellow", "Gold", "Silver")) Color.Black else Color.White
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
