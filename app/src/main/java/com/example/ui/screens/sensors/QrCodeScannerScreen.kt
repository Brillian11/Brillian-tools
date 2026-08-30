package com.example.ui.screens.sensors

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.ToolLogRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.collectAsState

// Classification of scanned codes
enum class QrPayloadType(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    WEB_URL("Website Link", Icons.Default.Language),
    APP_SHORTCUT("Native App Action", Icons.Default.OpenInNew),
    TECHNICAL_TEXT("Technical Code / Spec", Icons.Default.Translate)
}

// Simulated QR Preset
data class QrPreset(
    val title: String,
    val payload: String,
    val type: QrPayloadType,
    val description: String
)

class QrCodeScannerViewModel(
    private val toolLogRepository: ToolLogRepository? = null
) : ViewModel() {
    private val _scannedPayload = MutableStateFlow<String?>(null)
    val scannedPayload = _scannedPayload.asStateFlow()

    private val _payloadType = MutableStateFlow(QrPayloadType.TECHNICAL_TEXT)
    val payloadType = _payloadType.asStateFlow()

    private val _translatedText = MutableStateFlow<String?>(null)
    val translatedText = _translatedText.asStateFlow()

    private val _isFlashlightOn = MutableStateFlow(false)
    val isFlashlightOn = _isFlashlightOn.asStateFlow()

    private val _lastLoggedPayload = MutableStateFlow<String?>(null)
    val lastLoggedPayload = _lastLoggedPayload.asStateFlow()

    val qrPresets = listOf(
        QrPreset(
            title = "App Web Manual",
            payload = "https://ai.studio/build",
            type = QrPayloadType.WEB_URL,
            description = "Opens the trade workspace development center portal."
        ),
        QrPreset(
            title = "Jobsite Location",
            payload = "geo:-6.2088,106.8456",
            type = QrPayloadType.APP_SHORTCUT,
            description = "Triggers Google Maps to locate central site coordinates."
        ),
        QrPreset(
            title = "Weld Specification",
            payload = "SPEC_WELD_E7018_V24_A125",
            type = QrPayloadType.TECHNICAL_TEXT,
            description = "An abbreviated technical welding formula for pre-heat audits."
        ),
        QrPreset(
            title = "OSHA Hotline Mail",
            payload = "mailto:auditor@osha.gov?subject=SiteInspectionReport",
            type = QrPayloadType.APP_SHORTCUT,
            description = "Generates mail shortcut template for trade inspectors."
        ),
        QrPreset(
            title = "M3 Component Guide",
            payload = "https://m3.material.io/components",
            type = QrPayloadType.WEB_URL,
            description = "Navigates to Material Design component guidelines."
        ),
        QrPreset(
            title = "HVAC Config Formula",
            payload = "SPEC_HVAC_BTU_W90_H120_C35",
            type = QrPayloadType.TECHNICAL_TEXT,
            description = "HVAC load payload specs (Width 90ft, Height 120ft, Climate multiplier 35)."
        )
    )

    fun toggleFlashlight() {
        _isFlashlightOn.value = !_isFlashlightOn.value
    }

    fun scanPayload(payload: String) {
        _scannedPayload.value = payload
        _translatedText.value = null // reset translation for new scan
        
        // Detect type
        when {
            payload.startsWith("http://") || payload.startsWith("https://") -> {
                _payloadType.value = QrPayloadType.WEB_URL
            }
            payload.startsWith("geo:") || payload.startsWith("mailto:") || payload.startsWith("tel:") -> {
                _payloadType.value = QrPayloadType.APP_SHORTCUT
            }
            else -> {
                _payloadType.value = QrPayloadType.TECHNICAL_TEXT
            }
        }
    }

    fun clearScan() {
        _scannedPayload.value = null
        _translatedText.value = null
    }

    fun translatePayload() {
        val payload = _scannedPayload.value ?: return
        
        val translated = when {
            payload.startsWith("SPEC_WELD_E7018") -> {
                "Carbon Steel Electrode (E7018):\n- Tensile Strength: 70,000 PSI\n- Coating: Low Hydrogen Potassium Powder\n- Target Current: 125A Amps\n- Target Voltage: 24V Volts (DCEP)\n- Recommended Preheat: 110°C"
            }
            payload.startsWith("SPEC_HVAC_BTU") -> {
                "HVAC Configured Matrix:\n- Target Dimensions: 90ft Width x 120ft Length\n- Area: 10,800 sq ft\n- Climate Multiplier Factor: 35 BTU/sq ft\n- Total Required Heat Output: 378,000 BTU/hr"
            }
            payload.startsWith("http") -> {
                "Interactive Web Link Redirect\nPoints directly to external domain: $payload"
            }
            payload.startsWith("geo:") -> {
                "GPS Coordinates Map Location:\nLatitude: -6.2088\nLongitude: 106.8456\nLocation: Central Site Jakarta Office"
            }
            payload.startsWith("mailto:") -> {
                "Automated Dispatch Email Link:\nRecipient: auditor@osha.gov\nSubject: Site Inspection Report Form"
            }
            else -> {
                "Parsed Trade Data:\nRaw Sequence: $payload\nTimestamp Scanned: Just Now\nFormat: Standard QR Code Format 2D DataMatrix"
            }
        }
        _translatedText.value = translated
    }

    fun registerToLogs() {
        val payload = _scannedPayload.value ?: return
        viewModelScope.launch {
            toolLogRepository?.logToolActivity(
                toolType = "widget_qr_code_scanner",
                title = "QR Scanned Payload",
                summary = "Scanned payload of type ${_payloadType.value.label}. Contents: $payload",
                value = 1.0
            )
            _lastLoggedPayload.value = payload
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeScannerScreen(
    viewModel: QrCodeScannerViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val scannedPayload by viewModel.scannedPayload.collectAsState()
    val payloadType by viewModel.payloadType.collectAsState()
    val translatedText by viewModel.translatedText.collectAsState()
    val isFlashlightOn by viewModel.isFlashlightOn.collectAsState()
    val lastLoggedPayload by viewModel.lastLoggedPayload.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Pulsing Scanner laser animation
    val infiniteTransition = rememberInfiniteTransition()
    val laserYOffset by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Explore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INSTANT TRADE QR SCANNER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Instantly scan and process site-wide codes, structural guidelines, or dispatch web links and coordinate targets.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // --- Live Scanner Viewfinder ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Live camera viewfinder grid simulator
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw alignment targeting brackets at center
                val boxSize = w * 0.5f
                val left = (w - boxSize) / 2f
                val top = (h - boxSize) / 2f
                val right = left + boxSize
                val bottom = top + boxSize

                // Draw bounding target box
                drawRect(
                    color = Color.White.copy(alpha = 0.15f),
                    topLeft = Offset(left, top),
                    size = androidx.compose.ui.geometry.Size(boxSize, boxSize)
                )

                // Corner bracket vectors
                val bracketLen = 24.dp.toPx()
                val thickness = 4.dp.toPx()
                val laserColor = if (scannedPayload != null) Color(0xFF10B981) else Color(0xFFEF4444)

                // Top Left
                drawLine(laserColor, Offset(left, top), Offset(left + bracketLen, top), thickness)
                drawLine(laserColor, Offset(left, top), Offset(left, top + bracketLen), thickness)

                // Top Right
                drawLine(laserColor, Offset(right, top), Offset(right - bracketLen, top), thickness)
                drawLine(laserColor, Offset(right, top), Offset(right, top + bracketLen), thickness)

                // Bottom Left
                drawLine(laserColor, Offset(left, bottom), Offset(left + bracketLen, bottom), thickness)
                drawLine(laserColor, Offset(left, bottom), Offset(left, bottom - bracketLen), thickness)

                // Bottom Right
                drawLine(laserColor, Offset(right, bottom), Offset(right - bracketLen, bottom), thickness)
                drawLine(laserColor, Offset(right, bottom), Offset(right, bottom - bracketLen), thickness)

                // Dynamic Laser Scan Line
                val activeLaserY = top + (boxSize * laserYOffset)
                drawLine(
                    color = laserColor.copy(alpha = 0.85f),
                    start = Offset(left + 6.dp.toPx(), activeLaserY),
                    end = Offset(right - 6.dp.toPx(), activeLaserY),
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Viewfinder HUD Options (Flashlight switch overlay)
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                IconButton(
                    onClick = { viewModel.toggleFlashlight() },
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Flashlight",
                        tint = if (isFlashlightOn) Color(0xFFFDE047) else Color.White
                    )
                }
            }

            if (scannedPayload != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color(0xFF10B981).copy(alpha = 0.92f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("QR CODE DETECTED", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            } else {
                Text(
                    text = "ALIGN QR IN TARGET BOX",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                )
            }
        }

        // --- Sandbox: Click to Scan Simulated Presets ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Select Sample QR to Scan Instantly", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(viewModel.qrPresets) { qr ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (scannedPayload == qr.payload) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .width(150.dp)
                                .clickable { viewModel.scanPayload(qr.payload) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = qr.type.icon,
                                        contentDescription = null,
                                        tint = if (scannedPayload == qr.payload) Color.White else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        qr.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (scannedPayload == qr.payload) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    qr.description,
                                    fontSize = 10.sp,
                                    maxLines = 2,
                                    color = if (scannedPayload == qr.payload) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Results Drawer & Actions Panel ---
        AnimatedVisibility(
            visible = scannedPayload != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            if (scannedPayload != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Badge type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(payloadType.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = payloadType.label.uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            IconButton(onClick = { viewModel.clearScan() }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }

                        // Raw Payload Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Scanned Content:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                                Text(
                                    text = scannedPayload!!,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Smart App Launcher prompts based on parsed schema
                        when (payloadType) {
                            QrPayloadType.WEB_URL -> {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedPayload))
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open browser. Link copied instead.", Toast.LENGTH_SHORT).show()
                                            clipboardManager.setText(AnnotatedString(scannedPayload!!))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open Link in Web Browser")
                                }
                            }
                            QrPayloadType.APP_SHORTCUT -> {
                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedPayload))
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "No compatible app found for schema. Copied to clipboard.", Toast.LENGTH_SHORT).show()
                                            clipboardManager.setText(AnnotatedString(scannedPayload!!))
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Launch, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Launch Specific On-Device App")
                                }
                            }
                            QrPayloadType.TECHNICAL_TEXT -> {
                                Button(
                                    onClick = { viewModel.translatePayload() },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Translate, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Translate Formula / Spec Code")
                                }
                            }
                        }

                        // Transformed Translation Section
                        if (translatedText != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFEFF6FF), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Translate, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Translated Codebook Definition", fontSize = 11.sp, color = Color(0xFF1D4ED8), fontWeight = FontWeight.Bold)
                                    }
                                    Text(translatedText!!, fontSize = 12.sp, color = Color(0xFF1E3A8A), fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Universal Actions (Copy to Clipboard / Log Activity)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(scannedPayload!!))
                                    Toast.makeText(context, "Copied payload to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                                                                                          ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Raw")
                            }

                            OutlinedButton(
                                onClick = { viewModel.registerToLogs() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (lastLoggedPayload == scannedPayload) Icons.Default.CheckCircle else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (lastLoggedPayload == scannedPayload) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lastLoggedPayload == scannedPayload) "Logs Synced" else "Add to Logs")
                            }
                        }
                    }
                }
            }
        }
    }
}
