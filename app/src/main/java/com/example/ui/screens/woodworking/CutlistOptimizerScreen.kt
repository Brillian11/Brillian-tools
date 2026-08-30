package com.example.ui.screens.woodworking

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LinearScale
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.math.CutPiece
import com.example.domain.math.CutlistDraftStore
import com.example.domain.math.CutlistExportHelper
import com.example.domain.math.CutlistOptimizationResult
import com.example.domain.math.CutlistOptimizerEngine
import com.example.domain.math.DimensionUnit
import com.example.domain.math.MaterialType
import com.example.domain.math.StockBoard
import com.example.domain.math.StockProfilePreset
import com.example.domain.math.UsedBoardLayout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CutlistOptimizerScreen(
    viewModel: CutlistOptimizerViewModel,
    modifier: Modifier = Modifier,
    onNavigateToAi: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initProjects(context)
    }

    val activeProjectName by viewModel.activeProjectName.collectAsState()
    val autoSaveStatus by viewModel.autoSaveStatus.collectAsState()
    val savedProjects by viewModel.savedProjects.collectAsState()
    val dimensionUnit by viewModel.dimensionUnit.collectAsState()
    val rawStocks by viewModel.rawStocks.collectAsState()
    val selectedStockIndex by viewModel.selectedStockIndex.collectAsState()
    val autoScaleStockEnabled by viewModel.autoScaleStockEnabled.collectAsState()
    val requestedCuts by viewModel.requestedCuts.collectAsState()
    val bladeKerf by viewModel.bladeKerf.collectAsState()
    val trimMargin by viewModel.trimMargin.collectAsState()
    val allowRipCuts by viewModel.allowRipCuts.collectAsState()
    val result by viewModel.optimizationResult.collectAsState()
    val projectNotes by viewModel.projectNotes.collectAsState()
    val customPresets by viewModel.customPresets.collectAsState()

    // Custom Material Type Creation Dialog States
    var showAddCustomTypeDialog by remember { mutableStateOf(false) }
    var newCustomTypeName by remember { mutableStateOf("") }
    var newCustomTypeIsSheet by remember { mutableStateOf(false) }
    var newCustomTypeLength by remember(dimensionUnit) { mutableStateOf(if (dimensionUnit == DimensionUnit.CM) "200" else "2000") }
    var newCustomTypeWidth by remember(dimensionUnit) { mutableStateOf(if (dimensionUnit == DimensionUnit.CM) "20" else "200") }
    var newCustomTypeThickness by remember(dimensionUnit) { mutableStateOf(if (dimensionUnit == DimensionUnit.CM) "2" else "20") }

    // Add piece local inputs
    var newLabel by remember { mutableStateOf("") }
    var newLength by remember(dimensionUnit) { mutableStateOf(if (dimensionUnit == DimensionUnit.CM) "100" else "1000") }
    var newWidth by remember(dimensionUnit) { mutableStateOf(if (dimensionUnit == DimensionUnit.CM) "10" else "100") }
    var newThickness by remember(dimensionUnit) { mutableStateOf(if (dimensionUnit == DimensionUnit.CM) "2" else "20") }
    var newQty by remember { mutableStateOf("1") }

    var show3DPreview by remember { mutableStateOf(false) }

    // 3D Isometric View State
    var yawAngle by remember { mutableFloatStateOf(45f) }
    var pitchAngle by remember { mutableFloatStateOf(35f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var isHandToolActive by remember { mutableStateOf(false) }
    var is3DFullscreen by remember { mutableStateOf(false) }
    var isWireframeMode by remember { mutableStateOf(false) }

    // Dialog & Sheet States
    var showProjectsSheet by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var newProjectNameText by remember { mutableStateOf("") }
    var showProjectDropdownMenu by remember { mutableStateOf(false) }
    var showNotesBOMDialog by remember { mutableStateOf(false) }
    var quickNonCutInput by remember { mutableStateOf("") }
    var isHardwareSectionExpanded by remember { mutableStateOf(false) }

    // Add / Edit Stock Dialog States
    var showAddStockDialog by remember { mutableStateOf(false) }
    var stockDialogName by remember { mutableStateOf("") }
    var stockDialogType by remember { mutableStateOf(MaterialType.TIMBER_BOARD) }
    var stockDialogLength by remember { mutableStateOf("") }
    var stockDialogWidth by remember { mutableStateOf("") }
    var stockDialogThickness by remember { mutableStateOf("") }
    var stockDialogQty by remember { mutableStateOf("2") }
    var stockDialogCost by remember { mutableStateOf("0") }
    var editingStock by remember { mutableStateOf<StockBoard?>(null) }

    // Edit/Revise Piece Dialog State
    var editingPiece by remember { mutableStateOf<CutPiece?>(null) }
    var editPieceLabel by remember { mutableStateOf("") }
    var editPieceLength by remember { mutableStateOf("") }
    var editPieceWidth by remember { mutableStateOf("") }
    var editPieceQty by remember { mutableStateOf("") }
    var editPieceThickness by remember { mutableStateOf("") }

    val projectsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
            // Header Info Box
            com.example.ui.components.ToolInfoBox(
                icon = Icons.Default.Straighten,
                title = "Cut List Optimizer (1D & 2D)",
                description = "Smart stock cut list planner supporting multiple raw stocks, board ripping, CM/MM units, interactive 3D assembly, and PDF/Excel export."
            )

            // --- TOP PROJECT CONTROLS & COMPACT ACTIONS BAR ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Row 1: Active Project Title, Auto-Save Status, Notes Badge, Compact Ask AI & Menu
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Project Title & Rename Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    renameText = activeProjectName
                                    showRenameDialog = true
                                }
                                .padding(vertical = 4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FolderOpen,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE PROJECT",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 10.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    // Auto-save live indicator chip
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Text(
                                                text = autoSaveStatus,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = activeProjectName,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Rename Project",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Project Action Controls: Notes / Hardware BOM, Refined Ask AI, and Overflow Dropdown
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // "Notes & BOM" Button with count indicator badge
                            val nonCutItemsCount = projectNotes.split(";").map { it.trim() }.filter { it.isNotEmpty() }.size
                            IconButton(
                                onClick = { showNotesBOMDialog = true },
                                modifier = Modifier.size(38.dp).testTag("btn_project_notes_bom")
                            ) {
                                if (nonCutItemsCount > 0) {
                                    BadgedBox(
                                        badge = {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.secondary,
                                                contentColor = MaterialTheme.colorScheme.onSecondary
                                            ) {
                                                Text("$nonCutItemsCount", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp))
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingBag,
                                            contentDescription = "Project Notes & Hardware BOM",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = "Project Notes & Hardware BOM",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Refined, sleek "Ask AI" Action Button (compact tonal pill)
                            FilledTonalButton(
                                onClick = {
                                    viewModel.saveCurrentProject(context)
                                    val mentionPrompt = "Regarding project \"$activeProjectName\": "
                                    if (onNavigateToAi != null) {
                                        onNavigateToAi(mentionPrompt)
                                    } else {
                                        com.example.domain.agent.AiSessionBridge.startFreshSessionWithPrompt(
                                            title = "Cutlist: $activeProjectName",
                                            prompt = mentionPrompt,
                                            autoSend = false
                                        )
                                        Toast.makeText(context, "Mentioning project '$activeProjectName' in AI Copilot", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(36.dp).testTag("btn_cutlist_ask_ai")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ask AI",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            // Single Overflow Dropdown Menu Button
                            Box {
                                IconButton(
                                    onClick = { showProjectDropdownMenu = true },
                                    modifier = Modifier.size(38.dp).testTag("btn_project_dropdown_menu")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Project Actions Menu",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = showProjectDropdownMenu,
                                    onDismissRequest = { showProjectDropdownMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                        },
                                        text = {
                                            Column {
                                                Text("Project Notes & Hardware (BOM)", fontWeight = FontWeight.SemiBold)
                                                Text("Manage LEDs, fasteners, accessories & notes", style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        onClick = {
                                            showProjectDropdownMenu = false
                                            showNotesBOMDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        },
                                        text = {
                                            Column {
                                                Text("Consult AI Copilot", fontWeight = FontWeight.SemiBold)
                                                Text("Mention active project in AI session", style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        onClick = {
                                            showProjectDropdownMenu = false
                                            viewModel.saveCurrentProject(context)
                                            val mentionPrompt = "Regarding project \"$activeProjectName\": "
                                            if (onNavigateToAi != null) {
                                                onNavigateToAi(mentionPrompt)
                                            } else {
                                                com.example.domain.agent.AiSessionBridge.startFreshSessionWithPrompt(
                                                    title = "Cutlist: $activeProjectName",
                                                    prompt = mentionPrompt,
                                                    autoSend = false
                                                )
                                                Toast.makeText(context, "Mentioning project '$activeProjectName' in AI Copilot", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        },
                                        text = {
                                            Column {
                                                Text("Switch / Manage Projects", fontWeight = FontWeight.SemiBold)
                                                Text("Browse or open saved project sheets", style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        onClick = {
                                            showProjectDropdownMenu = false
                                            showProjectsSheet = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                        },
                                        text = {
                                            Column {
                                                Text("New Blank Project", fontWeight = FontWeight.SemiBold)
                                                Text("Start fresh cut plan with blank sheet", style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        onClick = {
                                            showProjectDropdownMenu = false
                                            newProjectNameText = ""
                                            showNewProjectDialog = true
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(Icons.Default.BookmarkBorder, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                        },
                                        text = {
                                            Column {
                                                Text("Save As New Copy", fontWeight = FontWeight.SemiBold)
                                                Text("Duplicate active project under new name", style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        onClick = {
                                            showProjectDropdownMenu = false
                                            viewModel.saveAsNewProject(context, "$activeProjectName (Copy)")
                                            Toast.makeText(context, "Saved as new copy!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        },
                                        text = { Text("Rename Project") },
                                        onClick = {
                                            showProjectDropdownMenu = false
                                            renameText = activeProjectName
                                            showRenameDialog = true
                                        }
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF2E7D32))
                                        },
                                        text = {
                                            Column {
                                                Text("Save Cutlist to Notes 📝", fontWeight = FontWeight.Bold)
                                                Text("Save layout specs & PDF attachment to Notes", style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        onClick = {
                                            showProjectDropdownMenu = false
                                            viewModel.exportMarkdownNoteToDB(context)
                                            Toast.makeText(context, "Cutlist saved to Field Notes as Markdown!", Toast.LENGTH_LONG).show()
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        },
                                        text = {
                                            Column {
                                                Text("Export as PDF Blueprint", fontWeight = FontWeight.SemiBold)
                                                Text("Includes 3D view, cut diagrams & spec table", style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        onClick = {
                                            showProjectDropdownMenu = false
                                            val uri = viewModel.exportPdf(context)
                                            if (uri != null) {
                                                CutlistExportHelper.openExportedFile(context, uri, "application/pdf")
                                            } else {
                                                Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF1D6F42))
                                        },
                                        text = {
                                            Column {
                                                Text("Export as Excel / CSV", fontWeight = FontWeight.SemiBold)
                                                Text("Complete parts coordinate data table", style = MaterialTheme.typography.labelSmall)
                                            }
                                        },
                                        onClick = {
                                            showProjectDropdownMenu = false
                                            val uri = viewModel.exportExcel(context)
                                            if (uri != null) {
                                                CutlistExportHelper.openExportedFile(context, uri, "text/csv")
                                            } else {
                                                Toast.makeText(context, "Failed to generate CSV", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    )
                                    DropdownMenuItem(
                                        leadingIcon = {
                                            Icon(Icons.Default.Save, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        },
                                        text = { Text("Force Save Now") },
                                        onClick = {
                                            showProjectDropdownMenu = false
                                            viewModel.saveCurrentProject(context)
                                            Toast.makeText(context, "Project \"$activeProjectName\" saved successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Row 2: Unit Switcher (CM vs MM) Selector Bar
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Measurement Unit:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Compact Segmented Pill Toggle
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(3.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (dimensionUnit == DimensionUnit.CM) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    modifier = Modifier
                                        .clickable { viewModel.setDimensionUnit(DimensionUnit.CM) }
                                        .testTag("chip_unit_cm")
                                ) {
                                    Text(
                                        text = "CM (cm)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (dimensionUnit == DimensionUnit.CM) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (dimensionUnit == DimensionUnit.CM) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (dimensionUnit == DimensionUnit.MM) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    modifier = Modifier
                                        .clickable { viewModel.setDimensionUnit(DimensionUnit.MM) }
                                        .testTag("chip_unit_mm")
                                ) {
                                    Text(
                                        text = "MM (mm)",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (dimensionUnit == DimensionUnit.MM) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (dimensionUnit == DimensionUnit.MM) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // AI Draft Banner
            var hasDraftState by remember { mutableStateOf(CutlistDraftStore.hasNewDraft) }
            if (hasDraftState) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Straighten,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Design Draft Available",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI Copilot generated a cut plan for \"${CutlistDraftStore.projectName}\" (${CutlistDraftStore.dimensions}). Loading it will create a new saved project without overwriting your current work.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    CutlistDraftStore.clearDraft()
                                    hasDraftState = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Text("Dismiss")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    viewModel.loadDraft(
                                        context,
                                        CutlistDraftStore.projectName,
                                        CutlistDraftStore.pendingDraft ?: emptyList(),
                                        CutlistDraftStore.notes
                                    )
                                    CutlistDraftStore.clearDraft()
                                    hasDraftState = false
                                    show3DPreview = true
                                    Toast.makeText(context, "Draft loaded as new project!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Load as New Project")
                            }
                        }
                    }
                }
            }

            // Summary Yield Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                CutlistStatBadge(
                    title = "MATERIAL YIELD",
                    value = String.format(Locale.US, "%.1f%%", result.yieldPercentage),
                    unit = "${result.usedBoards.size} board/sheet unit(s) used",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                CutlistStatBadge(
                    title = "TOTAL SCRAP LOSS",
                    value = if (result.usedBoards.any { it.materialType == MaterialType.PLYWOOD_SHEET }) {
                        String.format(Locale.US, "%.2f m²", result.totalScrapLengthMm / 1_000_000.0)
                    } else {
                        dimensionUnit.format(result.totalScrapLengthMm)
                    },
                    unit = if (result.usedBoards.any { it.materialType == MaterialType.PLYWOOD_SHEET }) "scrap area" else "scrap length",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            // 3D Isometric View Trigger Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Rotate90DegreesCcw,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "3D Isometric Vector Draft",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "Interactive 3D viewport with 360° orbit, hand pan tool & zoom.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Button(
                            onClick = { show3DPreview = !show3DPreview },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_toggle_3d_preview")
                        ) {
                            Text(text = if (show3DPreview) "Hide 3D" else "Render 3D")
                        }
                    }

                    if (show3DPreview) {
                        // 3D Viewport Controls Bar
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Tool Selector: Orbit Mode vs Hand Tool
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Orbit / Rotate Tool
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (!isHandToolActive) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                        modifier = Modifier.clickable { isHandToolActive = false }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Rotate90DegreesCcw,
                                                contentDescription = "Orbit Mode",
                                                tint = if (!isHandToolActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Orbit",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (!isHandToolActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    // Hand Tool (Pan / Shift)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isHandToolActive) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                                        modifier = Modifier
                                            .clickable { isHandToolActive = true }
                                            .testTag("btn_3d_hand_tool")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PanTool,
                                                contentDescription = "Hand Tool (Pan)",
                                                tint = if (isHandToolActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = "Hand",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isHandToolActive) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                // Zoom Controls & Reset View
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(
                                        onClick = { zoomScale = (zoomScale / 1.2f).coerceAtLeast(0.35f) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = "${(zoomScale * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    IconButton(
                                        onClick = { zoomScale = (zoomScale * 1.2f).coerceAtMost(3.5f) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            yawAngle = 45f
                                            pitchAngle = 35f
                                            zoomScale = 1.0f
                                            panOffset = Offset.Zero
                                        },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = "Reset Camera", modifier = Modifier.size(16.dp))
                                    }
                                    IconButton(
                                        onClick = { is3DFullscreen = true },
                                        modifier = Modifier.size(30.dp).testTag("btn_3d_fullscreen")
                                    ) {
                                        Icon(Icons.Default.Fullscreen, contentDescription = "Fullscreen 3D", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        // Interactive Canvas Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        ) {
                            ThreeDInteractiveIsometricCanvas(
                                requestedCuts = requestedCuts,
                                yawAngle = yawAngle,
                                pitchAngle = pitchAngle,
                                zoomScale = zoomScale,
                                panOffset = panOffset,
                                isHandToolActive = isHandToolActive,
                                isWireframeMode = isWireframeMode,
                                onYawPitchChange = { newYaw, newPitch ->
                                    yawAngle = newYaw
                                    pitchAngle = newPitch
                                },
                                onPanChange = { newPan -> panOffset = newPan },
                                onZoomChange = { newZoom -> zoomScale = newZoom },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Status Helper Chip at bottom
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.82f),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp)
                            ) {
                                Text(
                                    text = if (isHandToolActive) "✋ Hand Tool: Drag to shift / pan view" else "🔄 Orbit Mode: Drag to rotate 3D angle (Yaw: ${yawAngle.toInt()}°, Pitch: ${pitchAngle.toInt()}°)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.inverseOnSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- RAW STOCK INVENTORY (TABBED BY STOCK TYPE & SIZE) ---
            val safeStockIndex = selectedStockIndex.coerceIn(0, (rawStocks.size - 1).coerceAtLeast(0))
            val currentStock = rawStocks.getOrNull(safeStockIndex) ?: rawStocks.firstOrNull()

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
                        Column {
                            Text(
                                text = "Raw Stock Inventory",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "${rawStocks.size} stock tab(s) • ${rawStocks.sumOf { it.quantityAvailable }} total boards/sheets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                val defaultPreset = customPresets.firstOrNull()
                                val defaultLen = defaultPreset?.lengthMm ?: 2000.0
                                val defaultWid = defaultPreset?.widthMm ?: 200.0
                                val defaultThick = defaultPreset?.thicknessMm ?: 20.0
                                stockDialogName = ""
                                stockDialogType = defaultPreset?.type ?: MaterialType.TIMBER_BOARD
                                stockDialogLength = dimensionUnit.fromMm(defaultLen).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                                stockDialogWidth = dimensionUnit.fromMm(defaultWid).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                                stockDialogThickness = dimensionUnit.fromMm(defaultThick).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                                stockDialogQty = "2"
                                stockDialogCost = "0"
                                editingStock = null
                                showAddStockDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp).testTag("btn_add_raw_stock")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ New Tab", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    // Stock Category Tabs Single Scrollable Row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(rawStocks) { idx, stock ->
                            val isSelected = idx == safeStockIndex
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .clickable {
                                        viewModel.selectStock(idx)
                                        newThickness = dimensionUnit.fromMm(stock.thicknessMm).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                                    }
                                    .testTag("tab_stock_$idx")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (stock.type.isSheet) Icons.Default.GridOn else Icons.Default.Layers,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Column {
                                        Text(
                                            text = stock.name.take(18),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                        Text(
                                            text = "${dimensionUnit.format(stock.lengthMm, false)}×${dimensionUnit.format(stock.widthMm, false)} (${stock.quantityAvailable} pcs)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Active Selected Stock Tab Details Card
                    if (currentStock != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
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
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = currentStock.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = currentStock.type.displayName,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        val dimStr = "L: ${dimensionUnit.format(currentStock.lengthMm)}  •  W: ${dimensionUnit.format(currentStock.widthMm)}  •  T: ${dimensionUnit.format(currentStock.thicknessMm)}"
                                        Text(
                                            text = dimStr,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    // Action buttons for active stock tab
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                editingStock = currentStock
                                                stockDialogName = currentStock.name
                                                stockDialogType = currentStock.type
                                                stockDialogLength = dimensionUnit.fromMm(currentStock.lengthMm).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                                                stockDialogWidth = dimensionUnit.fromMm(currentStock.widthMm).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                                                stockDialogThickness = dimensionUnit.fromMm(currentStock.thicknessMm).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                                                stockDialogQty = currentStock.quantityAvailable.toString()
                                                stockDialogCost = currentStock.costPerUnit.toString()
                                                showAddStockDialog = true
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Stock", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                                        }

                                        IconButton(
                                            onClick = { viewModel.duplicateRawStock(currentStock) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Stock Tab", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(17.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.removeRawStock(currentStock.id)
                                                viewModel.selectStock(0)
                                            },
                                            modifier = Modifier.size(32.dp),
                                            enabled = rawStocks.size > 1
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Stock Tab",
                                                tint = if (rawStocks.size > 1) MaterialTheme.colorScheme.error else Color.Gray,
                                                modifier = Modifier.size(17.dp)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                // Quantity Stepper & Capacity Multiplier Status
                                val usedForCurrentStock = result.usedBoards.count {
                                    Math.abs(it.totalBoardThicknessMm - currentStock.thicknessMm) < 0.1 ||
                                    it.boardName.contains(currentStock.name, ignoreCase = true)
                                }
                                val requiredMultiplier = usedForCurrentStock.coerceAtLeast(1)
                                val hasDeficit = currentStock.quantityAvailable < requiredMultiplier

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Stock Quantity:",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                IconButton(
                                                    onClick = { viewModel.updateRawStockQuantity(currentStock.id, -1) },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(Icons.Default.Remove, contentDescription = "Decrease Quantity", modifier = Modifier.size(14.dp))
                                                }
                                                Text(
                                                    text = "${currentStock.quantityAvailable} pcs",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    modifier = Modifier.padding(horizontal = 8.dp)
                                                )
                                                IconButton(
                                                    onClick = { viewModel.updateRawStockQuantity(currentStock.id, 1) },
                                                    modifier = Modifier.size(26.dp)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = "Increase Quantity", modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                    }

                                    // Dynamic Multiplier Badge & Auto-Scale Action
                                    if (hasDeficit) {
                                        Button(
                                            onClick = { viewModel.setStockQuantity(currentStock.id, requiredMultiplier) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("⚠️ Multiply to ×$requiredMultiplier", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = "✓ Required: $usedForCurrentStock / ${currentStock.quantityAvailable} pcs",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stock Presets Quick Bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Add Stock Tab from Presets:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(CutlistOptimizerEngine.STOCK_PRESETS) { preset ->
                                SuggestionChip(
                                    onClick = {
                                        viewModel.addPresetRawStock(preset)
                                        viewModel.selectStock(rawStocks.size)
                                    },
                                    label = {
                                        Text(
                                            "${preset.name} (${dimensionUnit.format(preset.lengthMm, false)}×${dimensionUnit.format(preset.widthMm, false)}×${dimensionUnit.format(preset.thicknessMm, true)})",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // --- BLADE KERF, TRIM MARGIN & RIPPING SETTINGS ---
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
                            text = "Blade & Cutting Parameters",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = bladeKerf,
                            onValueChange = { viewModel.updateBladeKerf(it) },
                            label = { Text("Blade Kerf (${dimensionUnit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_cutlist_kerf")
                        )

                        OutlinedTextField(
                            value = trimMargin,
                            onValueChange = { viewModel.updateTrimMargin(it) },
                            label = { Text("End Trim Margin (${dimensionUnit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_cutlist_trim")
                        )
                    }

                    // Ripping (Resawing/Splitting) Mode Switch for Lumber / Solid Boards
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateAllowRipCuts(!allowRipCuts) }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CallSplit,
                                        contentDescription = null,
                                        tint = if (allowRipCuts) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Allow Board Ripping (Width Splitting)",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (allowRipCuts)
                                        "Active: Wide stock boards (e.g. 200 mm / 20 cm) can be ripped longitudinally into narrower pieces (e.g. 2× 100 mm / 10 cm)."
                                    else
                                        "Disabled: Treats stock strictly as 1D linear lumber with fixed width.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = allowRipCuts,
                                onCheckedChange = { viewModel.updateAllowRipCuts(it) },
                                modifier = Modifier.testTag("switch_allow_rip_cuts")
                            )
                        }
                    }
                }
            }

            // --- REQUESTED CUT PIECES & SPECS (WITH DIMENSION THRESHOLDS) ---
            val activeTabStock = rawStocks.getOrNull(selectedStockIndex.coerceIn(0, (rawStocks.size - 1).coerceAtLeast(0))) ?: rawStocks.firstOrNull()
            val maxStockLengthMm = activeTabStock?.lengthMm ?: 2000.0
            val maxStockWidthMm = activeTabStock?.widthMm ?: 200.0
            val activeStockThickMm = activeTabStock?.thicknessMm ?: 20.0

            val inputLenVal = newLength.toDoubleOrNull() ?: 0.0
            val inputWidVal = newWidth.toDoubleOrNull() ?: 0.0
            val inputThickVal = newThickness.toDoubleOrNull() ?: dimensionUnit.fromMm(activeStockThickMm)
            val inputQtyVal = newQty.toIntOrNull() ?: 1

            val inputLenMm = dimensionUnit.toMm(inputLenVal)
            val inputWidMm = dimensionUnit.toMm(inputWidVal)
            val inputThickMm = dimensionUnit.toMm(inputThickVal)

            val isLenExceeded = inputLenMm > maxStockLengthMm
            val isWidExceeded = inputWidMm > maxStockWidthMm
            val isThickDifferent = Math.abs(inputThickMm - activeStockThickMm) > 0.1
            val isInputValid = inputLenMm > 0 && inputWidMm > 0 && !isLenExceeded && !isWidExceeded

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        Column {
                            Text(
                                text = "Cut Pieces & Part Specs",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (activeTabStock != null) {
                                Text(
                                    text = "Active Tab: ${activeTabStock.name} (Max: ${dimensionUnit.format(maxStockLengthMm)} × ${dimensionUnit.format(maxStockWidthMm)})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            text = "${requestedCuts.sumOf { it.quantity }} total items",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Redesigned Spacious 2-Row Form to Avoid Text Wrapping & Squeezed Fields
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Row 1: Part Label, Thickness, and Quantity
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newLabel,
                                onValueChange = { newLabel = it },
                                label = { Text("Part Label") },
                                placeholder = { Text("e.g. Table Rail") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.4f).testTag("input_cut_label")
                            )

                            OutlinedTextField(
                                value = newThickness,
                                onValueChange = { newThickness = it },
                                label = { Text("T (${dimensionUnit.symbol})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(0.8f).testTag("input_cut_thickness")
                            )

                            OutlinedTextField(
                                value = newQty,
                                onValueChange = { newQty = it },
                                label = { Text("Qty") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(0.7f).testTag("input_cut_qty")
                            )
                        }

                        // Row 2: Length, Width, and Add Button
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = newLength,
                                onValueChange = { newLength = it },
                                label = { Text("Length (${dimensionUnit.symbol})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = isLenExceeded,
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("input_cut_length")
                            )

                            OutlinedTextField(
                                value = newWidth,
                                onValueChange = { newWidth = it },
                                label = { Text("Width (${dimensionUnit.symbol})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = isWidExceeded,
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("input_cut_width")
                            )

                            Button(
                                onClick = {
                                    if (isInputValid) {
                                        viewModel.addCutPiece(newLabel, inputLenMm, inputWidMm, inputQtyVal, inputThickMm)
                                        newLabel = ""
                                        viewModel.autoMultiplyStockForRequiredCuts()
                                    }
                                },
                                enabled = isInputValid,
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                                modifier = Modifier.height(56.dp).testTag("add_cut_piece_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Cut Piece", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Cut", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }

                    // Dimension Threshold Feedback Warnings / Guidance
                    if (isLenExceeded && activeTabStock != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "⚠️ Length ${dimensionUnit.format(inputLenMm)} exceeds tab stock limit (${dimensionUnit.format(maxStockLengthMm)})!",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else if (isWidExceeded && activeTabStock != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "⚠️ Width ${dimensionUnit.format(inputWidMm)} exceeds tab stock limit (${dimensionUnit.format(maxStockWidthMm)})!",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    } else if (isThickDifferent && activeTabStock != null && inputThickMm > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "ℹ️ Piece thickness (${dimensionUnit.format(inputThickMm)}) differs from active tab (${dimensionUnit.format(activeStockThickMm)}). Will match other stock tabs.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Requested Cuts List with COLOR CODING & EDIT/DELETE BUTTONS
                    requestedCuts.forEach { cut ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(cut.colorHex), CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = cut.label.ifEmpty { "Piece #${cut.id.take(4)}" },
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    val dimText = "L: ${dimensionUnit.format(cut.lengthMm)}  •  W: ${dimensionUnit.format(cut.widthMm)}  •  T: ${dimensionUnit.format(cut.thicknessMm)}  •  ${cut.quantity} pcs"
                                    Text(
                                        text = dimText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row {
                                // Edit piece button
                                IconButton(
                                    onClick = {
                                        editingPiece = cut
                                        editPieceLabel = cut.label
                                        val lVal = dimensionUnit.fromMm(cut.lengthMm)
                                        val wVal = dimensionUnit.fromMm(cut.widthMm)
                                        val tVal = dimensionUnit.fromMm(cut.thicknessMm)
                                        editPieceLength = if (lVal % 1.0 == 0.0) lVal.toInt().toString() else lVal.toString()
                                        editPieceWidth = if (wVal % 1.0 == 0.0) wVal.toInt().toString() else wVal.toString()
                                        editPieceThickness = if (tVal % 1.0 == 0.0) tVal.toInt().toString() else tVal.toString()
                                        editPieceQty = cut.quantity.toString()
                                    }
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Piece", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }

                                // Delete piece button
                                IconButton(onClick = { viewModel.removeCutPiece(cut.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // --- HARDWARE, ACCESSORIES & NON-CUT BOM CHECKLIST (COLLAPSIBLE DROPDOWN) ---
            val nonCutItems = remember(projectNotes) {
                projectNotes.split(";").map { it.trim() }.filter { it.isNotEmpty() }
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.28f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Row with Dropdown Arrow Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isHardwareSectionExpanded = !isHardwareSectionExpanded }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Hardware & BOM Notes",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    if (nonCutItems.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "${nonCutItems.size} items",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = if (isHardwareSectionExpanded) "LED strips, hinges, drawer slides, screws, finishes & notes" else if (nonCutItems.isNotEmpty()) nonCutItems.joinToString(", ").take(45) + if (nonCutItems.joinToString(", ").length > 45) "..." else "" else "Tap to expand hardware, finishes & assembly notes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showNotesBOMDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("btn_edit_notes_and_bom")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Edit All", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }

                            IconButton(
                                onClick = { isHardwareSectionExpanded = !isHardwareSectionExpanded },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("btn_toggle_hardware_section")
                            ) {
                                Icon(
                                    imageVector = if (isHardwareSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isHardwareSectionExpanded) "Collapse Section" else "Expand Section",
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }

                    // Collapsible Content
                    AnimatedVisibility(
                        visible = isHardwareSectionExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            // Interactive Checklist of Non-Cut Materials
                            if (nonCutItems.isNotEmpty()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    nonCutItems.forEach { accessory ->
                                        var checked by remember(accessory) { mutableStateOf(false) }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Checkbox(
                                                checked = checked,
                                                onCheckedChange = { checked = it },
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = accessory,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    textDecoration = if (checked) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                                ),
                                                color = if (checked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    val updated = nonCutItems.filter { it != accessory }.joinToString("; ")
                                                    viewModel.updateProjectNotes(updated)
                                                },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove Item", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f), modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "No non-cut hardware added yet. Add LED strips, hinges, screws, finishes, or workshop instructions below.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Quick Add Non-Cut Item Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = quickNonCutInput,
                                    onValueChange = { quickNonCutInput = it },
                                    placeholder = { Text("Add hardware (e.g. LED strip 24V 2m, hinges)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("input_quick_noncut_item"),
                                    textStyle = MaterialTheme.typography.bodySmall
                                )
                                Button(
                                    onClick = {
                                        if (quickNonCutInput.isNotBlank()) {
                                            val trimmed = quickNonCutInput.trim()
                                            val updated = if (projectNotes.isBlank()) trimmed else "$projectNotes; $trimmed"
                                            viewModel.updateProjectNotes(updated)
                                            quickNonCutInput = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                    modifier = Modifier.testTag("btn_quick_add_noncut_item")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Item", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // --- VISUAL BOARD & SHEET CUTTING DIAGRAMS ---
            Text(
                text = "Optimized Cutting Diagrams & Color Code Guide",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            result.usedBoards.forEachIndexed { index, board ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${board.boardName} #${index + 1}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Stock Specs: ${dimensionUnit.format(board.totalBoardLengthMm)} × ${dimensionUnit.format(board.totalBoardWidthMm)} × ${dimensionUnit.format(board.totalBoardThicknessMm)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val scrapText = if (board.materialType == MaterialType.PLYWOOD_SHEET) {
                                "Scrap: ${String.format(Locale.US, "%.2f m²", board.remainingScrapMm / 1_000_000.0)}"
                            } else {
                                "Scrap: ${dimensionUnit.format(board.remainingScrapMm)}"
                            }
                            Text(
                                text = scrapText,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        if (board.materialType == MaterialType.PLYWOOD_SHEET) {
                            PlywoodSheetDiagramCanvas(
                                board = board,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            )
                        } else {
                            val isRipped = board.isRippedAcrossWidth || board.placedPieces.any { it.startYMm > 0 }
                            StockBoardDiagramCanvas(
                                board = board,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isRipped) 110.dp else 65.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cut Section Breakdown:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (board.isRippedAcrossWidth) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "⚡ Ripped Width-Wise",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            board.placedPieces.forEach { piece ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        // Row 1: Color indicator, Piece Label, Highlighted Dimensions
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(Color(piece.colorHex), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = piece.pieceLabel,
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = if (board.materialType.isSheet || board.isRippedAcrossWidth || piece.startYMm > 0) {
                                                    "${dimensionUnit.format(piece.lengthMm, false)} × ${dimensionUnit.format(piece.widthMm, true)}"
                                                } else {
                                                    dimensionUnit.format(piece.lengthMm)
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Row 2: Thickness spec and Precise cut coordinates
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp)
                                        ) {
                                            Text(
                                                text = "Thick: ${dimensionUnit.format(piece.thicknessMm)}",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            val posText = if (board.materialType.isSheet || board.isRippedAcrossWidth || piece.startYMm > 0) {
                                                "X: ${dimensionUnit.format(piece.startPositionMm, false)}-${dimensionUnit.format(piece.endPositionMm, false)} | Y: ${dimensionUnit.format(piece.startYMm, false)}-${dimensionUnit.format(piece.endYMm, false)}"
                                            } else {
                                                "Pos: ${dimensionUnit.format(piece.startPositionMm, false)} - ${dimensionUnit.format(piece.endPositionMm, true)}"
                                            }
                                            Text(
                                                text = posText,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
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
    }

    // --- CREATE CUSTOM MATERIAL TYPE DIALOG ---
    if (showAddCustomTypeDialog) {
        AlertDialog(
            onDismissRequest = { showAddCustomTypeDialog = false },
            title = {
                Text("Create Custom Material Type", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = newCustomTypeName,
                        onValueChange = { newCustomTypeName = it },
                        label = { Text("Material Name") },
                        placeholder = { Text("e.g. Cedar Siding / Walnut 50mm Slab") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Category Form:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !newCustomTypeIsSheet,
                                onClick = { newCustomTypeIsSheet = false },
                                label = { Text("Solid Board / Lumber") },
                                leadingIcon = if (!newCustomTypeIsSheet) { { Icon(Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(14.dp)) } } else null
                            )
                            FilterChip(
                                selected = newCustomTypeIsSheet,
                                onClick = { newCustomTypeIsSheet = true },
                                label = { Text("Sheet / Panel") },
                                leadingIcon = if (newCustomTypeIsSheet) { { Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(14.dp)) } } else null
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newCustomTypeLength,
                            onValueChange = { newCustomTypeLength = it },
                            label = { Text("Length (${dimensionUnit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newCustomTypeWidth,
                            onValueChange = { newCustomTypeWidth = it },
                            label = { Text("Width (${dimensionUnit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = newCustomTypeThickness,
                        onValueChange = { newCustomTypeThickness = it },
                        label = { Text("Thickness (${dimensionUnit.symbol})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lenVal = newCustomTypeLength.toDoubleOrNull() ?: (if (dimensionUnit == DimensionUnit.CM) 200.0 else 2000.0)
                        val widVal = newCustomTypeWidth.toDoubleOrNull() ?: (if (dimensionUnit == DimensionUnit.CM) 20.0 else 200.0)
                        val thickVal = newCustomTypeThickness.toDoubleOrNull() ?: (if (dimensionUnit == DimensionUnit.CM) 2.0 else 20.0)

                        val lenMm = dimensionUnit.toMm(lenVal)
                        val widMm = dimensionUnit.toMm(widVal)
                        val thickMm = dimensionUnit.toMm(thickVal)

                        val created = viewModel.createCustomMaterialPreset(
                            name = newCustomTypeName,
                            type = if (newCustomTypeIsSheet) MaterialType.PLYWOOD_SHEET else MaterialType.TIMBER_BOARD,
                            lengthMm = lenMm,
                            widthMm = widMm,
                            thicknessMm = thickMm,
                            description = "Custom workshop profile"
                        )

                        // Autofill into active Add Stock dialog
                        stockDialogType = created.type
                        stockDialogLength = newCustomTypeLength
                        stockDialogWidth = newCustomTypeWidth
                        stockDialogThickness = newCustomTypeThickness
                        stockDialogName = created.name
                        showAddCustomTypeDialog = false
                    },
                    enabled = newCustomTypeName.isNotBlank()
                ) {
                    Text("Create Type")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCustomTypeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- ADD / EDIT RAW STOCK DIALOG ---
    if (showAddStockDialog) {
        AlertDialog(
            onDismissRequest = { showAddStockDialog = false },
            title = {
                Text(if (editingStock == null) "Add Raw Stock Lumber / Sheet" else "Edit Raw Stock Specs")
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = stockDialogName,
                        onValueChange = { stockDialogName = it },
                        label = { Text("Stock Name / Description") },
                        placeholder = { Text("e.g. Solid Mahogany Board") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Stock Material Type Selector with Autofill and "+ Add Type"
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Material Type / Preset:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text("Auto-fills size", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.primary)
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(customPresets) { preset ->
                                val isSelected = stockDialogType == preset.type && stockDialogName == preset.name
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        stockDialogType = preset.type
                                        stockDialogLength = dimensionUnit.fromMm(preset.lengthMm).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                                        stockDialogWidth = dimensionUnit.fromMm(preset.widthMm).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                                        stockDialogThickness = dimensionUnit.fromMm(preset.thicknessMm).let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }
                                        if (stockDialogName.isBlank() || customPresets.any { it.name == stockDialogName }) {
                                            stockDialogName = preset.name
                                        }
                                    },
                                    label = { Text(preset.name, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            item {
                                FilledTonalButton(
                                    onClick = {
                                        newCustomTypeName = ""
                                        newCustomTypeIsSheet = false
                                        newCustomTypeLength = if (dimensionUnit == DimensionUnit.CM) "200" else "2000"
                                        newCustomTypeWidth = if (dimensionUnit == DimensionUnit.CM) "20" else "200"
                                        newCustomTypeThickness = if (dimensionUnit == DimensionUnit.CM) "2" else "20"
                                        showAddCustomTypeDialog = true
                                    },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("+ New Type", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockDialogLength,
                            onValueChange = { stockDialogLength = it },
                            label = { Text("Length (${dimensionUnit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stockDialogWidth,
                            onValueChange = { stockDialogWidth = it },
                            label = { Text("Width (${dimensionUnit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = stockDialogThickness,
                            onValueChange = { stockDialogThickness = it },
                            label = { Text("Thickness (${dimensionUnit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stockDialogQty,
                            onValueChange = { stockDialogQty = it },
                            label = { Text("Quantity Available") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lenVal = stockDialogLength.toDoubleOrNull() ?: 200.0
                        val widVal = stockDialogWidth.toDoubleOrNull() ?: 20.0
                        val thickVal = stockDialogThickness.toDoubleOrNull() ?: 2.0
                        val qty = stockDialogQty.toIntOrNull() ?: 1
                        val cost = stockDialogCost.toDoubleOrNull() ?: 0.0

                        val lenMm = dimensionUnit.toMm(lenVal)
                        val widMm = dimensionUnit.toMm(widVal)
                        val thickMm = dimensionUnit.toMm(thickVal)

                        val currentEdit = editingStock
                        if (currentEdit != null) {
                            val updated = currentEdit.copy(
                                name = stockDialogName.ifEmpty { "${stockDialogType.displayName} (${dimensionUnit.format(lenMm, false)}×${dimensionUnit.format(widMm, false)}×${dimensionUnit.format(thickMm, true)})" },
                                type = stockDialogType,
                                lengthMm = lenMm,
                                widthMm = widMm,
                                thicknessMm = thickMm,
                                quantityAvailable = qty.coerceAtLeast(1),
                                costPerUnit = cost
                            )
                            viewModel.updateRawStock(updated)
                        } else {
                            viewModel.addRawStock(
                                name = stockDialogName,
                                type = stockDialogType,
                                lengthMm = lenMm,
                                widthMm = widMm,
                                thicknessMm = thickMm,
                                quantity = qty,
                                cost = cost
                            )
                        }
                        showAddStockDialog = false
                    }
                ) {
                    Text(if (editingStock == null) "Add to Stock" else "Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- ALL PROJECTS MODAL BOTTOM SHEET ---
    if (showProjectsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProjectsSheet = false },
            sheetState = projectsSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saved Woodworking Projects",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Button(
                        onClick = {
                            newProjectNameText = "Project ${savedProjects.size + 1}"
                            showNewProjectDialog = true
                        }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Project")
                    }
                }

                if (savedProjects.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No saved projects yet. Click 'New Project' to create one.")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(320.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(savedProjects, key = { it.id }) { proj ->
                            val isSelected = proj.name == activeProjectName
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.loadProject(proj)
                                        showProjectsSheet = false
                                        Toast.makeText(context, "Loaded \"${proj.name}\"", Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = proj.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isSelected) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Active",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(proj.createdAt))
                                        val stockSummary = "${proj.rawStocks.size} stock types • ${proj.requestedCuts.sumOf { it.quantity }} cuts • Created $dateStr"
                                        Text(
                                            text = stockSummary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            viewModel.deleteProject(context, proj.id)
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // --- RENAME PROJECT DIALOG ---
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Project") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameActiveProject(context, renameText)
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- NEW PROJECT DIALOG ---
    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("Create New Project") },
            text = {
                OutlinedTextField(
                    value = newProjectNameText,
                    onValueChange = { newProjectNameText = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjectNameText.isNotBlank()) {
                            viewModel.createNewBlankProject(context, newProjectNameText)
                            showProjectsSheet = false
                        }
                        showNewProjectDialog = false
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- REVISE / EDIT CUT PIECE DIALOG ---
    if (editingPiece != null) {
        AlertDialog(
            onDismissRequest = { editingPiece = null },
            title = { Text("Revise Cut Piece Specs") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editPieceLabel,
                        onValueChange = { editPieceLabel = it },
                        label = { Text("Part Label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editPieceLength,
                            onValueChange = { editPieceLength = it },
                            label = { Text("Length (${dimensionUnit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editPieceWidth,
                            onValueChange = { editPieceWidth = it },
                            label = { Text("Width (${dimensionUnit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editPieceThickness,
                            onValueChange = { editPieceThickness = it },
                            label = { Text("Thickness (${dimensionUnit.symbol})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editPieceQty,
                            onValueChange = { editPieceQty = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val piece = editingPiece ?: return@Button
                        val lVal = editPieceLength.toDoubleOrNull() ?: dimensionUnit.fromMm(piece.lengthMm)
                        val wVal = editPieceWidth.toDoubleOrNull() ?: dimensionUnit.fromMm(piece.widthMm)
                        val tVal = editPieceThickness.toDoubleOrNull() ?: dimensionUnit.fromMm(piece.thicknessMm)
                        val q = editPieceQty.toIntOrNull() ?: piece.quantity

                        val lMm = dimensionUnit.toMm(lVal)
                        val wMm = dimensionUnit.toMm(wVal)
                        val tMm = dimensionUnit.toMm(tVal)

                        viewModel.updateCutPiece(piece.id, editPieceLabel, lMm, wMm, q, tMm)
                        editingPiece = null
                    }
                ) {
                    Text("Apply Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPiece = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- PROJECT NOTES & HARDWARE (BOM) MANAGEMENT DIALOG ---
    if (showNotesBOMDialog) {
        var tempNotesText by remember(projectNotes) { mutableStateOf(projectNotes) }
        var tempNewItemText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showNotesBOMDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text("Project Notes & Hardware (BOM)", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Manage non-cut materials (LED strips, hinges, screws, finishes, adhesives) and workshop instructions for $activeProjectName.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Hardware Items List
                    val currentItems = tempNotesText.split(";").map { it.trim() }.filter { it.isNotEmpty() }
                    Text(
                        text = "Hardware & Non-Cut Pieces (${currentItems.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    if (currentItems.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            currentItems.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "${idx + 1}.",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = item,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            val updated = currentItems.filterIndexed { i, _ -> i != idx }.joinToString("; ")
                                            tempNotesText = updated
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Delete item", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Add item input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = tempNewItemText,
                            onValueChange = { tempNewItemText = it },
                            placeholder = { Text("e.g. LED Strip 24V 2m, 4x Hinges") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f),
                            textStyle = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = {
                                if (tempNewItemText.isNotBlank()) {
                                    val trimmed = tempNewItemText.trim()
                                    val updated = if (tempNotesText.isBlank()) trimmed else "$tempNotesText; $trimmed"
                                    tempNotesText = updated
                                    tempNewItemText = ""
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text("+ Add", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Raw Notes & Workshop Instructions Editor
                    Text(
                        text = "Workshop Instructions & Semicolon-Separated List",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    OutlinedTextField(
                        value = tempNotesText,
                        onValueChange = { tempNotesText = it },
                        label = { Text("All Notes & Hardware (Semicolon separated)") },
                        textStyle = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 5,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProjectNotes(tempNotesText)
                        showNotesBOMDialog = false
                        Toast.makeText(context, "Project notes & hardware updated!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save & Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotesBOMDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- FULLSCREEN 3D VIEWPORT DIALOG ---
    if (is3DFullscreen) {
        Dialog(
            onDismissRequest = { is3DFullscreen = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Fullscreen Top App Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Rotate90DegreesCcw,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "3D Isometric Assembly Studio",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    text = "$activeProjectName • ${requestedCuts.sumOf { it.quantity }} cut parts",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = { is3DFullscreen = false },
                                    modifier = Modifier.testTag("btn_close_3d_fullscreen")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Fullscreen",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Floating Interactive Tools Bar
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Mode Switcher: Orbit vs Hand Tool
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = !isHandToolActive,
                                    onClick = { isHandToolActive = false },
                                    label = { Text("360° Orbit") },
                                    leadingIcon = { Icon(Icons.Default.Rotate90DegreesCcw, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                                FilterChip(
                                    selected = isHandToolActive,
                                    onClick = { isHandToolActive = true },
                                    label = { Text("Hand Tool (Pan)") },
                                    leadingIcon = { Icon(Icons.Default.PanTool, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                )
                            }

                            // Zoom & Reset Controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.35f) }) {
                                    Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out")
                                }
                                Text(
                                    text = "${(zoomScale * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                IconButton(onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(4.0f) }) {
                                    Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In")
                                }
                                IconButton(onClick = {
                                    yawAngle = 45f
                                    pitchAngle = 35f
                                    zoomScale = 1.0f
                                    panOffset = Offset.Zero
                                }) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset Camera")
                                }
                            }
                        }
                    }

                    // Main Fullscreen 3D Canvas
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        ThreeDInteractiveIsometricCanvas(
                            requestedCuts = requestedCuts,
                            yawAngle = yawAngle,
                            pitchAngle = pitchAngle,
                            zoomScale = zoomScale,
                            panOffset = panOffset,
                            isHandToolActive = isHandToolActive,
                            isWireframeMode = isWireframeMode,
                            onYawPitchChange = { newYaw, newPitch ->
                                yawAngle = newYaw
                                pitchAngle = newPitch
                            },
                            onPanChange = { newPan -> panOffset = newPan },
                            onZoomChange = { newZoom -> zoomScale = newZoom },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Bottom gesture HUD Banner
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.85f),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = if (isHandToolActive) "✋ Hand Tool Active • Drag anywhere to shift & pan the assembly" else "🔄 Orbit Mode Active • Drag to rotate 3D angle (Yaw: ${yawAngle.toInt()}°, Pitch: ${pitchAngle.toInt()}°)",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StockBoardDiagramCanvas(
    board: UsedBoardLayout,
    modifier: Modifier = Modifier
) {
    val scrapColor = Color(0xFFE7E0EC)
    val isRipped = board.isRippedAcrossWidth || board.placedPieces.any { it.startYMm > 0 }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val totalLen = board.totalBoardLengthMm
        val totalWid = if (board.totalBoardWidthMm > 0) board.totalBoardWidthMm else 200.0

        // Background raw stock board (scrap base)
        drawRect(
            color = scrapColor,
            topLeft = Offset(0f, 0f),
            size = Size(w, h)
        )

        // Outer board boundary
        drawRect(
            color = Color(0xFF79747E),
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
        )

        board.placedPieces.forEach { piece ->
            val startX = (piece.startPositionMm / totalLen).toFloat() * w
            val endX = (piece.endPositionMm / totalLen).toFloat() * w
            val pieceW = (endX - startX).coerceAtLeast(4f)

            val startY = if (isRipped) (piece.startYMm / totalWid).toFloat() * h else 0f
            val endY = if (isRipped) (piece.endYMm / totalWid).toFloat() * h else h
            val pieceH = (endY - startY).coerceAtLeast(4f)

            drawRect(
                color = Color(piece.colorHex),
                topLeft = Offset(startX, startY),
                size = Size(pieceW, pieceH)
            )

            drawRect(
                color = Color.White,
                topLeft = Offset(startX, startY),
                size = Size(pieceW, pieceH),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5f)
            )
        }
    }
}

@Composable
fun PlywoodSheetDiagramCanvas(
    board: UsedBoardLayout,
    modifier: Modifier = Modifier
) {
    val scrapColor = Color(0xFFE7E0EC)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val totalLen = board.totalBoardLengthMm
        val totalWid = if (board.totalBoardWidthMm > 0) board.totalBoardWidthMm else 1219.2

        drawRect(
            color = scrapColor,
            topLeft = Offset(0f, 0f),
            size = Size(w, h)
        )

        drawRect(
            color = Color(0xFF79747E),
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )

        board.placedPieces.forEach { piece ->
            val startX = (piece.startPositionMm / totalLen).toFloat() * w
            val endX = (piece.endPositionMm / totalLen).toFloat() * w
            val pieceW = (endX - startX).coerceAtLeast(4f)

            val startY = (piece.startYMm / totalWid).toFloat() * h
            val endY = (piece.endYMm / totalWid).toFloat() * h
            val pieceH = (endY - startY).coerceAtLeast(4f)

            drawRect(
                color = Color(piece.colorHex),
                topLeft = Offset(startX, startY),
                size = Size(pieceW, pieceH)
            )

            drawRect(
                color = Color.White,
                topLeft = Offset(startX, startY),
                size = Size(pieceW, pieceH),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }
    }
}

// -------------------------------------------------------------
// 3D ISOMETRIC VECTOR ENGINE (ROTATION, ZOOM, PAN & LIGHTING)
// -------------------------------------------------------------

data class Vector3D(val x: Float, val y: Float, val z: Float)

data class Face3D(
    val vertices: List<Vector3D>,
    val baseColor: Color,
    val normal: Vector3D,
    val label: String? = null
)

@Composable
fun ThreeDInteractiveIsometricCanvas(
    requestedCuts: List<CutPiece>,
    yawAngle: Float,
    pitchAngle: Float,
    zoomScale: Float,
    panOffset: Offset,
    isHandToolActive: Boolean,
    isWireframeMode: Boolean,
    onYawPitchChange: (yaw: Float, pitch: Float) -> Unit,
    onPanChange: (Offset) -> Unit,
    onZoomChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val isCabinet = requestedCuts.any {
        it.label.contains("shelf", ignoreCase = true) ||
        it.label.contains("side", ignoreCase = true) ||
        it.label.contains("panel", ignoreCase = true) ||
        it.label.contains("back", ignoreCase = true) ||
        it.label.contains("lemari", ignoreCase = true) ||
        it.label.contains("rak", ignoreCase = true) ||
        it.label.contains("cabinet", ignoreCase = true) ||
        it.label.contains("cupboard", ignoreCase = true) ||
        it.label.contains("box", ignoreCase = true)
    }

    val hasLegs = requestedCuts.any {
        it.label.contains("leg", ignoreCase = true) ||
        it.label.contains("kaki", ignoreCase = true) ||
        it.label.contains("stool", ignoreCase = true) ||
        it.label.contains("bench", ignoreCase = true) ||
        it.label.contains("table", ignoreCase = true) ||
        it.label.contains("desk", ignoreCase = true) ||
        it.label.contains("meja", ignoreCase = true)
    }

    val gestureModifier = modifier.pointerInput(isHandToolActive) {
        detectTransformGestures { _, pan, zoom, _ ->
            onZoomChange((zoomScale * zoom).coerceIn(0.35f, 4.0f))
            if (isHandToolActive) {
                onPanChange(panOffset + pan)
            } else {
                var newYaw = (yawAngle + pan.x * 0.65f) % 360f
                if (newYaw < 0f) newYaw += 360f
                val newPitch = (pitchAngle - pan.y * 0.55f).coerceIn(5f, 88f)
                onYawPitchChange(newYaw, newPitch)
            }
        }
    }

    val wireframeOutlineColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = gestureModifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        val radY = Math.toRadians(yawAngle.toDouble())
        val radX = Math.toRadians(pitchAngle.toDouble())
        val cosY = Math.cos(radY).toFloat()
        val sinY = Math.sin(radY).toFloat()
        val cosX = Math.cos(radX).toFloat()
        val sinX = Math.sin(radX).toFloat()

        // 3D rotation projection function
        fun project(v: Vector3D): Triple<Float, Float, Float> {
            // Rotate around Y axis
            val x1 = v.x * cosY + v.z * sinY
            val z1 = -v.x * sinY + v.z * cosY
            val y1 = v.y

            // Rotate around X axis
            val y2 = y1 * cosX - z1 * sinX
            val z2 = y1 * sinX + z1 * cosX
            val x2 = x1

            val screenX = cx + panOffset.x + x2 * zoomScale
            val screenY = cy + panOffset.y + y2 * zoomScale
            return Triple(screenX, screenY, z2)
        }

        fun rotateNormal(n: Vector3D): Vector3D {
            val nx1 = n.x * cosY + n.z * sinY
            val nz1 = -n.x * sinY + n.z * cosY
            val ny1 = n.y

            val ny2 = ny1 * cosX - nz1 * sinX
            val nz2 = ny1 * sinX + nz1 * cosX
            val nx2 = nx1
            return Vector3D(nx2, ny2, nz2)
        }

        fun createCuboidFaces(
            x0: Float, y0: Float, z0: Float,
            x1: Float, y1: Float, z1: Float,
            color: Color,
            label: String? = null
        ): List<Face3D> {
            return listOf(
                // Top (+Y)
                Face3D(
                    vertices = listOf(
                        Vector3D(x0, y0, z0),
                        Vector3D(x0, y0, z1),
                        Vector3D(x1, y0, z1),
                        Vector3D(x1, y0, z0)
                    ),
                    baseColor = color,
                    normal = Vector3D(0f, -1f, 0f),
                    label = label
                ),
                // Bottom (-Y)
                Face3D(
                    vertices = listOf(
                        Vector3D(x0, y1, z0),
                        Vector3D(x1, y1, z0),
                        Vector3D(x1, y1, z1),
                        Vector3D(x0, y1, z1)
                    ),
                    baseColor = color,
                    normal = Vector3D(0f, 1f, 0f),
                    label = label
                ),
                // Front (+Z)
                Face3D(
                    vertices = listOf(
                        Vector3D(x0, y0, z1),
                        Vector3D(x0, y1, z1),
                        Vector3D(x1, y1, z1),
                        Vector3D(x1, y0, z1)
                    ),
                    baseColor = color,
                    normal = Vector3D(0f, 0f, 1f),
                    label = label
                ),
                // Back (-Z)
                Face3D(
                    vertices = listOf(
                        Vector3D(x1, y0, z0),
                        Vector3D(x1, y1, z0),
                        Vector3D(x0, y1, z0),
                        Vector3D(x0, y0, z0)
                    ),
                    baseColor = color,
                    normal = Vector3D(0f, 0f, -1f),
                    label = label
                ),
                // Left (-X)
                Face3D(
                    vertices = listOf(
                        Vector3D(x0, y0, z0),
                        Vector3D(x0, y1, z0),
                        Vector3D(x0, y1, z1),
                        Vector3D(x0, y0, z1)
                    ),
                    baseColor = color,
                    normal = Vector3D(-1f, 0f, 0f),
                    label = label
                ),
                // Right (+X)
                Face3D(
                    vertices = listOf(
                        Vector3D(x1, y0, z1),
                        Vector3D(x1, y1, z1),
                        Vector3D(x1, y1, z0),
                        Vector3D(x1, y0, z0)
                    ),
                    baseColor = color,
                    normal = Vector3D(1f, 0f, 0f),
                    label = label
                )
            )
        }

        // Draw Ground Isometric Plane Grid
        val gridMin = -160f
        val gridMax = 160f
        val gridStep = 40f
        val groundY = 85f
        var g = gridMin
        while (g <= gridMax) {
            val p1 = project(Vector3D(g, groundY, gridMin))
            val p2 = project(Vector3D(g, groundY, gridMax))
            val p3 = project(Vector3D(gridMin, groundY, g))
            val p4 = project(Vector3D(gridMax, groundY, g))

            drawLine(
                color = Color.LightGray.copy(alpha = 0.35f),
                start = Offset(p1.first, p1.second),
                end = Offset(p2.first, p2.second),
                strokeWidth = 1f
            )
            drawLine(
                color = Color.LightGray.copy(alpha = 0.35f),
                start = Offset(p3.first, p3.second),
                end = Offset(p4.first, p4.second),
                strokeWidth = 1f
            )
            g += gridStep
        }

        val allFaces = mutableListOf<Face3D>()

        if (isCabinet) {
            val sidePiece = requestedCuts.find { it.label.contains("side", ignoreCase = true) || it.label.contains("panel", ignoreCase = true) || it.label.contains("lemari", ignoreCase = true) }
            val shelfPiece = requestedCuts.find { it.label.contains("shelf", ignoreCase = true) || it.label.contains("rak", ignoreCase = true) || it.label.contains("top", ignoreCase = true) }

            val rawHeight = sidePiece?.lengthMm ?: 1000.0
            val rawWidth = shelfPiece?.lengthMm ?: 650.0
            val rawDepth = if (shelfPiece != null && shelfPiece.widthMm > 0) shelfPiece.widthMm else 300.0

            val H = (rawHeight * 0.11).coerceIn(70.0, 130.0).toFloat()
            val W = (rawWidth * 0.11).coerceIn(60.0, 110.0).toFloat()
            val D = (rawDepth * 0.12).coerceIn(35.0, 70.0).toFloat()
            val T = 8f

            val hw = W / 2f
            val hh = H / 2f
            val hd = D / 2f

            // Left Side Wall
            allFaces.addAll(createCuboidFaces(-hw, -hh, -hd, -hw + T, hh, hd, Color(0xFF8F6343), "Left Panel"))
            // Right Side Wall
            allFaces.addAll(createCuboidFaces(hw - T, -hh, -hd, hw, hh, hd, Color(0xFF8F6343), "Right Panel"))
            // Top Slab
            allFaces.addAll(createCuboidFaces(-hw, -hh, -hd, hw, -hh + T, hd, Color(0xFFD2B48C), "Top Slab"))
            // Bottom Slab
            allFaces.addAll(createCuboidFaces(-hw, hh - T, -hd, hw, hh, hd, Color(0xFFB59A7A), "Bottom Slab"))
            // Back Panel
            allFaces.addAll(createCuboidFaces(-hw + T, -hh + T, -hd, hw - T, hh - T, -hd + 4f, Color(0xFFEEDC82).copy(alpha = 0.5f), "Backing"))

            // Shelves
            val shelfCount = requestedCuts.filter { it.label.contains("shelf", ignoreCase = true) || it.label.contains("rak", ignoreCase = true) }.sumOf { it.quantity }.coerceIn(1, 3)
            for (i in 1..shelfCount) {
                val f = (i.toFloat() / (shelfCount + 1)) * (H - 2 * T) - (hh - T)
                allFaces.addAll(createCuboidFaces(-hw + T, f, -hd + 4f, hw - T, f + T, hd, Color(0xFFC19A6B), "Shelf $i"))
            }
        } else if (hasLegs) {
            val tableTopPiece = requestedCuts.find { it.label.contains("top", ignoreCase = true) || it.label.contains("meja", ignoreCase = true) }
            val legPiece = requestedCuts.find { it.label.contains("leg", ignoreCase = true) || it.label.contains("kaki", ignoreCase = true) }

            val topLen = tableTopPiece?.lengthMm ?: 900.0
            val topWid = if (tableTopPiece != null && tableTopPiece.widthMm > 0) tableTopPiece.widthMm else 550.0
            val topThick = tableTopPiece?.thicknessMm ?: 22.0

            val legLen = legPiece?.lengthMm ?: 450.0
            val legThick = legPiece?.thicknessMm ?: 40.0

            val W = (topLen * 0.12).coerceIn(60.0, 120.0).toFloat()
            val D = (topWid * 0.12).coerceIn(40.0, 90.0).toFloat()
            val H = (legLen * 0.14).coerceIn(45.0, 95.0).toFloat()
            val T = (topThick * 0.4).coerceIn(6.0, 16.0).toFloat()
            val legW = (legThick * 0.22).coerceIn(8.0, 16.0).toFloat()

            val hw = W / 2f
            val hd = D / 2f
            val topY = -15f

            // Tabletop Slab
            allFaces.addAll(createCuboidFaces(-hw, topY - T, -hd, hw, topY, hd, Color(0xFFD2B48C), "Table Top"))

            // 4 Corner Legs
            val legInset = 4f
            allFaces.addAll(createCuboidFaces(-hw + legInset, topY, -hd + legInset, -hw + legInset + legW, topY + H, -hd + legInset + legW, Color(0xFF8F6343), "Leg FL"))
            allFaces.addAll(createCuboidFaces(hw - legInset - legW, topY, -hd + legInset, hw - legInset, topY + H, -hd + legInset + legW, Color(0xFF8F6343), "Leg FR"))
            allFaces.addAll(createCuboidFaces(-hw + legInset, topY, hd - legInset - legW, -hw + legInset + legW, topY + H, hd - legInset, Color(0xFF6E4A2F), "Leg BL"))
            allFaces.addAll(createCuboidFaces(hw - legInset - legW, topY, hd - legInset - legW, hw - legInset, topY + H, hd - legInset, Color(0xFF6E4A2F), "Leg BR"))

            // Aprons / Rails under top
            val apronH = 14f
            val apronT = 6f
            allFaces.addAll(createCuboidFaces(-hw + legInset + legW, topY, -hd + legInset, hw - legInset - legW, topY + apronH, -hd + legInset + apronT, Color(0xFFA0714E), "Front Apron"))
            allFaces.addAll(createCuboidFaces(-hw + legInset + legW, topY, hd - legInset - apronT, hw - legInset - legW, topY + apronH, hd - legInset, Color(0xFFA0714E), "Back Apron"))
            allFaces.addAll(createCuboidFaces(-hw + legInset, topY, -hd + legInset + legW, -hw + legInset + apronT, topY + apronH, hd - legInset - legW, Color(0xFFA0714E), "Left Apron"))
            allFaces.addAll(createCuboidFaces(hw - legInset - apronT, topY, -hd + legInset + legW, hw - legInset, topY + apronH, hd - legInset - legW, Color(0xFFA0714E), "Right Apron"))
        } else {
            // General Exploded Layered Parts Stack
            val piecesToDraw = requestedCuts.take(6)
            var currentY = 50f
            piecesToDraw.forEachIndexed { _, piece ->
                val len = (piece.lengthMm * 0.12).coerceIn(40.0, 110.0).toFloat()
                val wid = (piece.widthMm * 0.15).coerceIn(18.0, 60.0).toFloat()
                val thick = (piece.thicknessMm * 0.5).coerceIn(6.0, 16.0).toFloat()

                val hw = len / 2f
                val hd = wid / 2f

                val pieceColor = Color(piece.colorHex)
                allFaces.addAll(
                    createCuboidFaces(
                        -hw, currentY - thick, -hd,
                        hw, currentY, hd,
                        pieceColor,
                        piece.label
                    )
                )
                currentY -= (thick + 14f)
            }
        }

        // Lighting vector pointing from top-left-front
        val lx = -0.35f
        val ly = -0.75f
        val lz = 0.55f
        val lLen = Math.sqrt((lx * lx + ly * ly + lz * lz).toDouble()).toFloat()
        val nlx = lx / lLen
        val nly = ly / lLen
        val nlz = lz / lLen

        data class RenderFace(
            val path: Path,
            val shadedColor: Color,
            val depth: Float,
            val isFacingCamera: Boolean
        )

        val renderFaces = mutableListOf<RenderFace>()

        allFaces.forEach { face ->
            val rotNormal = rotateNormal(face.normal)
            // Camera is at (0, 0, +Infinity) looking along -Z in camera space
            val isFacingCamera = rotNormal.z > -0.08f

            if (isFacingCamera || isWireframeMode) {
                var totalZ = 0f
                val path = Path()

                face.vertices.forEachIndexed { index, v ->
                    val proj = project(v)
                    totalZ += proj.third
                    if (index == 0) {
                        path.moveTo(proj.first, proj.second)
                    } else {
                        path.lineTo(proj.first, proj.second)
                    }
                }
                path.close()

                val avgZ = totalZ / face.vertices.size.coerceAtLeast(1)

                // Shading calculation
                val dot = (rotNormal.x * nlx + rotNormal.y * nly + rotNormal.z * nlz).coerceIn(-1f, 1f)
                val brightness = (0.65f + 0.35f * dot).coerceIn(0.30f, 1.15f)

                val shaded = Color(
                    red = (face.baseColor.red * brightness).coerceIn(0f, 1f),
                    green = (face.baseColor.green * brightness).coerceIn(0f, 1f),
                    blue = (face.baseColor.blue * brightness).coerceIn(0f, 1f),
                    alpha = face.baseColor.alpha
                )

                renderFaces.add(RenderFace(path, shaded, avgZ, isFacingCamera))
            }
        }

        // Painter's Algorithm: Sort faces by depth
        renderFaces.sortBy { it.depth }

        renderFaces.forEach { rf ->
            if (!isWireframeMode && rf.isFacingCamera) {
                drawPath(rf.path, color = rf.shadedColor)
            }
            drawPath(
                rf.path,
                color = if (isWireframeMode) wireframeOutlineColor else Color(0xFF3E2723).copy(alpha = 0.85f),
                style = Stroke(width = if (isWireframeMode) 1.5f else 1.2f)
            )
        }
    }
}

@Composable
private fun CutlistStatBadge(
    title: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
