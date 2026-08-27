package com.example.ui.screens.electrical

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Straighten
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConduitBenderScreen(
    viewModel: ConduitBenderViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var heightInput by remember { mutableStateOf(state.obstacleHeightInches.toString()) }
    var widthInput by remember { mutableStateOf(state.obstacleWidthInches.toString()) }
    var distInput by remember { mutableStateOf(state.distanceToObstacleInches.toString()) }
    var stubInput by remember { mutableStateOf(state.desiredStubHeightInches.toString()) }

    fun syncInputs(h: Double, w: Double, d: Double, s: Double) {
        viewModel.updateInputs(h, w, d, s)
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
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Conduit Bender Angles & Offsets",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Offsets, 3/4-bend saddles, 90° stub-up take-up & mark multipliers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Bender Mode Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "CONDUIT BEND OPERATION",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.benderMode == BenderMode.OFFSET_BEND,
                            onClick = { viewModel.setBenderMode(BenderMode.OFFSET_BEND) },
                            label = { Text("Offset Bend") }
                        )
                        FilterChip(
                            selected = state.benderMode == BenderMode.THREE_BEND_SADDLE,
                            onClick = { viewModel.setBenderMode(BenderMode.THREE_BEND_SADDLE) },
                            label = { Text("3-Bend Saddle") }
                        )
                        FilterChip(
                            selected = state.benderMode == BenderMode.FOUR_BEND_SADDLE,
                            onClick = { viewModel.setBenderMode(BenderMode.FOUR_BEND_SADDLE) },
                            label = { Text("4-Bend Saddle") }
                        )
                        FilterChip(
                            selected = state.benderMode == BenderMode.STUB_UP_90,
                            onClick = { viewModel.setBenderMode(BenderMode.STUB_UP_90) },
                            label = { Text("90° Stub-Up") }
                        )
                    }

                    Text(
                        text = "CONDUIT SIZE (TAKE-UP SPECS):",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        itemsIndexed(ConduitBenderUiState.CONDUIT_SPECS) { idx, spec ->
                            FilterChip(
                                selected = state.selectedConduitSizeIndex == idx,
                                onClick = { viewModel.setConduitSizeIndex(idx) },
                                label = { Text("${spec.conduitSize} (-${spec.takeUpInches}\")") }
                            )
                        }
                    }
                }
            }

            // Offset Angle Selector (If in offset or 4-bend mode)
            if (state.benderMode in listOf(BenderMode.OFFSET_BEND, BenderMode.FOUR_BEND_SADDLE)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "BEND ANGLE & MULTIPLIER",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            itemsIndexed(OffsetAngle.values()) { _, angle ->
                                FilterChip(
                                    selected = state.offsetAngle == angle,
                                    onClick = { viewModel.setOffsetAngle(angle) },
                                    label = { Text(angle.label) }
                                )
                            }
                        }
                    }
                }
            }

            // Input Fields
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "OBSTACLE & RUN MEASUREMENTS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    if (state.benderMode == BenderMode.STUB_UP_90) {
                        OutlinedTextField(
                            value = stubInput,
                            onValueChange = {
                                stubInput = it
                                it.toDoubleOrNull()?.let { s ->
                                    syncInputs(heightInput.toDoubleOrNull() ?: 4.0, widthInput.toDoubleOrNull() ?: 6.0, distInput.toDoubleOrNull() ?: 36.0, s)
                                }
                            },
                            label = { Text("Desired Stub-Up Height (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = heightInput,
                                onValueChange = {
                                    heightInput = it
                                    it.toDoubleOrNull()?.let { h ->
                                        syncInputs(h, widthInput.toDoubleOrNull() ?: 6.0, distInput.toDoubleOrNull() ?: 36.0, stubInput.toDoubleOrNull() ?: 18.0)
                                    }
                                },
                                label = { Text("Obstacle Height / Offset (in)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            if (state.benderMode == BenderMode.FOUR_BEND_SADDLE) {
                                OutlinedTextField(
                                    value = widthInput,
                                    onValueChange = {
                                        widthInput = it
                                        it.toDoubleOrNull()?.let { w ->
                                            syncInputs(heightInput.toDoubleOrNull() ?: 4.0, w, distInput.toDoubleOrNull() ?: 36.0, stubInput.toDoubleOrNull() ?: 18.0)
                                        }
                                    },
                                    label = { Text("Obstacle Width (in)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = distInput,
                            onValueChange = {
                                distInput = it
                                it.toDoubleOrNull()?.let { d ->
                                    syncInputs(heightInput.toDoubleOrNull() ?: 4.0, widthInput.toDoubleOrNull() ?: 6.0, d, stubInput.toDoubleOrNull() ?: 18.0)
                                }
                            },
                            label = { Text("Distance to Obstacle from Pipe End (in)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Visual Bend Diagram Canvas
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
                        text = "CONDUIT BEND PROFILE & MARK LOCATIONS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    ) {
                        val pipeColor = MaterialTheme.colorScheme.primary
                        val markColor = Color.Red

                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            val w = size.width
                            val h = size.height

                            when (state.benderMode) {
                                BenderMode.OFFSET_BEND -> {
                                    val path = Path().apply {
                                        moveTo(10f, h - 20f)
                                        lineTo(w * 0.35f, h - 20f)
                                        lineTo(w * 0.65f, 20f)
                                        lineTo(w - 10f, 20f)
                                    }
                                    drawPath(path, color = pipeColor, style = Stroke(width = 6f))

                                    // Mark lines
                                    drawLine(color = markColor, start = Offset(w * 0.35f, h - 35f), end = Offset(w * 0.35f, h - 5f), strokeWidth = 3f)
                                    drawLine(color = markColor, start = Offset(w * 0.65f, 5f), end = Offset(w * 0.65f, 35f), strokeWidth = 3f)
                                }
                                BenderMode.THREE_BEND_SADDLE -> {
                                    val path = Path().apply {
                                        moveTo(10f, h - 20f)
                                        lineTo(w * 0.3f, h - 20f)
                                        lineTo(w * 0.5f, 20f)
                                        lineTo(w * 0.7f, h - 20f)
                                        lineTo(w - 10f, h - 20f)
                                    }
                                    drawPath(path, color = pipeColor, style = Stroke(width = 6f))

                                    // 3 mark ticks
                                    drawLine(color = markColor, start = Offset(w * 0.3f, h - 35f), end = Offset(w * 0.3f, h - 5f), strokeWidth = 3f)
                                    drawLine(color = markColor, start = Offset(w * 0.5f, 5f), end = Offset(w * 0.5f, 35f), strokeWidth = 4f)
                                    drawLine(color = markColor, start = Offset(w * 0.7f, h - 35f), end = Offset(w * 0.7f, h - 5f), strokeWidth = 3f)
                                }
                                BenderMode.FOUR_BEND_SADDLE -> {
                                    val path = Path().apply {
                                        moveTo(10f, h - 20f)
                                        lineTo(w * 0.25f, h - 20f)
                                        lineTo(w * 0.4f, 20f)
                                        lineTo(w * 0.6f, 20f)
                                        lineTo(w * 0.75f, h - 20f)
                                        lineTo(w - 10f, h - 20f)
                                    }
                                    drawPath(path, color = pipeColor, style = Stroke(width = 6f))
                                }
                                BenderMode.STUB_UP_90 -> {
                                    val path = Path().apply {
                                        moveTo(10f, h - 20f)
                                        lineTo(w * 0.7f, h - 20f)
                                        quadraticBezierTo(w * 0.85f, h - 20f, w * 0.85f, 10f)
                                    }
                                    drawPath(path, color = pipeColor, style = Stroke(width = 6f))
                                    drawLine(color = markColor, start = Offset(w * 0.7f, h - 35f), end = Offset(w * 0.7f, h - 5f), strokeWidth = 3f)
                                }
                            }
                        }
                    }
                }
            }

            // Results & Mark Measurements Card
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
                            text = "CALCULATED PENCIL MARKS",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        IconButton(onClick = {
                            val info = "Conduit Bender Layout (${state.benderMode.name}):\n" +
                                    "Obstacle Rise: ${state.obstacleHeightInches}\"\n" +
                                    "Distance Between Marks: ${String.format("%.3f", state.distanceBetweenMarksInches)}\"\n" +
                                    "Shrink Allowance: ${String.format("%.3f", state.totalShrinkInches)}\"\n" +
                                    "Mark 1: ${String.format("%.3f", state.mark1DistanceInches)}\"\n" +
                                    "Mark 2: ${String.format("%.3f", state.mark2DistanceInches)}\""
                            clipboardManager.setText(AnnotatedString(info))
                            viewModel.logBenderCalculation()
                            Toast.makeText(context, "Copied Bender Marks!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }

                    when (state.benderMode) {
                        BenderMode.OFFSET_BEND -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Distance Between Marks", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${String.format("%.2f", state.distanceBetweenMarksInches)}\"",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Pipe Shrink", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${String.format("%.3f", state.totalShrinkInches)}\"",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                }
                            }

                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Mark #1 (First Bend): ${String.format("%.2f", state.mark1DistanceInches)}\" from pipe end", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Mark #2 (Second Bend): ${String.format("%.2f", state.mark2DistanceInches)}\" from pipe end", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                        BenderMode.THREE_BEND_SADDLE -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Center Bend Mark (45°)", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${String.format("%.2f", state.saddleCenterMarkInches)}\"",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Side Mark Spacing", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "±${String.format("%.2f", state.saddleSideMarkDistanceInches)}\"",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.secondary)
                                    )
                                }
                            }

                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Outer Mark A (22.5°): ${String.format("%.2f", state.saddleCenterMarkInches - state.saddleSideMarkDistanceInches)}\"", style = MaterialTheme.typography.bodyMedium)
                                    Text("Center Mark (45°): ${String.format("%.2f", state.saddleCenterMarkInches)}\"", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text("Outer Mark B (22.5°): ${String.format("%.2f", state.saddleCenterMarkInches + state.saddleSideMarkDistanceInches)}\"", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        BenderMode.STUB_UP_90 -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Mark from End of Pipe", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "${String.format("%.2f", state.stubMarkDistanceInches)}\"",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary)
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Deducted Bender Take-Up", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = "-${state.stubTakeUpInches}\"",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.error)
                                    )
                                }
                            }
                        }
                        BenderMode.FOUR_BEND_SADDLE -> {
                            Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Mark #1: ${String.format("%.2f", state.mark1DistanceInches)}\"", style = MaterialTheme.typography.bodyMedium)
                                    Text("Mark #2: ${String.format("%.2f", state.mark2DistanceInches)}\"", style = MaterialTheme.typography.bodyMedium)
                                    Text("Mark #3: ${String.format("%.2f", state.mark2DistanceInches + state.obstacleWidthInches)}\"", style = MaterialTheme.typography.bodyMedium)
                                    Text("Mark #4: ${String.format("%.2f", state.mark2DistanceInches + state.obstacleWidthInches + state.distanceBetweenMarksInches)}\"", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
