package com.example.ui.screens.ir

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ir.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobsiteIrRemoteScreen(
    viewModel: JobsiteIrRemoteViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentProfile = viewModel.getCurrentProfile()

    // Haptic feedback helper
    val triggerHaptic = {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(45)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Jobsite IR Remote & Commissioning",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isHardwareSupported) "Native Consumer IR Active (Offline 38kHz)" else "IR Simulator & Timing Waveform Mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.isHardwareSupported) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val summary = "Jobsite IR Remote: ${currentProfile?.brand} ${currentProfile?.modelOrSeries}\n" +
                                "Category: ${state.selectedCategory.title}\n" +
                                "Last Signal: ${state.lastTransmission?.title ?: "None"} (${state.lastTransmission?.hexSignature ?: ""})"
                        clipboardManager.setText(AnnotatedString(summary))
                        Toast.makeText(context, "IR profile info copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Profile")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // 1. Hardware Status & Transmission Indicator Banner
            item {
                HardwareStatusBar(
                    isHardwareSupported = state.isHardwareSupported,
                    lastTransmission = state.lastTransmission,
                    isMacroRunning = state.isMacroRunning,
                    macroStepTitle = state.macroStepTitle
                )
            }

            // 2. Trade Category Selector Tabs
            item {
                Text(
                    text = "Jobsite Trade Domain:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(IrTradeCategory.values()) { cat ->
                        FilterChip(
                            selected = state.selectedCategory == cat,
                            onClick = {
                                triggerHaptic()
                                viewModel.selectCategory(cat)
                            },
                            label = { Text(cat.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = {
                                Icon(
                                    imageVector = getCategoryIcon(cat),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }

            // 3. Equipment Brand / Model Selector Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentProfile?.brand ?: "Generic",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${currentProfile?.modelOrSeries} • ${currentProfile?.protocolName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${(currentProfile?.frequencyHz ?: 38000) / 1000} kHz",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Brand Selector Chips
                        val profilesInCat = state.availableProfiles.filter { it.category == state.selectedCategory }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(profilesInCat) { profile ->
                                SuggestionChip(
                                    onClick = {
                                        triggerHaptic()
                                        viewModel.selectProfile(profile.id)
                                    },
                                    label = {
                                        Text(
                                            profile.brand,
                                            fontWeight = if (profile.id == state.selectedProfileId) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = if (profile.id == state.selectedProfileId) {
                                        SuggestionChipDefaults.suggestionChipColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            labelColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        SuggestionChipDefaults.suggestionChipColors()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Quick Action Trade Tiles (High-Value Actions)
            item {
                Text(
                    text = "High-Value Trade Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val quickActions = currentProfile?.quickActions ?: emptyList()
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickActions.chunked(2).forEach { rowCommands ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowCommands.forEach { cmd ->
                                QuickActionCard(
                                    command = cmd,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        triggerHaptic()
                                        viewModel.transmitCommand(cmd)
                                        Toast.makeText(context, "Transmitted: ${cmd.title}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                            if (rowCommands.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // 5. Signal Macro Sequences (e.g., 1-Tap Commissioning)
            if (currentProfile?.macros?.isNotEmpty() == true) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AutoMode,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Automated Signal Macros",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            currentProfile.macros.forEach { macro ->
                                MacroRowCard(
                                    macro = macro,
                                    isRunning = state.isMacroRunning && state.runningMacroId == macro.id,
                                    onRun = {
                                        triggerHaptic()
                                        viewModel.runMacro(macro)
                                    },
                                    onStop = {
                                        viewModel.stopMacro()
                                    }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }

            // 6. Complete Virtual Keypad / Extended Commands
            item {
                Card(shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Full Remote Keypad & Controls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        val allCmds = currentProfile?.fullCommands ?: emptyList()
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            allCmds.forEach { cmd ->
                                OutlinedButton(
                                    onClick = {
                                        triggerHaptic()
                                        viewModel.transmitCommand(cmd)
                                        Toast.makeText(context, "Transmitted: ${cmd.title}", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(cmd.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(cmd.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text(
                                            cmd.hexSignature,
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Real-Time Optical Waveform Oscilloscope Canvas
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13171C)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Optical Pulse Train Waveform",
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            if (state.lastTransmission != null) {
                                Text(
                                    text = "${state.lastTransmission?.pulseCount} pulses • ${state.lastTransmission?.totalDurationUs?.let { it / 1000 }} ms",
                                    color = Color(0xFF00E676),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        IrWaveformCanvas(
                            pattern = state.lastTransmission?.timingPattern ?: intArrayOf(9000, 4500, 560, 1690, 560, 560, 560, 1690),
                            frequencyHz = state.lastTransmission?.frequencyHz ?: 38000,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (state.lastTransmission != null) {
                                "Last Sent: ${state.lastTransmission?.title} (${state.lastTransmission?.hexSignature}) @ ${state.lastTransmission?.frequencyHz} Hz"
                            } else {
                                "Idle • Tap any button to fire optical IR burst"
                            },
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 8. Custom Signal Terminal (Pronto HEX / Raw Microseconds Tester)
            item {
                Card(shape = RoundedCornerShape(14.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Direct IR Signal Terminal & Hex Sender",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Test unlisted equipment by transmitting Pronto Hex, 32-bit NEC codes, or raw timing arrays.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.customTerminalMode == "HEX",
                                onClick = { viewModel.setCustomTerminalMode("HEX") },
                                label = { Text("Pronto / 32-bit HEX") }
                            )
                            FilterChip(
                                selected = state.customTerminalMode == "RAW",
                                onClick = { viewModel.setCustomTerminalMode("RAW") },
                                label = { Text("Raw µs Timing Pattern") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Frequency Selector
                        Text("Carrier Frequency:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(36000, 38000, 40000, 56000).forEach { freq ->
                                FilterChip(
                                    selected = state.customCarrierFreqHz == freq,
                                    onClick = { viewModel.setCustomCarrierFreq(freq) },
                                    label = { Text("${freq / 1000} kHz", fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (state.customTerminalMode == "HEX") {
                            OutlinedTextField(
                                value = state.customHexInput,
                                onValueChange = { viewModel.setCustomHexInput(it) },
                                label = { Text("HEX Code / Pronto Sequence") },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                                minLines = 2,
                                maxLines = 4
                            )
                        } else {
                            OutlinedTextField(
                                value = state.customRawPatternInput,
                                onValueChange = { viewModel.setCustomRawPatternInput(it) },
                                label = { Text("Raw Microseconds (Mark, Space, ...)") },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 12.sp),
                                minLines = 2,
                                maxLines = 4
                            )
                        }

                        if (state.terminalStatusMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.terminalStatusMessage,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                triggerHaptic()
                                viewModel.transmitCustomTerminalSignal()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Transmit Optical Burst", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HardwareStatusBar(
    isHardwareSupported: Boolean,
    lastTransmission: IrTransmissionEvent?,
    isMacroRunning: Boolean,
    macroStepTitle: String
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isHardwareSupported) Color(0xFF1B3820) else Color(0xFF2A2E33)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Pulsing Optical Diode Indicator
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isHardwareSupported) Color(0xFF2E7D32) else Color(0xFF455A64)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isHardwareSupported) Icons.Default.FlashOn else Icons.Default.ElectricMeter,
                        contentDescription = null,
                        tint = if (isHardwareSupported) Color(0xFF69F0AE).copy(alpha = if (isMacroRunning) alpha else 1f) else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isHardwareSupported) "IR TRANSMITTER READY" else "IR SIMULATION & WAVEFORM MODE",
                        color = if (isHardwareSupported) Color(0xFF81C784) else Color.LightGray,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp
                    )
                    Text(
                        text = if (isMacroRunning) {
                            macroStepTitle
                        } else if (isHardwareSupported) {
                            "Direct line-of-sight optical control (Zero Wi-Fi/BT lag)"
                        } else {
                            "Live timing analysis active (Hardware emitter optional)"
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    command: IrCommand,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = command.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = command.description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun MacroRowCard(
    macro: IrMacroDefinition,
    isRunning: Boolean,
    onRun: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(macro.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(
                    macro.description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 14.sp
                )
                Text(
                    "${macro.steps.size} steps sequence",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isRunning) {
                IconButton(onClick = onStop) {
                    Icon(Icons.Default.StopCircle, contentDescription = "Stop", tint = MaterialTheme.colorScheme.error)
                }
            } else {
                FilledTonalButton(
                    onClick = onRun,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Run", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun IrWaveformCanvas(
    pattern: IntArray,
    frequencyHz: Int,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .background(Color(0xFF0A0D10), RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        val w = size.width
        val h = size.height

        val highY = h * 0.25f
        val lowY = h * 0.75f

        // Draw grid lines
        drawLine(
            color = Color(0xFF1E2630),
            start = Offset(0f, highY),
            end = Offset(w, highY),
            strokeWidth = 1f
        )
        drawLine(
            color = Color(0xFF1E2630),
            start = Offset(0f, lowY),
            end = Offset(w, lowY),
            strokeWidth = 1f
        )

        if (pattern.isEmpty()) return@Canvas

        val totalDurationUs = pattern.sum().coerceAtLeast(1)
        var currentX = 0f
        var isMark = true

        val path = Path()
        path.moveTo(0f, lowY)

        for (duration in pattern) {
            val stepW = (duration.toFloat() / totalDurationUs.toFloat()) * w
            val nextX = (currentX + stepW).coerceAtMost(w)
            val currentY = if (isMark) highY else lowY

            // Step transition
            path.lineTo(currentX, currentY)
            path.lineTo(nextX, currentY)

            currentX = nextX
            isMark = !isMark
        }

        path.lineTo(w, lowY)

        // Draw waveform stroke
        drawPath(
            path = path,
            color = Color(0xFF00E676),
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )
    }
}

private fun getCategoryIcon(category: IrTradeCategory) = when (category) {
    IrTradeCategory.HVAC -> Icons.Default.DeviceThermostat
    IrTradeCategory.AIR_FILTRATION -> Icons.Default.GraphicEq
    IrTradeCategory.LIGHTING_SENSORS -> Icons.Default.Lightbulb
    IrTradeCategory.HEATERS_FANS -> Icons.Default.FlashOn
    IrTradeCategory.SITE_AV -> Icons.Default.Videocam
}
