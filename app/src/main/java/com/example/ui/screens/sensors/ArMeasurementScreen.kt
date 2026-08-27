package com.example.ui.screens.sensors

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.screens.woodworking.ResultBadge

@Composable
fun ArMeasurementScreen(
    viewModel: ArMeasurementViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        viewModel.startSensors()
        onDispose { viewModel.stopSensors() }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    val state by viewModel.arState.collectAsState()
    var isFullScreen by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isFullScreen) {
            // FULL SCREEN CAMERA VIEW
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // AR Point Tracking Canvas
                ArMeasurementTrackingCanvas(
                    state = state,
                    modifier = Modifier.fillMaxSize()
                )

                // Top Floating Readout Header
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xEE0F172A))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "CALCULATED DISTANCE: %.2f m".format(state.calculatedDistanceMeters),
                                color = Color(0xFF60A5FA),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Total Height: %.2f m | Target Pitch: %.1f°".format(state.calculatedHeightMeters, state.currentPitchDeg),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton(onClick = { isFullScreen = false }) {
                            Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", tint = Color.White)
                        }
                    }
                }

                // Bottom Floating Action Bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xEE0F172A))
                        .padding(16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { viewModel.lockBaseAngle() },
                            modifier = Modifier.weight(1f).testTag("lock_base_angle_fullscreen"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                        ) {
                            Text("Lock Base Angle", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { viewModel.lockTopAngle() },
                            modifier = Modifier.weight(1f).testTag("lock_top_angle_fullscreen"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                        ) {
                            Text("Lock Top Angle", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetMeasurement() },
                            modifier = Modifier.weight(0.8f)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }
        } else {
            // COMPACT SCROLLABLE VIEW
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AR TAPE MEASURE & RANGEFINDER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Point-to-Point Distance & Height Solver",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(onClick = { isFullScreen = true }) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Full Screen Camera")
                        }
                    }
                }

                // Measured Distance Badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ResultBadge(
                        title = "GROUND DISTANCE",
                        value = "%.2f m".format(state.calculatedDistanceMeters),
                        unit = "%.1f ft".format(state.distanceFeet),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    ResultBadge(
                        title = "TOTAL OBJECT HEIGHT",
                        value = "%.2f m".format(state.calculatedHeightMeters),
                        unit = "%.1f ft".format(state.heightFeet),
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // AR Viewfinder Container
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (hasCameraPermission) {
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx)
                                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                    cameraProviderFuture.addListener({
                                        val cameraProvider = cameraProviderFuture.get()
                                        val preview = Preview.Builder().build().also {
                                            it.setSurfaceProvider(previewView.surfaceProvider)
                                        }
                                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                        try {
                                            cameraProvider.unbindAll()
                                            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                        } catch (_: Exception) {}
                                    }, ContextCompat.getMainExecutor(ctx))
                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // AR Measurement Overlay
                        ArMeasurementTrackingCanvas(
                            state = state,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Fullscreen Toggle
                        IconButton(
                            onClick = { isFullScreen = true },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Full Screen", tint = Color.White)
                        }
                    }
                }

                // Controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.lockBaseAngle() },
                        modifier = Modifier.weight(1f).testTag("lock_base_angle"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Lock Base Angle", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.lockTopAngle() },
                        modifier = Modifier.weight(1f).testTag("lock_top_angle"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Text("Lock Top Angle", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.resetMeasurement() },
                        modifier = Modifier.weight(0.8f).testTag("reset_ar_measurement")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }

                // Eye Height Adjustment
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Camera Eye Height: %.2f m".format(state.eyeHeightMeters), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Slider(
                            value = state.eyeHeightMeters.toFloat(),
                            onValueChange = { viewModel.setEyeHeight(it.toDouble()) },
                            valueRange = 0.8f..2.5f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArMeasurementTrackingCanvas(
    state: ArMeasurementData,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Center reticle
        drawCircle(color = Color(0x66000000), radius = 28.dp.toPx(), center = Offset(centerX, centerY))
        drawCircle(color = Color(0xFF3B82F6), radius = 22.dp.toPx(), center = Offset(centerX, centerY), style = Stroke(width = 3.dp.toPx()))
        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(centerX, centerY))

        // Center horizon reference pitch indicator
        val pitchOffset = (state.currentPitchDeg / 45f) * (size.height / 2f)
        val currentTargetY = centerY - pitchOffset

        drawLine(
            color = Color(0xAA60A5FA),
            start = Offset(40f, currentTargetY),
            end = Offset(size.width - 40f, currentTargetY),
            strokeWidth = 2f
        )

        // Calculate Y positions for Base and Top locked angles
        val baseY = state.lockedBasePitchDeg?.let { basePitch ->
            val pOff = (basePitch / 45f) * (size.height / 2f)
            centerY - pOff
        }

        val topY = state.lockedTopPitchDeg?.let { topPitch ->
            val pOff = (topPitch / 45f) * (size.height / 2f)
            centerY - pOff
        }

        val startPt = if (baseY != null) Offset(centerX, baseY) else null
        val endPt = if (topY != null) Offset(centerX, topY) else Offset(centerX, currentTargetY)

        if (startPt != null) {
            // Draw vertical measurement line
            drawLine(
                color = Color.White,
                start = startPt,
                end = endPt,
                strokeWidth = 4.dp.toPx()
            )

            // Calculate distance in meters & dual units
            val distMeters = if (state.calculatedHeightMeters > 0) state.calculatedHeightMeters else state.calculatedDistanceMeters
            val totFeet = distMeters * 3.28084
            val ftPart = kotlin.math.floor(totFeet).toInt()
            val inPart = kotlin.math.round((totFeet - ftPart) * 12).toInt()
            val dimensionLabel = "%.2f m (%d'%d\")".format(distMeters, ftPart, inPart)

            // Yellow Capsule Badge at line midpoint
            val midX = centerX
            val midY = (startPt.y + endPt.y) / 2f

            val pillWidth = 115.dp.toPx()
            val pillHeight = 26.dp.toPx()
            val topLeft = Offset(midX - pillWidth / 2f, midY - pillHeight / 2f)

            drawRoundRect(
                color = Color(0xFFFFD600), // Bright Yellow Capsule Pill
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(pillWidth, pillHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(13.dp.toPx())
            )
            drawRoundRect(
                color = Color.Black,
                topLeft = topLeft,
                size = androidx.compose.ui.geometry.Size(pillWidth, pillHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(13.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )

            val nativeCanvas = drawContext.canvas.nativeCanvas
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 11.dp.toPx()
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                textAlign = android.graphics.Paint.Align.CENTER
            }

            nativeCanvas.drawText(
                dimensionLabel,
                midX,
                midY + 4.dp.toPx(),
                textPaint
            )

            // Draw Base Reticle Node
            val r = 18.dp.toPx()
            val tickLen = 6.dp.toPx()
            drawCircle(color = Color.White, radius = r, center = startPt, style = Stroke(width = 3.dp.toPx()))
            drawLine(color = Color.White, start = Offset(startPt.x - r - tickLen, startPt.y), end = Offset(startPt.x - r, startPt.y), strokeWidth = 2.dp.toPx())
            drawLine(color = Color.White, start = Offset(startPt.x + r, startPt.y), end = Offset(startPt.x + r + tickLen, startPt.y), strokeWidth = 2.dp.toPx())
            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = startPt)

            // Draw Top Reticle Node if locked
            if (topY != null) {
                drawCircle(color = Color.White, radius = r, center = endPt, style = Stroke(width = 3.dp.toPx()))
                drawLine(color = Color.White, start = Offset(endPt.x - r - tickLen, endPt.y), end = Offset(endPt.x - r, endPt.y), strokeWidth = 2.dp.toPx())
                drawLine(color = Color.White, start = Offset(endPt.x + r, endPt.y), end = Offset(endPt.x + r + tickLen, endPt.y), strokeWidth = 2.dp.toPx())
                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = endPt)
            }
        }
    }
}
