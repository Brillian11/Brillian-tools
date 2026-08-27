package com.example.ui.screens.sensors

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
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.North
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.woodworking.ResultBadge

@Composable
fun BarometricAltimeterScreen(
    viewModel: BarometricAltimeterViewModel,
    modifier: Modifier = Modifier
) {
    val isSensorAvailable by viewModel.isSensorAvailable.collectAsState()
    val seaLevelQnhHpa by viewModel.seaLevelQnhHpa.collectAsState()
    val currentPressureHpa by viewModel.currentPressureHpa.collectAsState()
    val absoluteAltitudeM by viewModel.absoluteAltitudeM.collectAsState()
    val tareAltitudeM by viewModel.tareAltitudeM.collectAsState()
    val deltaAltitudeM by viewModel.deltaAltitudeM.collectAsState()
    val isFeet by viewModel.isFeet.collectAsState()
    val stations by viewModel.stations.collectAsState()
    val pressureTrend by viewModel.pressureTrend.collectAsState()
    val lastLogSaved by viewModel.lastLogSaved.collectAsState()

    var stationNameInput by remember { mutableStateOf("") }
    var surveyTitle by remember { mutableStateOf("Foundation to Roof Line Survey") }

    val primaryColor = Color(0xFF0284C7)

    fun formatElevation(meters: Float): String {
        return if (isFeet) {
            String.format("%.2f ft", meters * 3.28084f)
        } else {
            String.format("%.2f m", meters)
        }
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Card(
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Landscape, contentDescription = null, tint = primaryColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "BAROMETRIC ALTIMETER & DELTA",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                                color = primaryColor
                            )
                        }
                        OutlinedButton(
                            onClick = { viewModel.toggleUnit() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isFeet) "Unit: Feet" else "Unit: Meters", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sub-decimeter hypsometric elevation tracking, station survey logging, and benchmark tare delta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Benchmark Delta Elevation Big Display
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VERTICAL DELTA FROM BENCHMARK (Δh)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${if (deltaAltitudeM >= 0) "+" else ""}${formatElevation(deltaAltitudeM)}",
                        color = if (deltaAltitudeM >= 0) Color(0xFF34D399) else Color(0xFFF87171),
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ABSOLUTE (MSL)", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(formatElevation(absoluteAltitudeM), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PRESSURE", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.2f hPa", currentPressureHpa), color = Color(0xFF67E8F9), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("QNH BASE", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            Text(String.format("%.1f hPa", seaLevelQnhHpa), color = Color(0xFFFDE047), fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // Zero / Tare Benchmark Action Button
            Button(
                onClick = { viewModel.tareBenchmarkZero() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("tare_benchmark_zero")
            ) {
                Icon(Icons.Default.Landscape, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ZERO / TARE BENCHMARK (SET 0.00 M)", fontWeight = FontWeight.Bold)
            }

            // Barometric Trend Advisory Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(pressureTrend.colorHex).copy(alpha = 0.12f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Air, contentDescription = null, tint = Color(pressureTrend.colorHex), modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(pressureTrend.label, fontWeight = FontWeight.Bold, color = Color(pressureTrend.colorHex))
                        Text(pressureTrend.advisory, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Station Survey Recording
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Survey Stations Elevation Log", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = stationNameInput,
                            onValueChange = { stationNameInput = it },
                            placeholder = { Text("e.g., Station #1: Floor Slab") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                viewModel.recordStation(stationNameInput)
                                stationNameInput = ""
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AddLocation, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Log")
                        }
                    }

                    if (stations.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            stations.forEach { st ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(st.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text(
                                            text = "${if (st.deltaAltitudeM >= 0) "+" else ""}${formatElevation(st.deltaAltitudeM)}",
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace,
                                            color = if (st.deltaAltitudeM >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.clearStations() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear Recorded Stations")
                            }
                        }
                    }
                }
            }

            // QNH Pressure Calibration Slider
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sea-Level QNH Reference: ${String.format("%.1f hPa", seaLevelQnhHpa)}", fontWeight = FontWeight.Bold)
                        Text(String.format("%.2f inHg", seaLevelQnhHpa * 0.02953f), fontSize = 12.sp, color = primaryColor)
                    }
                    Slider(
                        value = seaLevelQnhHpa,
                        onValueChange = { viewModel.setSeaLevelQnh(it) },
                        valueRange = 980f..1040f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Calibrate using local airport METAR or weather station pressure for absolute accuracy.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Save Elevation Survey Log
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Save Elevation Survey Audit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = surveyTitle,
                        onValueChange = { surveyTitle = it },
                        label = { Text("Survey Project Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { viewModel.saveElevationLog(surveyTitle) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(if (lastLogSaved) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (lastLogSaved) "Survey Log Saved to Database" else "Save Elevation Survey to Database")
                    }
                }
            }
        }
    }
}
