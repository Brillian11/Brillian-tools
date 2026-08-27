package com.example.ui.screens.sensors

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Undo
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
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
import com.example.ui.utils.ToolIconMapper
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArAreaCalculatorScreen(
    viewModel: ArAreaCalculatorViewModel,
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

    val state by viewModel.state.collectAsState()
    var isFullScreen by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isFullScreen) {
            // FULL SCREEN CAMERA VIEW WITH FLOATING HUD OVERLAY
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

                // AR Dynamic Tracking Canvas Overlay
                ArTrackingCanvas(
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
                                text = "ENCLOSED AREA: ${if (state.isMetric) "%.2f m²".format(state.areaSquareMeters) else "%.1f ft²".format(state.areaSquareFeet)}",
                                color = Color(0xFF34D399),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Perimeter: ${if (state.isMetric) "%.2f m".format(state.perimeterMeters) else "%.1f ft".format(state.perimeterFeet)} | ${state.points.size} Nodes",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        IconButton(onClick = { isFullScreen = false }) {
                            Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Fullscreen", tint = Color.White)
                        }
                    }
                }

                // Bottom Floating Control Bar
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
                            onClick = { viewModel.addPointAtReticle() },
                            modifier = Modifier.weight(1.4f).testTag("add_ar_node_fullscreen"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Node", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.removeLastPoint() },
                            enabled = state.points.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, tint = Color.White)
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearAllPoints() },
                            enabled = state.points.isNotEmpty(),
                            modifier = Modifier.weight(1f)
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
                // Header Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.SquareFoot, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AR AREA CALCULATOR & OBJECT TRACKER",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.1.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Camera Surface & Plot Area Solver",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        IconButton(onClick = { isFullScreen = true }) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Full Screen Camera")
                        }
                    }
                }

                // Results Readout Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Enclosed Area", style = MaterialTheme.typography.labelMedium, color = Color(0xFF047857))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isMetric) "%.2f m²".format(state.areaSquareMeters) else "%.1f ft²".format(state.areaSquareFeet),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF064E3B)
                            )
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Perimeter Length", style = MaterialTheme.typography.labelMedium, color = Color(0xFF1D4ED8))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (state.isMetric) "%.2f m".format(state.perimeterMeters) else "%.1f ft".format(state.perimeterFeet),
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }
                }

                // AR Viewfinder Card
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

                        // AR Canvas Overlay
                        ArTrackingCanvas(
                            state = state,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Fullscreen Toggle Button on Top Right
                        IconButton(
                            onClick = { isFullScreen = true },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Full Screen", tint = Color.White)
                        }
                    }
                }

                // Action Control Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.addPointAtReticle() },
                        modifier = Modifier.weight(1.4f).testTag("add_ar_node_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Node", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.removeLastPoint() },
                        enabled = state.points.isNotEmpty(),
                        modifier = Modifier.weight(1f).testTag("undo_ar_node_button")
                    ) {
                        Icon(imageVector = Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Undo")
                    }

                    OutlinedButton(
                        onClick = { viewModel.clearAllPoints() },
                        enabled = state.points.isNotEmpty(),
                        modifier = Modifier.weight(1f).testTag("clear_ar_nodes_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }

                // Setup Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Eye Hold Height (h₀): %.2f m".format(state.eyeHeightMeters), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            OutlinedButton(onClick = { viewModel.toggleUnitSystem() }) {
                                Text(if (state.isMetric) "Metric (m²)" else "Imperial (ft²)")
                            }
                        }

                        Slider(
                            value = state.eyeHeightMeters.toFloat(),
                            onValueChange = { viewModel.setEyeHeight(it.toDouble()) },
                            valueRange = 0.8f..2.5f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Paint Surface Overlay & Paint Requirement Estimator Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Paint Imaginary Surface Coating",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF78350F)
                            )
                            Text(
                                "%.2f L (%.2f gal)".format(state.requiredPaintLiters, state.requiredPaintGallons),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF92400E)
                            )
                        }

                        Text("Select Imaginary Overlay Color:", style = MaterialTheme.typography.labelMedium, color = Color(0xFF92400E))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ArPaintColor.entries.forEach { paint ->
                                val isSelected = state.selectedPaintColor == paint
                                Surface(
                                    onClick = { viewModel.updatePaintColor(paint) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(paint.argbHex or 0xFF000000),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, Color.Black) else null,
                                    modifier = Modifier.size(36.dp)
                                ) {}
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Coating Layers:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF78350F))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(1, 2, 3).forEach { c ->
                                    Button(
                                        onClick = { viewModel.updatePaintCoats(c) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (state.paintCoats == c) Color(0xFFD97706) else Color(0xFFFDE68A),
                                            contentColor = if (state.paintCoats == c) Color.White else Color(0xFF78350F)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("$c Coat${if (c > 1) "s" else ""}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArTrackingCanvas(
    state: ArAreaState,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        // Reticle Center Target
        drawCircle(color = Color(0x88000000), radius = 28.dp.toPx(), center = Offset(centerX, centerY))
        drawCircle(color = Color(0xFF10B981), radius = 22.dp.toPx(), center = Offset(centerX, centerY), style = Stroke(width = 3.dp.toPx()))
        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(centerX, centerY))

        // Crosshair lines
        drawLine(color = Color(0xFF10B981), start = Offset(centerX - 30.dp.toPx(), centerY), end = Offset(centerX + 30.dp.toPx(), centerY), strokeWidth = 2.dp.toPx())
        drawLine(color = Color(0xFF10B981), start = Offset(centerX, centerY - 30.dp.toPx()), end = Offset(centerX, centerY + 30.dp.toPx()), strokeWidth = 2.dp.toPx())

        // Render AR points dynamically transformed by current device gyro/yaw
        val points = state.points
        if (points.isNotEmpty()) {
            val curYawRad = Math.toRadians(state.currentYawDegrees)

            val canvasPoints = points.map { pt ->
                // Calculate angle and distance of point relative to current camera orientation
                val ptAngleRad = kotlin.math.atan2(pt.xMeters, pt.yMeters)
                val relativeAngleRad = ptAngleRad - curYawRad

                val dist = pt.distanceMeters
                val scale = 50.dp.toPx()

                // Project point into current screen space
                val screenX = centerX + (dist * sin(relativeAngleRad) * scale).toFloat()
                val screenY = centerY - (dist * cos(relativeAngleRad) * scale).toFloat()

                Offset(screenX, screenY)
            }

            // 1. Draw translucent painted surface polygon fill if 3+ points
            if (canvasPoints.size >= 3) {
                val path = Path().apply {
                    moveTo(canvasPoints[0].x, canvasPoints[0].y)
                    for (i in 1 until canvasPoints.size) {
                        lineTo(canvasPoints[i].x, canvasPoints[i].y)
                    }
                    close()
                }
                drawPath(path = path, color = Color(state.selectedPaintColor.argbHex))
            }

            // 2. Draw connected perimeter lines with Yellow Dimension Pills attached to every edge
            val nativeCanvas = drawContext.canvas.nativeCanvas
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 11.dp.toPx()
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                textAlign = android.graphics.Paint.Align.CENTER
            }

            for (i in 0 until canvasPoints.size) {
                val p1 = canvasPoints[i]
                val p2 = if (canvasPoints.size >= 3) {
                    canvasPoints[(i + 1) % canvasPoints.size]
                } else if (i < canvasPoints.size - 1) {
                    canvasPoints[i + 1]
                } else null

                if (p2 != null) {
                    val endPt = p2
                    // Line
                    drawLine(
                        color = Color.White,
                        start = p1,
                        end = endPt,
                        strokeWidth = 4.dp.toPx()
                    )

                    // Edge segment length calculation in meters
                    val origP1 = points[i]
                    val origP2 = if (points.size >= 3) points[(i + 1) % points.size] else points[i + 1]
                    val dx = origP2.xMeters - origP1.xMeters
                    val dy = origP2.yMeters - origP1.yMeters
                    val edgeDistMeters = kotlin.math.sqrt(dx * dx + dy * dy)

                    val totFeet = edgeDistMeters * 3.28084
                    val ftPart = kotlin.math.floor(totFeet).toInt()
                    val inPart = kotlin.math.round((totFeet - ftPart) * 12).toInt()
                    val dimensionLabel = "%.2f m (%d'%d\")".format(edgeDistMeters, ftPart, inPart)

                    // Capsule Pill Badge at midpoint
                    val midX = (p1.x + endPt.x) / 2f
                    val midY = (p1.y + endPt.y) / 2f

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

                    nativeCanvas.drawText(
                        dimensionLabel,
                        midX,
                        midY + 4.dp.toPx(),
                        textPaint
                    )
                }
            }

            // 3. Draw Node Reticle Markers (White ring with 4 direction tick arrows & crosshair center ⊕)
            canvasPoints.forEach { pt ->
                // Outer white ring
                drawCircle(color = Color.White, radius = 18.dp.toPx(), center = pt, style = Stroke(width = 3.dp.toPx()))
                drawCircle(color = Color(0x66000000), radius = 18.dp.toPx(), center = pt)

                // 4 compass direction ticks
                val tickLen = 6.dp.toPx()
                val r = 18.dp.toPx()
                drawLine(color = Color.White, start = Offset(pt.x - r - tickLen, pt.y), end = Offset(pt.x - r, pt.y), strokeWidth = 2.dp.toPx())
                drawLine(color = Color.White, start = Offset(pt.x + r, pt.y), end = Offset(pt.x + r + tickLen, pt.y), strokeWidth = 2.dp.toPx())
                drawLine(color = Color.White, start = Offset(pt.x, pt.y - r - tickLen), end = Offset(pt.x, pt.y - r), strokeWidth = 2.dp.toPx())
                drawLine(color = Color.White, start = Offset(pt.x, pt.y + r), end = Offset(pt.x, pt.y + r + tickLen), strokeWidth = 2.dp.toPx())

                // Inner crosshair ⊕
                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
            }
        }
    }
}
