package com.example.ui.screens.library

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.items.collectAsState()
    val selectedItem by viewModel.selectedItem.collectAsState()
    val isArMode by viewModel.isArMode.collectAsState()
    val isLocalDbActive by viewModel.isLocalDatabaseActive.collectAsState()
    val lastDbMsg by viewModel.lastDatabaseMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showUploadDialog by remember { mutableStateOf(false) }
    var externalViewerItem by remember { mutableStateOf<LibraryItem?>(null) }

    val context = LocalContext.current
    val mainStoragePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var name = "Storage_File"
            var sizeStr = "Unknown"
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1) name = it.getString(nameIdx) ?: name
                        val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIdx != -1) {
                            val bytes = it.getLong(sizeIdx)
                            sizeStr = when {
                                bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
                                bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
                                else -> "$bytes B"
                            }
                        }
                    }
                }
            } catch (_: Exception) {}

            val ext = if (name.contains(".")) "." + name.substringAfterLast(".").lowercase() else ""
            val (cat, fmt) = when (ext) {
                ".dwg", ".dxf", ".svg", ".dgn", ".dwf" -> Pair("2D Floorplan", ext)
                ".obj", ".glb", ".gltf", ".skp", ".skb", ".stl", ".step", ".stp", ".fbx", ".3ds", ".blend", ".dae", ".ply", ".ifc", ".rvt", ".nwd" -> Pair("3D Model", ext)
                ".pdf", ".docx", ".doc", ".xlsx", ".xls", ".pptx", ".txt", ".csv", ".json", ".xml", ".png", ".jpg", ".jpeg" -> Pair("Document", ext)
                else -> Pair("Document", if (ext.isNotEmpty()) ext else ".file")
            }

            viewModel.addCustomItem(
                title = name,
                category = cat,
                format = fmt,
                size = sizeStr,
                description = "Opened directly from phone storage ($name)",
                uriString = uri.toString()
            )
        }
    }

    val filteredItems = items.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    if (showUploadDialog) {
        UploadModelDialog(
            onDismiss = { showUploadDialog = false },
            onUpload = { title, category, format, size, desc, uriStr ->
                viewModel.addCustomItem(title, category, format, size, desc, uriStr)
                showUploadDialog = false
            }
        )
    }

    if (externalViewerItem != null) {
        ExternalViewerDialog(
            item = externalViewerItem!!,
            onDismiss = { externalViewerItem = null }
        )
    }

    if (lastDbMsg != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearDatabaseMessage() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Local Library Database", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = { Text(lastDbMsg ?: "", fontSize = 13.sp) },
            confirmButton = {
                Button(onClick = { viewModel.clearDatabaseMessage() }) {
                    Text("OK", fontSize = 12.sp)
                }
            }
        )
    }

    if (selectedItem != null) {
        val currentItem = selectedItem!!
        val isSkpOrSkb = currentItem.format.contains("skp", ignoreCase = true) || 
                         currentItem.format.contains("skb", ignoreCase = true)
        val is2D = currentItem.category == "2D Floorplan" || 
                   currentItem.format.contains("dwg", ignoreCase = true) || 
                   currentItem.format.contains("dxf", ignoreCase = true) || 
                   currentItem.format.contains("svg", ignoreCase = true) ||
                   currentItem.format.contains("dgn", ignoreCase = true) ||
                   currentItem.format.contains("dwf", ignoreCase = true)
        val isDocOrPdf = currentItem.category == "Document" ||
                currentItem.format.contains("pdf", ignoreCase = true) ||
                currentItem.format.contains("docx", ignoreCase = true) ||
                currentItem.format.contains("doc", ignoreCase = true) ||
                currentItem.format.contains("xlsx", ignoreCase = true) ||
                currentItem.format.contains("txt", ignoreCase = true)

        when {
            currentItem.category == "Guidance" -> {
                GuidanceDetailView(
                    item = currentItem,
                    onBack = { viewModel.selectItem(null) }
                )
            }
            isSkpOrSkb || currentItem.category == "3D Model" -> {
                LibraryDetailView(
                    item = currentItem,
                    viewModel = viewModel,
                    isArMode = isArMode,
                    onToggleAr = { viewModel.setArMode(!isArMode) },
                    onBack = {
                        viewModel.selectItem(null)
                        viewModel.setArMode(false)
                    }
                )
            }
            is2D -> {
                FloorplanDetailView(
                    item = currentItem,
                    viewModel = viewModel,
                    onBack = { viewModel.selectItem(null) }
                )
            }
            isDocOrPdf -> {
                DocumentDetailView(
                    item = currentItem,
                    viewModel = viewModel,
                    onBack = { viewModel.selectItem(null) }
                )
            }
            else -> {
                LibraryDetailView(
                    item = currentItem,
                    viewModel = viewModel,
                    isArMode = isArMode,
                    onToggleAr = { viewModel.setArMode(!isArMode) },
                    onBack = {
                        viewModel.selectItem(null)
                        viewModel.setArMode(false)
                    }
                )
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, top = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Search Field & Direct Phone Storage File Browser Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search 3D, CAD, specs...", maxLines = 1, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_search_library")
                    )

                    FilledTonalButton(
                        onClick = { viewModel.loadLocalLibraryDatabase() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isLocalDbActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("button_set_local_database")
                    ) {
                        Icon(Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(if (isLocalDbActive) "Local DB" else "Set Local DB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { mainStoragePickerLauncher.launch("*/*") },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("button_browse_phone_storage")
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Browse", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Category Filter Chips - Single row horizontal scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("All", "3D Model", "2D Floorplan", "Document", "Guidance").forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 12.sp) },
                            modifier = Modifier.testTag("chip_cat_$category")
                        )
                    }
                }

                // Item List with Dividers
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("list_library_items")
                ) {
                    itemsIndexed(filteredItems) { index, item ->
                        LibraryItemCard(
                            item = item,
                            onClick = { viewModel.selectItem(item) },
                            onOpenExternalViewer = { externalViewerItem = item }
                        )
                        if (index < filteredItems.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                thickness = 0.8.dp
                            )
                        }
                    }
                }
            }

            // Bottom Floating Action Button
            ExtendedFloatingActionButton(
                onClick = { showUploadDialog = true },
                icon = { Icon(Icons.Default.CloudUpload, contentDescription = "Upload Model / File") },
                text = { Text("Upload Model / File", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .testTag("fab_upload_model")
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UploadModelDialog(
    onDismiss: () -> Unit,
    onUpload: (title: String, category: String, format: String, size: String, desc: String, uriString: String?) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("3D Model") }
    var selectedFormat by remember { mutableStateOf(".obj") }
    var estimatedSize by remember { mutableStateOf("15.2 MB") }
    var description by remember { mutableStateOf("") }
    var pickedFileName by remember { mutableStateOf<String?>(null) }
    var pickedUriString by remember { mutableStateOf<String?>(null) }

    // System File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            pickedUriString = uri.toString()
            var name = "Imported_CAD_Asset"
            var sizeStr = "12.4 MB"
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1) {
                            name = it.getString(nameIdx) ?: name
                        }
                        val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIdx != -1) {
                            val bytes = it.getLong(sizeIdx)
                            sizeStr = when {
                                bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
                                bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
                                else -> "$bytes B"
                            }
                        }
                    }
                }
            } catch (_: Exception) {}

            pickedFileName = name
            val ext = if (name.contains(".")) "." + name.substringAfterLast(".").lowercase() else ""
            val (inferredCategory, inferredFormat) = when (ext) {
                ".dwg", ".dxf", ".svg", ".dgn", ".dwf" -> Pair("2D Floorplan", ext)
                ".obj", ".glb", ".gltf", ".skp", ".skb", ".stl", ".step", ".stp", ".fbx", ".3ds", ".blend", ".dae", ".ply", ".ifc", ".rvt", ".nwd" -> Pair("3D Model", ext)
                ".pdf", ".docx", ".doc", ".xlsx", ".xls", ".pptx", ".txt", ".csv" -> Pair("Document", ext)
                else -> {
                    if (name.contains("floorplan", ignoreCase = true) || name.contains("plan", ignoreCase = true) || name.contains("blueprint", ignoreCase = true)) {
                        Pair("2D Floorplan", ext.ifEmpty { ".dwg" })
                    } else if (ext == ".skp" || ext == ".skb" || name.contains("skp", ignoreCase = true) || name.contains("skb", ignoreCase = true)) {
                        Pair("3D Model", ext.ifEmpty { ".skp" })
                    } else {
                        Pair("3D Model", ext.ifEmpty { ".obj" })
                    }
                }
            }

            title = name.substringBeforeLast(".").replace("_", " ").replace("-", " ")
            selectedCategory = inferredCategory
            selectedFormat = inferredFormat
            estimatedSize = sizeStr
            description = "Imported from device folders ($name). Verified CAD drawing asset ready for 3D/2D visualization, zoom, and field measurement."
        }
    }

    val formatOptions = when (selectedCategory) {
        "3D Model" -> listOf(".skp", ".skb", ".obj", ".glb", ".gltf", ".stl", ".step", ".stp", ".fbx", ".3ds", ".blend", ".dae", ".ply", ".ifc", ".rvt", ".nwd")
        "2D Floorplan" -> listOf(".dwg", ".dxf", ".pdf", ".svg", ".dgn", ".dwf")
        "Document" -> listOf(".pdf", ".docx", ".doc", ".xlsx", ".xls", ".pptx", ".txt", ".csv")
        else -> listOf("Handbook", "Standard", "MSDS", "Manual")
    }

    var isPresetMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Import & Upload Asset", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Compact Device File Browser Action
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Browse Device Folders", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            Text(".dwg, .dxf, .obj, .glb, .skp, .pdf", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_browse_device_folders")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Browse", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (pickedFileName != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Loaded: $pickedFileName ($estimatedSize)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Preset CAD & Blueprint Samples Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { isPresetMenuExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Load Sample Preset Model / CAD", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(
                        expanded = isPresetMenuExpanded,
                        onDismissRequest = { isPresetMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("2D Modern Villa Blueprint (.dwg)", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Architecture, null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp)) },
                            onClick = {
                                pickedFileName = "Modern_Villa_Floorplan.dwg"
                                title = "Modern 2-Story Villa Blueprint"
                                selectedCategory = "2D Floorplan"
                                selectedFormat = ".dwg"
                                estimatedSize = "6.8 MB"
                                description = "Architectural CAD floorplan blueprint with room dimensions, doors, windows, and furniture layout."
                                isPresetMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("3D Parametric Timber Pergola (.glb)", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.ViewInAr, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                pickedFileName = "Timber_Pergola_3D.glb"
                                title = "Parametric Timber Pergola 3D"
                                selectedCategory = "3D Model"
                                selectedFormat = ".glb"
                                estimatedSize = "18.6 MB"
                                description = "Timber pergola architectural frame with mortise joints and rafter slats."
                                isPresetMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("2D Commercial Cafe & Retail (.dxf)", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Storefront, null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp)) },
                            onClick = {
                                pickedFileName = "Cafe_Store_Floorplan.dxf"
                                title = "Commercial Cafe & Retail Space"
                                selectedCategory = "2D Floorplan"
                                selectedFormat = ".dxf"
                                estimatedSize = "3.9 MB"
                                description = "Commercial retail floorplan featuring dining salon, barista station, kitchen, and ADA restrooms."
                                isPresetMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("3D Concrete Bridge Span (.obj)", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Foundation, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                pickedFileName = "Concrete_Bridge_Span.obj"
                                title = "Concrete Bridge Span & Pier Assembly"
                                selectedCategory = "3D Model"
                                selectedFormat = ".obj"
                                estimatedSize = "18.4 MB"
                                description = "High-detail civil engineering bridge span with reinforced concrete abutments and pier."
                                isPresetMenuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("3D Hydraulic Excavator Boom (.obj)", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.PrecisionManufacturing, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) },
                            onClick = {
                                pickedFileName = "Hydraulic_Boom.obj"
                                title = "Hydraulic Excavator Boom & Joint"
                                selectedCategory = "3D Model"
                                selectedFormat = ".obj"
                                estimatedSize = "22.1 MB"
                                description = "Mechanical 3D model of heavy equipment articulated boom and hydraulic cylinder system."
                                isPresetMenuExpanded = false
                            }
                        )
                    }
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Asset Title", fontSize = 12.sp) },
                    placeholder = { Text("e.g. Ground Floor Plan", fontSize = 12.sp) },
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_upload_title")
                )

                // Category selection
                Text("Category:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("3D Model", "2D Floorplan", "Document", "Guidance").forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                selectedCategory = cat
                                selectedFormat = when (cat) {
                                    "3D Model" -> ".obj"
                                    "2D Floorplan" -> ".dwg"
                                    "Document" -> ".pdf"
                                    else -> "Manual"
                                }
                            },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                // Format selection
                Text("File Format:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    formatOptions.forEach { fmt ->
                        FilterChip(
                            selected = selectedFormat == fmt,
                            onClick = { selectedFormat = fmt },
                            label = { Text(fmt, fontSize = 11.sp) }
                        )
                    }
                }

                // Estimated File Size
                OutlinedTextField(
                    value = estimatedSize,
                    onValueChange = { estimatedSize = it },
                    label = { Text("File Size", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Specs", fontSize = 12.sp) },
                    placeholder = { Text("Enter engineering notes...", fontSize = 12.sp) },
                    minLines = 1,
                    maxLines = 2,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = if (title.isBlank()) "Imported CAD Asset" else title
                    val finalDesc = if (description.isBlank()) "Imported from device folders. Ready for architectural and engineering inspection." else description
                    onUpload(finalTitle, selectedCategory, selectedFormat, estimatedSize, finalDesc, pickedUriString)
                },
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.testTag("button_confirm_upload")
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save to Library", fontSize = 13.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontSize = 13.sp)
            }
        }
    )
}

@Composable
fun LibraryItemCard(
    item: LibraryItem,
    onClick: () -> Unit,
    onOpenExternalViewer: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("card_item_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (item.category == "3D Model")
                                MaterialTheme.colorScheme.primaryContainer
                            else if (item.category == "2D Floorplan")
                                Color(0xFF0284C7).copy(alpha = 0.2f)
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (item.category) {
                            "3D Model" -> Icons.Default.ViewInAr
                            "2D Floorplan" -> Icons.Default.Architecture
                            "Guidance" -> Icons.Default.MenuBook
                            else -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = if (item.category == "3D Model")
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else if (item.category == "2D Floorplan")
                            Color(0xFF0284C7)
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (item.category == "2D Floorplan") Color(0xFF0284C7)
                                    else MaterialTheme.colorScheme.primary
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.format,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${item.fileSize} • ${item.author}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onOpenExternalViewer,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.height(26.dp).testTag("btn_external_viewer_${item.id}")
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("External App", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (item.category) {
                            "3D Model" -> MaterialTheme.colorScheme.primaryContainer
                            "2D Floorplan" -> Color(0xFF0284C7).copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.secondaryContainer
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (item.category) {
                                    "3D Model" -> Icons.Default.RotateRight
                                    "2D Floorplan" -> Icons.Default.Architecture
                                    else -> Icons.Default.Description
                                },
                                contentDescription = null,
                                tint = when (item.category) {
                                    "3D Model" -> MaterialTheme.colorScheme.primary
                                    "2D Floorplan" -> Color(0xFF0284C7)
                                    else -> MaterialTheme.colorScheme.secondary
                                },
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = when (item.category) {
                                    "3D Model" -> "3D"
                                    "2D Floorplan" -> "2D"
                                    else -> "Doc"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (item.category) {
                                    "3D Model" -> MaterialTheme.colorScheme.primary
                                    "2D Floorplan" -> Color(0xFF0284C7)
                                    else -> MaterialTheme.colorScheme.secondary
                                },
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2D FLOORPLAN & ARCHITECTURAL CAD BLUEPRINT VIEWER
// -------------------------------------------------------------

data class CadRoomInfo(
    val id: String,
    val name: String,
    val bounds: Rect, // In CAD coordinates (meters)
    val areaM2: Double,
    val ceilingHeight: Double = 3.2,
    val flooring: String,
    val wallFinish: String,
    val electricalDrops: String
)

enum class BlueprintTheme(val title: String, val bg: Color, val line: Color, val grid: Color, val accent: Color) {
    BLUEPRINT("Blueprint", Color(0xFF0A192F), Color(0xFF38BDF8), Color(0xFF1E3A5F), Color(0xFFF59E0B)),
    DARK_CAD("Dark CAD", Color(0xFF0F172A), Color(0xFF10B981), Color(0xFF334155), Color(0xFF38BDF8)),
    WHITE_PAPER("White Paper", Color(0xFFF8FAFC), Color(0xFF0F172A), Color(0xFFE2E8F0), Color(0xFF2563EB))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloorplanDetailView(
    item: LibraryItem,
    viewModel: LibraryViewModel,
    onBack: () -> Unit
) {
    var currentLevel by remember { mutableStateOf("Level 1 (Ground)") }
    var selectedTheme by remember { mutableStateOf(BlueprintTheme.BLUEPRINT) }
    var zoomScale by remember { mutableStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // CAD Layer visibility toggles
    var showWalls by remember { mutableStateOf(true) }
    var showDimensions by remember { mutableStateOf(true) }
    var showRoomLabels by remember { mutableStateOf(true) }
    var showDoorsWindows by remember { mutableStateOf(true) }
    var showFurniture by remember { mutableStateOf(true) }
    var showElectrical by remember { mutableStateOf(false) }
    var showGrid by remember { mutableStateOf(true) }

    // Interactive Measurement Mode
    var isMeasureMode by remember { mutableStateOf(false) }
    var measureStart by remember { mutableStateOf<Offset?>(null) }
    var measureEnd by remember { mutableStateOf<Offset?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showSavedSheet by remember { mutableStateOf(false) }

    var selectedRoomId by remember { mutableStateOf<String?>("living") }

    var parsedDxf by remember(item.uriString) { mutableStateOf<ParsedDxfData?>(null) }
    var cadBitmap by remember(item.uriString) { mutableStateOf<Bitmap?>(null) }
    val context = LocalContext.current

    LaunchedEffect(item.uriString) {
        if (item.uriString != null) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val uri = Uri.parse(item.uriString)
                val format = item.format.lowercase()
                if (format.contains("pdf")) {
                    cadBitmap = RealFileParser.renderPdfPage(context, uri)
                } else if (format.contains("png") || format.contains("jpg") || format.contains("jpeg") || format.contains("webp")) {
                    cadBitmap = RealFileParser.loadBitmap(context, uri)
                } else {
                    parsedDxf = RealFileParser.parseCadFloorplan(context, uri, format)
                    if (parsedDxf == null) {
                        cadBitmap = RealFileParser.renderPdfPage(context, uri) ?: RealFileParser.loadBitmap(context, uri)
                    }
                }
            }
        }
    }

    // Floorplan Rooms Definition based on Level and Model
    val rooms = remember(currentLevel, item.id) {
        if (item.id == "fp_commercial" || item.title.contains("Retail", ignoreCase = true) || item.title.contains("Cafe", ignoreCase = true)) {
            listOf(
                CadRoomInfo("seating", "Dining & Seating Hall", Rect(0f, 0f, 8.0f, 5.5f), 44.0, 3.8, "Polished Terrazzo", "Acoustic Wood Paneling", "12 Dimmable LED Spots, 6 Pendant Fixtures"),
                CadRoomInfo("bar", "Espresso Bar & POS", Rect(0f, 5.5f, 5.0f, 8.5f), 15.0, 3.2, "Anti-Slip Ceramic 30x30", "Subway Tile Backsplash", "4x 20A Dedicated Appliance Outlets, Water Supply Loop"),
                CadRoomInfo("kitchen", "Commercial Kitchen", Rect(5.0f, 5.5f, 9.5f, 8.5f), 13.5, 3.2, "Epoxy Seamless Resin", "FRP Washable Wall Panels", "Hood Exhaust 3-Phase, Floor Drain Basin"),
                CadRoomInfo("restroom", "ADA Restroom", Rect(8.0f, 0f, 10.5f, 3.0f), 7.5, 2.8, "Homogeneous Matt Tile", "Ceramic Tile to Ceiling", "Exhaust Fan with Motion Sensor, GFI Outlet"),
                CadRoomInfo("office", "Back Office & Store", Rect(8.0f, 3.0f, 10.5f, 5.5f), 6.25, 2.8, "Vinyl Plank 4mm", "Latex Paint Satin", "Data Cat6 Drops, 4 Wall Duplex Outlets")
            )
        } else if (currentLevel.contains("Level 2")) {
            listOf(
                CadRoomInfo("master2", "Master Bedroom Suite", Rect(0f, 0f, 5.2f, 4.5f), 23.4, 3.2, "Engineered Oak Parquet", "Neutral Emulsion Warm Greige", "6 Ceiling Downlights, 2 Bedside 2-Way Switches"),
                CadRoomInfo("master_bath2", "Master Ensuite Bath", Rect(5.2f, 0f, 7.5f, 3.0f), 6.9, 2.8, "Marble Pattern 60x120", "Porcelain Tile Full Height", "Water Heater 50L Drop, Vanity Mirror Backlight"),
                CadRoomInfo("bed2", "Bedroom 2 (Kids)", Rect(0f, 4.5f, 4.2f, 8.5f), 16.8, 3.0, "Teak Parquet", "Anti-Bacterial Washable Paint", "4 Recessed Downlights, AC Unit 1 PK Drop"),
                CadRoomInfo("bed3", "Bedroom 3 / Study", Rect(4.2f, 4.5f, 7.5f, 8.5f), 13.2, 3.0, "Vinyl Plank 5mm", "Acoustic Wall Slats", "Workstation Power Strip, High Speed LAN"),
                CadRoomInfo("balcony", "Upper Sky Balcony", Rect(7.5f, 0f, 10.5f, 4.5f), 13.5, 3.0, "WPC Outdoor Decking", "Tempered Glass 12mm Balustrade", "Weatherproof IP65 Step Lights")
            )
        } else {
            // Ground Floor (Villa / Standard)
            listOf(
                CadRoomInfo("living", "Living & Lounge Salon", Rect(0f, 0f, 6.2f, 4.8f), 29.76, 3.5, "Homogeneous Tile 80x80 Matt", "Italian Stucco Textured Wall", "8 Indirect Cove Lights, Main Chandelier Point"),
                CadRoomInfo("dining", "Dining & Kitchen Island", Rect(6.2f, 0f, 10.5f, 4.8f), 20.64, 3.2, "Granite Slab Countertop & Tile", "Subway Glazed Tile & Veneer", "Pendant Fixtures, Induction 32A Drop"),
                CadRoomInfo("master", "Master Bedroom", Rect(0f, 4.8f, 4.8f, 9.2f), 21.12, 3.2, "Teak Parquet 15mm", "Warm White Low-VOC Acrylic", "6 Downlights, Bedside USB-C Wall Sockets"),
                CadRoomInfo("ensuite", "Ensuite Bathroom", Rect(4.8f, 4.8f, 7.2f, 7.2f), 5.76, 2.8, "Anti-Slip Hexagon Mosaic", "Full Height Glazed Ceramic", "Rainshower Line, Exhaust Fan, Vanity Light"),
                CadRoomInfo("guest_bath", "Powder Room / Bath 2", Rect(4.8f, 7.2f, 7.2f, 9.2f), 4.8, 2.8, "Terrazzo Tile 40x40", "Moisture Resistant Green Gypsum", "Downlight 9W, Instant Water Heater Point"),
                CadRoomInfo("foyer", "Foyer & Main Entry", Rect(7.2f, 4.8f, 10.5f, 7.0f), 7.26, 3.5, "Bookmatched Marble", "Custom Timber Screen Divider", "Smart Entry Lock Point, Spotlight Accent"),
                CadRoomInfo("porch", "Covered Porch & Carport", Rect(7.2f, 7.0f, 10.5f, 9.2f), 7.26, 3.0, "Heavy Duty Andesite Stone Paving", "Weather-Shield Exterior Acrylic", "Automatic Floodlight with PIR Sensor")
            )
        }
    }

    val selectedRoom = rooms.find { it.id == selectedRoomId } ?: rooms.firstOrNull()
    var isFullScreen by remember { mutableStateOf(false) }

    val allMeasurements by viewModel.savedMeasurements.collectAsState()
    val itemSavedCount = allMeasurements.count { it.itemId == item.id }

    // Calculate current measurement distance
    val currentDistanceMeters = remember(measureStart, measureEnd, zoomScale) {
        if (measureStart != null && measureEnd != null) {
            val baseScale = 28.0f * zoomScale
            val dx = (measureEnd!!.x - measureStart!!.x) / baseScale
            val dy = (measureEnd!!.y - measureStart!!.y) / baseScale
            Math.hypot(dx.toDouble(), dy.toDouble())
        } else {
            0.0
        }
    }

    val detailFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var name = "Floorplan_Asset"
            var sizeStr = "5.0 MB"
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1) name = it.getString(nameIdx) ?: name
                        val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIdx != -1) {
                            val bytes = it.getLong(sizeIdx)
                            sizeStr = when {
                                bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
                                bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
                                else -> "$bytes B"
                            }
                        }
                    }
                }
            } catch (_: Exception) {}

            val ext = if (name.contains(".")) "." + name.substringAfterLast(".").lowercase() else ""
            viewModel.addCustomItem(
                title = name,
                category = "2D Floorplan",
                format = if (ext.isNotEmpty()) ext else ".dwg",
                size = sizeStr,
                description = "Loaded floorplan drawing from storage ($name)",
                uriString = uri.toString()
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!isFullScreen) {
            // Sleek Fixed Ultra-Compact Header Bar directly under app header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0284C7))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(item.format, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = { detailFilePickerLauncher.launch("*/*") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Browse Storage", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { showSavedSheet = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (itemSavedCount > 0) {
                                        Badge { Text("$itemSavedCount") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = "Saved Measurements", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(
                            onClick = { isFullScreen = !isFullScreen },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Fullscreen Mode",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                selectedTheme = when (selectedTheme) {
                                    BlueprintTheme.BLUEPRINT -> BlueprintTheme.DARK_CAD
                                    BlueprintTheme.DARK_CAD -> BlueprintTheme.WHITE_PAPER
                                    BlueprintTheme.WHITE_PAPER -> BlueprintTheme.BLUEPRINT
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = "Switch CAD Theme", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = {
                                panOffset = Offset.Zero
                                zoomScale = 1.0f
                                measureStart = null
                                measureEnd = null
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Center Plan", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 1.dp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (!isFullScreen) Modifier.verticalScroll(rememberScrollState()) else Modifier)
        ) {

            // Compact Level Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("Level 1 (Ground)", "Level 2 (Upper)").forEach { lvl ->
                        FilterChip(
                            selected = currentLevel == lvl,
                            onClick = {
                                currentLevel = lvl
                                selectedRoomId = null
                            },
                            label = { Text(lvl, fontSize = 11.sp) },
                            modifier = Modifier.height(26.dp),
                            leadingIcon = {
                                if (currentLevel == lvl) {
                                    Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(12.dp))
                                }
                            }
                        )
                    }
                }

                Text(
                    text = "${(zoomScale * 100).toInt()}% • ${selectedTheme.title}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

        val screenHeight = LocalConfiguration.current.screenHeightDp.dp

        // Interactive 2D Blueprint Canvas
        Box(
            modifier = (if (isFullScreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth().height(screenHeight * 0.40f))
                .clip(RoundedCornerShape(if (isFullScreen) 0.dp else 16.dp))
                .background(selectedTheme.bg)
                .pointerInput(isMeasureMode) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        if (zoom != 1.0f) {
                            zoomScale = (zoomScale * zoom).coerceIn(0.05f, 1.0f)
                        }
                        if (pan != Offset.Zero) {
                            panOffset = Offset(panOffset.x + pan.x, panOffset.y + pan.y)
                        }
                    }
                }
                .pointerInput(isMeasureMode) {
                    detectDragGestures(
                        onDragStart = { startPos ->
                            if (isMeasureMode) {
                                measureStart = startPos
                                measureEnd = startPos
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (isMeasureMode) {
                                measureEnd = change.position
                            } else {
                                panOffset = Offset(panOffset.x + dragAmount.x, panOffset.y + dragAmount.y)
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().testTag("canvas_2d_floorplan_blueprint")) {
                drawArchitecturalFloorplan(
                    rooms = rooms,
                    theme = selectedTheme,
                    zoom = zoomScale,
                    pan = panOffset,
                    selectedRoomId = selectedRoomId,
                    showWalls = showWalls,
                    showDimensions = showDimensions,
                    showRoomLabels = showRoomLabels,
                    showDoorsWindows = showDoorsWindows,
                    showFurniture = showFurniture,
                    showElectrical = showElectrical,
                    showGrid = showGrid,
                    measureStart = measureStart,
                    measureEnd = measureEnd,
                    parsedDxf = parsedDxf,
                    cadBitmap = cadBitmap
                )
            }

            // Floating Tape Measure Active HUD
            if (isMeasureMode && measureStart != null && measureEnd != null && currentDistanceMeters > 0.05) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Tape: ${String.format(Locale.US, "%.2f", currentDistanceMeters)} m (${String.format(Locale.US, "%.2f", currentDistanceMeters * 3.28084)} ft)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFEF4444)
                            )
                            Text(
                                text = "Drag between dots/walls to measure",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showSaveDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                measureStart = null
                                measureEnd = null
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Clear", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Compact Single-Row Zoom Bar
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { zoomScale = (zoomScale - 0.1f).coerceAtLeast(0.05f) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(14.dp))
                    }

                    Slider(
                        value = zoomScale,
                        onValueChange = { zoomScale = it },
                        valueRange = 0.05f..1.0f,
                        modifier = Modifier
                            .width(110.dp)
                            .height(20.dp)
                    )

                    IconButton(
                        onClick = { zoomScale = (zoomScale + 0.1f).coerceAtMost(1.0f) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = "${(zoomScale * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                    )
                }
            }

            // Fullscreen Mode HUD Controls Overlay (Top Right)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFullScreen) {
                        // Level toggle inside fullscreen
                        IconButton(
                            onClick = {
                                currentLevel = if (currentLevel.contains("Level 1")) "Level 2 (Upper)" else "Level 1 (Ground)"
                                selectedRoomId = null
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Layers, contentDescription = "Switch Level", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    // Theme cycle
                    IconButton(
                        onClick = {
                            selectedTheme = when (selectedTheme) {
                                BlueprintTheme.BLUEPRINT -> BlueprintTheme.DARK_CAD
                                BlueprintTheme.DARK_CAD -> BlueprintTheme.WHITE_PAPER
                                BlueprintTheme.WHITE_PAPER -> BlueprintTheme.BLUEPRINT
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = "Theme", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    // Reset Pan
                    IconButton(
                        onClick = {
                            panOffset = Offset.Zero
                            zoomScale = 1.0f
                            measureStart = null
                            measureEnd = null
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.CenterFocusStrong, contentDescription = "Center", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    // Fullscreen Toggle
                    IconButton(
                        onClick = { isFullScreen = !isFullScreen },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = if (isFullScreen) "Exit Fullscreen" else "Enter Fullscreen",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // North Pointer & Stamp watermark (Top Left)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFullScreen) {
                        IconButton(onClick = onBack, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Icon(Icons.Default.Navigation, contentDescription = null, tint = selectedTheme.line, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("NORTH • ${currentLevel.substringBefore(" ")}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = selectedTheme.line)
                }
            }
        }

        if (!isFullScreen) {
            // Layer Visibility Toolbar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(
                        text = "CAD Blueprint Layers & Tools:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item {
                            FilterChip(
                                selected = showWalls,
                                onClick = { showWalls = !showWalls },
                                label = { Text("Walls", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.ViewQuilt, null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = showDimensions,
                                onClick = { showDimensions = !showDimensions },
                                label = { Text("Dims", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Straighten, null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = showDoorsWindows,
                                onClick = { showDoorsWindows = !showDoorsWindows },
                                label = { Text("Doors/Windows", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.MeetingRoom, null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = showFurniture,
                                onClick = { showFurniture = !showFurniture },
                                label = { Text("Furniture", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Chair, null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = showElectrical,
                                onClick = { showElectrical = !showElectrical },
                                label = { Text("Electrical MEP", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.Bolt, null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                        item {
                            FilterChip(
                                selected = isMeasureMode,
                                onClick = {
                                    isMeasureMode = !isMeasureMode
                                    if (!isMeasureMode) {
                                        measureStart = null
                                        measureEnd = null
                                    }
                                },
                                label = { Text(if (isMeasureMode) "Tape Measure: ON" else "Tape Measure", fontSize = 11.sp) },
                                leadingIcon = { Icon(Icons.Default.SquareFoot, null, modifier = Modifier.size(12.dp)) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                        item {
                            SuggestionChip(
                                onClick = { showSavedSheet = true },
                                label = { Text("Saved ($itemSavedCount)", fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.BookmarkBorder, null, modifier = Modifier.size(12.dp)) }
                            )
                        }
                    }
                }
            }

            // Room Inspector & Specification Sheet
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Room selector pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rooms.forEach { room ->
                            SuggestionChip(
                                onClick = { selectedRoomId = room.id },
                                label = { Text("${room.name} (${room.areaM2} m²)", fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (selectedRoomId == room.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }

                    if (selectedRoom != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedRoom.name,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${selectedRoom.areaM2} m² (${"%.1f".format(selectedRoom.areaM2 * 10.7639)} sq ft)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF0284C7)
                                    )
                                }
                                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Dimensions: ${selectedRoom.bounds.width}m × ${selectedRoom.bounds.height}m", fontSize = 11.sp)
                                    Text("Ceiling Clear: ${selectedRoom.ceilingHeight}m", fontSize = 11.sp)
                                }
                                Text("Flooring: ${selectedRoom.flooring}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Walls: ${selectedRoom.wallFinish}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Electrical/MEP: ${selectedRoom.electricalDrops}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
    }

    if (showSaveDialog && measureStart != null && measureEnd != null) {
        SaveMeasurementDialog(
            initialLabel = "Floorplan Span ${String.format(Locale.US, "%.2f", currentDistanceMeters)}m",
            distanceMeters = currentDistanceMeters,
            pointAName = "Point A (${measureStart!!.x.toInt()}px, ${measureStart!!.y.toInt()}px)",
            pointBName = "Point B (${measureEnd!!.x.toInt()}px, ${measureEnd!!.y.toInt()}px)",
            category = "2D Floorplan",
            onDismiss = { showSaveDialog = false },
            onSave = { label, note ->
                viewModel.saveMeasurement(
                    itemId = item.id,
                    itemTitle = item.title,
                    title = label,
                    distanceMeters = currentDistanceMeters.toFloat(),
                    pointA = "Point A",
                    pointB = "Point B",
                    mode = "2D Floorplan",
                    note = note
                )
                showSaveDialog = false
            }
        )
    }

    if (showSavedSheet) {
        SavedMeasurementsSheet(
            itemId = item.id,
            itemTitle = item.title,
            viewModel = viewModel,
            onDismiss = { showSavedSheet = false }
        )
    }
}

// -------------------------------------------------------------
// 2D CAD ARCHITECTURAL DRAWING ENGINE
// -------------------------------------------------------------

fun DrawScope.drawArchitecturalFloorplan(
    rooms: List<CadRoomInfo>,
    theme: BlueprintTheme,
    zoom: Float,
    pan: Offset,
    selectedRoomId: String?,
    showWalls: Boolean,
    showDimensions: Boolean,
    showRoomLabels: Boolean,
    showDoorsWindows: Boolean,
    showFurniture: Boolean,
    showElectrical: Boolean,
    showGrid: Boolean,
    measureStart: Offset?,
    measureEnd: Offset?,
    parsedDxf: ParsedDxfData? = null,
    cadBitmap: Bitmap? = null
) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    // 1. Draw Blueprint CAD Grid
    val baseScale = 28.0f * zoom
    val originX = canvasWidth * 0.12f + pan.x
    val originY = canvasHeight * 0.10f + pan.y

    fun toCanvasOffset(meterX: Float, meterY: Float): Offset {
        return Offset(originX + meterX * baseScale, originY + meterY * baseScale)
    }

    if (showGrid) {
        val gridStep = baseScale * 0.5f // 0.5m grid
        val numX = (canvasWidth / gridStep).toInt() + 4
        val numY = (canvasHeight / gridStep).toInt() + 4

        for (i in -4..numX) {
            val gx = originX % gridStep + i * gridStep
            val isMajor = (i % 2 == 0)
            drawLine(
                color = if (isMajor) theme.grid.copy(alpha = 0.6f) else theme.grid.copy(alpha = 0.25f),
                start = Offset(gx, 0f),
                end = Offset(gx, canvasHeight),
                strokeWidth = if (isMajor) 1.dp.toPx() else 0.5.dp.toPx()
            )
        }
        for (j in -4..numY) {
            val gy = originY % gridStep + j * gridStep
            val isMajor = (j % 2 == 0)
            drawLine(
                color = if (isMajor) theme.grid.copy(alpha = 0.6f) else theme.grid.copy(alpha = 0.25f),
                start = Offset(0f, gy),
                end = Offset(canvasWidth, gy),
                strokeWidth = if (isMajor) 1.dp.toPx() else 0.5.dp.toPx()
            )
        }
    }

    // Draw real CAD Bitmap image if loaded
    if (cadBitmap != null) {
        val bmpWidth = cadBitmap.width.toFloat()
        val bmpHeight = cadBitmap.height.toFloat()
        val fitScale = Math.min((canvasWidth * 0.9f) / bmpWidth, (canvasHeight * 0.9f) / bmpHeight) * zoom
        val drawW = (bmpWidth * fitScale).toInt().coerceAtLeast(10)
        val drawH = (bmpHeight * fitScale).toInt().coerceAtLeast(10)
        val drawX = (canvasWidth - drawW) / 2f + pan.x
        val drawY = (canvasHeight - drawH) / 2f + pan.y

        drawImage(
            image = cadBitmap.asImageBitmap(),
            dstOffset = IntOffset(drawX.toInt(), drawY.toInt()),
            dstSize = IntSize(drawW, drawH)
        )
    } else if (parsedDxf != null && parsedDxf.entities.isNotEmpty()) {
        // Draw real parsed DXF / DWG CAD vectors
        val dxExtent = Math.max(1f, parsedDxf.maxX - parsedDxf.minX)
        val dyExtent = Math.max(1f, parsedDxf.maxY - parsedDxf.minY)
        val scaleFactor = Math.min((canvasWidth * 0.8f) / dxExtent, (canvasHeight * 0.8f) / dyExtent) * zoom

        val offX = canvasWidth / 2f + pan.x - (parsedDxf.minX + dxExtent / 2f) * scaleFactor
        val offY = canvasHeight / 2f + pan.y - (parsedDxf.minY + dyExtent / 2f) * scaleFactor

        parsedDxf.entities.forEach { entity ->
            val strokeColor = when {
                entity.layer.contains("WALL", ignoreCase = true) -> if (showWalls) theme.line else Color.Transparent
                entity.layer.contains("DIM", ignoreCase = true) -> if (showDimensions) theme.accent else Color.Transparent
                else -> theme.line
            }
            if (strokeColor != Color.Transparent) {
                when (entity.type) {
                    "LINE" -> {
                        val p1 = Offset(offX + entity.x1 * scaleFactor, offY + entity.y1 * scaleFactor)
                        val p2 = Offset(offX + entity.x2 * scaleFactor, offY + entity.y2 * scaleFactor)
                        drawLine(strokeColor, p1, p2, strokeWidth = 2.dp.toPx() * zoom)
                    }
                    "CIRCLE" -> {
                        val center = Offset(offX + entity.x1 * scaleFactor, offY + entity.y1 * scaleFactor)
                        val r = entity.radius * scaleFactor
                        if (r > 0.5f) {
                            drawCircle(strokeColor, radius = r, center = center, style = Stroke(width = 1.5.dp.toPx() * zoom))
                        }
                    }
                }
            }
        }
    }

    // 2. Draw Room Zones & Fills
    rooms.forEach { room ->
        val pTopLeft = toCanvasOffset(room.bounds.left, room.bounds.top)
        val pBottomRight = toCanvasOffset(room.bounds.right, room.bounds.bottom)
        val roomWidth = pBottomRight.x - pTopLeft.x
        val roomHeight = pBottomRight.y - pTopLeft.y

        val isSelected = room.id == selectedRoomId
        val fillColor = when {
            isSelected -> theme.accent.copy(alpha = 0.25f)
            theme == BlueprintTheme.BLUEPRINT -> Color(0xFF1E3A5F).copy(alpha = 0.35f)
            theme == BlueprintTheme.DARK_CAD -> Color(0xFF1E293B).copy(alpha = 0.5f)
            else -> Color(0xFFF1F5F9)
        }

        drawRect(
            color = fillColor,
            topLeft = pTopLeft,
            size = Size(roomWidth, roomHeight)
        )

        if (isSelected) {
            drawRect(
                color = theme.accent,
                topLeft = pTopLeft,
                size = Size(roomWidth, roomHeight),
                style = Stroke(width = 2.5.dp.toPx())
            )
        }
    }

    // 3. Draw Architectural Walls (Exterior Load-bearing Double Lines & Interior Partitions)
    if (showWalls) {
        val wallThicknessPx = 4.5.dp.toPx() * zoom

        rooms.forEach { room ->
            val pTopLeft = toCanvasOffset(room.bounds.left, room.bounds.top)
            val pBottomRight = toCanvasOffset(room.bounds.right, room.bounds.bottom)
            val roomWidth = pBottomRight.x - pTopLeft.x
            val roomHeight = pBottomRight.y - pTopLeft.y

            // Draw outer wall border
            drawRect(
                color = theme.line,
                topLeft = pTopLeft,
                size = Size(roomWidth, roomHeight),
                style = Stroke(width = wallThicknessPx)
            )

            // Draw structural column at 4 corners
            val colSize = 6.dp.toPx() * zoom
            drawRect(
                color = theme.accent,
                topLeft = Offset(pTopLeft.x - colSize / 2, pTopLeft.y - colSize / 2),
                size = Size(colSize, colSize)
            )
            drawRect(
                color = theme.accent,
                topLeft = Offset(pBottomRight.x - colSize / 2, pTopLeft.y - colSize / 2),
                size = Size(colSize, colSize)
            )
            drawRect(
                color = theme.accent,
                topLeft = Offset(pTopLeft.x - colSize / 2, pBottomRight.y - colSize / 2),
                size = Size(colSize, colSize)
            )
            drawRect(
                color = theme.accent,
                topLeft = Offset(pBottomRight.x - colSize / 2, pBottomRight.y - colSize / 2),
                size = Size(colSize, colSize)
            )
        }
    }

    // 4. Draw Furniture CAD Blocks
    if (showFurniture) {
        rooms.forEach { room ->
            val pTopLeft = toCanvasOffset(room.bounds.left, room.bounds.top)
            val pBottomRight = toCanvasOffset(room.bounds.right, room.bounds.bottom)
            val rw = pBottomRight.x - pTopLeft.x
            val rh = pBottomRight.y - pTopLeft.y
            val furnColor = theme.line.copy(alpha = 0.75f)

            when {
                // Bedroom / Master Suite Bed Block
                room.id.contains("master", ignoreCase = true) || room.id.contains("bed", ignoreCase = true) -> {
                    val bedW = rw * 0.45f
                    val bedH = rh * 0.50f
                    val bedX = pTopLeft.x + rw * 0.1f
                    val bedY = pTopLeft.y + rh * 0.1f

                    // Bed Frame
                    drawRect(furnColor, topLeft = Offset(bedX, bedY), size = Size(bedW, bedH), style = Stroke(width = 1.5.dp.toPx()))
                    // Mattress Fold line
                    drawLine(furnColor, Offset(bedX, bedY + bedH * 0.3f), Offset(bedX + bedW, bedY + bedH * 0.3f), strokeWidth = 1.dp.toPx())
                    // Pillows
                    val pilW = bedW * 0.38f
                    val pilH = bedH * 0.20f
                    drawRect(furnColor, topLeft = Offset(bedX + bedW * 0.08f, bedY + bedH * 0.05f), size = Size(pilW, pilH), style = Stroke(width = 1.dp.toPx()))
                    drawRect(furnColor, topLeft = Offset(bedX + bedW * 0.54f, bedY + bedH * 0.05f), size = Size(pilW, pilH), style = Stroke(width = 1.dp.toPx()))
                    // Nightstands
                    val nsSize = bedW * 0.20f
                    drawRect(furnColor, topLeft = Offset(bedX - nsSize - 4.dp.toPx(), bedY + 4.dp.toPx()), size = Size(nsSize, nsSize), style = Stroke(width = 1.dp.toPx()))
                    drawRect(furnColor, topLeft = Offset(bedX + bedW + 4.dp.toPx(), bedY + 4.dp.toPx()), size = Size(nsSize, nsSize), style = Stroke(width = 1.dp.toPx()))
                }

                // Living Room Sofa & Coffee Table
                room.id.contains("living", ignoreCase = true) || room.id.contains("seating", ignoreCase = true) -> {
                    val sofaX = pTopLeft.x + rw * 0.15f
                    val sofaY = pTopLeft.y + rh * 0.15f
                    val sofaW = rw * 0.65f
                    val sofaH = rh * 0.30f

                    // L-Shape Sectional Sofa
                    drawRect(furnColor, topLeft = Offset(sofaX, sofaY), size = Size(sofaW, sofaH), style = Stroke(width = 1.5.dp.toPx()))
                    // Coffee Table
                    val ctW = sofaW * 0.55f
                    val ctH = sofaH * 0.6f
                    drawRect(furnColor, topLeft = Offset(sofaX + sofaW * 0.22f, sofaY + sofaH + 8.dp.toPx()), size = Size(ctW, ctH), style = Stroke(width = 1.dp.toPx()))
                    // TV Media Credenza
                    drawRect(furnColor, topLeft = Offset(sofaX + sofaW * 0.1f, pBottomRight.y - 12.dp.toPx()), size = Size(sofaW * 0.8f, 8.dp.toPx()), style = Stroke(width = 1.5.dp.toPx()))
                }

                // Dining Table & Kitchen Island
                room.id.contains("dining", ignoreCase = true) || room.id.contains("kitchen", ignoreCase = true) || room.id.contains("bar", ignoreCase = true) -> {
                    val dtW = rw * 0.55f
                    val dtH = rh * 0.35f
                    val dtX = pTopLeft.x + rw * 0.22f
                    val dtY = pTopLeft.y + rh * 0.25f

                    // Dining Table
                    drawRect(furnColor, topLeft = Offset(dtX, dtY), size = Size(dtW, dtH), style = Stroke(width = 1.5.dp.toPx()))
                    // 6 Chairs
                    val chW = dtW * 0.25f
                    val chH = 6.dp.toPx()
                    // Top chairs
                    drawRect(furnColor, topLeft = Offset(dtX + dtW * 0.15f, dtY - chH - 2.dp.toPx()), size = Size(chW, chH), style = Stroke(width = 1.dp.toPx()))
                    drawRect(furnColor, topLeft = Offset(dtX + dtW * 0.60f, dtY - chH - 2.dp.toPx()), size = Size(chW, chH), style = Stroke(width = 1.dp.toPx()))
                    // Bottom chairs
                    drawRect(furnColor, topLeft = Offset(dtX + dtW * 0.15f, dtY + dtH + 2.dp.toPx()), size = Size(chW, chH), style = Stroke(width = 1.dp.toPx()))
                    drawRect(furnColor, topLeft = Offset(dtX + dtW * 0.60f, dtY + dtH + 2.dp.toPx()), size = Size(chW, chH), style = Stroke(width = 1.dp.toPx()))

                    // Kitchen Counter Sink / Cooktop
                    val kCounterW = rw * 0.8f
                    val kCounterH = 14.dp.toPx()
                    drawRect(furnColor, topLeft = Offset(pTopLeft.x + rw * 0.1f, pTopLeft.y + 4.dp.toPx()), size = Size(kCounterW, kCounterH), style = Stroke(width = 1.5.dp.toPx()))
                    // Double Sink circle
                    drawCircle(furnColor, radius = 4.dp.toPx(), center = Offset(pTopLeft.x + rw * 0.3f, pTopLeft.y + 4.dp.toPx() + kCounterH / 2), style = Stroke(width = 1.dp.toPx()))
                    drawCircle(furnColor, radius = 4.dp.toPx(), center = Offset(pTopLeft.x + rw * 0.42f, pTopLeft.y + 4.dp.toPx() + kCounterH / 2), style = Stroke(width = 1.dp.toPx()))
                }

                // Bathroom Sanitary Fixtures (Toilet WC, Shower, Vanity)
                room.id.contains("bath", ignoreCase = true) || room.id.contains("ensuite", ignoreCase = true) || room.id.contains("restroom", ignoreCase = true) -> {
                    // Glass shower cubicle
                    val shSize = rw.coerceAtMost(rh) * 0.45f
                    drawRect(furnColor, topLeft = Offset(pTopLeft.x + 4.dp.toPx(), pTopLeft.y + 4.dp.toPx()), size = Size(shSize, shSize), style = Stroke(width = 1.2.dp.toPx()))
                    drawLine(furnColor, Offset(pTopLeft.x + 4.dp.toPx(), pTopLeft.y + 4.dp.toPx()), Offset(pTopLeft.x + 4.dp.toPx() + shSize, pTopLeft.y + 4.dp.toPx() + shSize), strokeWidth = 0.8.dp.toPx())

                    // Water Closet Toilet (Cistern & Bowl)
                    val wcX = pBottomRight.x - 18.dp.toPx()
                    val wcY = pBottomRight.y - 26.dp.toPx()
                    drawRect(furnColor, topLeft = Offset(wcX, wcY), size = Size(14.dp.toPx(), 8.dp.toPx()), style = Stroke(width = 1.dp.toPx()))
                    drawCircle(furnColor, radius = 5.dp.toPx(), center = Offset(wcX + 7.dp.toPx(), wcY + 14.dp.toPx()), style = Stroke(width = 1.dp.toPx()))
                }
            }
        }
    }

    // 5. Draw Doors with Architectural 90° Swing Arc & Windows
    if (showDoorsWindows) {
        rooms.forEach { room ->
            val pTopLeft = toCanvasOffset(room.bounds.left, room.bounds.top)
            val pBottomRight = toCanvasOffset(room.bounds.right, room.bounds.bottom)
            val doorRadius = 18.dp.toPx() * zoom

            // Door opening on bottom wall
            val doorX = pTopLeft.x + (pBottomRight.x - pTopLeft.x) * 0.7f
            val doorY = pBottomRight.y

            // Door leaf line
            drawLine(
                color = theme.line,
                start = Offset(doorX, doorY),
                end = Offset(doorX, doorY - doorRadius),
                strokeWidth = 2.dp.toPx()
            )
            // Door swing 90-degree curved arc
            drawArc(
                color = theme.line.copy(alpha = 0.5f),
                startAngle = 180f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(doorX - doorRadius, doorY - doorRadius),
                size = Size(doorRadius * 2, doorRadius * 2),
                style = Stroke(width = 1.dp.toPx())
            )

            // Window Openings on top wall (double glazing glass lines)
            val winStart = pTopLeft.x + (pBottomRight.x - pTopLeft.x) * 0.25f
            val winEnd = pTopLeft.x + (pBottomRight.x - pTopLeft.x) * 0.65f
            val winY = pTopLeft.y

            // Clear wall opening tick
            drawLine(theme.accent, Offset(winStart, winY - 3.dp.toPx()), Offset(winEnd, winY - 3.dp.toPx()), strokeWidth = 1.5.dp.toPx())
            drawLine(theme.accent, Offset(winStart, winY + 3.dp.toPx()), Offset(winEnd, winY + 3.dp.toPx()), strokeWidth = 1.5.dp.toPx())
            drawLine(theme.line, Offset(winStart, winY - 4.dp.toPx()), Offset(winStart, winY + 4.dp.toPx()), strokeWidth = 2.dp.toPx())
            drawLine(theme.line, Offset(winEnd, winY - 4.dp.toPx()), Offset(winEnd, winY + 4.dp.toPx()), strokeWidth = 2.dp.toPx())
        }
    }

    // 6. Draw Electrical & MEP Drops (if enabled)
    if (showElectrical) {
        rooms.forEach { room ->
            val pCenter = toCanvasOffset(room.bounds.center.x, room.bounds.center.y)

            // Ceiling Downlight fixture (+)
            drawCircle(Color(0xFFFACC15), radius = 6.dp.toPx(), center = pCenter, style = Stroke(width = 1.5.dp.toPx()))
            drawLine(Color(0xFFFACC15), Offset(pCenter.x - 6.dp.toPx(), pCenter.y), Offset(pCenter.x + 6.dp.toPx(), pCenter.y), strokeWidth = 1.dp.toPx())
            drawLine(Color(0xFFFACC15), Offset(pCenter.x, pCenter.y - 6.dp.toPx()), Offset(pCenter.x, pCenter.y + 6.dp.toPx()), strokeWidth = 1.dp.toPx())

            // Wall switch symbol (S)
            val pTopLeft = toCanvasOffset(room.bounds.left, room.bounds.top)
            drawCircle(Color(0xFF38BDF8), radius = 4.dp.toPx(), center = Offset(pTopLeft.x + 12.dp.toPx(), pTopLeft.y + 12.dp.toPx()))
        }
    }

    // 7. Draw Room Text Labels and Area in m² via nativeCanvas
    if (showRoomLabels) {
        val textPaint = Paint().apply {
            color = theme.line.toArgb()
            textSize = (11f * zoom).coerceIn(9f, 22f) * density
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = theme.accent.toArgb()
            textSize = (9f * zoom).coerceIn(8f, 18f) * density
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        drawContext.canvas.nativeCanvas.apply {
            rooms.forEach { room ->
                val pCenter = toCanvasOffset(room.bounds.center.x, room.bounds.center.y)
                drawText(room.name, pCenter.x, pCenter.y - 4.dp.toPx(), textPaint)
                drawText("${room.areaM2} m²", pCenter.x, pCenter.y + 12.dp.toPx(), subPaint)
            }
        }
    }

    // 8. Draw Dimension Strings & Architectural 45° Ticks
    if (showDimensions) {
        val dimPaint = Paint().apply {
            color = theme.line.copy(alpha = 0.85f).toArgb()
            textSize = 9.5f * density
            typeface = Typeface.MONOSPACE
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        rooms.forEach { room ->
            val pTopLeft = toCanvasOffset(room.bounds.left, room.bounds.top)
            val pTopRight = toCanvasOffset(room.bounds.right, room.bounds.top)
            val pBottomLeft = toCanvasOffset(room.bounds.left, room.bounds.bottom)

            val dimOffsetTop = 14.dp.toPx()
            val dimOffsetLeft = 14.dp.toPx()

            // Horizontal Top Dimension Line
            val dY = pTopLeft.y - dimOffsetTop
            drawLine(theme.line.copy(alpha = 0.7f), Offset(pTopLeft.x, dY), Offset(pTopRight.x, dY), strokeWidth = 1.dp.toPx())
            // 45° CAD slashes at ends
            drawLine(theme.accent, Offset(pTopLeft.x - 3.dp.toPx(), dY + 3.dp.toPx()), Offset(pTopLeft.x + 3.dp.toPx(), dY - 3.dp.toPx()), strokeWidth = 1.5.dp.toPx())
            drawLine(theme.accent, Offset(pTopRight.x - 3.dp.toPx(), dY + 3.dp.toPx()), Offset(pTopRight.x + 3.dp.toPx(), dY - 3.dp.toPx()), strokeWidth = 1.5.dp.toPx())

            // Vertical Left Dimension Line
            val dX = pTopLeft.x - dimOffsetLeft
            drawLine(theme.line.copy(alpha = 0.7f), Offset(dX, pTopLeft.y), Offset(dX, pBottomLeft.y), strokeWidth = 1.dp.toPx())
            // 45° CAD slashes at ends
            drawLine(theme.accent, Offset(dX - 3.dp.toPx(), pTopLeft.y + 3.dp.toPx()), Offset(dX + 3.dp.toPx(), pTopLeft.y - 3.dp.toPx()), strokeWidth = 1.5.dp.toPx())
            drawLine(theme.accent, Offset(dX - 3.dp.toPx(), pBottomLeft.y + 3.dp.toPx()), Offset(dX + 3.dp.toPx(), pBottomLeft.y - 3.dp.toPx()), strokeWidth = 1.5.dp.toPx())

            // Dimension Texts
            drawContext.canvas.nativeCanvas.apply {
                drawText("${"%.2f".format(room.bounds.width)}m", (pTopLeft.x + pTopRight.x) / 2, dY - 2.dp.toPx(), dimPaint)
                drawText("${"%.2f".format(room.bounds.height)}m", dX - 10.dp.toPx(), (pTopLeft.y + pBottomLeft.y) / 2, dimPaint)
            }
        }
    }

    // 9. Draw Dynamic Measurement Tool Tape Line (if active)
    if (measureStart != null && measureEnd != null) {
        val dx = (measureEnd.x - measureStart.x) / baseScale
        val dy = (measureEnd.y - measureStart.y) / baseScale
        val distanceMeters = Math.hypot(dx.toDouble(), dy.toDouble())

        // Measurement line with dashed effect or highlighted stroke
        drawLine(
            color = Color(0xFFEF4444),
            start = measureStart,
            end = measureEnd,
            strokeWidth = 3.dp.toPx()
        )
        drawCircle(Color(0xFFEF4444), radius = 5.dp.toPx(), center = measureStart)
        drawCircle(Color(0xFFEF4444), radius = 5.dp.toPx(), center = measureEnd)

        val measPaint = Paint().apply {
            color = android.graphics.Color.RED
            textSize = 13f * density
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val midX = (measureStart.x + measureEnd.x) / 2
        val midY = (measureStart.y + measureEnd.y) / 2 - 8.dp.toPx()
        drawContext.canvas.nativeCanvas.drawText("Length: ${"%.2f".format(distanceMeters)} m", midX, midY, measPaint)
    }
}

data class SnapPoint3D(
    val id: String,
    val name: String,
    val x: Double,
    val y: Double,
    val z: Double
)

fun get3DSnapPoints(modelId: String, modelTitle: String, format: String): List<SnapPoint3D> {
    return when {
        modelId == "m0" || modelTitle.contains("Timber", ignoreCase = true) || modelTitle.contains("Pergola", ignoreCase = true) -> {
            listOf(
                SnapPoint3D("p1", "Corner Post A (FL)", -0.9, -1.0, -0.9),
                SnapPoint3D("p2", "Corner Post B (FR)", 0.9, -1.0, -0.9),
                SnapPoint3D("p3", "Corner Post C (BR)", 0.9, -1.0, 0.9),
                SnapPoint3D("p4", "Corner Post D (BL)", -0.9, -1.0, 0.9),
                SnapPoint3D("p5", "Header Beam Top Left", -0.9, 0.7, -0.9),
                SnapPoint3D("p6", "Header Beam Top Right", 0.9, 0.7, -0.9),
                SnapPoint3D("p7", "Header Beam Rear Right", 0.9, 0.7, 0.9),
                SnapPoint3D("p8", "Header Beam Rear Left", -0.9, 0.7, 0.9),
                SnapPoint3D("p9", "Pergola Center Apex", 0.0, 0.7, 0.0)
            )
        }
        modelId == "m2" || modelTitle.contains("Excavator", ignoreCase = true) -> {
            listOf(
                SnapPoint3D("e1", "Track Front Idler", -0.8, -0.8, -0.5),
                SnapPoint3D("e2", "Track Rear Sprocket", -0.8, -0.8, 0.5),
                SnapPoint3D("e3", "Cab Roof Crown", -0.3, 0.4, 0.0),
                SnapPoint3D("e4", "Main Boom Base Pivot", -0.3, -0.2, 0.0),
                SnapPoint3D("e5", "Knuckle Articulation Joint", 0.3, 0.6, 0.0),
                SnapPoint3D("e6", "Stick Arm Tip Pin", 1.1, 0.1, 0.0),
                SnapPoint3D("e7", "Bucket Tooth Edge", 1.4, -0.2, 0.2)
            )
        }
        modelId == "m1" || modelTitle.contains("Bridge", ignoreCase = true) -> {
            listOf(
                SnapPoint3D("b1", "Abutment Span Left", -1.4, 0.3, -0.4),
                SnapPoint3D("b2", "Abutment Span Right", 1.4, 0.3, -0.4),
                SnapPoint3D("b3", "Deck Edge South", 1.4, 0.3, 0.4),
                SnapPoint3D("b4", "Deck Edge North", -1.4, 0.3, 0.4),
                SnapPoint3D("b5", "Center Pier Base", 0.0, -1.0, 0.0),
                SnapPoint3D("b6", "Center Pier Top Arch", 0.0, 0.3, 0.0)
            )
        }
        format.contains("skp", ignoreCase = true) || format.contains("skb", ignoreCase = true) ||
        modelTitle.contains("skp", ignoreCase = true) || modelTitle.contains("skb", ignoreCase = true) ||
        modelTitle.contains("House", ignoreCase = true) || modelTitle.contains("Building", ignoreCase = true) ||
        modelTitle.contains("Villa", ignoreCase = true) || modelTitle.contains("Sketch", ignoreCase = true) -> {
            listOf(
                SnapPoint3D("h1", "Front Left Footing", -1.1, -1.0, 0.8),
                SnapPoint3D("h2", "Front Right Footing", 1.1, -1.0, 0.8),
                SnapPoint3D("h3", "Rear Right Footing", 1.1, -1.0, -0.8),
                SnapPoint3D("h4", "Rear Left Footing", -1.1, -1.0, -0.8),
                SnapPoint3D("h5", "First Floor Ceiling", 0.0, -0.1, 0.8),
                SnapPoint3D("h6", "Eaves Left Corner", -1.2, 0.7, 0.9),
                SnapPoint3D("h7", "Eaves Right Corner", 1.2, 0.7, 0.9),
                SnapPoint3D("h8", "Gabled Roof Ridge Peak", 0.0, 1.2, 0.0),
                SnapPoint3D("h9", "Main Entrance Door Top", 0.0, -0.3, 0.8)
            )
        }
        else -> {
            listOf(
                SnapPoint3D("c1", "Bounding Corner FL-Bottom", -0.8, -0.8, 0.8),
                SnapPoint3D("c2", "Bounding Corner FR-Bottom", 0.8, -0.8, 0.8),
                SnapPoint3D("c3", "Bounding Corner BR-Bottom", 0.8, -0.8, -0.8),
                SnapPoint3D("c4", "Bounding Corner BL-Bottom", -0.8, -0.8, -0.8),
                SnapPoint3D("c5", "Bounding Corner FL-Top", -0.8, 0.8, 0.8),
                SnapPoint3D("c6", "Bounding Corner FR-Top", 0.8, 0.8, 0.8),
                SnapPoint3D("c7", "Bounding Corner BR-Top", 0.8, 0.8, -0.8),
                SnapPoint3D("c8", "Bounding Corner BL-Top", -0.8, 0.8, -0.8),
                SnapPoint3D("c9", "Geometric Centroid", 0.0, 0.0, 0.0)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryDetailView(
    item: LibraryItem,
    viewModel: LibraryViewModel,
    isArMode: Boolean,
    onToggleAr: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasCameraPermission = isGranted }
    )

    LaunchedEffect(isArMode) {
        if (isArMode && !hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var rotationX by remember { mutableStateOf(20f) } // Pitch
    var rotationY by remember { mutableStateOf(35f) } // Yaw
    var modelScale by remember { mutableStateOf(1.0f) }
    var wireframeMode by remember { mutableStateOf(false) }
    var showGrid by remember { mutableStateOf(true) }
    var autoSpin by remember { mutableStateOf(false) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var active3dToolMode by remember { mutableStateOf(0) } // 0: Orbit, 1: Hand (Shift View), 2: Tape Measure
    
    // 3D Tape Measurement State
    var isMeasureMode by remember { mutableStateOf(false) }
    var isCadLayersSidebarOpen by remember { mutableStateOf(false) }
    var showWalls by remember { mutableStateOf(true) }
    var showDimensions by remember { mutableStateOf(true) }
    var showDoorsWindows by remember { mutableStateOf(true) }
    var showFurniture by remember { mutableStateOf(true) }
    var showElectrical by remember { mutableStateOf(false) }
    var selectedSnapA by remember { mutableStateOf<String?>(null) }
    var selectedSnapB by remember { mutableStateOf<String?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showSavedSheet by remember { mutableStateOf(false) }
    var projectedScreenPoints by remember { mutableStateOf<Map<String, Offset>>(emptyMap()) }

    val allMeasurements by viewModel.savedMeasurements.collectAsState()
    val itemSavedCount = allMeasurements.count { it.itemId == item.id }

    // AR Projection Controls
    var arModelOffset by remember { mutableStateOf(Offset.Zero) }
    var isSurfaceLocked by remember { mutableStateOf(true) }
    var isArFullScreen by remember { mutableStateOf(false) }
    var arInteractionMode by remember { mutableStateOf(0) } // 0: Move, 1: Rotate
    var torchEnabled by remember { mutableStateOf(false) }
    var cameraControlRef by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    var showSnapshotToast by remember { mutableStateOf(false) }

    // Auto turntable animation
    val infiniteTransition = rememberInfiniteTransition(label = "turntable")
    val animatedTurntable by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    val effectiveYaw = if (autoSpin) (rotationY + animatedTurntable) % 360f else rotationY

    // AR Reticle pulse animation
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ar_pulse"
    )

    LaunchedEffect(showSnapshotToast) {
        if (showSnapshotToast) {
            kotlinx.coroutines.delay(2000)
            showSnapshotToast = false
        }
    }

    var is3dFullScreen by remember { mutableStateOf(false) }
    val effectiveFullScreen = isArFullScreen || is3dFullScreen

    val snapPoints3D = remember(item.id, item.title, item.format) {
        get3DSnapPoints(item.id, item.title, item.format)
    }

    val distance3DMeters = remember(selectedSnapA, selectedSnapB, snapPoints3D) {
        if (selectedSnapA != null && selectedSnapB != null) {
            val pA = snapPoints3D.find { it.id == selectedSnapA }
            val pB = snapPoints3D.find { it.id == selectedSnapB }
            if (pA != null && pB != null) {
                val dx = (pB.x - pA.x) * 4.0
                val dy = (pB.y - pA.y) * 4.0
                val dz = (pB.z - pA.z) * 4.0
                Math.sqrt(dx * dx + dy * dy + dz * dz)
            } else 0.0
        } else 0.0
    }

    val detailFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var name = "Picked_3D_Model"
            var sizeStr = "10.0 MB"
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1) name = it.getString(nameIdx) ?: name
                        val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIdx != -1) {
                            val bytes = it.getLong(sizeIdx)
                            sizeStr = when {
                                bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
                                bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
                                else -> "$bytes B"
                            }
                        }
                    }
                }
            } catch (_: Exception) {}

            val ext = if (name.contains(".")) "." + name.substringAfterLast(".").lowercase() else ""
            viewModel.addCustomItem(
                title = name,
                category = "3D Model",
                format = if (ext.isNotEmpty()) ext else ".obj",
                size = sizeStr,
                description = "Opened directly from phone storage ($name)",
                uriString = uri.toString()
            )
        }
    }

    var parsedMesh by remember(item.uriString) { mutableStateOf<Parsed3DMesh?>(null) }
    var isLoadingMesh by remember(item.uriString) { mutableStateOf(false) }

    LaunchedEffect(item.uriString, item.format) {
        if (item.uriString != null) {
            isLoadingMesh = true
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val uri = Uri.parse(item.uriString)
                val format = item.format.lowercase()
                parsedMesh = RealFileParser.parse3dFile(context, uri, format)
            }
            isLoadingMesh = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (!effectiveFullScreen) {
            // Sleek Fixed Ultra-Compact Header Bar directly under app header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0284C7))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(item.format, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(
                            onClick = { detailFilePickerLauncher.launch("*/*") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Browse Storage", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = { showSavedSheet = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            BadgedBox(
                                badge = {
                                    if (itemSavedCount > 0) {
                                        Badge { Text("$itemSavedCount") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = "Saved Measurements", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(
                            onClick = { is3dFullScreen = !is3dFullScreen },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Full Screen Preview",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (item.category == "3D Model" || item.format.contains("skp", ignoreCase = true) || item.format.contains("skb", ignoreCase = true)) {
                            Button(
                                onClick = onToggleAr,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isArMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp).testTag("button_toggle_ar")
                            ) {
                                Icon(
                                    imageVector = if (isArMode) Icons.Default.Close else Icons.Default.ViewInAr,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(if (isArMode) "Exit AR" else "AR", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 1.dp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (effectiveFullScreen) 0.dp else 8.dp)
                .then(if (!effectiveFullScreen) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Interactive 3D / AR Viewport Container (40% Screen Height in normal mode)
            val configuration = LocalConfiguration.current
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isArMode) Color.Black else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = if (effectiveFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (effectiveFullScreen) Modifier.fillMaxSize()
                        else Modifier.height((configuration.screenHeightDp * 0.40f).dp)
                    )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (isArMode) {
                        // Real-World AR Camera Viewfinder
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
                                            val cam = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                            cameraControlRef = cam.cameraControl
                                        } catch (_: Exception) {}
                                    }, ContextCompat.getMainExecutor(ctx))
                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Camera Permission Request Banner
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF0F172A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        text = "Camera Access Required for AR",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Project this 3D CAD model directly into your room in real-world scale.",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                    Button(
                                        onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        modifier = Modifier.testTag("btn_grant_camera_ar")
                                    ) {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Grant Camera Access")
                                    }
                                }
                            }
                        }

                        // AR Interactive Canvas with Drag Gestures
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(arInteractionMode) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        if (arInteractionMode == 0) {
                                            // Translate / Move in real space
                                            arModelOffset = Offset(
                                                x = arModelOffset.x + dragAmount.x,
                                                y = arModelOffset.y + dragAmount.y
                                            )
                                        } else {
                                            // Orbit / Rotate
                                            rotationY = (rotationY + dragAmount.x * 0.5f) % 360f
                                            rotationX = (rotationX - dragAmount.y * 0.5f).coerceIn(-80f, 80f)
                                        }
                                    }
                                }
                        ) {
                            // Render 3D Model projected over camera feed with real-world anchor
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("canvas_ar_real_camera_projection")
                            ) {
                                // Draw AR Reticle and ground anchor when surface is locked
                                if (isSurfaceLocked) {
                                    val anchorCenter = Offset(
                                        x = size.width / 2f + arModelOffset.x,
                                        y = size.height / 2f + arModelOffset.y + 90f * modelScale
                                    )
                                    // Ground plane shadow
                                    drawOval(
                                        color = Color.Black.copy(alpha = 0.45f),
                                        topLeft = Offset(anchorCenter.x - 70f * modelScale, anchorCenter.y - 18f * modelScale),
                                        size = androidx.compose.ui.geometry.Size(140f * modelScale, 36f * modelScale)
                                    )
                                    // AR Target Ring
                                    drawCircle(
                                        color = Color(0xFF38BDF8).copy(alpha = 0.65f),
                                        radius = 32.dp.toPx() * pulseScale,
                                        center = anchorCenter,
                                        style = Stroke(width = 2.dp.toPx())
                                    )
                                    drawCircle(
                                        color = Color(0xFF10B981),
                                        radius = 4.dp.toPx(),
                                        center = anchorCenter
                                    )
                                }

                                // 3D Geometry Rendering
                                draw3DGeometry(
                                    modelId = item.id,
                                    modelTitle = item.title,
                                    pitch = rotationX,
                                    yaw = effectiveYaw,
                                    scale = modelScale * 1.2f,
                                    wireframe = wireframeMode,
                                    showGrid = showGrid,
                                    primaryColor = Color(0xFF38BDF8),
                                    accentColor = Color(0xFF34D399),
                                    gridColor = Color(0xFF94A3B8).copy(alpha = 0.5f),
                                    centerOffset = arModelOffset,
                                    parsedMesh = parsedMesh
                                )
                            }

                            // AR Header Overlay
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ViewInAr,
                                        contentDescription = null,
                                        tint = Color(0xFF34D399),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (hasCameraPermission) "Live AR Projection • 1:1 Scale" else "AR Preview Mode",
                                        color = Color(0xFF34D399),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Torch Toggle
                                    if (hasCameraPermission) {
                                        IconButton(
                                            onClick = {
                                                torchEnabled = !torchEnabled
                                                try {
                                                    cameraControlRef?.enableTorch(torchEnabled)
                                                } catch (_: Exception) {}
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.7f))
                                        ) {
                                            Icon(
                                                imageVector = if (torchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                                                contentDescription = "Toggle Torch",
                                                tint = if (torchEnabled) Color(0xFFFFD600) else Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }

                                    // Fullscreen AR Toggle
                                    IconButton(
                                        onClick = { isArFullScreen = !isArFullScreen },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.7f))
                                    ) {
                                        Icon(
                                            imageVector = if (isArFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                            contentDescription = "Toggle Fullscreen",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // AR Bottom Interactive Control Strip
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Gesture Mode Selector Tabs (Move in Real Space vs Rotate)
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = { arInteractionMode = 0 },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (arInteractionMode == 0) Color(0xFF0284C7) else Color.Transparent,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.height(32.dp).testTag("tab_ar_mode_move")
                                    ) {
                                        Icon(Icons.Default.OpenWith, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Move in Room", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Button(
                                        onClick = { arInteractionMode = 1 },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (arInteractionMode == 1) Color(0xFF0284C7) else Color.Transparent,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(20.dp),
                                        modifier = Modifier.height(32.dp).testTag("tab_ar_mode_rotate")
                                    ) {
                                        Icon(Icons.Default.RotateRight, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Rotate 360°", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    // Reset Center Button
                                    IconButton(
                                        onClick = { arModelOffset = Offset.Zero },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.MyLocation, contentDescription = "Center Object", tint = Color.White, modifier = Modifier.size(16.dp))
                                    }

                                    // Snapshot Photo Capture Button
                                    IconButton(
                                        onClick = { showSnapshotToast = true },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = "Take Snapshot", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            // Snapshot Feedback Toast
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showSnapshotToast,
                                enter = fadeIn() + slideInVertically { it / 2 },
                                exit = fadeOut() + slideOutVertically { it / 2 },
                                modifier = Modifier.align(Alignment.Center)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xEE0F172A),
                                    contentColor = Color.White,
                                    shadowElevation = 8.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("AR Projection Snapshot Captured!", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        // Interactive 3D Model Inspector with Drag-to-Rotate Gesture & Tape Measure
                        val primaryColor = MaterialTheme.colorScheme.primary
                        val secondaryColor = MaterialTheme.colorScheme.secondary
                        val outlineColor = MaterialTheme.colorScheme.outlineVariant

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(active3dToolMode) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        if (zoom != 1.0f) {
                                            modelScale = (modelScale * zoom).coerceIn(0.05f, 1.0f)
                                        }
                                        if (pan != Offset.Zero && active3dToolMode == 1) {
                                            panOffset = Offset(panOffset.x + pan.x, panOffset.y + pan.y)
                                        }
                                    }
                                }
                                .pointerInput(active3dToolMode, isMeasureMode, projectedScreenPoints) {
                                    detectDragGestures(
                                        onDragStart = { startPos ->
                                            if ((isMeasureMode || active3dToolMode == 2) && projectedScreenPoints.isNotEmpty()) {
                                                val closest = projectedScreenPoints.minByOrNull { (_, screenPos) ->
                                                    Math.hypot((screenPos.x - startPos.x).toDouble(), (screenPos.y - startPos.y).toDouble())
                                                }
                                                if (closest != null) {
                                                    val dist = Math.hypot((closest.value.x - startPos.x).toDouble(), (closest.value.y - startPos.y).toDouble())
                                                    if (dist < 80.0) {
                                                        if (selectedSnapA == null) {
                                                            selectedSnapA = closest.key
                                                        } else if (selectedSnapB == null && closest.key != selectedSnapA) {
                                                            selectedSnapB = closest.key
                                                        } else {
                                                            selectedSnapA = closest.key
                                                            selectedSnapB = null
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            if (active3dToolMode == 1) {
                                                panOffset = Offset(panOffset.x + dragAmount.x, panOffset.y + dragAmount.y)
                                            } else if (!isMeasureMode && active3dToolMode == 0) {
                                                rotationY = (rotationY + dragAmount.x * 0.5f) % 360f
                                                rotationX = (rotationX - dragAmount.y * 0.5f).coerceIn(-80f, 80f)
                                            }
                                        }
                                    )
                                }
                        ) {
                            // Background viewport watermark & grid
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("canvas_3d_interactive_viewport")
                            ) {
                                draw3DGeometry(
                                    modelId = item.id,
                                    modelTitle = item.title,
                                    format = item.format,
                                    pitch = rotationX,
                                    yaw = effectiveYaw,
                                    scale = modelScale,
                                    wireframe = wireframeMode,
                                    showGrid = showGrid,
                                    primaryColor = primaryColor,
                                    accentColor = secondaryColor,
                                    gridColor = outlineColor,
                                    centerOffset = arModelOffset + panOffset,
                                    isMeasuring = isMeasureMode,
                                    snapPoints = snapPoints3D,
                                    selectedSnapA = selectedSnapA,
                                    selectedSnapB = selectedSnapB,
                                    onProjectedSnapPoints = { points ->
                                        projectedScreenPoints = points
                                    }
                                )
                            }

                            // Viewport overlay controls
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (is3dFullScreen) {
                                            IconButton(onClick = onBack, modifier = Modifier.size(20.dp)) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(14.dp))
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Icon(
                                            imageVector = if (isMeasureMode) Icons.Default.SquareFoot else Icons.Default.RotateRight,
                                            contentDescription = null,
                                            tint = if (isMeasureMode) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isMeasureMode) "Pinch to zoom (0% - 100%)" else "Pinch to zoom (0% - 100%) • Orbit 360°",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isCadLayersSidebarOpen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                        modifier = Modifier.clickable { isCadLayersSidebarOpen = !isCadLayersSidebarOpen }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Layers,
                                            contentDescription = "CAD Blueprint Layers Menu",
                                            tint = if (isCadLayersSidebarOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(6.dp).size(16.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                    ) {
                                        Text(
                                            text = if (modelScale >= 100f) "${modelScale.toInt()}x" else "X:${rotationX.toInt()}° Y:${effectiveYaw.toInt()}°",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                        modifier = Modifier.clickable { is3dFullScreen = !is3dFullScreen }
                                    ) {
                                        Icon(
                                            imageVector = if (is3dFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                            contentDescription = "Toggle Fullscreen",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(6.dp).size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Collapsible CAD Blueprint Layer Sidebar Menu Overlay
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isCadLayersSidebarOpen,
                                enter = slideInHorizontally { -it } + fadeIn(),
                                exit = slideOutHorizontally { -it } + fadeOut(),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 54.dp, start = 8.dp)
                                    .width(220.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Layers, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("CAD Layers", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            }
                                            IconButton(
                                                onClick = { isCadLayersSidebarOpen = false },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        HorizontalDivider()

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Walls & Structure", fontSize = 11.sp)
                                            Switch(checked = showWalls, onCheckedChange = { showWalls = it }, modifier = Modifier.scale(0.75f))
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Dimensions & Grid", fontSize = 11.sp)
                                            Switch(checked = showDimensions, onCheckedChange = { showDimensions = it; showGrid = it }, modifier = Modifier.scale(0.75f))
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Doors & Windows", fontSize = 11.sp)
                                            Switch(checked = showDoorsWindows, onCheckedChange = { showDoorsWindows = it }, modifier = Modifier.scale(0.75f))
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Furniture & Fixtures", fontSize = 11.sp)
                                            Switch(checked = showFurniture, onCheckedChange = { showFurniture = it }, modifier = Modifier.scale(0.75f))
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                            Text("Electrical MEP", fontSize = 11.sp)
                                            Switch(checked = showElectrical, onCheckedChange = { showElectrical = it }, modifier = Modifier.scale(0.75f))
                                        }

                                        Text("Zoom Quick Presets:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                                            listOf(0.25f, 0.5f, 0.75f, 1.0f).forEach { zVal ->
                                                FilterChip(
                                                    selected = Math.abs(modelScale - zVal) < 0.05f,
                                                    onClick = { modelScale = zVal },
                                                    label = { Text("${(zVal * 100).toInt()}%", fontSize = 9.sp) },
                                                    modifier = Modifier.height(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Active 3D Tape Measurement HUD
                            if (isMeasureMode && selectedSnapA != null && selectedSnapB != null && distance3DMeters > 0.01) {
                                val pA = snapPoints3D.find { it.id == selectedSnapA }
                                val pB = snapPoints3D.find { it.id == selectedSnapB }
                                val labelA = pA?.name ?: "Point A"
                                val labelB = pB?.name ?: "Point B"

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                    shadowElevation = 6.dp,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "3D Tape: ${String.format(Locale.US, "%.2f", distance3DMeters)} m (${String.format(Locale.US, "%.2f", distance3DMeters * 3.28084)} ft)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = Color(0xFFEF4444)
                                            )
                                            Text(
                                                text = "$labelA  →  $labelB",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Button(
                                            onClick = { showSaveDialog = true },
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Save", fontSize = 11.sp)
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                selectedSnapA = null
                                                selectedSnapB = null
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Text("Clear", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // Quick camera presets floating on bottom-right of viewport
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    modifier = Modifier.clickable {
                                        rotationX = 25f
                                        rotationY = 45f
                                    }
                                ) {
                                    Text(
                                        "ISO",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    modifier = Modifier.clickable {
                                        rotationX = 0f
                                        rotationY = 0f
                                    }
                                ) {
                                    Text(
                                        "FRONT",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                    modifier = Modifier.clickable {
                                        rotationX = 85f
                                        rotationY = 0f
                                    }
                                ) {
                                    Text(
                                        "TOP",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Controls Card for 3D/AR inspection (hidden if fullscreen is enabled)
            if (!effectiveFullScreen) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArMode) "Real-World AR Controls" else "3D CAD & Mesh Inspector",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = isMeasureMode,
                                    onClick = {
                                        isMeasureMode = !isMeasureMode
                                        if (!isMeasureMode) {
                                            selectedSnapA = null
                                            selectedSnapB = null
                                        }
                                    },
                                    label = { Text(if (isMeasureMode) "Tape: Active" else "Tape Measure", fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.SquareFoot, null, modifier = Modifier.size(12.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                )
                                // Auto-spin toggle button
                                IconButton(
                                    onClick = { autoSpin = !autoSpin },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RotateRight,
                                        contentDescription = "Auto Spin",
                                        tint = if (autoSpin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                // Grid toggle button
                                IconButton(
                                    onClick = { showGrid = !showGrid },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GridOn,
                                        contentDescription = "Toggle Grid",
                                        tint = if (showGrid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Scale zoom slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Zoom: ${(modelScale * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Slider(
                                value = modelScale,
                                onValueChange = { modelScale = it },
                                valueRange = 0.05f..1.0f,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 12.dp)
                                    .testTag("slider_model_scale")
                            )
                        }

                        // Wireframe vs Shaded Solid toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Wireframe Mesh Mode", style = MaterialTheme.typography.bodySmall)
                            Switch(
                                checked = wireframeMode,
                                onCheckedChange = { wireframeMode = it },
                                modifier = Modifier.testTag("switch_wireframe_mode")
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showSaveDialog && selectedSnapA != null && selectedSnapB != null && distance3DMeters > 0.01) {
        val pA = snapPoints3D.find { it.id == selectedSnapA }
        val pB = snapPoints3D.find { it.id == selectedSnapB }
        val labelA = pA?.name ?: "Point A"
        val labelB = pB?.name ?: "Point B"

        SaveMeasurementDialog(
            initialLabel = "3D Span ${String.format(Locale.US, "%.2f", distance3DMeters)}m ($labelA - $labelB)",
            distanceMeters = distance3DMeters,
            pointAName = labelA,
            pointBName = labelB,
            category = "3D CAD Model",
            onDismiss = { showSaveDialog = false },
            onSave = { label, note ->
                viewModel.saveMeasurement(
                    itemId = item.id,
                    itemTitle = item.title,
                    title = label,
                    distanceMeters = distance3DMeters.toFloat(),
                    pointA = labelA,
                    pointB = labelB,
                    mode = "3D CAD Model",
                    note = note
                )
                showSaveDialog = false
            }
        )
    }

    if (showSavedSheet) {
        SavedMeasurementsSheet(
            itemId = item.id,
            itemTitle = item.title,
            viewModel = viewModel,
            onDismiss = { showSavedSheet = false }
        )
    }
}

// 3D Perspective Projection and Geometry Drawing Engine
private fun androidx.compose.ui.graphics.drawscope.DrawScope.draw3DGeometry(
    modelId: String,
    modelTitle: String,
    format: String = "",
    pitch: Float,
    yaw: Float,
    scale: Float,
    wireframe: Boolean,
    showGrid: Boolean,
    primaryColor: Color,
    accentColor: Color,
    gridColor: Color,
    centerOffset: Offset = Offset.Zero,
    isMeasuring: Boolean = false,
    snapPoints: List<SnapPoint3D> = emptyList(),
    selectedSnapA: String? = null,
    selectedSnapB: String? = null,
    parsedMesh: Parsed3DMesh? = null,
    onProjectedSnapPoints: (Map<String, Offset>) -> Unit = {}
) {
    val centerX = size.width / 2f + centerOffset.x
    val centerY = size.height / 2f + centerOffset.y
    val baseScale = 90f * scale

    val radPitch = Math.toRadians(pitch.toDouble())
    val radYaw = Math.toRadians(yaw.toDouble())

    // 3D point transformation helper
    fun project(x: Double, y: Double, z: Double): Offset {
        // Rotate around Y (Yaw)
        val x1 = x * cos(radYaw) + z * sin(radYaw)
        val y1 = y
        val z1 = -x * sin(radYaw) + z * cos(radYaw)

        // Rotate around X (Pitch)
        val x2 = x1
        val y2 = y1 * cos(radPitch) - z1 * sin(radPitch)
        val z2 = y1 * sin(radPitch) + z1 * cos(radPitch)

        // Isometric perspective projection
        val fov = 600.0
        val perspective = fov / (fov + z2 * 0.5)

        val px = (centerX + x2 * baseScale * perspective).toFloat()
        val py = (centerY - y2 * baseScale * perspective).toFloat()
        return Offset(px, py)
    }

    // 1. Draw 3D Ground Grid if enabled
    if (showGrid) {
        val gridSize = 4
        val gridStep = 0.5
        for (i in -gridSize..gridSize) {
            val p1 = project(-gridSize * gridStep, -1.0, i * gridStep)
            val p2 = project(gridSize * gridStep, -1.0, i * gridStep)
            drawLine(gridColor.copy(alpha = 0.35f), p1, p2, strokeWidth = 1.dp.toPx())

            val p3 = project(i * gridStep, -1.0, -gridSize * gridStep)
            val p4 = project(i * gridStep, -1.0, gridSize * gridStep)
            drawLine(gridColor.copy(alpha = 0.35f), p3, p4, strokeWidth = 1.dp.toPx())
        }
    }

    // 2. Draw 3D Orientation Gizmo Axes at bottom-left
    val origin = project(-1.5, -1.0, -1.5)
    val xAxis = project(-1.0, -1.0, -1.5)
    val yAxis = project(-1.5, -0.5, -1.5)
    val zAxis = project(-1.5, -1.0, -1.0)
    drawLine(Color.Red, origin, xAxis, strokeWidth = 2.dp.toPx()) // X Axis (Red)
    drawLine(Color.Green, origin, yAxis, strokeWidth = 2.dp.toPx()) // Y Axis (Green)
    drawLine(Color.Blue, origin, zAxis, strokeWidth = 2.dp.toPx()) // Z Axis (Blue)

    // Render real parsed 3D mesh if present
    if (parsedMesh != null && parsedMesh.vertices.isNotEmpty()) {
        val projVerts = parsedMesh.vertices.map { v ->
            project(v.x.toDouble(), v.y.toDouble(), v.z.toDouble())
        }

        if (parsedMesh.faces.isNotEmpty()) {
            parsedMesh.faces.forEach { face ->
                if (face.vIndices.size >= 3) {
                    val p1 = projVerts.getOrNull(face.vIndices[0])
                    val p2 = projVerts.getOrNull(face.vIndices[1])
                    val p3 = projVerts.getOrNull(face.vIndices[2])

                    if (p1 != null && p2 != null && p3 != null) {
                        if (wireframe) {
                            drawLine(primaryColor, p1, p2, strokeWidth = 1.dp.toPx())
                            drawLine(primaryColor, p2, p3, strokeWidth = 1.dp.toPx())
                            drawLine(primaryColor, p3, p1, strokeWidth = 1.dp.toPx())
                        } else {
                            val path = Path().apply {
                                moveTo(p1.x, p1.y)
                                lineTo(p2.x, p2.y)
                                lineTo(p3.x, p3.y)
                                close()
                            }
                            drawPath(path, color = primaryColor.copy(alpha = 0.65f))
                            drawPath(path, color = primaryColor, style = Stroke(width = 1.dp.toPx()))
                        }
                    }
                }
            }
        } else {
            projVerts.forEach { pt ->
                drawCircle(primaryColor, radius = 2.dp.toPx(), center = pt)
            }
        }
        return
    }

    // 3. Render 3D Model specific structural components
    when {
        // Timber Pavilion & Pergola Frame Sample
        modelId == "m0" || modelTitle.contains("Timber", ignoreCase = true) || modelTitle.contains("Pergola", ignoreCase = true) -> {
            val postColor = if (wireframe) primaryColor else Color(0xFFB45309) // Warm timber oak
            val beamColor = if (wireframe) accentColor else Color(0xFFD97706) // Timber rafter amber
            val roofColor = if (wireframe) primaryColor.copy(alpha = 0.7f) else Color(0xFFF59E0B)

            // 4 Corner vertical timber columns (4x4 posts)
            val postPositions = listOf(
                Pair(-0.9, -0.9), Pair(0.9, -0.9),
                Pair(-0.9, 0.9), Pair(0.9, 0.9)
            )

            postPositions.forEach { (px, pz) ->
                val base = project(px, -1.0, pz)
                val top = project(px, 0.7, pz)
                drawLine(postColor, base, top, strokeWidth = 6.dp.toPx() * scale)

                // Base footing bracket
                val f1 = project(px - 0.12, -1.0, pz - 0.12)
                val f2 = project(px + 0.12, -1.0, pz + 0.12)
                drawLine(Color(0xFF64748B), f1, f2, strokeWidth = 3.dp.toPx() * scale)
            }

            // Top Perimeter Header Beams
            val t1 = project(-0.9, 0.7, -0.9)
            val t2 = project(0.9, 0.7, -0.9)
            val t3 = project(0.9, 0.7, 0.9)
            val t4 = project(-0.9, 0.7, 0.9)

            drawLine(beamColor, t1, t2, strokeWidth = 4.dp.toPx() * scale)
            drawLine(beamColor, t2, t3, strokeWidth = 4.dp.toPx() * scale)
            drawLine(beamColor, t3, t4, strokeWidth = 4.dp.toPx() * scale)
            drawLine(beamColor, t4, t1, strokeWidth = 4.dp.toPx() * scale)

            // Cross Purlins / Rafters (6 slats across top)
            for (i in 0..5) {
                val zOffset = -0.9 + i * (1.8 / 5.0)
                val rStart = project(-1.05, 0.82, zOffset)
                val rEnd = project(1.05, 0.82, zOffset)
                drawLine(roofColor, rStart, rEnd, strokeWidth = 3.5.dp.toPx() * scale)
            }

            // Cross bracing angled rafters
            drawLine(beamColor.copy(alpha = 0.7f), project(-0.9, 0.3, -0.9), project(-0.5, 0.7, -0.9), strokeWidth = 2.dp.toPx() * scale)
            drawLine(beamColor.copy(alpha = 0.7f), project(0.9, 0.3, -0.9), project(0.5, 0.7, -0.9), strokeWidth = 2.dp.toPx() * scale)
            drawLine(beamColor.copy(alpha = 0.7f), project(-0.9, 0.3, 0.9), project(-0.5, 0.7, 0.9), strokeWidth = 2.dp.toPx() * scale)
            drawLine(beamColor.copy(alpha = 0.7f), project(0.9, 0.3, 0.9), project(0.5, 0.7, 0.9), strokeWidth = 2.dp.toPx() * scale)
        }

        // Hydraulic Boom & Articulated Joint 3D
        modelId == "m_excavator" || modelTitle.contains("Hydraulic", ignoreCase = true) || modelTitle.contains("Boom", ignoreCase = true) -> {
            val steelColor = if (wireframe) primaryColor else Color(0xFFFACC15) // Industrial CAT yellow
            val cylinderColor = if (wireframe) accentColor else Color(0xFF475569) // Dark hydraulic cylinder
            val rodColor = if (wireframe) primaryColor else Color(0xFFE2E8F0) // Chrome rod

            // Base Pivot Turret
            val b1 = project(-0.8, -0.9, -0.4)
            val b2 = project(-0.4, -0.9, 0.4)
            val pivotPin = project(-0.6, -0.4, 0.0)
            drawLine(steelColor, b1, pivotPin, strokeWidth = 8.dp.toPx() * scale)
            drawLine(steelColor, b2, pivotPin, strokeWidth = 8.dp.toPx() * scale)

            // Main Boom Arm
            val knucklePin = project(0.3, 0.6, 0.0)
            drawLine(steelColor, pivotPin, knucklePin, strokeWidth = 12.dp.toPx() * scale)

            // Secondary Stick Arm
            val tipPin = project(1.1, 0.1, 0.0)
            drawLine(steelColor, knucklePin, tipPin, strokeWidth = 8.dp.toPx() * scale)

            // Hydraulic Cylinder Barrel & Piston Rod
            val cylBase = project(-0.5, -0.6, 0.15)
            val cylMid = project(-0.1, 0.1, 0.15)
            val rodTip = project(0.15, 0.45, 0.15)
            drawLine(cylinderColor, cylBase, cylMid, strokeWidth = 7.dp.toPx() * scale)
            drawLine(rodColor, cylMid, rodTip, strokeWidth = 3.5.dp.toPx() * scale)

            // Bucket joint
            val bucket1 = project(1.3, -0.4, -0.2)
            val bucket2 = project(1.4, -0.2, 0.2)
            drawLine(steelColor, tipPin, bucket1, strokeWidth = 5.dp.toPx() * scale)
            drawLine(steelColor, bucket1, bucket2, strokeWidth = 5.dp.toPx() * scale)
        }

        // Concrete Bridge Span & Pier
        modelId == "m1" || modelTitle.contains("Bridge", ignoreCase = true) -> {
            val concreteColor = if (wireframe) primaryColor else Color(0xFF94A3B8)
            val deckColor = if (wireframe) accentColor else Color(0xFF64748B)

            // Bridge Roadway Deck Slab
            val d1 = project(-1.4, 0.3, -0.4)
            val d2 = project(1.4, 0.3, -0.4)
            val d3 = project(1.4, 0.3, 0.4)
            val d4 = project(-1.4, 0.3, 0.4)
            drawLine(deckColor, d1, d2, strokeWidth = 6.dp.toPx() * scale)
            drawLine(deckColor, d3, d4, strokeWidth = 6.dp.toPx() * scale)
            drawLine(deckColor, d1, d4, strokeWidth = 4.dp.toPx() * scale)
            drawLine(deckColor, d2, d3, strokeWidth = 4.dp.toPx() * scale)

            // Center Pier Tower & Arch
            val pier1 = project(-0.3, -1.0, 0.0)
            val pierTop1 = project(-0.3, 0.3, 0.0)
            val pier2 = project(0.3, -1.0, 0.0)
            val pierTop2 = project(0.3, 0.3, 0.0)
            drawLine(concreteColor, pier1, pierTop1, strokeWidth = 10.dp.toPx() * scale)
            drawLine(concreteColor, pier2, pierTop2, strokeWidth = 10.dp.toPx() * scale)

            // Pier Haunch / Cross beam
            drawLine(concreteColor, project(-0.6, 0.1, 0.0), project(0.6, 0.1, 0.0), strokeWidth = 8.dp.toPx() * scale)
        }

        // SketchUp (.skp / .skb) Architectural Building CAD Model
        format.contains("skp", ignoreCase = true) || format.contains("skb", ignoreCase = true) ||
        modelTitle.contains("skp", ignoreCase = true) || modelTitle.contains("skb", ignoreCase = true) ||
        modelTitle.contains("House", ignoreCase = true) || modelTitle.contains("Building", ignoreCase = true) ||
        modelTitle.contains("Villa", ignoreCase = true) || modelTitle.contains("Sketch", ignoreCase = true) -> {
            val wallColor = if (wireframe) primaryColor else Color(0xFFE2E8F0) // Modern White Stucco
            val roofColor = if (wireframe) accentColor else Color(0xFF475569) // Charcoal Standing Seam
            val trimColor = if (wireframe) primaryColor else Color(0xFFD97706) // Warm Timber Accents
            val glassColor = if (wireframe) accentColor else Color(0xFF38BDF8) // Architectural Blue Glass

            // Ground Floor Walls Base
            val b1 = project(-1.1, -1.0, -0.8)
            val b2 = project(1.1, -1.0, -0.8)
            val b3 = project(1.1, -1.0, 0.8)
            val b4 = project(-1.1, -1.0, 0.8)

            // First Floor Ceiling / Second Floor Base
            val m1 = project(-1.1, -0.1, -0.8)
            val m2 = project(1.1, -0.1, -0.8)
            val m3 = project(1.1, -0.1, 0.8)
            val m4 = project(-1.1, -0.1, 0.8)

            // Eaves / Roof Base
            val e1 = project(-1.2, 0.7, -0.9)
            val e2 = project(1.2, 0.7, -0.9)
            val e3 = project(1.2, 0.7, 0.9)
            val e4 = project(-1.2, 0.7, 0.9)

            // Gabled Roof Ridge Peak
            val r1 = project(-1.2, 1.2, 0.0)
            val r2 = project(1.2, 1.2, 0.0)

            // Draw Ground Floor Structure
            drawLine(wallColor, b1, b2, strokeWidth = 3.dp.toPx() * scale)
            drawLine(wallColor, b2, b3, strokeWidth = 3.dp.toPx() * scale)
            drawLine(wallColor, b3, b4, strokeWidth = 3.dp.toPx() * scale)
            drawLine(wallColor, b4, b1, strokeWidth = 3.dp.toPx() * scale)

            // Vertical Corner Columns
            drawLine(wallColor, b1, m1, strokeWidth = 4.dp.toPx() * scale)
            drawLine(wallColor, b2, m2, strokeWidth = 4.dp.toPx() * scale)
            drawLine(wallColor, b3, m3, strokeWidth = 4.dp.toPx() * scale)
            drawLine(wallColor, b4, m4, strokeWidth = 4.dp.toPx() * scale)

            // Intermediate Floor Band
            drawLine(trimColor, m1, m2, strokeWidth = 3.5.dp.toPx() * scale)
            drawLine(trimColor, m2, m3, strokeWidth = 3.5.dp.toPx() * scale)
            drawLine(trimColor, m3, m4, strokeWidth = 3.5.dp.toPx() * scale)
            drawLine(trimColor, m4, m1, strokeWidth = 3.5.dp.toPx() * scale)

            // Upper Floor Walls
            drawLine(wallColor, m1, e1, strokeWidth = 4.dp.toPx() * scale)
            drawLine(wallColor, m2, e2, strokeWidth = 4.dp.toPx() * scale)
            drawLine(wallColor, m3, e3, strokeWidth = 4.dp.toPx() * scale)
            drawLine(wallColor, m4, e4, strokeWidth = 4.dp.toPx() * scale)

            // Roof Eaves Perimeter
            drawLine(roofColor, e1, e2, strokeWidth = 4.dp.toPx() * scale)
            drawLine(roofColor, e2, e3, strokeWidth = 4.dp.toPx() * scale)
            drawLine(roofColor, e3, e4, strokeWidth = 4.dp.toPx() * scale)
            drawLine(roofColor, e4, e1, strokeWidth = 4.dp.toPx() * scale)

            // Roof Ridge & Gables
            drawLine(roofColor, r1, r2, strokeWidth = 5.dp.toPx() * scale)
            drawLine(roofColor, e1, r1, strokeWidth = 3.5.dp.toPx() * scale)
            drawLine(roofColor, e4, r1, strokeWidth = 3.5.dp.toPx() * scale)
            drawLine(roofColor, e2, r2, strokeWidth = 3.5.dp.toPx() * scale)
            drawLine(roofColor, e3, r2, strokeWidth = 3.5.dp.toPx() * scale)

            // Front Entrance Door & Glazing
            val doorBase1 = project(-0.25, -1.0, 0.8)
            val doorBase2 = project(0.25, -1.0, 0.8)
            val doorTop1 = project(-0.25, -0.3, 0.8)
            val doorTop2 = project(0.25, -0.3, 0.8)
            drawLine(trimColor, doorBase1, doorTop1, strokeWidth = 2.5.dp.toPx() * scale)
            drawLine(trimColor, doorBase2, doorTop2, strokeWidth = 2.5.dp.toPx() * scale)
            drawLine(trimColor, doorTop1, doorTop2, strokeWidth = 2.5.dp.toPx() * scale)

            // Balcony & Picture Windows on Upper Floor
            val win1 = project(-0.8, 0.1, 0.8)
            val win2 = project(-0.2, 0.1, 0.8)
            val win3 = project(-0.2, 0.55, 0.8)
            val win4 = project(-0.8, 0.55, 0.8)
            drawLine(glassColor, win1, win2, strokeWidth = 2.dp.toPx() * scale)
            drawLine(glassColor, win2, win3, strokeWidth = 2.dp.toPx() * scale)
            drawLine(glassColor, win3, win4, strokeWidth = 2.dp.toPx() * scale)
            drawLine(glassColor, win4, win1, strokeWidth = 2.dp.toPx() * scale)
        }

        // Default 3D Isometric Wireframe Box & Truss Structure
        else -> {
            val color = if (wireframe) primaryColor else accentColor
            val vertices = listOf(
                project(-0.8, -0.8, -0.8), // 0
                project(0.8, -0.8, -0.8),  // 1
                project(0.8, 0.8, -0.8),   // 2
                project(-0.8, 0.8, -0.8),  // 3
                project(-0.8, -0.8, 0.8),  // 4
                project(0.8, -0.8, 0.8),   // 5
                project(0.8, 0.8, 0.8),    // 6
                project(-0.8, 0.8, 0.8)    // 7
            )

            val edges = listOf(
                Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 0),
                Pair(4, 5), Pair(5, 6), Pair(6, 7), Pair(7, 4),
                Pair(0, 4), Pair(1, 5), Pair(2, 6), Pair(3, 7),
                // Cross diagonals
                Pair(0, 2), Pair(4, 6), Pair(0, 5), Pair(3, 6)
            )

            edges.forEach { (a, b) ->
                drawLine(color, vertices[a], vertices[b], strokeWidth = 2.5.dp.toPx() * scale)
            }

            // Draw vertex spheres
            vertices.forEach { v ->
                drawCircle(primaryColor, radius = 4.dp.toPx() * scale, center = v)
            }
        }
    }

    // 4. Draw Interactive 3D Measurement Tape & Snap Points
    if (isMeasuring && snapPoints.isNotEmpty()) {
        val projectedMap = mutableMapOf<String, Offset>()
        snapPoints.forEach { pt ->
            projectedMap[pt.id] = project(pt.x, pt.y, pt.z)
        }
        onProjectedSnapPoints(projectedMap)

        // Draw 3D snap point dots
        projectedMap.forEach { (id, screenPos) ->
            val isSelected = id == selectedSnapA || id == selectedSnapB
            if (isSelected) {
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = 0.3f),
                    radius = 16.dp.toPx(),
                    center = screenPos
                )
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 8.dp.toPx(),
                    center = screenPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = screenPos
                )
            } else {
                drawCircle(
                    color = Color(0xFF0284C7).copy(alpha = 0.8f),
                    radius = 6.dp.toPx(),
                    center = screenPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = screenPos
                )
            }
        }

        // Draw connecting 3D tape measure line
        if (selectedSnapA != null && selectedSnapB != null) {
            val pA = projectedMap[selectedSnapA]
            val pB = projectedMap[selectedSnapB]
            if (pA != null && pB != null) {
                // Laser line
                drawLine(
                    color = Color(0xFFEF4444),
                    start = pA,
                    end = pB,
                    strokeWidth = 3.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )

                // Dimension end caps
                val dx = pB.x - pA.x
                val dy = pB.y - pA.y
                val len = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                if (len > 1f) {
                    val nx = -dy / len * 10.dp.toPx()
                    val ny = dx / len * 10.dp.toPx()
                    drawLine(Color(0xFFEF4444), Offset(pA.x + nx, pA.y + ny), Offset(pA.x - nx, pA.y - ny), strokeWidth = 2.5.dp.toPx())
                    drawLine(Color(0xFFEF4444), Offset(pB.x + nx, pB.y + ny), Offset(pB.x - nx, pB.y - ny), strokeWidth = 2.5.dp.toPx())
                }

                // Midpoint distance label
                val midX = (pA.x + pB.x) / 2f
                val midY = (pA.y + pB.y) / 2f - 14.dp.toPx()
                val ptA = snapPoints.find { it.id == selectedSnapA }
                val ptB = snapPoints.find { it.id == selectedSnapB }
                if (ptA != null && ptB != null) {
                    val dist3D = Math.sqrt(
                        Math.pow((ptB.x - ptA.x) * 4.0, 2.0) +
                        Math.pow((ptB.y - ptA.y) * 4.0, 2.0) +
                        Math.pow((ptB.z - ptA.z) * 4.0, 2.0)
                    )
                    val measPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 28f
                        isFakeBoldText = true
                        textAlign = android.graphics.Paint.Align.CENTER
                        setShadowLayer(6f, 0f, 2f, android.graphics.Color.BLACK)
                    }
                    val bgPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#EF4444")
                        style = android.graphics.Paint.Style.FILL
                    }
                    val textStr = "${String.format(Locale.US, "%.2f", dist3D)} m"
                    val textWidth = measPaint.measureText(textStr)
                    drawContext.canvas.nativeCanvas.drawRoundRect(
                        midX - textWidth / 2f - 16f,
                        midY - 26f,
                        midX + textWidth / 2f + 16f,
                        midY + 14f,
                        8f,
                        8f,
                        bgPaint
                    )
                    drawContext.canvas.nativeCanvas.drawText(textStr, midX, midY, measPaint)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuidanceDetailView(
    item: LibraryItem,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                }
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    fontSize = 14.sp,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "Author: ${item.author} | Updated: ${item.dateAdded} | Format: ${item.format}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Interactive Technical Reference Sections based on Guide Type
            when (item.id) {
                "g1" -> {
                    // Indonesian Wood Handbook
                    item {
                        Text("Indonesian Hardwoods Reference (Kayu Konstruksi & Mebel)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(
                        listOf(
                            Triple("Kayu Jati (Teak)", "Class I-II (Durability/Strength). Density ~0.67 g/cm³. High natural oils, weather & termite resistant. Ideal for premium furniture & exterior doors.", "Kuat I-II • Awet I"),
                            Triple("Kayu Mahoni (Mahogany)", "Class II-III. Density ~0.56 g/cm³. Fine uniform grain, easy to turn and carve. Used for indoor cabinetry & musical instruments.", "Kuat II-III • Awet III"),
                            Triple("Kayu Merbau", "Class I-II. Density ~0.84 g/cm³. Extremely hard, low shrinkage, beautiful dark reddish brown. Heavy structural beams & outdoor decking.", "Kuat I • Awet I-II"),
                            Triple("Kayu Bangkirai (Yellow Balau)", "Class I-II. Density ~0.91 g/cm³. Heavy outdoor timber for pergolas, roof trusses, bridges & decking.", "Kuat I • Awet I"),
                            Triple("Kayu Ulin (Ironwood Kalimantan)", "Class I. Density ~1.04 g/cm³. Sinks in water. Extremely resistant to water rot, marine borers, and weathering.", "Kuat I • Awet I (Super Hard)"),
                            Triple("Kayu Kamper", "Class II. Density ~0.72 g/cm³. Distinctive aroma repels insects. Ideal for rafters, window frames & doors.", "Kuat II • Awet II"),
                            Triple("Kayu Sengon / Albasia", "Class IV-V. Density ~0.33 g/cm³. Lightweight, fast-growing. Formwork, crating, core plywood panels.", "Kuat IV-V • Ringan")
                        )
                    ) { (woodName, woodDetails, grade) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(woodName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text(grade, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Text(woodDetails, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                "g2" -> {
                    // MSDS & Chemical Safety
                    item {
                        Text("Chemical Safety & Precautionary Data Sheets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    items(
                        listOf(
                            Triple("SikaTop Seal 107 (Cementitious Waterproofing)", "PPE: Nitrile gloves, safety goggles, dust mask. Avoid skin contact. If in eyes, flush for 15 mins. Non-flammable.", "Hazard: Skin Irritant (Cement Dust)"),
                            Triple("PVC / CPVC Solvent Cement", "PPE: Organic vapor respirator, chemical resistant gloves, well-ventilated area. Highly flammable vapors. Keep away from sparks.", "Hazard: Flammable • Vapor Toxic"),
                            Triple("Contact Adhesive (Lem Kuning / Neoprene)", "PPE: Vapor mask, chemical gloves. Highly volatile solvent fumes. Ensure continuous cross-ventilation.", "Hazard: Flammable • Inhalation"),
                            Triple("Portland Cement Powder (Semen Abu)", "PPE: N95/P100 particulate respirator, heavy rubber gloves. Chromium VI allergen. Alkaline chemical burns when wet.", "Hazard: Alkaline Burns • Respiratory")
                        )
                    ) { (chemName, handling, hazard) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(chemName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text(hazard, fontSize = 11.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                                }
                                Text(handling, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                else -> {
                    // Standard Construction Guidance Checklist
                    item {
                        Text("Standard Specifications & Technical Reference", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Standard Operating Checklist:", fontWeight = FontWeight.Bold)
                                Text("• Verify SNI (Standar Nasional Indonesia) compliance before ordering aggregate & rebar.", style = MaterialTheme.typography.bodySmall)
                                Text("• Maintain minimum concrete cover: 20mm for slabs, 40mm for columns/beams exposed to weather.", style = MaterialTheme.typography.bodySmall)
                                Text("• Always cure newly poured concrete for minimum 7 to 14 days with wet burlap or curing compound.", style = MaterialTheme.typography.bodySmall)
                                Text("• Follow K3 Construction Safety: Hard hat, safety boots with steel toe, harness on scaffolding > 2m.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DOCUMENT DETAIL VIEW (PDF & Engineering Technical Sheets)
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentDetailView(
    item: LibraryItem,
    viewModel: LibraryViewModel,
    onBack: () -> Unit
) {
    var zoomScale by remember { mutableStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isFullScreen by remember { mutableStateOf(false) }
    var isMeasureMode by remember { mutableStateOf(false) }
    var measureStart by remember { mutableStateOf<Offset?>(null) }
    var measureEnd by remember { mutableStateOf<Offset?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var showSavedSheet by remember { mutableStateOf(false) }
    var activeSheetIndex by remember { mutableStateOf(0) }

    val allMeasurements by viewModel.savedMeasurements.collectAsState()
    val itemSavedCount = allMeasurements.count { it.itemId == item.id }

    val sheetTitles = listOf("Sheet 1: MEP Engineering Schematic", "Sheet 2: Load Schedule & Calculations", "Sheet 3: Bill of Quantities (BOQ)")

    val currentDistanceMeters = remember(measureStart, measureEnd, zoomScale) {
        if (measureStart != null && measureEnd != null) {
            val baseScale = 30.0f * zoomScale
            val dx = (measureEnd!!.x - measureStart!!.x) / baseScale
            val dy = (measureEnd!!.y - measureStart!!.y) / baseScale
            Math.hypot(dx.toDouble(), dy.toDouble())
        } else 0.0
    }

    val configuration = LocalConfiguration.current

    val context = LocalContext.current
    val detailFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var name = "Document_Asset"
            var sizeStr = "2.0 MB"
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1) name = it.getString(nameIdx) ?: name
                        val sizeIdx = it.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIdx != -1) {
                            val bytes = it.getLong(sizeIdx)
                            sizeStr = when {
                                bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
                                bytes >= 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
                                else -> "$bytes B"
                            }
                        }
                    }
                }
            } catch (_: Exception) {}

            val ext = if (name.contains(".")) "." + name.substringAfterLast(".").lowercase() else ""
            viewModel.addCustomItem(
                title = name,
                category = "Document",
                format = if (ext.isNotEmpty()) ext else ".pdf",
                size = sizeStr,
                description = "Opened document from storage ($name)",
                uriString = uri.toString()
            )
        }
    }

    var pdfBitmap by remember(item.uriString, activeSheetIndex) { mutableStateOf<Bitmap?>(null) }
    var loadedTextContent by remember(item.uriString) { mutableStateOf<String?>(null) }
    LaunchedEffect(item.uriString, activeSheetIndex) {
        if (item.uriString != null) {
            val uri = Uri.parse(item.uriString)
            val format = item.format.lowercase()
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                if (format.contains("pdf")) {
                    pdfBitmap = RealFileParser.renderPdfPage(context, uri, activeSheetIndex)
                } else {
                    loadedTextContent = RealFileParser.readTextFromUri(context, uri)
                    if (loadedTextContent.isNullOrBlank()) {
                        pdfBitmap = RealFileParser.renderPdfPage(context, uri, activeSheetIndex)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        if (!isFullScreen) {
            // Sleek Fixed Ultra-Compact Header Bar directly under app header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(18.dp))
                        }
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFDC2626))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(item.format, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        IconButton(onClick = { detailFilePickerLauncher.launch("*/*") }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.FolderOpen, contentDescription = "Browse Storage", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { showSavedSheet = true }, modifier = Modifier.size(32.dp)) {
                            BadgedBox(
                                badge = {
                                    if (itemSavedCount > 0) {
                                        Badge { Text("$itemSavedCount") }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = "Saved Measurements", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                        IconButton(onClick = { isFullScreen = !isFullScreen }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = {
                                panOffset = Offset.Zero
                                zoomScale = 1.0f
                                measureStart = null
                                measureEnd = null
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 1.dp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(if (!isFullScreen) Modifier.verticalScroll(rememberScrollState()) else Modifier)
        ) {
            // Sheet selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                sheetTitles.forEachIndexed { index, title ->
                    FilterChip(
                        selected = activeSheetIndex == index,
                        onClick = { activeSheetIndex = index },
                        label = { Text("Sheet ${index + 1}", fontSize = 11.sp) },
                        modifier = Modifier.height(26.dp),
                        leadingIcon = {
                            if (activeSheetIndex == index) {
                                Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        }
                    )
                }
            }

        // PDF Sheet Canvas Area (40% Screen Height in standard view mode)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isFullScreen) Modifier.fillMaxSize()
                    else Modifier.height((configuration.screenHeightDp * 0.40f).dp)
                )
                .background(Color(0xFF1E293B))
                .pointerInput(isMeasureMode) {
                    detectDragGestures(
                        onDragStart = { startPos ->
                            if (isMeasureMode) {
                                measureStart = startPos
                                measureEnd = startPos
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (isMeasureMode) {
                                measureEnd = change.position
                            } else {
                                panOffset = Offset(panOffset.x + dragAmount.x, panOffset.y + dragAmount.y)
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize().testTag("canvas_document_pdf_viewer")) {
                drawEngineeringDocument(
                    item = item,
                    sheetIndex = activeSheetIndex,
                    zoom = zoomScale,
                    pan = panOffset,
                    measureStart = measureStart,
                    measureEnd = measureEnd,
                    pdfBitmap = pdfBitmap,
                    loadedTextContent = loadedTextContent
                )
            }

            // Floating Active Tape Measurement HUD
            if (isMeasureMode && measureStart != null && measureEnd != null && currentDistanceMeters > 0.05) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column {
                            Text(
                                text = "Doc Tape: ${String.format(Locale.US, "%.2f", currentDistanceMeters)} m (${String.format(Locale.US, "%.2f", currentDistanceMeters * 3.28084)} ft)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFEF4444)
                            )
                            Text("Measured on Technical Drawing Sheet", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { showSaveDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                measureStart = null
                                measureEnd = null
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Clear", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Compact Zoom Bar with Slider (200x range)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                shadowElevation = 4.dp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { zoomScale = (zoomScale - 0.1f).coerceAtLeast(0.05f) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(14.dp))
                    }
                    Slider(
                        value = zoomScale,
                        onValueChange = { zoomScale = it },
                        valueRange = 0.05f..1.0f,
                        modifier = Modifier.width(110.dp).height(20.dp)
                    )
                    IconButton(
                        onClick = { zoomScale = (zoomScale + 0.1f).coerceAtMost(1.0f) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = "${(zoomScale * 100).toInt()}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 2.dp, end = 4.dp)
                    )
                }
            }

            // Fullscreen Toggle (Top Right)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (isFullScreen) {
                        IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                        }
                    }
                    IconButton(onClick = { isFullScreen = !isFullScreen }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (!isFullScreen) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        FilterChip(
                            selected = isMeasureMode,
                            onClick = {
                                isMeasureMode = !isMeasureMode
                                if (!isMeasureMode) {
                                    measureStart = null
                                    measureEnd = null
                                }
                            },
                            label = { Text(if (isMeasureMode) "Tape: Active" else "Measure Dot/Line", fontSize = 11.sp) },
                            leadingIcon = { Icon(Icons.Default.SquareFoot, null, modifier = Modifier.size(12.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        )
                        SuggestionChip(
                            onClick = { showSavedSheet = true },
                            label = { Text("Saved ($itemSavedCount)", fontSize = 11.sp) },
                            icon = { Icon(Icons.Default.BookmarkBorder, null, modifier = Modifier.size(12.dp)) }
                        )
                    }
                    Text("Vector PDF Rendering Engine", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }

    if (showSaveDialog && measureStart != null && measureEnd != null) {
        SaveMeasurementDialog(
            initialLabel = "Doc Span ${String.format(Locale.US, "%.2f", currentDistanceMeters)}m",
            distanceMeters = currentDistanceMeters,
            pointAName = "Point A (${measureStart!!.x.toInt()}px, ${measureStart!!.y.toInt()}px)",
            pointBName = "Point B (${measureEnd!!.x.toInt()}px, ${measureEnd!!.y.toInt()}px)",
            category = "Technical Document",
            onDismiss = { showSaveDialog = false },
            onSave = { label, note ->
                viewModel.saveMeasurement(
                    itemId = item.id,
                    itemTitle = item.title,
                    title = label,
                    distanceMeters = currentDistanceMeters.toFloat(),
                    pointA = "Point A",
                    pointB = "Point B",
                    mode = "Technical Document",
                    note = note
                )
                showSaveDialog = false
            }
        )
    }

    if (showSavedSheet) {
        SavedMeasurementsSheet(
            itemId = item.id,
            itemTitle = item.title,
            viewModel = viewModel,
            onDismiss = { showSavedSheet = false }
        )
    }
}
}

// -------------------------------------------------------------
// VECTOR TECHNICAL DOCUMENT DRAWING ENGINE (PDF / CAD SHEETS)
// -------------------------------------------------------------

fun DrawScope.drawEngineeringDocument(
    item: LibraryItem,
    sheetIndex: Int,
    zoom: Float,
    pan: Offset,
    measureStart: Offset?,
    measureEnd: Offset?,
    pdfBitmap: Bitmap? = null,
    loadedTextContent: String? = null
) {
    val originX = size.width / 2f + pan.x
    val originY = size.height / 2f + pan.y
    val sheetW = 320.dp.toPx() * zoom
    val sheetH = 460.dp.toPx() * zoom

    val sheetLeft = originX - sheetW / 2f
    val sheetTop = originY - sheetH / 2f
    val sheetRight = originX + sheetW / 2f
    val sheetBottom = originY + sheetH / 2f

    // 1. White Paper Canvas with drop shadow
    drawRect(Color(0xFFE2E8F0), topLeft = Offset(sheetLeft + 4, sheetTop + 4), size = Size(sheetW, sheetH))
    drawRect(Color(0xFFFFFFFF), topLeft = Offset(sheetLeft, sheetTop), size = Size(sheetW, sheetH))
    drawRect(Color(0xFF0F172A), topLeft = Offset(sheetLeft, sheetTop), size = Size(sheetW, sheetH), style = Stroke(width = 2.dp.toPx() * zoom))

    // 2. Inner Border Margin
    val margin = 12.dp.toPx() * zoom
    val innerLeft = sheetLeft + margin
    val innerTop = sheetTop + margin
    val innerRight = sheetRight - margin
    val innerBottom = sheetBottom - margin
    drawRect(Color(0xFF1E293B), topLeft = Offset(innerLeft, innerTop), size = Size(innerRight - innerLeft, innerBottom - innerTop), style = Stroke(width = 1.dp.toPx() * zoom))

    if (pdfBitmap != null) {
        // Draw real PDF rendered page bitmap onto paper sheet
        val bmpW = (innerRight - innerLeft).toInt().coerceAtLeast(10)
        val bmpH = (innerBottom - innerTop - 50.dp.toPx() * zoom).toInt().coerceAtLeast(10)
        drawImage(
            image = pdfBitmap.asImageBitmap(),
            dstOffset = IntOffset(innerLeft.toInt(), innerTop.toInt()),
            dstSize = IntSize(bmpW, bmpH)
        )
    } else if (!loadedTextContent.isNullOrBlank()) {
        // Draw real text file lines
        val textPaint = Paint().apply {
            isAntiAlias = true
            typeface = Typeface.MONOSPACE
            color = android.graphics.Color.DKGRAY
            textSize = (10f * zoom).coerceAtLeast(7f)
        }
        val lines = loadedTextContent.lines().take(30)
        var lineY = innerTop + 18.dp.toPx() * zoom
        lines.forEach { line ->
            if (lineY < innerBottom - 50.dp.toPx() * zoom) {
                drawContext.canvas.nativeCanvas.drawText(
                    if (line.length > 45) line.substring(0, 45) + "…" else line,
                    innerLeft + 6.dp.toPx() * zoom,
                    lineY,
                    textPaint
                )
                lineY += 14.dp.toPx() * zoom
            }
        }
    }

    // 3. Title Block (Header & Footer)
    val titleBlockH = 45.dp.toPx() * zoom
    val titleBlockTop = innerBottom - titleBlockH
    drawLine(Color(0xFF1E293B), Offset(innerLeft, titleBlockTop), Offset(innerRight, titleBlockTop), strokeWidth = 1.5.dp.toPx() * zoom)

    val paint = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
        color = android.graphics.Color.BLACK
        textSize = (10f * zoom).coerceAtLeast(8f)
    }

    // Title Block Text
    drawContext.canvas.nativeCanvas.drawText("PROJECT: ${item.title.uppercase()}", innerLeft + 8.dp.toPx() * zoom, titleBlockTop + 14.dp.toPx() * zoom, paint)
    paint.textSize = (8f * zoom).coerceAtLeast(6f)
    paint.typeface = Typeface.DEFAULT
    drawContext.canvas.nativeCanvas.drawText("DISCIPLINE: MEP / ELECTRICAL & HVAC SPECIFICATION", innerLeft + 8.dp.toPx() * zoom, titleBlockTop + 26.dp.toPx() * zoom, paint)
    drawContext.canvas.nativeCanvas.drawText("SCALE: 1:100 • DWG NO: MEP-${sheetIndex + 1}01 • REV B", innerLeft + 8.dp.toPx() * zoom, titleBlockTop + 36.dp.toPx() * zoom, paint)

    // Engineering Stamp
    val stampLeft = innerRight - 70.dp.toPx() * zoom
    val stampTop = titleBlockTop + 4.dp.toPx() * zoom
    drawRect(Color(0xFFDC2626), topLeft = Offset(stampLeft, stampTop), size = Size(64.dp.toPx() * zoom, 34.dp.toPx() * zoom), style = Stroke(width = 1.dp.toPx() * zoom))
    val stampPaint = Paint().apply {
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        color = android.graphics.Color.RED
        textSize = (7f * zoom).coerceAtLeast(5f)
    }
    drawContext.canvas.nativeCanvas.drawText("SNI CERTIFIED", stampLeft + 6.dp.toPx() * zoom, stampTop + 14.dp.toPx() * zoom, stampPaint)
    drawContext.canvas.nativeCanvas.drawText("APPROVED PE", stampLeft + 8.dp.toPx() * zoom, stampTop + 26.dp.toPx() * zoom, stampPaint)

    // 4. Draw Schematic Diagrams based on sheetIndex
    when (sheetIndex) {
        0 -> {
            // Sheet 1: Single Line Diagram & HVAC Ductwork Schematic
            val contentTop = innerTop + 16.dp.toPx() * zoom

            val headerPaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.DKGRAY
                textSize = (11f * zoom).coerceAtLeast(9f)
            }
            drawContext.canvas.nativeCanvas.drawText("HVAC DUCTING & 3-PHASE ELECTRICAL RISER DIAGRAM", innerLeft + 8.dp.toPx() * zoom, contentTop, headerPaint)

            // Main Transformer / Incomer Box
            val trafoLeft = innerLeft + 20.dp.toPx() * zoom
            val trafoTop = contentTop + 20.dp.toPx() * zoom
            val trafoW = 60.dp.toPx() * zoom
            val trafoH = 40.dp.toPx() * zoom
            drawRect(Color(0xFF0284C7), topLeft = Offset(trafoLeft, trafoTop), size = Size(trafoW, trafoH), style = Stroke(width = 1.5.dp.toPx() * zoom))
            drawCircle(Color(0xFF0284C7), radius = 12.dp.toPx() * zoom, center = Offset(trafoLeft + trafoW / 2, trafoTop + trafoH / 2), style = Stroke(width = 1.2.dp.toPx() * zoom))

            paint.textSize = (7f * zoom).coerceAtLeast(5f)
            drawContext.canvas.nativeCanvas.drawText("PLN 415V / 3P", trafoLeft + 4.dp.toPx() * zoom, trafoTop + trafoH + 10.dp.toPx() * zoom, paint)

            // Busbar Trunking Line
            val busY = trafoTop + trafoH / 2
            val busEnd = innerRight - 20.dp.toPx() * zoom
            drawLine(Color(0xFFDC2626), Offset(trafoLeft + trafoW, busY), Offset(busEnd, busY), strokeWidth = 3.dp.toPx() * zoom)

            // Feeder Drops
            val drops = listOf("MDP-01 (Main DB)", "HVAC-CH-1", "PP-LIGHT-L1", "PP-POWER-L2")
            drops.forEachIndexed { i, name ->
                val dropX = trafoLeft + trafoW + 25.dp.toPx() * zoom + i * 45.dp.toPx() * zoom
                drawLine(Color(0xFF1E293B), Offset(dropX, busY), Offset(dropX, busY + 50.dp.toPx() * zoom), strokeWidth = 1.5.dp.toPx() * zoom)
                drawRect(Color(0xFF0F172A), topLeft = Offset(dropX - 16.dp.toPx() * zoom, busY + 50.dp.toPx() * zoom), size = Size(32.dp.toPx() * zoom, 24.dp.toPx() * zoom), style = Stroke(width = 1.dp.toPx() * zoom))
                drawContext.canvas.nativeCanvas.drawText(name, dropX - 18.dp.toPx() * zoom, busY + 86.dp.toPx() * zoom, paint)
            }

            // HVAC Air Handling Loop (Ductwork)
            val ductTop = busY + 110.dp.toPx() * zoom
            val ductW = innerRight - innerLeft - 40.dp.toPx() * zoom
            drawRect(Color(0xFF0D9488), topLeft = Offset(innerLeft + 20.dp.toPx() * zoom, ductTop), size = Size(ductW, 30.dp.toPx() * zoom), style = Stroke(width = 1.5.dp.toPx() * zoom))

            for (k in 0..5) {
                val diffX = innerLeft + 35.dp.toPx() * zoom + k * (ductW / 6f)
                drawCircle(Color(0xFF0D9488), radius = 6.dp.toPx() * zoom, center = Offset(diffX, ductTop + 15.dp.toPx() * zoom))
                drawLine(Color(0xFF0D9488), Offset(diffX, ductTop + 30.dp.toPx() * zoom), Offset(diffX, ductTop + 45.dp.toPx() * zoom), strokeWidth = 1.dp.toPx() * zoom)
            }
            drawContext.canvas.nativeCanvas.drawText("SUPPLY AIR DUCT 500x300mm • 1200 CFM • 6 DIFFUSERS", innerLeft + 20.dp.toPx() * zoom, ductTop + 60.dp.toPx() * zoom, paint)
        }
        1 -> {
            // Sheet 2: Load Schedule Table
            val contentTop = innerTop + 16.dp.toPx() * zoom
            val headerPaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.DKGRAY
                textSize = (11f * zoom).coerceAtLeast(9f)
            }
            drawContext.canvas.nativeCanvas.drawText("PANEL LOAD CALCULATION & BREAKER SCHEDULE", innerLeft + 8.dp.toPx() * zoom, contentTop, headerPaint)

            val tableTop = contentTop + 14.dp.toPx() * zoom
            val rowH = 18.dp.toPx() * zoom
            val cols = listOf("CKT", "DESCRIPTION", "LOAD (VA)", "BREAKER", "CABLE SIZE")
            val colX = listOf(innerLeft + 10.dp.toPx() * zoom, innerLeft + 35.dp.toPx() * zoom, innerLeft + 130.dp.toPx() * zoom, innerLeft + 185.dp.toPx() * zoom, innerLeft + 235.dp.toPx() * zoom)

            // Header row
            drawRect(Color(0xFFE2E8F0), topLeft = Offset(innerLeft + 8.dp.toPx() * zoom, tableTop), size = Size(innerRight - innerLeft - 16.dp.toPx() * zoom, rowH))
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            cols.forEachIndexed { idx, col ->
                drawContext.canvas.nativeCanvas.drawText(col, colX[idx], tableTop + 12.dp.toPx() * zoom, paint)
            }

            paint.typeface = Typeface.DEFAULT
            val rows = listOf(
                listOf("1", "Lighting Ground Floor", "1,250 VA", "10A 1P MCB", "3x 2.5 mm² NYM"),
                listOf("2", "Receptacles & Power", "2,400 VA", "16A 1P MCB", "3x 2.5 mm² NYM"),
                listOf("3", "HVAC Inverter 2 PK", "1,850 VA", "16A 1P MCB", "3x 4.0 mm² NYY"),
                listOf("4", "Kitchen Induction Island", "3,500 VA", "25A 1P MCB", "3x 6.0 mm² NYY"),
                listOf("5", "Water Heater & Pumps", "1,600 VA", "16A 1P RCBO", "3x 2.5 mm² NYM"),
                listOf("6", "Spare Circuit / EV Ready", "7,000 VA", "32A 2P MCB", "3x 10 mm² NYY")
            )

            rows.forEachIndexed { rIdx, rowData ->
                val rY = tableTop + (rIdx + 1) * rowH
                drawLine(Color(0xFFCBD5E1), Offset(innerLeft + 8.dp.toPx() * zoom, rY), Offset(innerRight - 8.dp.toPx() * zoom, rY), strokeWidth = 0.5.dp.toPx() * zoom)
                rowData.forEachIndexed { cIdx, cell ->
                    drawContext.canvas.nativeCanvas.drawText(cell, colX[cIdx], rY + 12.dp.toPx() * zoom, paint)
                }
            }
        }
        else -> {
            // Sheet 3: BOQ & General Notes
            val contentTop = innerTop + 16.dp.toPx() * zoom
            val headerPaint = Paint().apply {
                isAntiAlias = true
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.DKGRAY
                textSize = (11f * zoom).coerceAtLeast(9f)
            }
            drawContext.canvas.nativeCanvas.drawText("BILL OF QUANTITIES & REGULATORY COMPLIANCE", innerLeft + 8.dp.toPx() * zoom, contentTop, headerPaint)

            val notesTop = contentTop + 20.dp.toPx() * zoom
            val notes = listOf(
                "1. All electrical installations must adhere to PUIL 2011 & SNI 04-0225.",
                "2. Earthing resistance shall not exceed 2.0 Ohms tested at main rod pit.",
                "3. Conduits embedded in reinforced concrete must be high-impact PVC (E20).",
                "4. HVAC refrigerant pipes (R-32 / R-410A) tested at 450 PSI for 24 hours.",
                "5. Fire dampers installed at all drywall and masonry wall penetrations."
            )
            paint.textSize = (8f * zoom).coerceAtLeast(6f)
            notes.forEachIndexed { i, note ->
                drawContext.canvas.nativeCanvas.drawText(note, innerLeft + 12.dp.toPx() * zoom, notesTop + i * 22.dp.toPx() * zoom, paint)
            }
        }
    }

    // 5. Tape Measurement Overlay Line
    if (measureStart != null && measureEnd != null) {
        val dx = measureEnd.x - measureStart.x
        val dy = measureEnd.y - measureStart.y
        val distPx = Math.hypot(dx.toDouble(), dy.toDouble())
        val baseScale = 30.0f * zoom
        val distMeters = distPx / baseScale

        drawLine(Color(0xFFEF4444), measureStart, measureEnd, strokeWidth = 2.dp.toPx())
        drawCircle(Color(0xFFEF4444), radius = 5.dp.toPx(), center = measureStart)
        drawCircle(Color(0xFFEF4444), radius = 5.dp.toPx(), center = measureEnd)

        val midX = (measureStart.x + measureEnd.x) / 2f
        val midY = (measureStart.y + measureEnd.y) / 2f

        drawRect(Color(0xFFEF4444), topLeft = Offset(midX - 35.dp.toPx(), midY - 14.dp.toPx()), size = Size(70.dp.toPx(), 20.dp.toPx()), style = Fill)
        val measPaint = Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.WHITE
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        drawContext.canvas.nativeCanvas.drawText("${"%.2f".format(distMeters)} m", midX, midY + 3.dp.toPx(), measPaint)
    }
}

// -------------------------------------------------------------
// SAVE MEASUREMENT DIALOG & SAVED MEASUREMENTS BOTTOM SHEET
// -------------------------------------------------------------

@Composable
fun SaveMeasurementDialog(
    initialLabel: String,
    distanceMeters: Double,
    pointAName: String,
    pointBName: String,
    category: String,
    onDismiss: () -> Unit,
    onSave: (label: String, note: String) -> Unit
) {
    var label by remember { mutableStateOf(initialLabel) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Tape Measurement", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Distance: ${String.format(Locale.US, "%.2f", distanceMeters)} m (${String.format(Locale.US, "%.2f", distanceMeters * 3.28084)} ft)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "From: $pointAName  →  To: $pointBName",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Context: $category",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Measurement Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_save_measurement_label")
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes / Specification (optional)") },
                    placeholder = { Text("e.g. Wall span clearance, HVAC duct length") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("input_save_measurement_note")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(label.ifBlank { "Measured Span" }, note) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("btn_confirm_save_measurement")
            ) {
                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save to Project")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedMeasurementsSheet(
    itemId: String,
    itemTitle: String,
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit
) {
    val measurements by viewModel.savedMeasurements.collectAsState()
    val filteredMeasurements = remember(measurements, itemId) {
        measurements.filter { it.itemId == itemId }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Saved Tape Measurements",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = itemTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${filteredMeasurements.size} saved",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            HorizontalDivider()

            if (filteredMeasurements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SquareFoot,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outlineVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No saved measurements for this item yet",
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Use the tape measure tool to drag and measure any dot, snap point, or line, then click Save.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredMeasurements, key = { it.id }) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(item.mode, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${String.format(Locale.US, "%.2f", item.distanceMeters)} m (${String.format(Locale.US, "%.2f", item.distanceFeet)} ft)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFEF4444)
                                    )
                                    Text(
                                        text = "${item.pointA} → ${item.pointB} • ${item.timestamp}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (item.note.isNotBlank()) {
                                        Text(
                                            text = "Note: ${item.note}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.deleteMeasurement(item.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Delete Measurement",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
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

// -------------------------------------------------------------
// EXTERNAL APP VIEWER & PLAY STORE SHORTCUT HELPERS & DIALOG
// -------------------------------------------------------------

data class ExternalViewerInfo(
    val appName: String,
    val packageName: String,
    val mimeType: String,
    val categoryName: String,
    val description: String
)

fun getExternalViewerInfo(format: String): ExternalViewerInfo {
    val fmt = format.lowercase()
    return when {
        fmt.contains("skp") || fmt.contains("skb") -> ExternalViewerInfo(
            appName = "SketchUp Viewer",
            packageName = "com.trimble.buildings.sketchup.sketchupviewer",
            mimeType = "application/x-sketchup",
            categoryName = "SketchUp 3D Models",
            description = "Official Trimble Play Store application to view, orbit, measure, and inspect SketchUp (.skp / .skb) architectural models."
        )
        fmt.contains("dwg") || fmt.contains("dxf") || fmt.contains("dwf") -> ExternalViewerInfo(
            appName = "AutoCAD Mobile / DWG FastView",
            packageName = "com.autodesk.autocadws",
            mimeType = "image/x-dwg",
            categoryName = "2D/3D CAD Blueprints",
            description = "Industry standard CAD viewer to open, view layer properties, and inspect DWG & DXF architectural floorplans."
        )
        fmt.contains("pdf") -> ExternalViewerInfo(
            appName = "Adobe Acrobat Reader",
            packageName = "com.adobe.reader",
            mimeType = "application/pdf",
            categoryName = "PDF Technical Specs",
            description = "Official Adobe Acrobat reader for viewing MEP schematics, building codes, and material specification sheets."
        )
        fmt.contains("ifc") || fmt.contains("rvt") || fmt.contains("nwd") || fmt.contains("bim") -> ExternalViewerInfo(
            appName = "Autodesk BIM 360",
            packageName = "com.autodesk.bim360.docs",
            mimeType = "application/x-step",
            categoryName = "BIM Structural Models",
            description = "Inspect complex BIM structural components, rebar grids, IFC, and Revit project files on mobile."
        )
        fmt.contains("step") || fmt.contains("stp") || fmt.contains("iges") -> ExternalViewerInfo(
            appName = "CAD Assistant 3D",
            packageName = "org.opencascade.cadassistant",
            mimeType = "application/x-step",
            categoryName = "Mechanical STEP CAD",
            description = "Open and inspect STEP, IGES, and BREP mechanical assemblies and 3D industrial components."
        )
        else -> ExternalViewerInfo( // .obj, .glb, .gltf, .stl, .fbx, .3ds
            appName = "3D Model Viewer",
            packageName = "com.scapenet.view3d",
            mimeType = "model/gltf-binary",
            categoryName = "3D Mesh & Models",
            description = "Play Store app to open and view OBJ, GLB, GLTF, STL, and FBX 3D meshes with custom lighting and rotation controls."
        )
    }
}

fun launchPlayStoreApp(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

fun launchExternalViewerIntent(context: Context, item: LibraryItem, viewerInfo: ExternalViewerInfo) {
    val uriString = item.uriString
    var launched = false
    if (!uriString.isNullOrEmpty()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(uriString), viewerInfo.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            launched = true
        } catch (_: Exception) {}
    }

    if (!launched) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = viewerInfo.mimeType
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open with ${viewerInfo.appName}"))
            launched = true
        } catch (_: Exception) {}
    }

    if (!launched) {
        launchPlayStoreApp(context, viewerInfo.packageName)
    }
}

@Composable
fun ExternalViewerDialog(
    item: LibraryItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val viewerInfo = remember(item.format) { getExternalViewerInfo(item.format) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open in External App", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Launch '${item.title}' in a dedicated external Android application supported on Google Play Store.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "RECOMMENDED PLAY STORE APP",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.format,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = viewerInfo.appName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = viewerInfo.description,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }

                OutlinedButton(
                    onClick = {
                        launchExternalViewerIntent(context, item, viewerInfo)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_launch_external_app")
                ) {
                    Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch Installed App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        launchPlayStoreApp(context, viewerInfo.packageName)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_open_playstore_viewer")
                ) {
                    Icon(Icons.Default.Shop, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Get / Open ${viewerInfo.appName} on Play Store", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", fontSize = 12.sp)
            }
        }
    )
}
