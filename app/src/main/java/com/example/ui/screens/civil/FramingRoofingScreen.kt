package com.example.ui.screens.civil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.math.StudSpacingOption
import com.example.ui.screens.woodworking.ResultBadge

@Composable
fun FramingRoofingScreen(
    viewModel: FramingRoofingViewModel,
    modifier: Modifier = Modifier
) {
    val wallLength by viewModel.wallLength.collectAsState()
    val wallHeight by viewModel.wallHeight.collectAsState()
    val studSpacing by viewModel.studSpacing.collectAsState()
    val doors by viewModel.doors.collectAsState()
    val windows by viewModel.windows.collectAsState()
    val roofRun by viewModel.roofRun.collectAsState()
    val roofRise by viewModel.roofRise.collectAsState()
    val roofOverhang by viewModel.roofOverhang.collectAsState()
    val roofLength by viewModel.roofLength.collectAsState()
    val wallResult by viewModel.wallResult.collectAsState()
    val roofResult by viewModel.roofResult.collectAsState()

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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "FRAMING, DRYWALL & ROOFING ESTIMATOR",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Stud Layout, Drywall & Roof Pitch Calculator",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Metric & Imperial wall framing estimator (studs, top/bottom plates, headers), 1.2m x 2.4m drywall sheet count, and roof rafter pitch angle.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // Wall Framing Section
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
                        text = "Wall Framing & Openings",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = wallLength,
                            onValueChange = { viewModel.updateWallLength(it) },
                            label = { Text("Wall Length (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_framing_wall_len")
                        )
                        OutlinedTextField(
                            value = wallHeight,
                            onValueChange = { viewModel.updateWallHeight(it) },
                            label = { Text("Wall Height (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_framing_wall_height")
                        )
                    }

                    // Stud Spacing Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = studSpacing == StudSpacingOption.SPACING_40CM,
                            onClick = { viewModel.updateStudSpacing(StudSpacingOption.SPACING_40CM) },
                            label = { Text("400 mm (16\") On-Center") }
                        )
                        FilterChip(
                            selected = studSpacing == StudSpacingOption.SPACING_60CM,
                            onClick = { viewModel.updateStudSpacing(StudSpacingOption.SPACING_60CM) },
                            label = { Text("600 mm (24\") On-Center") }
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = doors,
                            onValueChange = { viewModel.updateDoors(it) },
                            label = { Text("Door Openings") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_framing_doors")
                        )
                        OutlinedTextField(
                            value = windows,
                            onValueChange = { viewModel.updateWindows(it) },
                            label = { Text("Window Openings") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_framing_windows")
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ResultBadge(
                            title = "TOTAL STUDS NEEDED",
                            value = "${wallResult.totalStuds}",
                            unit = "vertical studs",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        ResultBadge(
                            title = "DRYWALL 1.2x2.4m SHEETS",
                            value = "${wallResult.drywallSheetsMetric12x24Count}",
                            unit = "sheets (${String.format("%.1f", wallResult.totalWallAreaM2 * 2)} m² total)",
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Roofing Pitch Section
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Roof Pitch & Rafters",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = roofRun,
                            onValueChange = { viewModel.updateRoofRun(it) },
                            label = { Text("Run (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_roof_run")
                        )
                        OutlinedTextField(
                            value = roofRise,
                            onValueChange = { viewModel.updateRoofRise(it) },
                            label = { Text("Rise (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_roof_rise")
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ResultBadge(
                            title = "RAFTER LENGTH",
                            value = String.format("%.2f m", roofResult.rafterLengthMeters),
                            unit = "${roofResult.totalRaftersCount} rafters total",
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                        ResultBadge(
                            title = "ROOF PITCH & SLOPE",
                            value = "${String.format("%.1f", roofResult.pitchAngleDegrees)}°",
                            unit = "${String.format("%.1f", roofResult.pitchSlopePercentage)}% grade (${String.format("%.1f", roofResult.pitchRatioInches)}/12)",
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
