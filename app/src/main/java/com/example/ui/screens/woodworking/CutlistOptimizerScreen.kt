package com.example.ui.screens.woodworking

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.math.CutPiece
import com.example.domain.math.CutlistOptimizationResult
import com.example.domain.math.MaterialType
import com.example.domain.math.UsedBoardLayout

@Composable
fun CutlistOptimizerScreen(
    viewModel: CutlistOptimizerViewModel,
    modifier: Modifier = Modifier
) {
    val materialType by viewModel.materialType.collectAsState()
    val stockLength by viewModel.stockBoardLength.collectAsState()
    val stockWidth by viewModel.stockBoardWidth.collectAsState()
    val kerf by viewModel.bladeKerf.collectAsState()
    val trim by viewModel.trimMargin.collectAsState()
    val requestedCuts by viewModel.requestedCuts.collectAsState()
    val result by viewModel.optimizationResult.collectAsState()

    var newLabel by remember { mutableStateOf("") }
    var newLength by remember { mutableStateOf("600") }
    var newWidth by remember { mutableStateOf("150") }
    var newQty by remember { mutableStateOf("1") }

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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CUTLIST OPTIMIZER & PANEL PACKER",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Timber Board & Plywood Layout Optimizer",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Optimize 1D timber cuts or 2D plywood sheet layouts with color-coded section mapping and blade kerf loss compensation.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            // Material Type Selector Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilterChip(
                    selected = materialType == MaterialType.TIMBER_BOARD,
                    onClick = { viewModel.updateMaterialType(MaterialType.TIMBER_BOARD) },
                    label = { Text("Timber Board (1D)") },
                    leadingIcon = { Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f).testTag("chip_material_timber")
                )
                FilterChip(
                    selected = materialType == MaterialType.PLYWOOD_SHEET,
                    onClick = { viewModel.updateMaterialType(MaterialType.PLYWOOD_SHEET) },
                    label = { Text("Plywood Sheet (2D)") },
                    leadingIcon = { Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f).testTag("chip_material_plywood")
                )
            }

            // Summary Yield Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                ResultBadge(
                    title = "MATERIAL YIELD",
                    value = String.format("%.1f%%", result.yieldPercentage),
                    unit = "${result.usedBoards.size} ${if (materialType == MaterialType.PLYWOOD_SHEET) "sheet(s)" else "board(s)"}",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                ResultBadge(
                    title = "TOTAL SCRAP LOSS",
                    value = if (materialType == MaterialType.PLYWOOD_SHEET) {
                        String.format("%.1f m²", result.totalScrapLengthMm / 1_000_000.0)
                    } else {
                        String.format("%.0f mm", result.totalScrapLengthMm)
                    },
                    unit = if (materialType == MaterialType.PLYWOOD_SHEET) "scrap area" else "scrap length",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Material Dimensions & Blade Settings
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
                        text = if (materialType == MaterialType.PLYWOOD_SHEET) "Stock Sheet & Blade Dimensions" else "Stock Board & Blade Dimensions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = stockLength,
                            onValueChange = { viewModel.updateStockLength(it) },
                            label = { Text("Length (mm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_stock_length")
                        )

                        OutlinedTextField(
                            value = stockWidth,
                            onValueChange = { viewModel.updateStockWidth(it) },
                            label = { Text("Width (mm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("input_stock_width")
                        )

                        OutlinedTextField(
                            value = kerf,
                            onValueChange = { viewModel.updateBladeKerf(it) },
                            label = { Text("Kerf (mm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.9f).testTag("input_cutlist_kerf")
                        )
                    }
                }
            }

            // Requested Cut Pieces
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
                        text = "Requested Cut Pieces",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // Entry Inputs
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = newLabel,
                            onValueChange = { newLabel = it },
                            label = { Text("Label") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.3f).testTag("input_cut_label")
                        )
                        OutlinedTextField(
                            value = newLength,
                            onValueChange = { newLength = it },
                            label = { Text("Len (mm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.0f).testTag("input_cut_length")
                        )
                        if (materialType == MaterialType.PLYWOOD_SHEET) {
                            OutlinedTextField(
                                value = newWidth,
                                onValueChange = { newWidth = it },
                                label = { Text("Wid (mm)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.0f).testTag("input_cut_width")
                            )
                        }
                        OutlinedTextField(
                            value = newQty,
                            onValueChange = { newQty = it },
                            label = { Text("Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(0.7f).testTag("input_cut_qty")
                        )
                        IconButton(
                            onClick = {
                                val len = newLength.toDoubleOrNull() ?: 0.0
                                val wid = newWidth.toDoubleOrNull() ?: (stockWidth.toDoubleOrNull() ?: 89.0)
                                val q = newQty.toIntOrNull() ?: 1
                                if (len > 0) {
                                    viewModel.addCutPiece(newLabel, len, wid, q)
                                    newLabel = ""
                                }
                            },
                            modifier = Modifier.testTag("add_cut_piece_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Cut Piece")
                        }
                    }

                    // Requested Cuts List with COLOR CODING
                    requestedCuts.forEach { cut ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Color Code Pill/Badge matching diagram section
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(cut.colorHex), CircleShape)
                                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = cut.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    val dimText = if (materialType == MaterialType.PLYWOOD_SHEET) {
                                        "${cut.lengthMm} mm × ${cut.widthMm} mm (${cut.quantity} pcs)"
                                    } else {
                                        "${cut.lengthMm} mm (${cut.quantity} pcs)"
                                    }
                                    Text(
                                        text = dimText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.removeCutPiece(cut.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Visual Board / Sheet Diagrams
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
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${board.boardName} #${index + 1}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            val scrapText = if (board.materialType == MaterialType.PLYWOOD_SHEET) {
                                "Scrap Area: ${String.format("%.2f m²", board.remainingScrapMm / 1_000_000.0)}"
                            } else {
                                "Scrap Length: ${String.format("%.1f mm", board.remainingScrapMm)}"
                            }
                            Text(
                                text = scrapText,
                                style = MaterialTheme.typography.bodySmall,
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
                            StockBoardDiagramCanvas(
                                board = board,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(65.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // COLOR CODED SECTION LEGEND FOR THIS BOARD/SHEET
                        Text(
                            text = "Cut Section Breakdown:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            board.placedPieces.forEach { piece ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .background(Color(piece.colorHex), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = piece.pieceLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.weight(1f)
                                    )
                                    val sectionInfo = if (board.materialType == MaterialType.PLYWOOD_SHEET) {
                                        "${String.format("%.0f", piece.lengthMm)}×${String.format("%.0f", piece.widthMm)}mm  [X: ${String.format("%.0f", piece.startPositionMm)}-${String.format("%.0f", piece.endPositionMm)}, Y: ${String.format("%.0f", piece.startYMm)}-${String.format("%.0f", piece.endYMm)}]"
                                    } else {
                                        "${String.format("%.1f", piece.lengthMm)} mm  [Pos: ${String.format("%.1f", piece.startPositionMm)} - ${String.format("%.1f", piece.endPositionMm)} mm]"
                                    }
                                    Text(
                                        text = sectionInfo,
                                        style = MaterialTheme.typography.labelSmall,
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

@Composable
fun StockBoardDiagramCanvas(
    board: UsedBoardLayout,
    modifier: Modifier = Modifier
) {
    val scrapColor = Color(0xFFE7E0EC)
    val kerfColor = Color(0xFF1D1B20)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val totalLen = board.totalBoardLengthMm

        // Background Stock Board
        drawRect(
            color = scrapColor,
            topLeft = Offset(0f, 0f),
            size = Size(w, h)
        )

        board.placedPieces.forEach { piece ->
            val startX = (piece.startPositionMm / totalLen).toFloat() * w
            val endX = (piece.endPositionMm / totalLen).toFloat() * w
            val pieceW = (endX - startX).coerceAtLeast(4f)

            // Draw colored cut section
            drawRect(
                color = Color(piece.colorHex),
                topLeft = Offset(startX, 0f),
                size = Size(pieceW, h)
            )

            // Draw thin border around cut section
            drawRect(
                color = Color.White.copy(alpha = 0.5f),
                topLeft = Offset(startX, 0f),
                size = Size(pieceW, h),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
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

        // Draw Plywood Sheet Base
        drawRect(
            color = scrapColor,
            topLeft = Offset(0f, 0f),
            size = Size(w, h)
        )

        // Sheet Border
        drawRect(
            color = Color(0xFF79747E),
            topLeft = Offset(0f, 0f),
            size = Size(w, h),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
        )

        // Draw placed 2D plywood panel pieces
        board.placedPieces.forEach { piece ->
            val startX = (piece.startPositionMm / totalLen).toFloat() * w
            val endX = (piece.endPositionMm / totalLen).toFloat() * w
            val pieceW = (endX - startX).coerceAtLeast(4f)

            val startY = (piece.startYMm / totalWid).toFloat() * h
            val endY = (piece.endYMm / totalWid).toFloat() * h
            val pieceH = (endY - startY).coerceAtLeast(4f)

            // Fill color block
            drawRect(
                color = Color(piece.colorHex),
                topLeft = Offset(startX, startY),
                size = Size(pieceW, pieceH)
            )

            // Cut outline
            drawRect(
                color = Color.White,
                topLeft = Offset(startX, startY),
                size = Size(pieceW, pieceH),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }
    }
}

