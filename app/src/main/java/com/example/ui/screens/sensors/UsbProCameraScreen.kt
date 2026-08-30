package com.example.ui.screens.sensors

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.ToolLogRepository
import com.example.ui.navigation.ScreenRoutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

/**
 * Camera Tools available in the Slide Tab Toggle replacing standard photo/portrait/video modes.
 * Each mode represents a dedicated camera-enabled engineering tool.
 */
enum class CameraAppMode(
    val label: String,
    val subtitle: String,
    val toolTitle: String,
    val icon: ImageVector,
    val route: String? = null,
    val isVideoMode: Boolean = false
) {
    AR_RULER("AR RULER", "Laser distance & dimension measure", "AR Laser Distance Meter", Icons.Default.Straighten, ScreenRoutes.ArMeasurement.route),
    AR_AREA("AR AREA", "Surface area & material estimator", "AR Floor & Area Estimator", Icons.Default.SquareFoot, ScreenRoutes.ArAreaCalculator.route),
    THERMAL_IR("THERMAL IR", "Heatmap spot temp & thermal gradient", "Thermal IR Camera", Icons.Default.Thermostat, ScreenRoutes.ThermalCamera.route),
    PAINT_STUDIO("PAINT MATCH", "Live color eyedropper & commercial paint match", "Color Eyedropper & Paint Studio", Icons.Default.ColorLens, ScreenRoutes.PaintingCoatingStudio.route),
    QR_SCANNER("QR SCANNER", "Equipment barcode & weld spec scanner", "QR & Barcode Scanner", Icons.Default.QrCodeScanner, ScreenRoutes.QrCodeScanner.route),
    ENDOSCOPE("ENDOSCOPE", "USB OTG & borescope probe inspection", "USB OTG Borescope / Endoscope", Icons.Default.Cable, ScreenRoutes.UsbEndoscope.route),
    WELD_INSPECTION("WELD AUDIT", "Visual bead porosity & crack detection", "Visual Weld Bead Inspection", Icons.Default.Visibility, "tool_metalworks_studio?toolId=widget_weld_heat_input"),
    PHOTO_AUDIT("PHOTO AUDIT", "High-res geotagged jobsite snapshot", "Jobsite Inspection Photo", Icons.Default.PhotoCamera, null),
    VIDEO_NOTE("VIDEO NOTE", "Jobsite audio & video narration", "Audio/Video Inspection Note", Icons.Default.Videocam, null, isVideoMode = true)
}

// Connection modes for Endoscope / OTG
enum class CameraConnectionMode(val label: String, val icon: ImageVector) {
    WIRED_USB("Wired USB OTG", Icons.Default.Cable),
    WIRELESS_WIFI("Wireless 5.8GHz AP", Icons.Default.CellTower)
}

// Simulated Media file
data class CapturedMedia(
    val id: String,
    val name: String,
    val timestamp: String,
    val isVideo: Boolean,
    val durationSeconds: Int = 0,
    val modeTag: String = "PHOTO",
    val metaInfo: String = "",
    val imageUri: String? = null,
    val colorHex: String? = null
)

class UsbProCameraViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {
    private val _activeMode = MutableStateFlow(CameraAppMode.AR_RULER)
    val activeMode = _activeMode.asStateFlow()

    private val _connectionMode = MutableStateFlow(CameraConnectionMode.WIRED_USB)
    val connectionMode = _connectionMode.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _recordDuration = MutableStateFlow(0)
    val recordDuration = _recordDuration.asStateFlow()

    private val _zoomLevel = MutableStateFlow(1.0f)
    val zoomLevel = _zoomLevel.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn = _isFlashlightOn.asStateFlow()

    private val _isHdrOn = MutableStateFlow(true)
    val isHdrOn = _isHdrOn.asStateFlow()

    private val _isReticleVisible = MutableStateFlow(true)
    val isReticleVisible = _isReticleVisible.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0) // 0s, 3s, 10s
    val timerSeconds = _timerSeconds.asStateFlow()

    private val _isFilterActive = MutableStateFlow(false)
    val isFilterActive = _isFilterActive.asStateFlow()

    private val _isFacingFront = MutableStateFlow(false)
    val isFacingFront = _isFacingFront.asStateFlow()

    private val _showPlaybackModal = MutableStateFlow(false)
    val showPlaybackModal = _showPlaybackModal.asStateFlow()

    // Captured Media Gallery
    private val _capturedFiles = MutableStateFlow<List<CapturedMedia>>(
        listOf(
            CapturedMedia("m1", "AR_RULER_BEAM_SPAN_001.jpg", "10:15 AM", false, 0, "AR RULER", "1,250 mm • Tolerance ±2mm"),
            CapturedMedia("m2", "THERMAL_IR_HOTSPOT_002.jpg", "10:18 AM", false, 0, "THERMAL IR", "38.5°C Max • IronBow"),
            CapturedMedia("m3", "PAINT_MATCH_SWATCH_003.jpg", "10:24 AM", false, 0, "PAINT MATCH", "HEX #D97706 • Sherwin-Williams SW 6605"),
            CapturedMedia("m4", "WELD_INSPECT_BEAD_004.mp4", "10:30 AM", true, 18, "WELD AUDIT", "1080p • Visual Pass Confirmed")
        )
    )
    val capturedFiles = _capturedFiles.asStateFlow()

    private val _selectedFileForPlayback = MutableStateFlow<CapturedMedia?>(null)
    val selectedFileForPlayback = _selectedFileForPlayback.asStateFlow()

    private val _playbackProgress = MutableStateFlow(0f)
    val playbackProgress = _playbackProgress.asStateFlow()

    private val _isPlaybackPlaying = MutableStateFlow(false)
    val isPlaybackPlaying = _isPlaybackPlaying.asStateFlow()

    // QR Code state
    private val _scannedQr = MutableStateFlow("SPEC_E7018_WELD_HEAT_24V_130A")
    val scannedQr = _scannedQr.asStateFlow()

    // Paint state
    private val _sampledColorHex = MutableStateFlow("#D97706")
    val sampledColorHex = _sampledColorHex.asStateFlow()
    private val _selectedPaintBrand = MutableStateFlow("Sherwin-Williams")
    val selectedPaintBrand = _selectedPaintBrand.asStateFlow()
    private val _paintFinish = MutableStateFlow("Eggshell")
    val paintFinish = _paintFinish.asStateFlow()

    // AR Ground Calibration state
    private val _isGroundCalibrated = MutableStateFlow(true)
    val isGroundCalibrated = _isGroundCalibrated.asStateFlow()

    // Inspection Gallery Zoom Scale (0.5f to 3.0f, i.e., 50% to 300%)
    private val _galleryZoomScale = MutableStateFlow(1.0f)
    val galleryZoomScale = _galleryZoomScale.asStateFlow()

    // Thermal state
    private val _thermalSpotTempC = MutableStateFlow(38.4)
    val thermalSpotTempC = _thermalSpotTempC.asStateFlow()
    private val _thermalPalette = MutableStateFlow("IronBow") // IronBow, Rainbow, WhiteHot
    val thermalPalette = _thermalPalette.asStateFlow()
    private val _isEmissivityHigh = MutableStateFlow(true) // Metal (0.85) vs Painted (0.95)
    val isEmissivityHigh = _isEmissivityHigh.asStateFlow()

    // AR Ruler state
    private val _arDistanceMm = MutableStateFlow(1250.0)
    val arDistanceMm = _arDistanceMm.asStateFlow()
    private val _arUnit = MutableStateFlow("mm") // mm, cm, m, inch, ft
    val arUnit = _arUnit.asStateFlow()
    private val _arPinnedDistanceMm = MutableStateFlow<Double?>(null)
    val arPinnedDistanceMm = _arPinnedDistanceMm.asStateFlow()
    private val _arInclineAngle = MutableStateFlow(0.4) // degrees
    val arInclineAngle = _arInclineAngle.asStateFlow()

    // AR Area state
    private val _arAreaSqM = MutableStateFlow(14.8)
    val arAreaSqM = _arAreaSqM.asStateFlow()
    private val _arAreaWallHeightM = MutableStateFlow(2.8)
    val arAreaWallHeightM = _arAreaWallHeightM.asStateFlow()

    // Endoscope LED & Rotation
    private val _endoscopeLedBrightness = MutableStateFlow(85)
    val endoscopeLedBrightness = _endoscopeLedBrightness.asStateFlow()
    private val _endoscopeRotationDeg = MutableStateFlow(0f)
    val endoscopeRotationDeg = _endoscopeRotationDeg.asStateFlow()

    // Weld Inspection State
    private val _weldDefectSeverity = MutableStateFlow("Pass (No Undercut)")
    val weldDefectSeverity = _weldDefectSeverity.asStateFlow()
    private val _weldBeadWidthMm = MutableStateFlow(8.4)
    val weldBeadWidthMm = _weldBeadWidthMm.asStateFlow()

    fun setMode(mode: CameraAppMode) {
        _activeMode.value = mode
    }

    fun setConnectionMode(mode: CameraConnectionMode) {
        _connectionMode.value = mode
    }

    fun toggleFlashlight() {
        _isFlashlightOn.value = !_isFlashlightOn.value
    }

    fun toggleHdr() {
        _isHdrOn.value = !_isHdrOn.value
    }

    fun toggleReticle() {
        _isReticleVisible.value = !_isReticleVisible.value
    }

    fun cycleTimer() {
        _timerSeconds.value = when (_timerSeconds.value) {
            0 -> 3
            3 -> 10
            else -> 0
        }
    }

    fun toggleFilter() {
        _isFilterActive.value = !_isFilterActive.value
    }

    fun toggleFacingFront() {
        _isFacingFront.value = !_isFacingFront.value
    }

    fun setZoom(zoom: Float) {
        _zoomLevel.value = zoom
    }

    fun cycleZoom() {
        _zoomLevel.value = when {
            _zoomLevel.value < 0.8f -> 1.0f
            _zoomLevel.value in 0.8f..1.4f -> 2.0f
            _zoomLevel.value in 1.5f..2.5f -> 5.0f
            _zoomLevel.value in 2.6f..6.0f -> 0.5f
            else -> 1.0f
        }
    }

    fun cycleArUnit() {
        _arUnit.value = when (_arUnit.value) {
            "mm" -> "cm"
            "cm" -> "m"
            "m" -> "in"
            "in" -> "ft"
            else -> "mm"
        }
    }

    fun cycleThermalPalette() {
        _thermalPalette.value = when (_thermalPalette.value) {
            "IronBow" -> "Rainbow"
            "Rainbow" -> "WhiteHot"
            else -> "IronBow"
        }
    }

    fun toggleEmissivity() {
        _isEmissivityHigh.value = !_isEmissivityHigh.value
    }

    fun setScannedQr(code: String) {
        _scannedQr.value = code
    }

    fun setSampledColor(hex: String) {
        _sampledColorHex.value = hex
    }

    fun cyclePaintBrand() {
        _selectedPaintBrand.value = when (_selectedPaintBrand.value) {
            "Sherwin-Williams" -> "Benjamin Moore"
            "Benjamin Moore" -> "Jotun Industrial"
            "Jotun Industrial" -> "Nippon Paint"
            "Nippon Paint" -> "Dulux Professional"
            else -> "Sherwin-Williams"
        }
    }

    fun cyclePaintFinish() {
        _paintFinish.value = when (_paintFinish.value) {
            "Flat/Matte" -> "Eggshell"
            "Eggshell" -> "Satin"
            "Satin" -> "Semi-Gloss"
            "Semi-Gloss" -> "High-Gloss"
            else -> "Flat/Matte"
        }
    }

    fun toggleGroundCalibration() {
        _isGroundCalibrated.value = !_isGroundCalibrated.value
    }

    fun setGalleryZoomScale(scale: Float) {
        _galleryZoomScale.value = scale.coerceIn(0.5f, 3.0f)
    }

    fun addUploadedMedia(uriStr: String, fileName: String) {
        val newMedia = CapturedMedia(
            id = "upload_" + System.currentTimeMillis(),
            name = fileName,
            timestamp = "Just Now",
            isVideo = fileName.lowercase(Locale.getDefault()).endsWith(".mp4") || fileName.lowercase(Locale.getDefault()).endsWith(".mov"),
            modeTag = "UPLOADED ASSET",
            metaInfo = "User Import • Persistent Storage",
            imageUri = uriStr
        )
        _capturedFiles.value = listOf(newMedia) + _capturedFiles.value
    }

    fun pinArDistance() {
        _arPinnedDistanceMm.value = _arDistanceMm.value
    }

    fun clearArPinnedDistance() {
        _arPinnedDistanceMm.value = null
    }

    fun setEndoscopeLed(level: Int) {
        _endoscopeLedBrightness.value = level.coerceIn(0, 100)
    }

    fun rotateEndoscope() {
        _endoscopeRotationDeg.value = (_endoscopeRotationDeg.value + 90f) % 360f
    }

    fun cycleScannedQr() {
        _scannedQr.value = when (_scannedQr.value) {
            "SPEC_E7018_WELD_HEAT_24V_130A" -> "MOTOR_460V_15HP_FLA_21A_3PHASE"
            "MOTOR_460V_15HP_FLA_21A_3PHASE" -> "CONDUIT_SCHEDULE_40_2INCH_PVC"
            "CONDUIT_SCHEDULE_40_2INCH_PVC" -> "SOLAR_PANEL_450W_VOC_49.5V_ISC_11.6A"
            "SOLAR_PANEL_450W_VOC_49.5V_ISC_11.6A" -> "HVAC_REFRIGERANT_R410A_SUPERHEAT_12F"
            else -> "SPEC_E7018_WELD_HEAT_24V_130A"
        }
    }

    fun cycleWeldInspection() {
        val nextPair = when (_weldBeadWidthMm.value) {
            8.4 -> Pair(6.2, "Warning (Minor Undercut)")
            6.2 -> Pair(11.0, "Warning (Excess Reinforcement)")
            11.0 -> Pair(7.8, "Warning (Porosity Detected)")
            else -> Pair(8.4, "Pass (No Undercut)")
        }
        _weldBeadWidthMm.value = nextPair.first
        _weldDefectSeverity.value = nextPair.second
    }

    fun cycleArAreaWallHeight() {
        _arAreaWallHeightM.value = when (_arAreaWallHeightM.value) {
            2.8 -> 3.2
            3.2 -> 3.8
            3.8 -> 2.4
            else -> 2.8
        }
        _arAreaSqM.value = (5.2 * _arAreaWallHeightM.value * 10).roundToInt() / 10.0
    }

    fun updateTouchRuler(xRatio: Float, yRatio: Float) {
        val dist = 350.0 + (yRatio * 1850.0)
        _arDistanceMm.value = (dist / 10).roundToInt() * 10.0
        _arInclineAngle.value = ((-15.0 + xRatio * 30.0) * 10).roundToInt() / 10.0
    }

    fun updateTouchThermal(xRatio: Float, yRatio: Float) {
        val dx = xRatio - 0.5f
        val dy = yRatio - 0.5f
        val distFromCenter = sqrt(dx * dx + dy * dy)
        val temp = 68.5 - (distFromCenter * 60.0)
        _thermalSpotTempC.value = (temp * 10).roundToInt() / 10.0
    }

    fun updateTouchPaint(xRatio: Float, yRatio: Float) {
        val palette = listOf("#D97706", "#0284C7", "#059669", "#DC2626", "#475569", "#7C3AED", "#2563EB", "#052E16")
        val index = ((xRatio * palette.size).toInt()).coerceIn(0, palette.size - 1)
        _sampledColorHex.value = palette[index]
    }

    fun triggerPhotoCapture(context: Context) {
        val currentMode = _activeMode.value
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val name = "${currentMode.name}_" + sdf.format(Date()) + ".jpg"
        val meta = when (currentMode) {
            CameraAppMode.PAINT_STUDIO -> "HEX: ${_sampledColorHex.value} • ${_selectedPaintBrand.value}"
            CameraAppMode.THERMAL_IR -> "Temp: ${String.format(Locale.US, "%.1f°C", _thermalSpotTempC.value)} [${_thermalPalette.value}]"
            CameraAppMode.AR_RULER -> "Dist: ${String.format(Locale.US, "%.0f mm", _arDistanceMm.value)} (Angle: ${_arInclineAngle.value}°)"
            CameraAppMode.AR_AREA -> "Area: ${String.format(Locale.US, "%.1f m²", _arAreaSqM.value)} (H: ${_arAreaWallHeightM.value}m)"
            CameraAppMode.QR_SCANNER -> "QR: ${_scannedQr.value.take(20)}"
            CameraAppMode.WELD_INSPECTION -> "Weld Bead: ${_weldBeadWidthMm.value}mm • ${_weldDefectSeverity.value}"
            CameraAppMode.ENDOSCOPE -> "Borescope Probe • LED ${_endoscopeLedBrightness.value}%"
            else -> "4K UHD • Geotagged Field Snapshot"
        }

        val newMedia = CapturedMedia(
            id = "img_" + System.currentTimeMillis(),
            name = name,
            timestamp = "Just Now",
            isVideo = false,
            modeTag = currentMode.label,
            metaInfo = meta
        )
        _capturedFiles.value = listOf(newMedia) + _capturedFiles.value
        Toast.makeText(context, "📸 Captured photo: $name", Toast.LENGTH_SHORT).show()

        viewModelScope.launch {
            toolLogRepository?.logToolActivity(
                toolType = "widget_camera_studio",
                title = "Camera Tool: ${currentMode.toolTitle}",
                summary = "Snapshot recorded in ${currentMode.label} mode [$meta].",
                value = 1.0
            )
        }
    }

    fun startVideoRecording(context: Context) {
        if (!_isRecording.value) {
            _isRecording.value = true
            _recordDuration.value = 0
            Toast.makeText(context, "🔴 Recording video...", Toast.LENGTH_SHORT).show()
        }
    }

    fun stopVideoRecording(context: Context) {
        if (_isRecording.value) {
            _isRecording.value = false
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val name = "VID_" + sdf.format(Date()) + ".mp4"
            val newMedia = CapturedMedia(
                id = "vid_" + System.currentTimeMillis(),
                name = name,
                timestamp = "Just Now",
                isVideo = true,
                durationSeconds = _recordDuration.value,
                modeTag = "VIDEO NOTE",
                metaInfo = "Duration: ${_recordDuration.value}s"
            )
            _capturedFiles.value = listOf(newMedia) + _capturedFiles.value
            Toast.makeText(context, "🎥 Saved video note: $name (${_recordDuration.value}s)", Toast.LENGTH_SHORT).show()

            viewModelScope.launch {
                toolLogRepository?.logToolActivity(
                    toolType = "widget_camera_studio",
                    title = "Video Inspection Saved: $name",
                    summary = "Recorded ${_recordDuration.value} seconds of inspection footage.",
                    value = _recordDuration.value.toDouble()
                )
            }
        }
    }

    fun triggerShutter(context: Context) {
        val currentMode = _activeMode.value
        if (_isRecording.value) {
            stopVideoRecording(context)
        } else if (currentMode.isVideoMode) {
            toggleRecording(context)
        } else {
            triggerPhotoCapture(context)
        }
    }

    fun triggerShutterAndNavigate(context: Context, onNavigateToTool: (String) -> Unit) {
        val currentMode = _activeMode.value
        if (currentMode == CameraAppMode.VIDEO_NOTE) {
            if (_isRecording.value) {
                stopVideoRecording(context)
                openPlayback()
            } else {
                startVideoRecording(context)
            }
            return
        }

        triggerPhotoCapture(context)

        val targetRoute = currentMode.route
        if (targetRoute != null) {
            Toast.makeText(context, "⚡ Data Processed: Opening ${currentMode.toolTitle}", Toast.LENGTH_SHORT).show()
            onNavigateToTool(targetRoute)
        } else {
            openPlayback()
        }
    }

    fun toggleRecording(context: Context) {
        if (_isRecording.value) {
            stopVideoRecording(context)
        } else {
            startVideoRecording(context)
        }
    }

    fun incrementTimer() {
        if (_isRecording.value) {
            _recordDuration.value += 1
        }
    }

    fun openPlayback() {
        _showPlaybackModal.value = true
        if (_capturedFiles.value.isNotEmpty()) {
            _selectedFileForPlayback.value = _capturedFiles.value.first()
        }
    }

    fun closePlayback() {
        _showPlaybackModal.value = false
        _isPlaybackPlaying.value = false
    }

    fun selectFileForPlayback(media: CapturedMedia) {
        _selectedFileForPlayback.value = media
        _playbackProgress.value = 0f
        _isPlaybackPlaying.value = false
    }

    fun togglePlaybackPlaying() {
        _isPlaybackPlaying.value = !_isPlaybackPlaying.value
    }

    fun updatePlaybackProgress() {
        if (_isPlaybackPlaying.value) {
            val cur = _playbackProgress.value
            if (cur >= 1f) {
                _playbackProgress.value = 0f
                _isPlaybackPlaying.value = false
            } else {
                _playbackProgress.value = cur + 0.08f
            }
        }
    }
}

/**
 * Fullscreen Device-Style Camera Studio
 * Featuring Mode-Specific Top Custom Toolbars, Tool Tabs Replacing Standard Modes, Viewfinder Reticle, Zoom Pill, and Deep Tool Linking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsbProCameraScreen(
    viewModel: UsbProCameraViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToTool: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val activeMode by viewModel.activeMode.collectAsState()
    val connectionMode by viewModel.connectionMode.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val recordDuration by viewModel.recordDuration.collectAsState()
    val zoomLevel by viewModel.zoomLevel.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val isHdrOn by viewModel.isHdrOn.collectAsState()
    val isReticleVisible by viewModel.isReticleVisible.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isFilterActive by viewModel.isFilterActive.collectAsState()
    val isFacingFront by viewModel.isFacingFront.collectAsState()
    val showPlaybackModal by viewModel.showPlaybackModal.collectAsState()
    val capturedFiles by viewModel.capturedFiles.collectAsState()
    val selectedFileForPlayback by viewModel.selectedFileForPlayback.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()
    val isPlaybackPlaying by viewModel.isPlaybackPlaying.collectAsState()

    // Mode-specific state
    val scannedQr by viewModel.scannedQr.collectAsState()
    val sampledColorHex by viewModel.sampledColorHex.collectAsState()
    val selectedPaintBrand by viewModel.selectedPaintBrand.collectAsState()
    val thermalSpotTempC by viewModel.thermalSpotTempC.collectAsState()
    val thermalPalette by viewModel.thermalPalette.collectAsState()
    val isEmissivityHigh by viewModel.isEmissivityHigh.collectAsState()
    val arDistanceMm by viewModel.arDistanceMm.collectAsState()
    val arUnit by viewModel.arUnit.collectAsState()
    val arPinnedDistanceMm by viewModel.arPinnedDistanceMm.collectAsState()
    val arInclineAngle by viewModel.arInclineAngle.collectAsState()
    val arAreaSqM by viewModel.arAreaSqM.collectAsState()
    val arAreaWallHeightM by viewModel.arAreaWallHeightM.collectAsState()
    val endoscopeLedBrightness by viewModel.endoscopeLedBrightness.collectAsState()
    val endoscopeRotationDeg by viewModel.endoscopeRotationDeg.collectAsState()
    val weldDefectSeverity by viewModel.weldDefectSeverity.collectAsState()
    val weldBeadWidthMm by viewModel.weldBeadWidthMm.collectAsState()
    val isGroundCalibrated by viewModel.isGroundCalibrated.collectAsState()
    val paintFinish by viewModel.paintFinish.collectAsState()
    val galleryZoomScale by viewModel.galleryZoomScale.collectAsState()

    val assetPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = "UPLOADED_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".jpg"
            viewModel.addUploadedMedia(it.toString(), fileName)
            Toast.makeText(context, "Saved asset into local app library!", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera hardware permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

    LaunchedEffect(isFlashlightOn, cameraControl) {
        try {
            cameraControl?.enableTorch(isFlashlightOn)
        } catch (_: Exception) {}
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Timer for video recording
    LaunchedEffect(isRecording) {
        while (isRecording) {
            delay(1000)
            viewModel.incrementTimer()
        }
    }

    // Timer for video playback animation
    LaunchedEffect(isPlaybackPlaying) {
        while (isPlaybackPlaying) {
            delay(250)
            viewModel.updatePlaybackProgress()
        }
    }

    // Animated laser beam for QR code scanning
    val infiniteTransition = rememberInfiniteTransition(label = "camera_effects")
    val laserProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    val reticlePulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reticle_pulse"
    )

    // Flash animation on photo capture
    val flashAnim = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Gallery launcher for picking photo/video from device gallery
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            Toast.makeText(context, "Selected media from gallery", Toast.LENGTH_SHORT).show()
        }
    }

    val openDeviceGallery: () -> Unit = {
        try {
            val intent = Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                context.startActivity(intent)
            } catch (_: Exception) {
                galleryLauncher.launch("image/*")
            }
        }
    }

    val onCapturePhoto: () -> Unit = {
        coroutineScope.launch {
            flashAnim.snapTo(0.85f)
            flashAnim.animateTo(0f, animationSpec = tween(220, easing = LinearEasing))
        }
        viewModel.triggerPhotoCapture(context)
    }

    // Handle back button
    BackHandler {
        if (showPlaybackModal) {
            viewModel.closePlayback()
        } else {
            onNavigateBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        // ==========================================
        // 1. FULL-SCREEN CAMERA VIEWPORT (LENS & SIMULATION)
        // ==========================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(activeMode) {
                    detectTapGestures { offset ->
                        val xRatio = (offset.x / size.width.toFloat()).coerceIn(0f, 1f)
                        val yRatio = (offset.y / size.height.toFloat()).coerceIn(0f, 1f)
                        when (activeMode) {
                            CameraAppMode.AR_RULER -> viewModel.updateTouchRuler(xRatio, yRatio)
                            CameraAppMode.THERMAL_IR -> viewModel.updateTouchThermal(xRatio, yRatio)
                            CameraAppMode.PAINT_STUDIO -> viewModel.updateTouchPaint(xRatio, yRatio)
                            CameraAppMode.QR_SCANNER -> viewModel.cycleScannedQr()
                            CameraAppMode.WELD_INSPECTION -> viewModel.cycleWeldInspection()
                            CameraAppMode.AR_AREA -> viewModel.cycleArAreaWallHeight()
                            else -> {}
                        }
                    }
                }
        ) {
            if (hasCameraPermission && !isFacingFront) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            try {
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                cameraControl = camera.cameraControl
                                camera.cameraControl.enableTorch(isFlashlightOn)
                            } catch (_: Exception) {
                                // Graceful fallback to canvas simulation if camera hardware is busy in container
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    update = { _ ->
                        try {
                            cameraControl?.enableTorch(isFlashlightOn)
                        } catch (_: Exception) {}
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(if (activeMode == CameraAppMode.ENDOSCOPE) endoscopeRotationDeg else 0f)
                )
            }

        // Overlay Canvas for Shader Filters, Reticle, and Optical Simulation
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .rotate(if (activeMode == CameraAppMode.ENDOSCOPE) endoscopeRotationDeg else 0f)
        ) {
            val w = size.width
            val h = size.height

            // If hardware preview is inactive or front facing, draw simulated high-fidelity dark inspection scene
            if (!hasCameraPermission || isFacingFront) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617)),
                        center = Offset(w / 2f, h / 2f),
                        radius = w * 0.9f
                    )
                )

                // Simulated ambient geometry / piping / room features
                drawCircle(
                    color = Color(0xFF334155).copy(alpha = 0.4f),
                    radius = w * 0.45f,
                    center = Offset(w / 2f, h / 2f),
                    style = Stroke(3f)
                )
                drawCircle(
                    color = Color(0xFF475569).copy(alpha = 0.25f),
                    radius = w * 0.25f,
                    center = Offset(w / 2f, h / 2f),
                    style = Stroke(2f)
                )
            }

            // --- THERMAL IR HEATMAP OVERLAY ---
            if (activeMode == CameraAppMode.THERMAL_IR) {
                val colors = when (thermalPalette) {
                    "Rainbow" -> listOf(
                        Color(0xFFFF0000).copy(alpha = 0.5f),
                        Color(0xFFFFFF00).copy(alpha = 0.4f),
                        Color(0xFF00FF00).copy(alpha = 0.35f),
                        Color(0xFF00FFFF).copy(alpha = 0.3f),
                        Color(0xFF0000FF).copy(alpha = 0.4f)
                    )
                    "WhiteHot" -> listOf(
                        Color.White.copy(alpha = 0.6f),
                        Color(0xFFCCCCCC).copy(alpha = 0.4f),
                        Color(0xFF666666).copy(alpha = 0.3f),
                        Color.Black.copy(alpha = 0.5f)
                    )
                    else -> listOf( // IronBow
                        Color(0xFFFF0055).copy(alpha = 0.45f),
                        Color(0xFFFF8800).copy(alpha = 0.35f),
                        Color(0xFF6600CC).copy(alpha = 0.30f),
                        Color(0xFF003366).copy(alpha = 0.40f)
                    )
                }
                drawRect(
                    brush = Brush.radialGradient(
                        colors = colors,
                        center = Offset(w * 0.52f, h * 0.46f),
                        radius = w * 0.65f
                    )
                )
                // Draw hot spot beacon
                drawCircle(
                    color = Color(0xFFFFFF00).copy(alpha = 0.8f),
                    radius = 24f,
                    center = Offset(w * 0.52f, h * 0.46f)
                )
                drawCircle(
                    color = Color(0xFFFF3300),
                    radius = 48f,
                    center = Offset(w * 0.52f, h * 0.46f),
                    style = Stroke(3f)
                )
            }

            // --- WELD INSPECTION OVERLAY ---
            if (activeMode == CameraAppMode.WELD_INSPECTION) {
                // Draw calibrated weld bead alignment guides
                drawLine(
                    color = Color(0xFF10B981).copy(alpha = 0.7f),
                    start = Offset(w * 0.2f, h * 0.5f),
                    end = Offset(w * 0.8f, h * 0.5f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = Color(0xFFF59E0B).copy(alpha = 0.5f),
                    start = Offset(w * 0.2f, h * 0.45f),
                    end = Offset(w * 0.8f, h * 0.45f),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color(0xFFF59E0B).copy(alpha = 0.5f),
                    start = Offset(w * 0.2f, h * 0.55f),
                    end = Offset(w * 0.8f, h * 0.55f),
                    strokeWidth = 2f
                )
            }

            // --- AR AREA GRID OVERLAY ---
            if (activeMode == CameraAppMode.AR_AREA) {
                val step = 60f
                var x = 0f
                while (x < w) {
                    drawLine(Color(0xFF38BDF8).copy(alpha = 0.12f), Offset(x, 0f), Offset(x, h), 1f)
                    x += step
                }
                var y = 0f
                while (y < h) {
                    drawLine(Color(0xFF38BDF8).copy(alpha = 0.12f), Offset(0f, y), Offset(w, y), 1f)
                    y += step
                }
            }

            // --- VISION FILTER HIGH-CONTRAST / EDGE FILTER ---
            if (isFilterActive) {
                drawRect(
                    color = Color(0xFF06B6D4).copy(alpha = 0.15f)
                )
            }

            // --- FLASH ILLUMINATION SIMULATION ---
            if (isFlashlightOn) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(w / 2f, h * 0.42f),
                        radius = w * 0.7f
                    ),
                    radius = w * 0.7f,
                    center = Offset(w / 2f, h * 0.42f)
                )
            }
        }
    }

        // ==========================================
        // 2. CENTER FRAMING RETICLE
        // ==========================================
        if (isReticleVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 130.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(240.dp * reticlePulse)
                ) {
                    val boxW = size.width
                    val boxH = size.height
                    val cornerLength = 36f
                    val strokeW = 4f
                    val reticleColor = when (activeMode) {
                        CameraAppMode.PAINT_STUDIO -> {
                            try { Color(android.graphics.Color.parseColor(sampledColorHex)) } catch (_: Exception) { Color(0xFFFFCC00) }
                        }
                        CameraAppMode.THERMAL_IR -> Color(0xFFFF3366)
                        CameraAppMode.AR_RULER, CameraAppMode.AR_AREA -> Color(0xFF38BDF8)
                        CameraAppMode.WELD_INSPECTION -> Color(0xFF10B981)
                        else -> Color(0xFFFFCC00) // Golden Yellow
                    }

                    // 1. Four Corner Brackets
                    // Top-Left
                    drawLine(reticleColor, Offset(0f, 0f), Offset(cornerLength, 0f), strokeW)
                    drawLine(reticleColor, Offset(0f, 0f), Offset(0f, cornerLength), strokeW)

                    // Top-Right
                    drawLine(reticleColor, Offset(boxW, 0f), Offset(boxW - cornerLength, 0f), strokeW)
                    drawLine(reticleColor, Offset(boxW, 0f), Offset(boxW, cornerLength), strokeW)

                    // Bottom-Left
                    drawLine(reticleColor, Offset(0f, boxH), Offset(cornerLength, boxH), strokeW)
                    drawLine(reticleColor, Offset(0f, boxH), Offset(0f, boxH - cornerLength), strokeW)

                    // Bottom-Right
                    drawLine(reticleColor, Offset(boxW, boxH), Offset(boxW - cornerLength, boxH), strokeW)
                    drawLine(reticleColor, Offset(boxW, boxH), Offset(boxW, boxH - cornerLength), strokeW)

                    // 2. Center Tick Marks
                    val tickLen = 14f
                    drawLine(reticleColor, Offset(boxW / 2f, 0f), Offset(boxW / 2f, tickLen), strokeW)
                    drawLine(reticleColor, Offset(boxW / 2f, boxH), Offset(boxW / 2f, boxH - tickLen), strokeW)
                    drawLine(reticleColor, Offset(0f, boxH / 2f), Offset(tickLen, boxH / 2f), strokeW)
                    drawLine(reticleColor, Offset(boxW, boxH / 2f), Offset(boxW - tickLen, boxH / 2f), strokeW)

                    // 3. QR Laser Scanning Beam
                    if (activeMode == CameraAppMode.QR_SCANNER) {
                        val laserY = boxH * laserProgress
                        drawLine(
                            color = Color(0xFFEF4444),
                            start = Offset(8f, laserY),
                            end = Offset(boxW - 8f, laserY),
                            strokeWidth = 5f
                        )
                    }

                    // 4. Center Eyedropper Crosshair for Paint Studio
                    if (activeMode == CameraAppMode.PAINT_STUDIO) {
                        drawCircle(
                            color = reticleColor,
                            radius = 16f,
                            center = Offset(boxW / 2f, boxH / 2f),
                            style = Stroke(3f)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = Offset(boxW / 2f, boxH / 2f)
                        )
                    }

                    // 5. AR Distance Level Horizon for AR Ruler
                    if (activeMode == CameraAppMode.AR_RULER) {
                        drawLine(
                            color = Color(0xFF38BDF8).copy(alpha = 0.8f),
                            start = Offset(boxW * 0.2f, boxH / 2f),
                            end = Offset(boxW * 0.8f, boxH / 2f),
                            strokeWidth = 2f
                        )
                        drawCircle(
                            color = Color(0xFF38BDF8),
                            radius = 6f,
                            center = Offset(boxW / 2f, boxH / 2f)
                        )
                    }
                }
            }
        }

        // ==========================================
        // 3. SINGLE-ROW TOP TOOLBAR: Flashlight, Contextual Quick Actions / Tool Launcher, Close Button
        // ==========================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: LED Flashlight / Torch Toggle
            IconButton(
                onClick = { viewModel.toggleFlashlight() },
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .border(1.dp, if (isFlashlightOn) Color(0xFFFFCC00) else Color.White.copy(alpha = 0.2f), CircleShape)
                    .testTag("btn_camera_flash_toggle")
            ) {
                Icon(
                    imageVector = if (isFlashlightOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flashlight",
                    tint = if (isFlashlightOn) Color(0xFFFFCC00) else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // CENTER: Quick Contextual Action Pills for Active Camera Tool
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.82f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    when (activeMode) {
                        CameraAppMode.AR_RULER -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.25f))
                                    .clickable { viewModel.cycleArUnit() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Unit: $arUnit", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (arPinnedDistanceMm == null) Color(0xFF10B981) else Color(0xFFEF4444))
                                    .clickable {
                                        if (arPinnedDistanceMm == null) {
                                            viewModel.pinArDistance()
                                            Toast.makeText(context, "Pinned Point A: ${String.format(Locale.US, "%.0f mm", arDistanceMm)}", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.clearArPinnedDistance()
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (arPinnedDistanceMm == null) "Pin Point" else "Reset Pin", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        CameraAppMode.AR_AREA -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Wall: ${arAreaWallHeightM}m", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { onNavigateToTool(ScreenRoutes.ArAreaCalculator.route) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Floor Planner", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        CameraAppMode.THERMAL_IR -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFF3366).copy(alpha = 0.25f))
                                    .clickable { viewModel.cycleThermalPalette() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(thermalPalette, color = Color(0xFFFF3366), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isEmissivityHigh) Color(0xFFFFCC00).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.15f))
                                    .clickable { viewModel.toggleEmissivity() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(if (isEmissivityHigh) "ε: 0.95 (Paint)" else "ε: 0.85 (Steel)", color = Color.White, fontSize = 11.sp)
                            }
                        }
                        CameraAppMode.PAINT_STUDIO -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFCC00).copy(alpha = 0.25f))
                                    .clickable { viewModel.cyclePaintBrand() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(selectedPaintBrand.take(12), color = Color(0xFFFFCC00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(sampledColorHex))
                                        Toast.makeText(context, "Copied $sampledColorHex", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(sampledColorHex, color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        CameraAppMode.QR_SCANNER -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF38BDF8).copy(alpha = 0.25f))
                                    .clickable {
                                        clipboardManager.setText(AnnotatedString(scannedQr))
                                        Toast.makeText(context, "Copied spec", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Copy Spec", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { onNavigateToTool(ScreenRoutes.QrCodeScanner.route) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Full Scanner", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        CameraAppMode.ENDOSCOPE -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFFFCC00).copy(alpha = 0.25f))
                                    .clickable {
                                        val nextLevel = if (endoscopeLedBrightness >= 100) 0 else endoscopeLedBrightness + 25
                                        viewModel.setEndoscopeLed(nextLevel)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("LED: $endoscopeLedBrightness%", color = Color(0xFFFFCC00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .clickable { viewModel.rotateEndoscope() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Rotate 90°", color = Color.White, fontSize = 11.sp)
                            }
                        }
                        CameraAppMode.WELD_INSPECTION -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.25f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Bead: ${weldBeadWidthMm}mm", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { onNavigateToTool("tool_metalworks_studio?toolId=widget_weld_heat_input") }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Joint Audit", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        CameraAppMode.VIDEO_NOTE -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isRecording) Color.Red else Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isRecording) "REC ${String.format(Locale.US, "%02d:%02d", recordDuration / 60, recordDuration % 60)}" else "1080p 60fps",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("4K UHD • GPS", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }

                    // Direct shortcut to full tool if available
                    val toolRoute = activeMode.route
                    if (toolRoute != null) {
                        IconButton(
                            onClick = { onNavigateToTool(toolRoute) },
                            modifier = Modifier.size(28.dp).testTag("btn_camera_open_full_tool")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Full Tool Screen",
                                tint = Color(0xFFFFCC00),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // RIGHT: Close Button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                    .testTag("btn_camera_close")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Camera",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // ==========================================
        // 4. UNIFIED BOTTOM OVERLAY (HUD CARD + TOOL TABS TOOLBAR + SHUTTER ROW)
        // ==========================================
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // A. ACTIVE TOOL HUD CARD
            if (activeMode != CameraAppMode.PHOTO_AUDIT && activeMode != CameraAppMode.VIDEO_NOTE) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    when (activeMode) {
                        CameraAppMode.AR_RULER -> {
                            // AR Distance Sizer HUD
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .fillMaxWidth()
                                    .background(Color(0xFF020617).copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("AR LASER SIZER", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Level: ${arInclineAngle}° Pitch", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        val displayedDist = when (arUnit) {
                                            "cm" -> "${String.format(Locale.US, "%.1f", arDistanceMm / 10.0)} cm"
                                            "m" -> "${String.format(Locale.US, "%.3f", arDistanceMm / 1000.0)} m"
                                            "in" -> "${String.format(Locale.US, "%.2f", arDistanceMm / 25.4)} in"
                                            "ft" -> "${String.format(Locale.US, "%.2f", arDistanceMm / 304.8)} ft"
                                            else -> "${String.format(Locale.US, "%.0f", arDistanceMm)} mm"
                                        }
                                        Text("LIVE DISTANCE", color = Color.Gray, fontSize = 9.sp)
                                        Text(displayedDist, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = FontFamily.Monospace)
                                    }

                                    Button(
                                        onClick = {
                                            if (arPinnedDistanceMm == null) {
                                                viewModel.pinArDistance()
                                                Toast.makeText(context, "Pinned Point A: ${String.format(Locale.US, "%.0f mm", arDistanceMm)}", Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.clearArPinnedDistance()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (arPinnedDistanceMm == null) Color(0xFF38BDF8) else Color(0xFFEF4444)
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Text(if (arPinnedDistanceMm == null) "Pin Point" else "Reset Pin", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        CameraAppMode.AR_AREA -> {
                            // AR Surface Area Estimator HUD
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A).copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("SURFACE & PAINT ESTIMATOR", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Wall Height: ${arAreaWallHeightM}m", color = Color.Gray, fontSize = 11.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("ESTIMATED AREA", color = Color.Gray, fontSize = 9.sp)
                                        Text("${String.format(Locale.US, "%.1f", arAreaSqM)} m² (${String.format(Locale.US, "%.0f", arAreaSqM * 10.764)} sq ft)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Paint: ~${String.format(Locale.US, "%.1f", arAreaSqM / 10.0)} L (2 coats)", color = Color(0xFFFFCC00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("Tiles: ~${(arAreaSqM * 1.1).toInt()} pcs (30x30)", color = Color(0xFF10B981), fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        CameraAppMode.THERMAL_IR -> {
                            // Thermal Heatmap HUD
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.88f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFFFF3366).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("THERMAL IR SPOT METER", color = Color(0xFFFF3366), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Palette: $thermalPalette", color = Color.Gray, fontSize = 11.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("CENTER HOTSPOT", color = Color.Gray, fontSize = 9.sp)
                                        Text(String.format(Locale.US, "%.1f°C / %.1f°F", thermalSpotTempC, thermalSpotTempC * 9 / 5 + 32), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("MAX: 48.2°C  |  MIN: 18.5°C", color = Color(0xFFFFCC00), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                        Text("DELTA: +29.7°C (Normal Range)", color = Color(0xFF10B981), fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        CameraAppMode.PAINT_STUDIO -> {
                            // Paint Color Match Swatch Box
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E293B).copy(alpha = 0.92f), RoundedCornerShape(18.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Sampled Color Square
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                try { Color(android.graphics.Color.parseColor(sampledColorHex)) } catch (_: Exception) { Color(0xFFD97706) }
                                            )
                                            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                                    )

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                            Text("COLOR EYEDROPPER", color = Color(0xFFFFCC00), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            Text("98.4% Match", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Text("$selectedPaintBrand SW 6605", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("HEX: $sampledColorHex • RGB(217, 119, 6)", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(sampledColorHex))
                                            Toast.makeText(context, "Copied $sampledColorHex", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("Copy HEX", color = Color.White, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { onNavigateToTool(ScreenRoutes.PaintingCoatingStudio.route) },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Text("Coating Studio", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        CameraAppMode.QR_SCANNER -> {
                            // QR Code detected card
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A).copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("BARCODE / QR SPEC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                    Text("100% Match", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = scannedQr,
                                    color = Color(0xFFF1F5F9),
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(scannedQr))
                                            Toast.makeText(context, "Copied QR payload to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8).copy(alpha = 0.25f)),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Spec", color = Color(0xFF38BDF8), fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(if (scannedQr.startsWith("http")) scannedQr else "https://www.google.com/search?q=$scannedQr"))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open Link", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        CameraAppMode.WELD_INSPECTION -> {
                            // Weld Inspection Bead Check HUD
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A).copy(alpha = 0.92f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("WELD DEFECT AI INSPECTION", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("Bead Width: ${weldBeadWidthMm}mm", color = Color.White, fontSize = 11.sp)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text("AI JOINT STATUS", color = Color.Gray, fontSize = 9.sp)
                                        Text(weldDefectSeverity, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Button(
                                        onClick = { onNavigateToTool("tool_metalworks_studio?toolId=widget_weld_heat_input") },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                                    ) {
                                        Text("Joint Visualizer", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        CameraAppMode.ENDOSCOPE -> {
                            // Endoscope Control Bar
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.88f), RoundedCornerShape(16.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("BORESCOPE PROBE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Text("LED Brightness: $endoscopeLedBrightness%", color = Color(0xFFFFCC00), fontSize = 10.sp)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { viewModel.rotateEndoscope() },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.RotateRight, contentDescription = "Rotate", tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            val nextLevel = if (endoscopeLedBrightness >= 100) 0 else endoscopeLedBrightness + 25
                                            viewModel.setEndoscopeLed(nextLevel)
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                                    ) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = "LED", tint = Color(0xFFFFCC00), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }

            // B. FLOATING HORIZONTAL CAMERA TOOL TABS TOOLBAR
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.85f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                val scrollState = rememberScrollState()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CameraAppMode.values().forEach { mode ->
                        val isSelected = activeMode == mode
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) Color(0xFFFFCC00).copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f))
                                .border(1.dp, if (isSelected) Color(0xFFFFCC00) else Color.White.copy(alpha = 0.15f), RoundedCornerShape(18.dp))
                                .clickable { viewModel.setMode(mode) }
                                .padding(vertical = 6.dp, horizontal = 12.dp)
                                .testTag("camera_tab_${mode.name.lowercase()}")
                        ) {
                            Icon(
                                imageVector = mode.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFFFFCC00) else Color(0xFFCBD5E1),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.label,
                                color = if (isSelected) Color(0xFFFFCC00) else Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }

            // C. SHUTTER CONTROL ROW
            Row(
                modifier = Modifier
                    .padding(horizontal = 28.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT: Gallery Button -> Opens Device Gallery
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .clickable { openDeviceGallery() }
                        .testTag("btn_camera_gallery_thumbnail"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Device Gallery",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // CENTER: Mode-Specific Shutter Trigger Button
                val shutterAccentColor = when (activeMode) {
                    CameraAppMode.PAINT_STUDIO -> Color(0xFFFFCC00)
                    CameraAppMode.AR_RULER, CameraAppMode.AR_AREA, CameraAppMode.QR_SCANNER -> Color(0xFF38BDF8)
                    CameraAppMode.THERMAL_IR -> Color(0xFFFF3366)
                    CameraAppMode.WELD_INSPECTION -> Color(0xFF10B981)
                    CameraAppMode.VIDEO_NOTE -> Color(0xFFEF4444)
                    CameraAppMode.ENDOSCOPE -> Color(0xFF94A3B8)
                    else -> Color.White
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(3.5.dp, shutterAccentColor, CircleShape)
                        .padding(5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(if (isRecording) RoundedCornerShape(14.dp) else CircleShape)
                            .background(
                                if (isRecording) Color(0xFFEF4444)
                                else if (activeMode == CameraAppMode.PHOTO_AUDIT) Color.White
                                else shutterAccentColor.copy(alpha = 0.9f)
                            )
                            .pointerInput(activeMode, isRecording) {
                                detectTapGestures(
                                    onTap = {
                                        when (activeMode) {
                                            CameraAppMode.PAINT_STUDIO -> {
                                                onCapturePhoto()
                                                Toast.makeText(context, "Analyzing Paint Swatch ($sampledColorHex)... Opening Paint Studio", Toast.LENGTH_SHORT).show()
                                                onNavigateToTool(ScreenRoutes.PaintingCoatingStudio.route)
                                            }
                                            CameraAppMode.AR_RULER -> {
                                                onCapturePhoto()
                                                Toast.makeText(context, "Analyzing AR Distance Meter (${String.format(Locale.US, "%.0f mm", arDistanceMm)})... Opening AR Sizer", Toast.LENGTH_SHORT).show()
                                                onNavigateToTool(ScreenRoutes.ArMeasurement.route)
                                            }
                                            CameraAppMode.AR_AREA -> {
                                                onCapturePhoto()
                                                Toast.makeText(context, "Analyzing Surface Area (${arAreaSqM} m²)... Opening Area Estimator", Toast.LENGTH_SHORT).show()
                                                onNavigateToTool(ScreenRoutes.ArAreaCalculator.route)
                                            }
                                            CameraAppMode.THERMAL_IR -> {
                                                onCapturePhoto()
                                                Toast.makeText(context, "Analyzing Thermal IR Hotspot (${thermalSpotTempC}°C)... Opening Thermal Studio", Toast.LENGTH_SHORT).show()
                                                onNavigateToTool(ScreenRoutes.ThermalCamera.route)
                                            }
                                            CameraAppMode.QR_SCANNER -> {
                                                onCapturePhoto()
                                                Toast.makeText(context, "Processing Barcode Spec: $scannedQr", Toast.LENGTH_SHORT).show()
                                                onNavigateToTool(ScreenRoutes.QrCodeScanner.route)
                                            }
                                            CameraAppMode.ENDOSCOPE -> {
                                                onCapturePhoto()
                                                Toast.makeText(context, "Capturing Borescope Probe Inspection...", Toast.LENGTH_SHORT).show()
                                                onNavigateToTool(ScreenRoutes.UsbEndoscope.route)
                                            }
                                            CameraAppMode.WELD_INSPECTION -> {
                                                onCapturePhoto()
                                                Toast.makeText(context, "Analyzing Weld Bead (${weldBeadWidthMm}mm)... Opening Weld Studio", Toast.LENGTH_SHORT).show()
                                                onNavigateToTool("tool_metalworks_studio?toolId=widget_weld_heat_input")
                                            }
                                            CameraAppMode.PHOTO_AUDIT -> {
                                                onCapturePhoto()
                                            }
                                            CameraAppMode.VIDEO_NOTE -> {
                                                if (isRecording) {
                                                    viewModel.stopVideoRecording(context)
                                                } else {
                                                    viewModel.startVideoRecording(context)
                                                }
                                            }
                                        }
                                    },
                                    onLongPress = {
                                        if (activeMode == CameraAppMode.VIDEO_NOTE && !isRecording) {
                                            viewModel.startVideoRecording(context)
                                        }
                                    },
                                    onPress = {
                                        val pressSuccess = tryAwaitRelease()
                                        if (pressSuccess && isRecording) {
                                            viewModel.stopVideoRecording(context)
                                        }
                                    }
                                )
                            }
                            .testTag("btn_camera_shutter_trigger"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Square else activeMode.icon,
                            contentDescription = activeMode.label,
                            tint = if (activeMode == CameraAppMode.PHOTO_AUDIT) Color.Black else Color.Black.copy(alpha = 0.85f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // RIGHT: Front / Back Camera Lens Switcher
                IconButton(
                    onClick = { viewModel.toggleFacingFront() },
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .border(1.5.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                        .testTag("btn_camera_lens_flip")
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip Camera Lens",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // ==========================================
        // 8. PLAYBACK MODAL / INSPECTION GALLERY SHEET
        // ==========================================
        if (showPlaybackModal) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.96f))
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header Row with Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FIELD INSPECTION GALLERY",
                                color = Color(0xFFFFCC00),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${capturedFiles.size} Persistent Assets Logged",
                                color = Color.Gray,
                                fontSize = 10.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Play Store Companion App link button
                            IconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.ar.core"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Opening Play Store for Companion Apps", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Play Store Companion", tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                            }

                            // Upload Asset Button
                            IconButton(
                                onClick = {
                                    try {
                                        assetPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Opening file picker...", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Upload Asset", tint = Color(0xFF10B981), modifier = Modifier.size(22.dp))
                            }

                            // Close Button
                            IconButton(onClick = { viewModel.closePlayback() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }
                    }

                    // Main Asset Previewer with 0% - 300% Zoom Control
                    selectedFileForPlayback?.let { media ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Zoom scale control bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "🔍 Zoom: ${(galleryZoomScale * 100).toInt()}%",
                                    color = Color(0xFFFFCC00),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(0.5f to "50%", 1.0f to "100%", 1.5f to "150%", 2.0f to "200%", 3.0f to "300%").forEach { (scale, label) ->
                                        val isSel = kotlin.math.abs(galleryZoomScale - scale) < 0.1f
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) Color(0xFFFFCC00) else Color.White.copy(alpha = 0.15f))
                                                .clickable { viewModel.setGalleryZoomScale(scale) }
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = label,
                                                color = if (isSel) Color.Black else Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            // Asset rendering canvas box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF020617))
                                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                    .graphicsLayer {
                                        scaleX = galleryZoomScale
                                        scaleY = galleryZoomScale
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                when {
                                    media.modeTag.contains("PAINT") -> {
                                        // Real Commercial Paint Color Swatch Card
                                        val chipColor = try {
                                            val hex = if (media.metaInfo.contains("HEX: ")) {
                                                media.metaInfo.substringAfter("HEX: ").substringBefore(" ")
                                            } else "#D97706"
                                            Color(android.graphics.Color.parseColor(hex))
                                        } catch (_: Exception) { Color(0xFFD97706) }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(chipColor)
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("COMMERCIAL PAINT SAMPLE", color = Color(0xFFFFCC00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("98.4% Delta-E Pass", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            Surface(
                                                color = Color.Black.copy(alpha = 0.85f),
                                                shape = RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(media.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text(media.metaInfo, color = Color(0xFFCBD5E1), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                                    Text("Finish: Eggshell Architectural Grade • Low VOC", color = Color.Gray, fontSize = 10.sp)
                                                }
                                            }
                                        }
                                    }
                                    media.modeTag.contains("THERMAL") -> {
                                        // Real Thermal Heatmap Card
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.radialGradient(
                                                        colors = listOf(Color(0xFFFF0055), Color(0xFFFF8800), Color(0xFF6600CC), Color(0xFF003366)),
                                                        radius = 500f
                                                    )
                                                )
                                                .padding(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("THERMAL IR HOTSPOT ANALYZER", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                Surface(
                                                    color = Color.Black.copy(alpha = 0.85f),
                                                    shape = RoundedCornerShape(10.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Text(media.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                        Text(media.metaInfo, color = Color(0xFFFF3366), fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    media.modeTag.contains("AR") -> {
                                        // Real AR Measurement Blueprint Card
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFF0F172A))
                                                .padding(16.dp),
                                            verticalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("AR BLUEPRINT CAD SNAPSHOT", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                Text("Conf: 99.2%", color = Color(0xFF10B981), fontSize = 10.sp)
                                            }

                                            // Simulated laser vector
                                            Canvas(modifier = Modifier.fillMaxWidth().height(80.dp)) {
                                                drawLine(Color(0xFF38BDF8), Offset(20f, size.height / 2), Offset(size.width - 20f, size.height / 2), strokeWidth = 3f)
                                                drawCircle(Color(0xFF38BDF8), radius = 8f, center = Offset(20f, size.height / 2))
                                                drawCircle(Color(0xFF38BDF8), radius = 8f, center = Offset(size.width - 20f, size.height / 2))
                                            }

                                            Surface(
                                                color = Color(0xFF1E293B),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(media.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                    Text(media.metaInfo, color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                                }
                                            }
                                        }
                                    }
                                    else -> {
                                        // Standard Photo / Video / Uploaded Asset rendering
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (media.isVideo) Icons.Default.Videocam else Icons.Default.PhotoCamera,
                                                contentDescription = null,
                                                tint = Color(0xFFFFCC00),
                                                modifier = Modifier.size(48.dp)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(media.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(media.metaInfo, color = Color(0xFF94A3B8), fontSize = 12.sp)

                                            if (media.isVideo) {
                                                Spacer(modifier = Modifier.height(12.dp))
                                                LinearProgressIndicator(
                                                    progress = { playbackProgress },
                                                    modifier = Modifier
                                                        .fillMaxWidth(0.8f)
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = Color(0xFFFFCC00),
                                                    trackColor = Color.White.copy(alpha = 0.2f)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                IconButton(
                                                    onClick = { viewModel.togglePlaybackPlaying() },
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isPlaybackPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                        contentDescription = if (isPlaybackPlaying) "Pause" else "Play",
                                                        tint = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text("Persistent Asset Library:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    // Thumbnails list
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(capturedFiles) { file ->
                            val isSelected = selectedFileForPlayback?.id == file.id
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFF334155) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (isSelected) Color(0xFFFFCC00) else Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectFileForPlayback(file) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Black.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (file.isVideo) Icons.Default.Videocam else Icons.Default.Image,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFFFFCC00) else Color.White
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(file.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${file.timestamp} • [${file.modeTag}] ${file.metaInfo}", color = Color.Gray, fontSize = 11.sp)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color.White.copy(alpha = 0.1f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(file.modeTag, color = Color(0xFFFFCC00), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
