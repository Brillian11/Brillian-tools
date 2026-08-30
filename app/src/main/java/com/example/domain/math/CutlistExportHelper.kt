package com.example.domain.math

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

object CutlistExportHelper {

    fun exportToPdf(
        context: Context,
        projectName: String,
        materialType: MaterialType,
        stockLengthMm: Double,
        stockWidthMm: Double,
        stockThicknessMm: Double = 20.0,
        bladeKerfMm: Double,
        trimMarginMm: Double,
        requestedCuts: List<CutPiece>,
        optimizationResult: CutlistOptimizationResult,
        projectNotes: String
    ): Uri? {
        val document = PdfDocument()
        try {
            // Standard A4: 595 x 842 points (72 dpi)
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            var currentY = 36f

            // --- HEADER ---
            // Header background banner
            paint.color = Color.rgb(30, 41, 59) // Deep Slate
            canvas.drawRect(24f, 24f, pageWidth - 24f, 85f, paint)

            // Header Title
            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 18f
            canvas.drawText("BRILLIAN CUT LIST & WOODWORKING PLAN", 38f, 52f, paint)

            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.rgb(203, 213, 225)
            canvas.drawText("Project: ${projectName.ifEmpty { "Woodwork Project" }}  •  Generated: $dateStr", 38f, 72f, paint)

            currentY = 100f

            // --- PROJECT SPECIFICATIONS SUMMARY ---
            paint.color = Color.rgb(241, 245, 249)
            val summaryRect = RectF(24f, currentY, pageWidth - 24f, currentY + 54f)
            canvas.drawRoundRect(summaryRect, 6f, 6f, paint)

            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            canvas.drawText("Material Profile:", 36f, currentY + 18f, paint)
            canvas.drawText("Stock (L × W × T):", 160f, currentY + 18f, paint)
            canvas.drawText("Kerf / Trim:", 320f, currentY + 18f, paint)
            canvas.drawText("Efficiency / Yield:", 440f, currentY + 18f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            paint.color = Color.rgb(51, 65, 85)
            val matStr = materialType.displayName
            canvas.drawText(matStr, 36f, currentY + 36f, paint)
            canvas.drawText("${stockLengthMm.toInt()} × ${stockWidthMm.toInt()} × ${stockThicknessMm.toInt()} mm", 160f, currentY + 36f, paint)
            canvas.drawText("${bladeKerfMm}mm / ${trimMarginMm}mm", 320f, currentY + 36f, paint)

            paint.color = Color.rgb(22, 101, 52) // Green
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("${String.format(Locale.US, "%.1f", optimizationResult.yieldPercentage)}% (${optimizationResult.usedBoards.size} Boards)", 440f, currentY + 36f, paint)

            currentY += 70f

            // --- CUTTING DIAGRAM MAPS (1D / 2D) ---
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 12f
            canvas.drawText("CUTTING MAP LAYOUTS (Total: ${optimizationResult.usedBoards.size} Stock Sheet/Board${if (optimizationResult.usedBoards.size > 1) "s" else ""})", 24f, currentY, paint)
            currentY += 12f

            val maxBoardsToDraw = 2
            val drawnBoards = optimizationResult.usedBoards.take(maxBoardsToDraw)

            for ((idx, board) in drawnBoards.withIndex()) {
                val boardBoxTop = currentY
                val boardBoxWidth = pageWidth - 48f
                val boardBoxHeight = if (materialType == MaterialType.PLYWOOD_SHEET) 95f else 32f

                // Label
                paint.color = Color.rgb(71, 85, 105)
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("Sheet #${idx + 1}: ${board.boardName} (${board.placedPieces.size} cuts placed)", 24f, boardBoxTop + 9f, paint)

                val diagramTop = boardBoxTop + 14f
                // Board outline background
                paint.color = Color.rgb(226, 232, 240)
                canvas.drawRect(24f, diagramTop, 24f + boardBoxWidth, diagramTop + boardBoxHeight, paint)
                paint.color = Color.rgb(148, 163, 184)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1f
                canvas.drawRect(24f, diagramTop, 24f + boardBoxWidth, diagramTop + boardBoxHeight, paint)
                paint.style = Paint.Style.FILL

                val totalLen = if (stockLengthMm > 0) stockLengthMm else 2438.4
                val totalWid = if (stockWidthMm > 0) stockWidthMm else 1219.2

                // Draw placed pieces
                for (piece in board.placedPieces) {
                    val scaleX = boardBoxWidth / totalLen.toFloat()
                    val scaleY = boardBoxHeight / totalWid.toFloat()

                    val rx = 24f + (piece.startPositionMm.toFloat() * scaleX)
                    val rw = (piece.lengthMm.toFloat() * scaleX).coerceAtLeast(4f)

                    if (materialType == MaterialType.PLYWOOD_SHEET) {
                        val ry = diagramTop + (piece.startYMm.toFloat() * scaleY)
                        val rh = (piece.widthMm.toFloat() * scaleY).coerceAtLeast(4f)

                        paint.color = piece.colorHex.toInt() or 0xFF000000.toInt()
                        canvas.drawRect(rx, ry, rx + rw, ry + rh, paint)

                        paint.color = Color.BLACK
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 0.5f
                        canvas.drawRect(rx, ry, rx + rw, ry + rh, paint)
                        paint.style = Paint.Style.FILL

                        if (rw > 28f && rh > 14f) {
                            paint.color = Color.WHITE
                            paint.textSize = 7f
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            canvas.drawText(piece.pieceLabel.take(12), rx + 3f, ry + 10f, paint)
                        }
                    } else {
                        paint.color = piece.colorHex.toInt() or 0xFF000000.toInt()
                        canvas.drawRect(rx, diagramTop, rx + rw, diagramTop + boardBoxHeight, paint)

                        paint.color = Color.BLACK
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 0.5f
                        canvas.drawRect(rx, diagramTop, rx + rw, diagramTop + boardBoxHeight, paint)
                        paint.style = Paint.Style.FILL

                        if (rw > 25f) {
                            paint.color = Color.WHITE
                            paint.textSize = 7.5f
                            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                            canvas.drawText(piece.pieceLabel.take(10), rx + 3f, diagramTop + 18f, paint)
                        }
                    }
                }

                currentY = diagramTop + boardBoxHeight + 14f
            }

            // --- 3D STRUCTURAL SCHEMATIC (ISOMETRIC) ---
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 12f
            canvas.drawText("3D STRUCTURAL ISOMETRIC PREVIEW", 24f, currentY, paint)
            currentY += 10f

            val isoBoxTop = currentY
            val isoBoxHeight = 110f
            paint.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(RectF(24f, isoBoxTop, pageWidth - 24f, isoBoxTop + isoBoxHeight), 6f, 6f, paint)
            paint.color = Color.rgb(226, 232, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            canvas.drawRoundRect(RectF(24f, isoBoxTop, pageWidth - 24f, isoBoxTop + isoBoxHeight), 6f, 6f, paint)
            paint.style = Paint.Style.FILL

            // Draw isometric 3D drawing inside PDF
            drawIsometricVectorOnPdf(canvas, requestedCuts, 24f + (pageWidth - 48f) / 2f, isoBoxTop + 65f)

            currentY = isoBoxTop + isoBoxHeight + 16f

            // --- CUTTING LIST SCHEDULE (TABLE) ---
            paint.color = Color.rgb(15, 23, 42)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 12f
            canvas.drawText("CUT PIECES SCHEDULE (${requestedCuts.sumOf { it.quantity }} total parts)", 24f, currentY, paint)
            currentY += 8f

            // Table Header
            val tableTop = currentY
            paint.color = Color.rgb(30, 41, 59)
            canvas.drawRect(24f, tableTop, pageWidth - 24f, tableTop + 18f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("#", 32f, tableTop + 12f, paint)
            canvas.drawText("PART LABEL", 55f, tableTop + 12f, paint)
            canvas.drawText("LENGTH", 250f, tableTop + 12f, paint)
            canvas.drawText("WIDTH", 330f, tableTop + 12f, paint)
            canvas.drawText("THICK", 410f, tableTop + 12f, paint)
            canvas.drawText("QTY", 480f, tableTop + 12f, paint)
            canvas.drawText("TOTAL", 520f, tableTop + 12f, paint)

            currentY = tableTop + 18f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f

            for ((i, cut) in requestedCuts.withIndex()) {
                val rowHeight = 15f
                if (i % 2 == 1) {
                    paint.color = Color.rgb(241, 245, 249)
                    canvas.drawRect(24f, currentY, pageWidth - 24f, currentY + rowHeight, paint)
                }

                paint.color = Color.rgb(30, 41, 59)
                canvas.drawText("${i + 1}", 32f, currentY + 11f, paint)
                canvas.drawText(cut.label, 55f, currentY + 11f, paint)
                canvas.drawText("${cut.lengthMm.toInt()} mm", 250f, currentY + 11f, paint)
                val wText = if (cut.widthMm > 0) "${cut.widthMm.toInt()} mm" else "Standard"
                canvas.drawText(wText, 330f, currentY + 11f, paint)
                canvas.drawText("${cut.thicknessMm.toInt()} mm", 410f, currentY + 11f, paint)
                canvas.drawText("${cut.quantity}x", 480f, currentY + 11f, paint)
                val totalArea = (cut.lengthMm * (if (cut.widthMm > 0) cut.widthMm else 89.0) * cut.quantity) / 1000000.0
                canvas.drawText(String.format(Locale.US, "%.2f m²", totalArea), 520f, currentY + 11f, paint)

                currentY += rowHeight
                if (currentY > pageHeight - 90f) break
            }

            // --- NON-CUT ACCESSORIES & HARDWARE ---
            if (projectNotes.isNotBlank() && currentY < pageHeight - 70f) {
                currentY += 10f
                paint.color = Color.rgb(15, 23, 42)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 10f
                canvas.drawText("ASSEMBLY HARDWARE & NON-CUT MATERIALS:", 24f, currentY, paint)
                currentY += 12f

                paint.color = Color.rgb(234, 244, 255)
                val notesRect = RectF(24f, currentY, pageWidth - 24f, (currentY + 28f).coerceAtMost(pageHeight - 35f))
                canvas.drawRoundRect(notesRect, 4f, 4f, paint)

                paint.color = Color.rgb(30, 58, 138)
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 8f

                val cleanedNotes = projectNotes.replace(";", "  •  ").trim()
                canvas.drawText("• ${cleanedNotes.take(130)}", 32f, currentY + 14f, paint)
                if (cleanedNotes.length > 130) {
                    canvas.drawText(cleanedNotes.substring(130).take(130), 32f, currentY + 24f, paint)
                }
            }

            // --- FOOTER ---
            paint.color = Color.rgb(148, 163, 184)
            paint.textSize = 8f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Generated by Brillian Tools - Engineering & Carpentry Optimizer  •  Sheet 1 of 1", 24f, pageHeight - 16f, paint)

            document.finishPage(page)

            // Save PDF file to cache directory
            val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val fileName = "Cutlist_${projectName.replace(Regex("[^a-zA-Z0-9_]"), "_").ifEmpty { "Project" }}_${System.currentTimeMillis()}.pdf"
            val pdfFile = File(outputDir, fileName)
            val fos = FileOutputStream(pdfFile)
            document.writeTo(fos)
            fos.close()

            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            document.close()
        }
    }

    private fun drawIsometricVectorOnPdf(canvas: Canvas, cuts: List<CutPiece>, cx: Float, cy: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val cos30 = 0.866f
        val sin30 = 0.5f

        val hasTable = cuts.any { it.label.contains("table", true) || it.label.contains("meja", true) || it.label.contains("desk", true) || it.label.contains("leg", true) }
        val hasCabinet = cuts.any { it.label.contains("cabinet", true) || it.label.contains("lemari", true) || it.label.contains("shelf", true) || it.label.contains("rak", true) }

        if (hasTable) {
            val lenFactor = 65f
            val widFactor = 45f
            val legH = 40f
            val thick = 7f

            val lx = -lenFactor * cos30
            val ly = lenFactor * sin30
            val rx = widFactor * cos30
            val ry = widFactor * sin30

            val bx = cx
            val by = cy - 10f

            val pBottomCenter = Pair(bx, by)
            val pBottomLeft = Pair(bx + lx, by + ly)
            val pBottomFar = Pair(bx + lx + rx, by + ly + ry)
            val pBottomRight = Pair(bx + rx, by + ry)

            // Draw 4 legs
            val legs = listOf(pBottomLeft, pBottomRight, pBottomFar, pBottomCenter)
            for (leg in legs) {
                paint.color = Color.rgb(143, 99, 67)
                canvas.drawRect(leg.first - 3f, leg.second, leg.first + 3f, leg.second + legH, paint)
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(62, 39, 35)
                paint.strokeWidth = 0.8f
                canvas.drawRect(leg.first - 3f, leg.second, leg.first + 3f, leg.second + legH, paint)
                paint.style = Paint.Style.FILL
            }

            // Top tabletop face
            val pathTop = Path().apply {
                moveTo(bx, by - thick)
                lineTo(bx + lx, by - thick + ly)
                lineTo(bx + lx + rx, by - thick + ly + ry)
                lineTo(bx + rx, by - thick + ry)
                close()
            }
            paint.color = Color.rgb(210, 180, 140)
            canvas.drawPath(pathTop, paint)

            val pathLeft = Path().apply {
                moveTo(bx + lx, by - thick + ly)
                lineTo(bx, by - thick)
                lineTo(bx, by)
                lineTo(bx + lx, by + ly)
                close()
            }
            paint.color = Color.rgb(181, 154, 122)
            canvas.drawPath(pathLeft, paint)

            val pathRight = Path().apply {
                moveTo(bx, by - thick)
                lineTo(bx + rx, by - thick + ry)
                lineTo(bx + rx, by + ry)
                lineTo(bx, by)
                close()
            }
            paint.color = Color.rgb(158, 133, 105)
            canvas.drawPath(pathRight, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(62, 39, 35)
            paint.strokeWidth = 1f
            canvas.drawPath(pathTop, paint)
            canvas.drawPath(pathLeft, paint)
            canvas.drawPath(pathRight, paint)
            paint.style = Paint.Style.FILL
        } else {
            // General 3D Stack of Planks
            val uniqueCuts = cuts.take(4)
            var startY = cy + (uniqueCuts.size * 5f)
            var startX = cx - 20f

            for ((idx, cut) in uniqueCuts.withIndex()) {
                val len = (cut.lengthMm * 0.06).coerceIn(30.0, 75.0).toFloat()
                val wid = (cut.widthMm * 0.1).coerceIn(12.0, 30.0).toFloat()
                val thick = 6f

                val lx = -wid * cos30
                val ly = wid * sin30
                val rx = len * cos30
                val ry = len * sin30

                val px = startX + (idx * 10f)
                val py = startY - (idx * 14f)

                val topPath = Path().apply {
                    moveTo(px, py - thick)
                    lineTo(px + lx, py - thick + ly)
                    lineTo(px + lx + rx, py - thick + ly + ry)
                    lineTo(px + rx, py - thick + ry)
                    close()
                }
                paint.color = cut.colorHex.toInt() or 0xFF000000.toInt()
                canvas.drawPath(topPath, paint)

                val leftPath = Path().apply {
                    moveTo(px + lx, py - thick + ly)
                    lineTo(px, py - thick)
                    lineTo(px, py)
                    lineTo(px + lx, py + ly)
                    close()
                }
                paint.color = Color.rgb(160, 113, 78)
                canvas.drawPath(leftPath, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(62, 39, 35)
                paint.strokeWidth = 0.8f
                canvas.drawPath(topPath, paint)
                canvas.drawPath(leftPath, paint)
                paint.style = Paint.Style.FILL
            }
        }
    }

    fun exportToExcelCsv(
        context: Context,
        projectName: String,
        materialType: MaterialType,
        stockLengthMm: Double,
        stockWidthMm: Double,
        stockThicknessMm: Double = 20.0,
        bladeKerfMm: Double,
        trimMarginMm: Double,
        requestedCuts: List<CutPiece>,
        optimizationResult: CutlistOptimizationResult,
        projectNotes: String
    ): Uri? {
        try {
            val sb = StringBuilder()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date())

            // Header Section
            sb.append("BRILLIAN CUT LIST OPTIMIZER EXPORT\n")
            sb.append("Project Name,").append("\"").append(projectName.replace("\"", "\"\"")).append("\"\n")
            sb.append("Export Date,").append(dateStr).append("\n")
            sb.append("Material Profile,").append("\"").append(materialType.displayName).append("\"\n")
            sb.append("Stock Size (L x W x T mm),").append("${stockLengthMm.toInt()} x ${stockWidthMm.toInt()} x ${stockThicknessMm.toInt()}\n")
            sb.append("Blade Kerf (mm),").append(bladeKerfMm).append("\n")
            sb.append("Trim Margin (mm),").append(trimMarginMm).append("\n")
            sb.append("Optimization Yield,").append(String.format(Locale.US, "%.2f%%", optimizationResult.yieldPercentage)).append("\n")
            sb.append("Stock Units Needed,").append(optimizationResult.usedBoards.size).append("\n\n")

            // Cut Piece List Table
            sb.append("Item #,Part Name,Length (mm),Width (mm),Thickness (mm),Quantity,Area Each (m2),Total Area (m2),Volume Each (cm3)\n")
            for ((idx, piece) in requestedCuts.withIndex()) {
                val effectiveWidth = if (piece.widthMm > 0) piece.widthMm else 89.0
                val areaEach = (piece.lengthMm * effectiveWidth) / 1000000.0
                val totalArea = areaEach * piece.quantity
                val volEach = (piece.lengthMm * effectiveWidth * piece.thicknessMm) / 1000.0

                sb.append("${idx + 1},")
                sb.append("\"").append(piece.label.replace("\"", "\"\"")).append("\",")
                sb.append("${piece.lengthMm.toInt()},")
                sb.append("${piece.widthMm.toInt()},")
                sb.append("${piece.thicknessMm.toInt()},")
                sb.append("${piece.quantity},")
                sb.append(String.format(Locale.US, "%.3f,", areaEach))
                sb.append(String.format(Locale.US, "%.3f,", totalArea))
                sb.append(String.format(Locale.US, "%.1f\n", volEach))
            }
            sb.append("\n")

            // Layout Placement Details
            sb.append("STOCK BOARD / SHEET CUTTING SCHEDULE\n")
            sb.append("Sheet/Board #,Board Name,Piece Placed,Length (mm),Width (mm),Start X (mm),End X (mm),Start Y (mm),End Y (mm),Thickness (mm)\n")
            for ((bIdx, board) in optimizationResult.usedBoards.withIndex()) {
                for (p in board.placedPieces) {
                    sb.append("Board ${bIdx + 1},")
                    sb.append("\"").append(board.boardName.replace("\"", "\"\"")).append("\",")
                    sb.append("\"").append(p.pieceLabel.replace("\"", "\"\"")).append("\",")
                    sb.append("${p.lengthMm.toInt()},")
                    sb.append("${p.widthMm.toInt()},")
                    sb.append("${p.startPositionMm.toInt()},")
                    sb.append("${p.endPositionMm.toInt()},")
                    sb.append("${p.startYMm.toInt()},")
                    sb.append("${p.endYMm.toInt()},")
                    sb.append("${p.thicknessMm.toInt()}\n")
                }
            }
            sb.append("\n")

            // Non-cut hardware accessories
            if (projectNotes.isNotBlank()) {
                sb.append("HARDWARE & NON-CUT MATERIALS LIST\n")
                sb.append("Item #,Description\n")
                val notes = projectNotes.split(";")
                for ((nIdx, note) in notes.withIndex()) {
                    if (note.trim().isNotBlank()) {
                        sb.append("${nIdx + 1},\"").append(note.trim().replace("\"", "\"\"")).append("\"\n")
                    }
                }
            }

            val outputDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val fileName = "Cutlist_${projectName.replace(Regex("[^a-zA-Z0-9_]"), "_").ifEmpty { "Project" }}_${System.currentTimeMillis()}.csv"
            val file = File(outputDir, fileName)
            file.writeText(sb.toString(), Charsets.UTF_8)

            return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun shareExportedFile(context: Context, fileUri: Uri, mimeType: String, chooserTitle: String = "Share Woodworking Plan") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun openExportedFile(context: Context, fileUri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            shareExportedFile(context, fileUri, mimeType, "Open / Share Exported Plan")
        }
    }

    fun exportToMarkdown(
        projectName: String,
        materialType: MaterialType,
        stockLengthMm: Double,
        stockWidthMm: Double,
        stockThicknessMm: Double,
        bladeKerfMm: Double,
        trimMarginMm: Double,
        requestedCuts: List<CutPiece>,
        optimizationResult: CutlistOptimizationResult,
        projectNotes: String
    ): String {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val sb = java.lang.StringBuilder()
        sb.append("# 🪚 Cutlist Cutting Plan: $projectName\n\n")
        sb.append("> Exported from Cutlist Optimizer on $dateStr\n\n")
        sb.append("## 📐 Material & Stock Specifications\n")
        sb.append("- **Material Type**: ${materialType.displayName}\n")
        sb.append("- **Stock Dimensions**: ${stockLengthMm / 10.0} cm x ${stockWidthMm / 10.0} cm x ${stockThicknessMm / 10.0} cm\n")
        sb.append("- **Blade Kerf**: ${bladeKerfMm / 10.0} cm | **Trim Margin**: ${trimMarginMm / 10.0} cm\n")
        val wastePct = 100.0 - optimizationResult.yieldPercentage
        sb.append("- **Total Stock Sheets Required**: **${optimizationResult.usedBoards.size}**\n")
        sb.append("- **Overall Yield Efficiency**: **${String.format(Locale.US, "%.1f", optimizationResult.yieldPercentage)}%**\n")
        sb.append("- **Total Waste Rate**: **${String.format(Locale.US, "%.1f", wastePct)}%**\n\n")

        sb.append("## 📋 Cutting Parts & Bill of Materials (BOM)\n")
        sb.append("| Part Label | Length (cm) | Width (cm) | Qty |\n")
        sb.append("|---|---|---|---|\n")
        requestedCuts.forEach { cut ->
            sb.append("| ${cut.label.ifBlank { "Part" }} | ${cut.lengthMm / 10.0} | ${cut.widthMm / 10.0} | ${cut.quantity} |\n")
        }
        sb.append("\n")

        if (optimizationResult.usedBoards.isNotEmpty()) {
            sb.append("## ✂️ Sheet Cut Breakdown\n")
            optimizationResult.usedBoards.forEachIndexed { idx, board ->
                sb.append("### Sheet #${idx + 1}: ${board.boardName} (${board.totalBoardLengthMm / 10.0} x ${board.totalBoardWidthMm / 10.0} cm)\n")
                sb.append("- **Placed Parts**: ${board.placedPieces.size} pieces\n")
                board.placedPieces.forEach { placed ->
                    sb.append("  - **${placed.pieceLabel}** @ (${placed.startPositionMm / 10.0}, ${placed.startYMm / 10.0}) cm — Size: ${placed.lengthMm / 10.0} x ${placed.widthMm / 10.0} cm\n")
                }
                sb.append("\n")
            }
        }

        if (projectNotes.isNotBlank()) {
            sb.append("## 📝 Hardware & Notes\n")
            val cleanNotes = projectNotes.replace(";", "\n- ")
            sb.append("- $cleanNotes\n\n")
        }

        return sb.toString()
    }
}
