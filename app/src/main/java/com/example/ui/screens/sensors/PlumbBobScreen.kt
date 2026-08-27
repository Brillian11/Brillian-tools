package com.example.ui.screens.sensors

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PlumbBobScreen(
    viewModel: PlumbBobViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    val plumbColor by animateColorAsState(
        targetValue = if (state.isPlumb) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
        label = "plumb_color"
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
            // Header
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
                            imageVector = Icons.Default.Architecture,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Plumb Bob & Wall Squareness",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Sensor-assisted vertical alignment & 3-4-5 rule corner squareness solver",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Plumb Bob Line", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Architecture, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_plumb_line")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("90° Corner Squareness", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.SquareFoot, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_squareness")
                )
            }

            if (selectedTab == 0) {
                // Plumb Line Visualizer Canvas
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GRAVITY PLUMB LINE",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                            )
                            Button(
                                onClick = { viewModel.toggleHold() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("hold_button")
                            ) {
                                Icon(
                                    imageVector = if (state.isHold) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (state.isHold) "Locked" else "Lock Angle")
                            }
                        }

                        // Plumb Line Animation Canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(2.dp, plumbColor, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width / 2f
                                val topY = 20f
                                val stringLen = size.height - 70f

                                // Target centerline vertical reference
                                drawLine(
                                    color = Color.Gray.copy(alpha = 0.3f),
                                    start = Offset(centerX, topY),
                                    end = Offset(centerX, topY + stringLen + 30f),
                                    strokeWidth = 2f
                                )

                                // Calculate plumb sway offset based on roll angle
                                val maxOffset = size.width / 2f - 30f
                                val swayX = centerX + (state.rollAngle / 45f * maxOffset).coerceIn(-maxOffset, maxOffset)
                                val plumbY = topY + stringLen

                                // Plumb string line
                                drawLine(
                                    color = plumbColor,
                                    start = Offset(centerX, topY),
                                    end = Offset(swayX, plumbY),
                                    strokeWidth = 4f
                                )

                                // Plumb bob brass weight shape
                                val bobPath = Path().apply {
                                    moveTo(swayX, plumbY)
                                    lineTo(swayX - 18f, plumbY + 28f)
                                    lineTo(swayX, plumbY + 50f)
                                    lineTo(swayX + 18f, plumbY + 28f)
                                    close()
                                }
                                drawPath(path = bobPath, color = plumbColor)
                            }

                            if (state.isPlumb) {
                                Surface(
                                    color = Color(0xFF2E7D32),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "TRUE VERTICAL PLUMB (0.0°)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Plumb Metrics Readout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Pitch Angle", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format("%.1f", state.pitchAngle)}°",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Roll Tilt", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format("%.1f", state.rollAngle)}°",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Plumb Deviation", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = "${String.format("%.1f", state.plumbDeviationMmPerM)} mm/m",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace),
                                    color = plumbColor
                                )
                            }
                        }
                    }
                }
            } else {
                // 90° Corner Squareness Solver Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "3-4-5 PYTHAGOREAN 90° CORNER SQUARENESS CHECKER",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        Text(
                            text = "Verify wall framing, drywall corners, and tile layouts for true 90° squareness using hypotenuse laser measurement.",
                            style = MaterialTheme.typography.bodySmall
                        )

                        var sideAInput by remember { mutableStateOf(state.cornerLengthA.toString()) }
                        var sideBInput by remember { mutableStateOf(state.cornerLengthB.toString()) }
                        var diagInput by remember { mutableStateOf(state.measuredDiagonalC.toString()) }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = sideAInput,
                                onValueChange = {
                                    sideAInput = it
                                    val a = it.toDoubleOrNull() ?: 3.0
                                    val b = sideBInput.toDoubleOrNull() ?: 4.0
                                    val c = diagInput.toDoubleOrNull() ?: 5.0
                                    viewModel.updateCornerDimensions(a, b, c)
                                },
                                label = { Text("Side A (m)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = sideBInput,
                                onValueChange = {
                                    sideBInput = it
                                    val a = sideAInput.toDoubleOrNull() ?: 3.0
                                    val b = it.toDoubleOrNull() ?: 4.0
                                    val c = diagInput.toDoubleOrNull() ?: 5.0
                                    viewModel.updateCornerDimensions(a, b, c)
                                },
                                label = { Text("Side B (m)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = diagInput,
                            onValueChange = {
                                diagInput = it
                                val a = sideAInput.toDoubleOrNull() ?: 3.0
                                val b = sideBInput.toDoubleOrNull() ?: 4.0
                                val c = it.toDoubleOrNull() ?: 5.0
                                viewModel.updateCornerDimensions(a, b, c)
                            },
                            label = { Text("Measured Diagonal Hypotenuse C (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Results
                        Surface(
                            color = if (state.isSquare90) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (state.isSquare90) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (state.isSquare90) "PERFECT TRUE 90° SQUARE CORNER" else "OFF-SQUARE CORNER DETECTED",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (state.isSquare90) Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                                Text(
                                    text = "Ideal Hypotenuse: ${String.format("%.3f", state.idealDiagonalC)} m",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Squareness Deviation: ${String.format("%.1f", state.cornerErrorMm)} mm off true 90°",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (state.isSquare90) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
