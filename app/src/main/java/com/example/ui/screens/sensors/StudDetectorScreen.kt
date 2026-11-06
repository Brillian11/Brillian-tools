package com.example.ui.screens.sensors

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StudDetectorScreen(
    viewModel: StudDetectorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    val signalColor by animateColorAsState(
        targetValue = if (state.isStudDetected) Color(0xFFD32F2F) else MaterialTheme.colorScheme.primary,
        label = "signal_color"
    )

    val animatedPct by animateFloatAsState(
        targetValue = state.signalPercent / 100f,
        label = "signal_pct"
    )

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
                            imageVector = Icons.Default.Sensors,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Stud & Metal Detector",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Magnetometer metal scanner with dynamic acoustic beeper & haptic feedback",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Quick Audio & Feedback Toolbar
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sound Toggle Button
                    Button(
                        onClick = { viewModel.toggleAudio() },
                        colors = if (state.isAudioEnabled) {
                            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        } else {
                            ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("audio_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (state.isAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Audio Beeper",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (state.isAudioEnabled) "Sound ON" else "Sound OFF",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Haptic Vibration Toggle
                    FilledTonalButton(
                        onClick = { viewModel.toggleVibration() },
                        colors = if (state.isVibrationEnabled) {
                            ButtonDefaults.filledTonalButtonColors()
                        } else {
                            ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("vibration_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Toggle Vibration",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isVibrationEnabled) "Haptic ON" else "Haptic OFF",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    // Calibrate / Zero Baseline
                    OutlinedButton(
                        onClick = { viewModel.calibrateBaseline() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("calibrate_button")
                    ) {
                        Icon(Icons.Default.CompassCalibration, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Zero", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Detector Signal Visualizer Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MAGNETIC PROXIMITY METER",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )

                        // Mode indicator
                        Surface(
                            color = if (state.isSimulationMode) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (state.isSimulationMode) "TEST / VIRTUAL MODE" else "HARDWARE SENSOR",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (state.isSimulationMode) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    // Main Circular Signal Meter
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(signalColor.copy(alpha = 0.15f))
                            .border(4.dp, signalColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${String.format("%.0f", state.signalPercent)}%",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 42.sp
                                ),
                                color = signalColor
                            )
                            Text(
                                text = "Δ ${String.format("%.1f", state.signalDeltaUT)} μT",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Flux: ${String.format("%.1f", state.magneticFieldUT)} μT",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Live Audio Tone Indicator
                    Surface(
                        color = if (state.isAudioEnabled && (state.isStudDetected || state.signalPercent >= 10f)) {
                            signalColor.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (state.isAudioEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = if (state.isAudioEnabled) signalColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    !state.isAudioEnabled -> "Beeper Muted (Press Sound ON to enable audio)"
                                    state.signalPercent >= 80f -> "Rapid Peak Alarm: ~${(850 + state.signalPercent * 17.5f).toInt()} Hz (50ms pulses)"
                                    state.signalPercent >= 40f -> "Acoustic Tone: ~${(850 + state.signalPercent * 17.5f).toInt()} Hz (Steady beeps)"
                                    state.signalPercent >= 10f -> "Approaching Object: Slow warning pips"
                                    else -> "Beeper Standby: Quiet / Awaiting Metal Signal"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (state.isAudioEnabled) signalColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Status Banner
                    Surface(
                        color = if (state.isStudDetected) Color(0xFFFFEBEE) else Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.isStudDetected) Icons.Default.Warning else Icons.Default.Build,
                                contentDescription = null,
                                tint = if (state.isStudDetected) Color(0xFFC62828) else Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (state.isStudDetected) "METAL OBJECT DETECTED!" else "SEARCHING FOR STUDS / PIPES...",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (state.isStudDetected) Color(0xFFC62828) else Color(0xFF2E7D32)
                                )
                                Text(
                                    text = "Estimated Object: ${state.detectedMaterialType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (state.isStudDetected) Color(0xFFC62828) else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    // Proximity Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedPct)
                                .height(16.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(signalColor)
                        )
                    }

                    // Sensitivity Adjuster
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Trigger Sensitivity Threshold", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            Text("${String.format("%.0f", state.sensitivityThresholdUT)} μT", style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace))
                        }
                        Slider(
                            value = state.sensitivityThresholdUT,
                            onValueChange = { viewModel.setSensitivity(it) },
                            valueRange = 5f..50f,
                            modifier = Modifier.testTag("sensitivity_slider")
                        )
                    }
                }
            }

            // Interactive Audio Test & Sweep Simulator Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Acoustic Test & Sweep Simulator",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        if (state.isSensorAvailable && state.isSimulationMode) {
                            TextButton(
                                onClick = { viewModel.setLiveHardwareMode() },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text("Live Sensor", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

                    Text(
                        text = "Test the metal detector sound beeper and haptic response immediately. Drag the slider to test metal proximity, or run the auto-sweep demo across a virtual wall stud.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Manual Proximity Sweep Slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Simulated Metal Proximity",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                            )
                            Text(
                                text = "${state.simulatedProximity.toInt()}%",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = signalColor
                                )
                            )
                        }
                        Slider(
                            value = state.simulatedProximity,
                            onValueChange = { viewModel.setSimulatedProximity(it) },
                            valueRange = 0f..100f,
                            modifier = Modifier.testTag("proximity_test_slider")
                        )
                    }

                    // Auto-Sweep Demo Button & Preset Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.toggleAutoSweep() },
                            colors = if (state.isAutoSweeping) {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            } else {
                                ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.3f)
                                .testTag("auto_sweep_button")
                        ) {
                            Icon(
                                imageVector = if (state.isAutoSweeping) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (state.isAutoSweeping) "Stop Demo" else "Auto-Sweep Demo")
                        }

                        FilledTonalButton(
                            onClick = { viewModel.setSimulatedProximity(0f) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(0.9f)
                        ) {
                            Text("Clear", style = MaterialTheme.typography.labelSmall)
                        }

                        FilledTonalButton(
                            onClick = { viewModel.setSimulatedProximity(95f) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Max Stud", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
