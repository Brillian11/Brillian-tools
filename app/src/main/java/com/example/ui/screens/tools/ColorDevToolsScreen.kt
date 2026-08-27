package com.example.ui.screens.tools

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.PaintCatalogRepository
import com.example.domain.model.PaletteStyle
import com.example.domain.model.SiteLightingTime
import com.example.domain.model.SurfaceRole
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorDevToolsScreen(
    viewModel: ColorDevToolsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val activity = context as? Activity

    // Sync window screen brightness if activity available
    DisposableEffect(state.screenBrightness) {
        val window = activity?.window
        val layoutParams = window?.attributes
        val originalBrightness = layoutParams?.screenBrightness ?: -1f
        layoutParams?.screenBrightness = state.screenBrightness
        window?.attributes = layoutParams

        onDispose {
            layoutParams?.screenBrightness = originalBrightness
            window?.attributes = layoutParams
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Title Card
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
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trade Color & Paint Studio Pro",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Multi-Surface Palette Generator, Site Photo Color Analyzer & Calibration",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Top Tab Bar
            TabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = state.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    text = { Text("Paint Swatches & Screen", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_swatches_calibration")
                )
                Tab(
                    selected = state.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    text = { Text("Multi-Surface Palette", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.FormatPaint, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_multi_surface_palette")
                )
                Tab(
                    selected = state.selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    text = { Text("Site Photo Analyzer", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_photo_analyzer")
                )
            }

            // Tab Content Switcher
            when (state.selectedTab) {
                0 -> SwatchesAndCalibrationTab(viewModel = viewModel, state = state)
                1 -> MultiSurfacePaletteTab(viewModel = viewModel, state = state)
                2 -> SitePhotoAnalyzerTab(viewModel = viewModel, state = state)
            }
        }
    }
}

// ==========================================
// TAB 0: Paint Swatches & Screen Calibration
// ==========================================

@Composable
private fun SwatchesAndCalibrationTab(
    viewModel: ColorDevToolsViewModel,
    state: ColorDevToolsUiState
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val selectedColorHex = state.selectedPaint.hex
    val parsedBaseColor = try {
        Color(android.graphics.Color.parseColor(selectedColorHex))
    } catch (_: Exception) {
        Color.LightGray
    }

    val lighting = state.selectedLighting
    val gamma = state.gammaCorrection
    val lum = state.luminanceScale

    val adjustedColor = rememberAdjustedColor(
        baseColor = parsedBaseColor,
        redFactor = lighting.redFactor,
        greenFactor = lighting.greenFactor,
        blueFactor = lighting.blueFactor,
        gamma = gamma,
        luminance = lum
    )

    val brands = listOf("ALL", "Nippon Paint", "Mowilex", "Avitex", "Propan", "Dulux")
    val trades = listOf("ALL", "Woodworker", "House Work", "Building Work", "Technical Work")

    val filteredPaints = viewModel.paintCatalog.filter { paint ->
        val matchesBrand = state.selectedBrandFilter == "ALL" || paint.brand.contains(state.selectedBrandFilter, ignoreCase = true)
        val matchesTrade = state.selectedTradeFilter == "ALL" || paint.tradeCategory.equals(state.selectedTradeFilter, ignoreCase = true)
        val matchesSearch = state.searchQuery.isBlank() ||
                paint.name.contains(state.searchQuery, ignoreCase = true) ||
                paint.code.contains(state.searchQuery, ignoreCase = true) ||
                paint.brand.contains(state.searchQuery, ignoreCase = true)
        matchesBrand && matchesTrade && matchesSearch
    }

    val filteredCombinations = viewModel.tradeCombinations.filter { combo ->
        state.selectedTradeFilter == "ALL" || combo.tradeCategory.equals(state.selectedTradeFilter, ignoreCase = true)
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Real-Life Preview & Screen Calibration Hero Box
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
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
                    Text(
                        text = "REAL-LIFE LOOK & SCREEN CALIBRATION",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString("${state.selectedPaint.brand} ${state.selectedPaint.code} ${state.selectedPaint.name} (${state.selectedPaint.hex})"))
                        Toast.makeText(context, "Copied Paint Code!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code")
                    }
                }

                // Surface Simulation Viewport
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(adjustedColor)
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                        .testTag("color_surface_viewport")
                ) {
                    // Texture Overlays
                    when (state.selectedSurface) {
                        "Timber Grain" -> {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val lines = 12
                                for (i in 0..lines) {
                                    val y = size.height * (i.toFloat() / lines)
                                    drawLine(
                                        color = Color.Black.copy(alpha = 0.08f),
                                        start = Offset(0f, y),
                                        end = Offset(size.width, y + 15f),
                                        strokeWidth = 3f
                                    )
                                }
                            }
                        }
                        "Concrete Surface" -> {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val count = 100
                                for (i in 0..count) {
                                    val x = (i * 37) % size.width
                                    val y = (i * 59) % size.height
                                    drawCircle(
                                        color = Color.Black.copy(alpha = 0.06f),
                                        radius = (i % 4 + 1).toFloat(),
                                        center = Offset(x, y)
                                    )
                                }
                            }
                        }
                        "Gloss Metal" -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        androidx.compose.ui.graphics.Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.25f),
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.20f)
                                            )
                                        )
                                    )
                            )
                        }
                    }

                    // Surface Tag & Lighting Tag
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatPaint,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.selectedPaint.brand} • ${state.selectedPaint.code}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${state.selectedPaint.name} • ${state.selectedPaint.hex} • LRV ${state.selectedPaint.lrv}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    // Top Right Tag: Lighting Environment
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Light: ${lighting.title} (${lighting.tempK})",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.Yellow,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Lighting Condition Selector Row
                Text(
                    text = "Light Source Simulation",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LightingCondition.entries.forEach { cond ->
                        FilterChip(
                            selected = state.selectedLighting == cond,
                            onClick = { viewModel.setLightingCondition(cond) },
                            label = { Text("${cond.title} (${cond.tempK})", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("lighting_chip_${cond.name}")
                        )
                    }
                }

                // Surface Texture Selector
                Text(
                    text = "Surface Material Texture",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Wall Plaster", "Timber Grain", "Concrete Surface", "Gloss Metal").forEach { surf ->
                        FilterChip(
                            selected = state.selectedSurface == surf,
                            onClick = { viewModel.setSurface(surf) },
                            label = { Text(surf, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Paint Specs & Contrast
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("RGB: ${state.rgbCode}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                        Text("CMYK: ${state.cmykCode}", style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("White Text Contrast: ${state.contrastRatioWhite}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Text("Black Text Contrast: ${state.contrastRatioBlack}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        // Screen Brightness & Calibration Studio Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "Device Display Screen Calibration Studio",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Screen Brightness Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Brightness6, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Screen Brightness: ${(state.screenBrightness * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Slider(
                    value = state.screenBrightness,
                    onValueChange = { viewModel.setScreenBrightness(it) },
                    valueRange = 0.1f..1.0f,
                    modifier = Modifier.testTag("brightness_slider")
                )

                // Preset Calibration Profiles
                Text(
                    text = "Calibration Profiles",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DisplayCalibrationProfile.entries.forEach { prof ->
                        FilterChip(
                            selected = state.selectedCalibrationProfile == prof,
                            onClick = { viewModel.setCalibrationProfile(prof) },
                            label = { Text(prof.title, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("calibration_profile_${prof.name}")
                        )
                    }
                }
            }
        }

        // Indonesian Paint Catalog Search & Selector Card
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
                    text = "Indonesian Paint Brands Color Picker",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    label = { Text("Search Code or Color Name (e.g. NP OW, WS-500, Teak, Yellow)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paint_search_input")
                )

                // Brand Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    brands.forEach { brand ->
                        FilterChip(
                            selected = state.selectedBrandFilter == brand,
                            onClick = { viewModel.setBrandFilter(brand) },
                            label = { Text(brand, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("brand_chip_$brand")
                        )
                    }
                }

                // Trade Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    trades.forEach { trade ->
                        FilterChip(
                            selected = state.selectedTradeFilter == trade,
                            onClick = { viewModel.setTradeFilter(trade) },
                            label = { Text(trade, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("trade_chip_$trade")
                        )
                    }
                }

                // Paint Swatches Catalog Grid
                Text(
                    text = "Available Catalog Colors (${filteredPaints.size} found)",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredPaints.forEach { paint ->
                        val isSelected = state.selectedPaint.code == paint.code
                        val pColor = try { Color(android.graphics.Color.parseColor(paint.hex)) } catch (_: Exception) { Color.Gray }

                        Surface(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectPaint(paint) }
                                .testTag("paint_item_${paint.code}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(pColor)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${paint.brand} • ${paint.code}",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "LRV ${paint.lrv}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = paint.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = paint.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 1: Multi-Surface Area Palette Generator
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiSurfacePaletteTab(
    viewModel: ColorDevToolsViewModel,
    state: ColorDevToolsUiState
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var newObjName by remember { mutableStateOf("") }
    var newObjRole by remember { mutableStateOf(SurfaceRole.MAIN_SURFACE) }
    var newObjWidth by remember { mutableStateOf("6.0") }
    var newObjHeight by remember { mutableStateOf("3.0") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Description Card
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
                    text = "MULTI-OBJECT AREA & WIDTH PALETTE GENERATOR",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Combine 2 or more architectural objects (Main Wall, Accent Column, Door Trim, Ceiling). Colors are dynamically generated based on surface area percentages to ensure non-fatiguing visual contrast.",
                    style = MaterialTheme.typography.bodySmall
                )

                // Palette Style Selector Chips
                Text(
                    text = "Select Architectural Design Style",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaletteStyle.entries.forEach { style ->
                        FilterChip(
                            selected = state.selectedPaletteStyle == style,
                            onClick = { viewModel.setPaletteStyle(style) },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text(style.title, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(style.description, style = MaterialTheme.typography.labelSmall)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("style_chip_${style.name}")
                        )
                    }
                }
            }
        }

        // Proportional Visualizer Card
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
                    text = "Proportional Color Distribution Preview",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                // Visual Render Bar
                if (state.generatedMultiPalette.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    ) {
                        state.generatedMultiPalette.forEach { item ->
                            val pColor = try { Color(android.graphics.Color.parseColor(item.recommendedPaint.hex)) } catch (_: Exception) { Color.Gray }
                            Box(
                                modifier = Modifier
                                    .weight(item.areaPercentage.toFloat().coerceAtLeast(0.05f))
                                    .fillMaxHeight()
                                    .background(pColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${String.format("%.0f", item.areaPercentage)}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configured Surfaces (${state.surfaces.size} Objects)",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("add_surface_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Surface")
                    }
                }

                // Surface Objects List
                state.surfaces.forEach { surface ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = surface.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${surface.role.title} • ${surface.widthMeters}m W × ${surface.heightMeters}m H (${String.format("%.1f", surface.areaSqM)} m²)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (state.surfaces.size > 1) {
                                IconButton(onClick = { viewModel.removeSurfaceObject(surface.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Surface", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Generated Paint Specification & Order Cards
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
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
                    Text(
                        text = "RECOMMENDED PAINT SPECIFICATIONS",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    )
                    IconButton(onClick = {
                        val summaryText = state.generatedMultiPalette.joinToString("\n") { g ->
                            "${g.surfaceObject.name}: ${g.recommendedPaint.brand} ${g.recommendedPaint.code} (${g.recommendedPaint.name}) - Est: ${String.format("%.1f", g.paintLitresNeeded)} Litres"
                        }
                        clipboardManager.setText(AnnotatedString(summaryText))
                        Toast.makeText(context, "Copied Complete Paint Bill!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Order")
                    }
                }

                state.generatedMultiPalette.forEach { item ->
                    val pColor = try { Color(android.graphics.Color.parseColor(item.recommendedPaint.hex)) } catch (_: Exception) { Color.Gray }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(pColor)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.surfaceObject.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Area Share: ${String.format("%.1f", item.areaPercentage)}% (${String.format("%.1f", item.surfaceObject.areaSqM)} m²)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Code: ${item.recommendedPaint.brand} ${item.recommendedPaint.code}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = item.recommendedPaint.hex,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                                )
                            }

                            Text(
                                text = "Color Name: ${item.recommendedPaint.name} (${item.recommendedPaint.finishType})",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Estimated Paint Required: ${String.format("%.1f", item.paintLitresNeeded)} Litres (2 coats)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.tertiary
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = item.designReasoning,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(8.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Surface Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Architectural Surface Object") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newObjName,
                        onValueChange = { newObjName = it },
                        label = { Text("Object Name (e.g. Facade Wall, Front Door)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Surface Role", style = MaterialTheme.typography.labelMedium)
                    SurfaceRole.entries.forEach { role ->
                        FilterChip(
                            selected = newObjRole == role,
                            onClick = { newObjRole = role },
                            label = { Text(role.title, style = MaterialTheme.typography.labelSmall) }
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newObjWidth,
                            onValueChange = { newObjWidth = it },
                            label = { Text("Width (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newObjHeight,
                            onValueChange = { newObjHeight = it },
                            label = { Text("Height (m)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val w = newObjWidth.toDoubleOrNull() ?: 5.0
                    val h = newObjHeight.toDoubleOrNull() ?: 3.0
                    viewModel.addSurfaceObject(newObjName, newObjRole, w, h)
                    showAddDialog = false
                    newObjName = ""
                }) {
                    Text("Add Surface")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ==========================================
// TAB 2: Site Photo & Time Analyzer
// ==========================================

@Composable
private fun SitePhotoAnalyzerTab(
    viewModel: ColorDevToolsViewModel,
    state: ColorDevToolsUiState
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            selectedBitmap = bitmap
            val centerPixel = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
            val hex = PaintCatalogRepository.rgbToHex(
                android.graphics.Color.red(centerPixel),
                android.graphics.Color.green(centerPixel),
                android.graphics.Color.blue(centerPixel)
            )
            viewModel.sampleRawColor(hex)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    selectedBitmap = bitmap
                    val centerPixel = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
                    val hex = PaintCatalogRepository.rgbToHex(
                        android.graphics.Color.red(centerPixel),
                        android.graphics.Color.green(centerPixel),
                        android.graphics.Color.blue(centerPixel)
                    )
                    viewModel.sampleRawColor(hex)
                }
            } catch (_: Exception) {}
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Description Card
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
                    text = "SITE TIME & LIGHTING COLOR ANALYZER",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Capture or upload a site photo. The analyzer removes color temperature bias caused by site time conditions (Golden Hour, Dawn, Night LED) to find the exact authentic paint code.",
                    style = MaterialTheme.typography.bodySmall
                )

                // Site Lighting Time Condition Selector
                Text(
                    text = "Select Site Time / Lighting Condition",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SiteLightingTime.entries.forEach { timeCond ->
                        FilterChip(
                            selected = state.siteLightingTime == timeCond,
                            onClick = { viewModel.setSiteLightingTime(timeCond) },
                            label = { Text("${timeCond.title} (${timeCond.tempK}K)", style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("time_chip_${timeCond.name}")
                        )
                    }
                }
            }
        }

        // Photo Input & Interactive Eyedropper Card
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
                    text = "Site Photo Input & Color Eyedropper",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("take_photo_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Take Photo")
                    }
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("gallery_pick_button")
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pick Gallery")
                    }
                }

                // Preset Site Samples Row
                Text("Or choose sample preset photo:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Plaster Wall Living Room", "Indonesian Jati Wood Board", "Civil Concrete Facade", "Workshop Safety Steel").forEach { preset ->
                        FilterChip(
                            selected = state.samplePhotoPreset == preset && selectedBitmap == null,
                            onClick = {
                                selectedBitmap = null
                                viewModel.setPhotoPreset(preset)
                            },
                            label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.testTag("preset_chip_$preset")
                        )
                    }
                }

                // Interactive Photo Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                ) {
                    if (selectedBitmap != null) {
                        val bmp = selectedBitmap!!
                        androidx.compose.foundation.Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Site Photo Sample",
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(bmp) {
                                    detectTapGestures { offset ->
                                        val x = ((offset.x / size.width) * bmp.width).toInt().coerceIn(0, bmp.width - 1)
                                        val y = ((offset.y / size.height) * bmp.height).toInt().coerceIn(0, bmp.height - 1)
                                        val pixel = bmp.getPixel(x, y)
                                        val hex = PaintCatalogRepository.rgbToHex(
                                            android.graphics.Color.red(pixel),
                                            android.graphics.Color.green(pixel),
                                            android.graphics.Color.blue(pixel)
                                        )
                                        viewModel.sampleRawColor(hex)
                                    }
                                }
                        )
                    } else {
                        // Render simulated realistic texture canvas
                        val rawColor = try { Color(android.graphics.Color.parseColor(state.rawSampledHex)) } catch (_: Exception) { Color.Gray }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(rawColor)
                                .pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        // Slight variation on tap
                                        val hex = state.rawSampledHex
                                        viewModel.sampleRawColor(hex)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.ColorLens, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap canvas or upload photo to sample color",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Color Comparison Viewport
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val rawCol = try { Color(android.graphics.Color.parseColor(state.rawSampledHex)) } catch (_: Exception) { Color.Gray }
                    val trueCol = try { Color(android.graphics.Color.parseColor(state.debiasedTrueHex)) } catch (_: Exception) { Color.Gray }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("RAW Photo Color", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(rawCol)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.rawSampledHex, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                            Text("With ${state.siteLightingTime.title} Shift", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("De-Biased True Base", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(trueCol)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(state.debiasedTrueHex, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold))
                            Text("Neutralized 6500K Base", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        // Closest Authentic Paint Code Matches Card
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
                    text = "AUTHENTIC PAINT CODE MATCHES",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                )

                state.topMatches.forEachIndexed { idx, match ->
                    val pColor = try { Color(android.graphics.Color.parseColor(match.paint.hex)) } catch (_: Exception) { Color.Gray }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(pColor)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("#${idx + 1}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${match.paint.brand} • ${match.paint.code}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${String.format("%.1f", match.matchPercentage)}% Match",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = match.paint.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = match.paint.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun rememberAdjustedColor(
    baseColor: Color,
    redFactor: Float,
    greenFactor: Float,
    blueFactor: Float,
    gamma: Float,
    luminance: Float
): Color {
    val r = (baseColor.red * redFactor * luminance).coerceIn(0f, 1f).pow(gamma)
    val g = (baseColor.green * greenFactor * luminance).coerceIn(0f, 1f).pow(gamma)
    val b = (baseColor.blue * blueFactor * luminance).coerceIn(0f, 1f).pow(gamma)

    return Color(red = r, green = g, blue = b, alpha = 1.0f)
}
